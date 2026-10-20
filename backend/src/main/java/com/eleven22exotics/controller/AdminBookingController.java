package com.eleven22exotics.controller;

import com.eleven22exotics.dto.Dtos.*;
import com.eleven22exotics.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminBookingController {
    private final BookingService bookings;

    public AdminBookingController(BookingService bookings) { this.bookings = bookings; }

    @GetMapping("/bookings")
    public List<BookingResponse> all() { return bookings.all(); }

    @GetMapping("/stats")
    public StatsResponse stats() { return bookings.stats(); }

    @PatchMapping("/bookings/{id}/approve")
    public BookingResponse approve(@PathVariable Long id, @Valid @RequestBody(required = false) NoteRequest n) {
        return bookings.approve(id, n == null ? null : n.adminNote());
    }

    @PatchMapping("/bookings/{id}/reject")
    public BookingResponse reject(@PathVariable Long id, @Valid @RequestBody(required = false) NoteRequest n) {
        return bookings.reject(id, n == null ? null : n.adminNote());
    }

    @PatchMapping("/bookings/{id}/activate")
    public BookingResponse activate(@PathVariable Long id) { return bookings.activate(id); }

    @PatchMapping("/bookings/{id}/complete")
    public BookingResponse complete(@PathVariable Long id) { return bookings.complete(id); }
}
