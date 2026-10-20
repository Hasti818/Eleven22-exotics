package com.eleven22exotics.service;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.entity.*;
import com.eleven22exotics.exception.ApiException;
import com.eleven22exotics.repository.BookingRepository;
import com.eleven22exotics.repository.CarRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CarService {
    static final List<BookingStatus> BLOCKING = List.of(BookingStatus.PENDING, BookingStatus.APPROVED, BookingStatus.ACTIVE);

    private final CarRepository cars;
    private final BookingRepository bookings;

    public CarService(CarRepository cars, BookingRepository bookings) {
        this.cars = cars;
        this.bookings = bookings;
    }

    // ---------- public ----------
    @Transactional(readOnly = true)
    public List<CarResponse> list(String brand, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                  String transmission, String fuelType) {
        Specification<Car> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.notEqual(root.get("status"), CarStatus.INACTIVE));
            if (has(brand)) p.add(cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase()));
            // "SUPERCAR" and "GRAND_TOURER" style values are accepted
            if (has(category)) p.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase().replace('_', ' ')));
            if (has(transmission)) p.add(cb.equal(cb.lower(root.get("transmission")), transmission.trim().toLowerCase()));
            if (has(fuelType)) p.add(cb.equal(cb.lower(root.get("fuelType")), fuelType.trim().toLowerCase()));
            if (minPrice != null) p.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("dailyPrice"), minPrice));
            if (maxPrice != null) p.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("dailyPrice"), maxPrice));
            return cb.and(p.toArray(new Predicate[0]));
        };
        return cars.findAll(spec, Sort.by("id")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CarResponse get(Long id) {
        Car c = find(id);
        if (c.getStatus() == CarStatus.INACTIVE) throw notFound();
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse availability(Long id, LocalDate start, LocalDate end) {
        Car c = find(id);
        List<DateRange> blocked = bookings.findByCarIdAndStatusInOrderByStartDate(id, BLOCKING).stream()
                .map(b -> new DateRange(b.getStartDate(), b.getEndDate())).toList();
        boolean free = c.getStatus() == CarStatus.ACTIVE;
        if (free && start != null && end != null) {
            free = end.isAfter(start) && !bookings.existsOverlap(id, BLOCKING, start, end);
        }
        return new AvailabilityResponse(free, blocked);
    }

    // ---------- admin ----------
    @Transactional(readOnly = true)
    public List<CarResponse> listAll() {
        return cars.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    public CarResponse create(CarRequest r) {
        Car c = new Car();
        apply(c, r);
        if (has(r.mainImageUrl())) addImage(c, r.mainImageUrl(), r.brand() + " " + r.model(), 0, ImageType.MAIN);
        return toResponse(cars.save(c));
    }

    public CarResponse update(Long id, CarRequest r) {
        Car c = find(id);
        apply(c, r);
        return toResponse(cars.save(c));
    }

    /** Cars with booking history are deactivated instead of deleted so that old bookings stay valid. */
    public void delete(Long id) {
        Car c = find(id);
        if (bookings.existsByCarId(id)) {
            c.setStatus(CarStatus.INACTIVE);
            cars.save(c);
        } else {
            cars.delete(c);
        }
    }

    public CarResponse setStatus(Long id, CarStatus status) {
        Car c = find(id);
        c.setStatus(status);
        return toResponse(cars.save(c));
    }

    public CarResponse setPrices(Long id, PriceRequest r) {
        Car c = find(id);
        c.setDailyPrice(r.dailyPrice());
        c.setMonthlyPrice(r.monthlyPrice());
        return toResponse(cars.save(c));
    }

    public CarResponse addImage(Long carId, ImageRequest r) {
        Car c = find(carId);
        ImageType type = r.imageType() == null ? ImageType.GALLERY : r.imageType();
        int order = r.displayOrder() == null ? c.getImages().size() : r.displayOrder();
        addImage(c, r.imageUrl(), r.altText(), order, type);
        if (type == ImageType.MAIN) c.setMainImageUrl(r.imageUrl());
        return toResponse(cars.save(c));
    }

    public void deleteImage(Long carId, Long imageId) {
        Car c = find(carId);
        if (!c.getImages().removeIf(i -> i.getId().equals(imageId))) throw new ApiException(HttpStatus.NOT_FOUND, "Image not found");
        cars.save(c);
    }

    // ---------- helpers ----------
    private Car find(Long id) { return cars.findById(id).orElseThrow(CarService::notFound); }
    private static ApiException notFound() { return new ApiException(HttpStatus.NOT_FOUND, "Car not found"); }
    private static boolean has(String s) { return s != null && !s.isBlank(); }

    /** A car is shown as available when it is in service and not rented out right now. */
    private CarResponse toResponse(Car c) {
        boolean available = c.getStatus() == CarStatus.ACTIVE && !bookings.existsByCarIdAndStatus(c.getId(), BookingStatus.ACTIVE);
        return CarResponse.from(c, available);
    }

    private static void addImage(Car c, String url, String alt, int order, ImageType type) {
        CarImage img = new CarImage();
        img.setCar(c);
        img.setImageUrl(url);
        img.setAltText(alt);
        img.setDisplayOrder(order);
        img.setImageType(type);
        c.getImages().add(img);
    }

    private static void apply(Car c, CarRequest r) {
        c.setBrand(r.brand().trim());
        c.setModel(r.model().trim());
        c.setModelYear(r.modelYear());
        c.setCategory(r.category().trim());
        c.setDescription(r.description());
        c.setDailyPrice(r.dailyPrice());
        c.setMonthlyPrice(r.monthlyPrice());
        if (r.status() != null) c.setStatus(r.status());
        c.setEngine(r.engine());
        c.setHorsepower(r.horsepower());
        c.setTransmission(r.transmission());
        c.setDrivetrain(r.drivetrain());
        c.setFuelType(r.fuelType());
        c.setSeats(r.seats());
        c.setDoors(r.doors());
        c.setZeroToOneHundred(r.zeroToOneHundred());
        c.setTopSpeed(r.topSpeed());
        if (r.mainImageUrl() != null) c.setMainImageUrl(r.mainImageUrl());
    }
}
