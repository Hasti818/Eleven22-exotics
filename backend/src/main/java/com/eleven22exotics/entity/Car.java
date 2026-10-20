package com.eleven22exotics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
@Getter @Setter @NoArgsConstructor
public class Car {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String brand;
    @Column(nullable = false) private String model;
    @Column(nullable = false) private int modelYear;
    @Column(nullable = false) private String category;
    @Column(length = 2000) private String description;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal dailyPrice;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal monthlyPrice;
    // Availability is derived from status + bookings, there is deliberately no "available" boolean.
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private CarStatus status = CarStatus.ACTIVE;
    private String engine;
    private int horsepower;
    private String transmission;
    private String drivetrain;
    private String fuelType;
    private int seats;
    private int doors;
    @Column(precision = 4, scale = 2) private BigDecimal zeroToOneHundred;
    private int topSpeed;
    private String mainImageUrl;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<CarImage> images = new ArrayList<>();

    @PreUpdate void touch() { updatedAt = Instant.now(); }
}
