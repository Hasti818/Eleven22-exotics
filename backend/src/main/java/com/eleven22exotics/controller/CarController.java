package com.eleven22exotics.controller;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.service.CarService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private final CarService cars;

    public CarController(CarService cars) { this.cars = cars; }

    @GetMapping
    public List<CarResponse> list(@RequestParam(required = false) String brand,
                                  @RequestParam(required = false) String category,
                                  @RequestParam(required = false) BigDecimal minPrice,
                                  @RequestParam(required = false) BigDecimal maxPrice,
                                  @RequestParam(required = false) String transmission,
                                  @RequestParam(required = false) String fuelType) {
        return cars.list(brand, category, minPrice, maxPrice, transmission, fuelType);
    }

    @GetMapping("/{id}")
    public CarResponse get(@PathVariable Long id) { return cars.get(id); }

    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(@PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return cars.availability(id, start, end);
    }
}
