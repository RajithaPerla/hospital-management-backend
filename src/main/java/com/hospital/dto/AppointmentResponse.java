package com.hospital.dto;

import com.hospital.model.Appointment;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class AppointmentResponse {
    Long id;
    Long doctorId;
    String doctorName;
    String department;
    LocalDate appointmentDate;
    String timeSlot;
    String status;
    String reason;

    public static AppointmentResponse fromEntity(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFullName())
                .department(a.getDepartment())
                .appointmentDate(a.getAppointmentDate())
                .timeSlot(a.getTimeSlot())
                .status(a.getStatus().name())
                .reason(a.getReason())
                .build();
    }
}
