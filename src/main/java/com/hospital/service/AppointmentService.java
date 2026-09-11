package com.hospital.service;

import com.hospital.dto.AppointmentRequest;
import com.hospital.dto.AppointmentResponse;
import com.hospital.dto.PageResponse;
import com.hospital.model.Appointment;
import com.hospital.model.User;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponse book(User patient, AppointmentRequest request) {
        User doctor = userRepository.findById(request.getDoctorId())
                .filter(u -> u.getRole() == User.Role.DOCTOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        boolean slotTaken = appointmentRepository.existsByDoctorIdAndAppointmentDateAndTimeSlot(
                doctor.getId(), request.getAppointmentDate(), request.getTimeSlot());
        if (slotTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That time slot is already booked");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .department(request.getDepartment())
                .appointmentDate(request.getAppointmentDate())
                .timeSlot(request.getTimeSlot())
                .reason(request.getReason())
                .status(Appointment.Status.PENDING)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    // Paginated + sorted so we never load all 10k+ rows into memory at once.
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getMyAppointments(User patient, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appointmentDate"));
        Page<Appointment> result = appointmentRepository.findByPatientId(patient.getId(), pageable);
        Page<AppointmentResponse> mapped = result.map(AppointmentResponse::fromEntity);
        return PageResponse.from(mapped);
    }

    @Transactional
    public AppointmentResponse cancel(User patient, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel another patient's appointment");
        }

        appointment.setStatus(Appointment.Status.CANCELLED);
        return AppointmentResponse.fromEntity(appointment);
    }
}
