package com.eleven22exotics.controller;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Protected by SecurityConfig: only ROLE_ADMIN can reach /api/admin/**. */
@RestController
@RequestMapping("/api/admin/cars")
public class AdminCarController {
    private final CarService cars;

    public AdminCarController(CarService cars) { this.cars = cars; }

    @GetMapping
    public List<CarResponse> all() { return cars.listAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse create(@Valid @RequestBody CarRequest r) { return cars.create(r); }

    @PutMapping("/{id}")
    public CarResponse update(@PathVariable Long id, @Valid @RequestBody CarRequest r) { return cars.update(id, r); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { cars.delete(id); }

    @PatchMapping("/{id}/status")
    public CarResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest r) { return cars.setStatus(id, r.status()); }

    @PatchMapping("/{id}/prices")
    public CarResponse prices(@PathVariable Long id, @Valid @RequestBody PriceRequest r) { return cars.setPrices(id, r); }

    @PostMapping("/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse addImage(@PathVariable Long id, @Valid @RequestBody ImageRequest r) { return cars.addImage(id, r); }

    @DeleteMapping("/{carId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable Long carId, @PathVariable Long imageId) { cars.deleteImage(carId, imageId); }
}
