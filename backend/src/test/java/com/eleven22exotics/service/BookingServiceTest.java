package com.eleven22exotics.service;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.entity.*;
import com.eleven22exotics.exception.ApiException;
import com.eleven22exotics.repository.BookingRepository;
import com.eleven22exotics.repository.CarRepository;
import com.eleven22exotics.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepository bookings;
    @Mock CarRepository cars;
    @Mock UserRepository users;
    BookingService service;
    User customer, other, admin;
    Car car;
    final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        service = new BookingService(bookings, cars, users);
        customer = user(1L, "a@b.dk", Role.CUSTOMER);
        other = user(2L, "c@d.dk", Role.CUSTOMER);
        admin = user(3L, "admin@b.dk", Role.ADMIN);
        car = new Car();
        car.setId(10L);
        car.setBrand("Ferrari");
        car.setModel("296 GTB");
        car.setStatus(CarStatus.ACTIVE);
        car.setDailyPrice(new BigDecimal("1000"));
        car.setMonthlyPrice(new BigDecimal("20000"));
        lenient().when(users.findByEmail("a@b.dk")).thenReturn(Optional.of(customer));
        lenient().when(users.findByEmail("c@d.dk")).thenReturn(Optional.of(other));
        lenient().when(cars.findByIdForUpdate(10L)).thenReturn(Optional.of(car));
        lenient().when(bookings.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
    }

    private User user(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName("T");
        u.setLastName("U");
        u.setRole(role);
        return u;
    }

    private Booking booking(BookingStatus status, User owner) {
        Booking b = new Booking();
        b.setId(5L);
        b.setUser(owner);
        b.setCar(car);
        b.setStartDate(today.plusDays(5));
        b.setEndDate(today.plusDays(8));
        b.setTotalPrice(new BigDecimal("3000"));
        b.setStatus(status);
        lenient().when(bookings.findById(5L)).thenReturn(Optional.of(b));
        return b;
    }

    private ApiException failure(Runnable r) { return assertThrows(ApiException.class, r::run); }

    private BookingRequest req(int startOffset, int endOffset) {
        return new BookingRequest(10L, today.plusDays(startOffset), today.plusDays(endOffset), "note");
    }

    @Test void createsPendingBookingWithServerSidePrice() {
        when(bookings.existsOverlap(eq(10L), anyCollection(), any(), any())).thenReturn(false);
        BookingResponse r = service.create("a@b.dk", req(5, 8));
        assertEquals(BookingStatus.PENDING, r.status());
        assertEquals(0, new BigDecimal("3000").compareTo(r.totalPrice()));
    }

    @Test void rejectsStartDateInThePast() {
        assertEquals(HttpStatus.BAD_REQUEST, failure(() -> service.create("a@b.dk", req(-1, 2))).getStatus());
        verify(bookings, never()).save(any());
    }

    @Test void rejectsEndDateNotAfterStartDate() {
        assertEquals(HttpStatus.BAD_REQUEST, failure(() -> service.create("a@b.dk", req(5, 5))).getStatus());
        assertEquals(HttpStatus.BAD_REQUEST, failure(() -> service.create("a@b.dk", req(5, 3))).getStatus());
    }

    @Test void rejectsOverlappingBooking() {
        when(bookings.existsOverlap(eq(10L), anyCollection(), any(), any())).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT, failure(() -> service.create("a@b.dk", req(5, 8))).getStatus());
        verify(bookings, never()).save(any());
    }

    @Test void rejectsCarThatIsNotActive() {
        car.setStatus(CarStatus.MAINTENANCE);
        assertEquals(HttpStatus.CONFLICT, failure(() -> service.create("a@b.dk", req(5, 8))).getStatus());
    }

    @Test void rejectsUnknownCar() {
        when(cars.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        BookingRequest r = new BookingRequest(99L, today.plusDays(5), today.plusDays(8), null);
        assertEquals(HttpStatus.NOT_FOUND, failure(() -> service.create("a@b.dk", r)).getStatus());
    }

    @Test void ownerCanCancelPendingBooking() {
        Booking b = booking(BookingStatus.PENDING, customer);
        assertEquals(BookingStatus.CANCELLED, service.cancel(5L, "a@b.dk").status());
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test void otherCustomerCannotCancel() {
        booking(BookingStatus.PENDING, customer);
        assertEquals(HttpStatus.NOT_FOUND, failure(() -> service.cancel(5L, "c@d.dk")).getStatus());
    }

    @Test void activeBookingCannotBeCancelled() {
        booking(BookingStatus.ACTIVE, customer);
        assertEquals(HttpStatus.CONFLICT, failure(() -> service.cancel(5L, "a@b.dk")).getStatus());
    }

    @Test void adminCanApproveAndRejectPendingBookings() {
        Booking b = booking(BookingStatus.PENDING, customer);
        assertEquals(BookingStatus.APPROVED, service.approve(5L, "Welcome").status());
        assertEquals("Welcome", b.getAdminNote());
        b.setStatus(BookingStatus.PENDING);
        assertEquals(BookingStatus.REJECTED, service.reject(5L, null).status());
    }

    @Test void onlyPendingBookingsCanBeApproved() {
        booking(BookingStatus.CANCELLED, customer);
        assertEquals(HttpStatus.CONFLICT, failure(() -> service.approve(5L, null)).getStatus());
    }

    @Test void followsTheFullLifecycle() {
        Booking b = booking(BookingStatus.PENDING, customer);
        service.approve(5L, null);
        service.activate(5L);
        assertEquals(BookingStatus.ACTIVE, b.getStatus());
        service.complete(5L);
        assertEquals(BookingStatus.COMPLETED, b.getStatus());
    }
}
