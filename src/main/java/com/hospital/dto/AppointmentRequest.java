package com.hospital.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentRequest {

    @NotNull(message = "doctorId is required")
    private Long doctorId;

    @NotBlank(message = "department is required")
    private String department;

    @NotNull(message = "appointmentDate is required")
    @Future(message = "appointmentDate must be in the future")
    private LocalDate appointmentDate;

    @NotBlank(message = "timeSlot is required")
    private String timeSlot; // e.g. "09:00-09:30"

    private String reason;
}
