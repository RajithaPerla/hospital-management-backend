package com.hospital.controller;

import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AppointmentResponse;
import com.hospital.dto.PageResponse;
import com.hospital.model.User;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // The authenticated User is injected by FirebaseTokenFilter's
    // Authentication#getPrincipal() via @AuthenticationPrincipal.
    @PostMapping
    public ResponseEntity<AppointmentResponse> book(
            @AuthenticationPrincipal User patient,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.book(patient, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal User patient,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Cap page size so a malicious/careless client can't request the
        // whole table in one call.
        int safeSize = Math.min(size, 50);
        return ResponseEntity.ok(appointmentService.getMyAppointments(patient, page, safeSize));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @AuthenticationPrincipal User patient,
            @PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancel(patient, id));
    }
}
