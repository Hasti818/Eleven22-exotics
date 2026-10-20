package com.eleven22exotics.service;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.entity.*;
import com.eleven22exotics.exception.ApiException;
import com.eleven22exotics.repository.BookingRepository;
import com.eleven22exotics.repository.CarRepository;
import com.eleven22exotics.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookings;
    private final CarRepository cars;
    private final UserRepository users;

    public BookingService(BookingRepository bookings, CarRepository cars, UserRepository users) {
        this.bookings = bookings;
        this.cars = cars;
        this.users = users;
    }

    /**
     * Availability check and insert run in one transaction, and the car row is locked first,
     * so two customers cannot book the same car for overlapping dates at the same time.
     */
    public BookingResponse create(String email, BookingRequest req) {
        User user = users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in"));
        Car car = cars.findByIdForUpdate(req.carId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Car not found"));
        if (car.getStatus() != CarStatus.ACTIVE) throw new ApiException(HttpStatus.CONFLICT, "This car is not available for rental");
        if (req.startDate().isBefore(LocalDate.now())) throw new ApiException(HttpStatus.BAD_REQUEST, "The start date cannot be in the past");
        if (!req.endDate().isAfter(req.startDate())) throw new ApiException(HttpStatus.BAD_REQUEST, "The end date must be after the start date");
        if (bookings.existsOverlap(car.getId(), CarService.BLOCKING, req.startDate(), req.endDate()))
            throw new ApiException(HttpStatus.CONFLICT, "Those dates overlap an existing booking");

        Booking b = new Booking();
        b.setUser(user);
        b.setCar(car);
        b.setStartDate(req.startDate());
        b.setEndDate(req.endDate());
        b.setTotalPrice(PriceCalculator.calculate(car, req.startDate(), req.endDate()));   // always calculated on the server
        b.setStatus(BookingStatus.PENDING);
        b.setCustomerNote(req.customerNote());
        return BookingResponse.from(bookings.save(b));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> mine(String email) {
        User u = user(email);
        return bookings.findByUserIdOrderByStartDateDesc(u.getId()).stream().map(BookingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, String email) {
        User u = user(email);
        Booking b = find(id);
        if (!b.getUser().getId().equals(u.getId()) && u.getRole() != Role.ADMIN) throw notFound();
        return BookingResponse.from(b);
    }

    public BookingResponse cancel(Long id, String email) {
        User u = user(email);
        Booking b = find(id);
        if (!b.getUser().getId().equals(u.getId())) throw notFound();
        if (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.APPROVED)
            throw new ApiException(HttpStatus.CONFLICT, "This booking can no longer be cancelled");
        b.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookings.save(b));
    }

    // ---------- admin ----------
    @Transactional(readOnly = true)
    public List<BookingResponse> all() {
        return bookings.findAllByOrderByIdDesc().stream().map(BookingResponse::from).toList();
    }

    public BookingResponse approve(Long id, String note) { return move(id, BookingStatus.PENDING, BookingStatus.APPROVED, note); }
    public BookingResponse reject(Long id, String note) { return move(id, BookingStatus.PENDING, BookingStatus.REJECTED, note); }
    public BookingResponse activate(Long id) { return move(id, BookingStatus.APPROVED, BookingStatus.ACTIVE, null); }
    public BookingResponse complete(Long id) { return move(id, BookingStatus.ACTIVE, BookingStatus.COMPLETED, null); }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        long rented = bookings.countByStatus(BookingStatus.ACTIVE);
        long active = cars.countByStatus(CarStatus.ACTIVE);
        return new StatsResponse(cars.count(), Math.max(0, active - rented), cars.countByStatus(CarStatus.MAINTENANCE),
                bookings.countByStatus(BookingStatus.PENDING), rented, users.countByRole(Role.CUSTOMER));
    }

    // ---------- helpers ----------
    private BookingResponse move(Long id, BookingStatus from, BookingStatus to, String note) {
        Booking b = find(id);
        if (b.getStatus() != from) throw new ApiException(HttpStatus.CONFLICT, "Only " + from + " bookings can be moved to " + to);
        b.setStatus(to);
        if (note != null && !note.isBlank()) b.setAdminNote(note);
        return BookingResponse.from(bookings.save(b));
    }

    private Booking find(Long id) { return bookings.findById(id).orElseThrow(BookingService::notFound); }
    private static ApiException notFound() { return new ApiException(HttpStatus.NOT_FOUND, "Booking not found"); }
    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in"));
    }
}
