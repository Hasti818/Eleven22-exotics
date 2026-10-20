package com.eleven22exotics.controller;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookings;

    public BookingController(BookingService bookings) { this.bookings = bookings; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(Authentication a, @Valid @RequestBody BookingRequest r) { return bookings.create(a.getName(), r); }

    @GetMapping("/my")
    public List<BookingResponse> mine(Authentication a) { return bookings.mine(a.getName()); }

    @GetMapping("/{id}")
    public BookingResponse get(Authentication a, @PathVariable Long id) { return bookings.get(id, a.getName()); }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(Authentication a, @PathVariable Long id) { return bookings.cancel(id, a.getName()); }
}
