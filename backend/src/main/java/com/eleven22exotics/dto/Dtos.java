package com.eleven22exotics.dto;

import com.eleven22exotics.entity.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Request and response DTOs. Entities are never returned directly from controllers. */
public final class Dtos {
    private Dtos() {}

    // ---- auth ----
    public record RegisterRequest(@NotBlank String firstName, @NotBlank String lastName,
                                  @NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password,
                                  String phoneNumber) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record UserResponse(Long id, String firstName, String lastName, String email, String phoneNumber, Role role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhoneNumber(), u.getRole());
        }
    }
    public record AuthResponse(String token, UserResponse user) {}

    // ---- cars ----
    public record ImageResponse(Long id, String imageUrl, String altText, int displayOrder, ImageType imageType) {
        public static ImageResponse from(CarImage i) {
            return new ImageResponse(i.getId(), i.getImageUrl(), i.getAltText(), i.getDisplayOrder(), i.getImageType());
        }
    }
    public record CarResponse(Long id, String brand, String model, int modelYear, String category, String description,
                              BigDecimal dailyPrice, BigDecimal monthlyPrice, CarStatus status, String engine,
                              int horsepower, String transmission, String drivetrain, String fuelType, int seats,
                              int doors, BigDecimal zeroToOneHundred, int topSpeed, String mainImageUrl,
                              List<ImageResponse> images, boolean available) {
        public static CarResponse from(Car c, boolean available) {
            return new CarResponse(c.getId(), c.getBrand(), c.getModel(), c.getModelYear(), c.getCategory(),
                    c.getDescription(), c.getDailyPrice(), c.getMonthlyPrice(), c.getStatus(), c.getEngine(),
                    c.getHorsepower(), c.getTransmission(), c.getDrivetrain(), c.getFuelType(), c.getSeats(),
                    c.getDoors(), c.getZeroToOneHundred(), c.getTopSpeed(), c.getMainImageUrl(),
                    c.getImages().stream().map(ImageResponse::from).toList(), available);
        }
    }
    public record CarSummary(Long id, String brand, String model, String mainImageUrl) {
        public static CarSummary from(Car c) { return new CarSummary(c.getId(), c.getBrand(), c.getModel(), c.getMainImageUrl()); }
    }
    public record CarRequest(@NotBlank String brand, @NotBlank String model,
                             @NotNull @Min(1980) @Max(2100) Integer modelYear, @NotBlank String category,
                             String description, @NotNull @Positive BigDecimal dailyPrice,
                             @NotNull @Positive BigDecimal monthlyPrice, CarStatus status, @NotBlank String engine,
                             @NotNull @Positive Integer horsepower, @NotBlank String transmission,
                             @NotBlank String drivetrain, @NotBlank String fuelType, @NotNull @Positive Integer seats,
                             @NotNull @Positive Integer doors, @NotNull @Positive BigDecimal zeroToOneHundred,
                             @NotNull @Positive Integer topSpeed, String mainImageUrl) {}
    public record StatusRequest(@NotNull CarStatus status) {}
    public record PriceRequest(@NotNull @Positive BigDecimal dailyPrice, @NotNull @Positive BigDecimal monthlyPrice) {}
    public record ImageRequest(@NotBlank String imageUrl, String altText, Integer displayOrder, ImageType imageType) {}
    public record DateRange(LocalDate start, LocalDate end) {}
    public record AvailabilityResponse(boolean available, List<DateRange> blockedRanges) {}

    // ---- bookings ----
    public record BookingRequest(@NotNull Long carId, @NotNull LocalDate startDate, @NotNull LocalDate endDate,
                                 @Size(max = 1000) String customerNote) {}
    public record NoteRequest(@Size(max = 1000) String adminNote) {}
    public record BookingResponse(Long id, CarSummary car, UserResponse customer, LocalDate startDate, LocalDate endDate,
                                  BigDecimal totalPrice, BookingStatus status, String customerNote, String adminNote,
                                  Instant createdAt) {
        public static BookingResponse from(Booking b) {
            return new BookingResponse(b.getId(), CarSummary.from(b.getCar()), UserResponse.from(b.getUser()),
                    b.getStartDate(), b.getEndDate(), b.getTotalPrice(), b.getStatus(), b.getCustomerNote(),
                    b.getAdminNote(), b.getCreatedAt());
        }
    }
    public record StatsResponse(long cars, long availableCars, long maintenanceCars, long pendingBookings,
                                long activeRentals, long customers) {}
}
