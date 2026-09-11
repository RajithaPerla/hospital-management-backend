package com.hospital.repository;

import com.hospital.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Uses idx_appt_patient_date. Fetch-joins doctor/patient in the same
    // query so the paginated list doesn't trigger N+1 lookups per row.
    @Query(value = """
            select a from Appointment a
            join fetch a.doctor d
            join fetch a.patient p
            where a.patient.id = :patientId
            order by a.appointmentDate desc, a.id desc
            """,
            countQuery = """
            select count(a) from Appointment a where a.patient.id = :patientId
            """)
    Page<Appointment> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    // Uses idx_appt_doctor_date - a doctor's schedule for a given day.
    @Query(value = """
            select a from Appointment a
            join fetch a.patient p
            where a.doctor.id = :doctorId and a.appointmentDate = :date
            order by a.timeSlot asc
            """,
            countQuery = """
            select count(a) from Appointment a
            where a.doctor.id = :doctorId and a.appointmentDate = :date
            """)
    Page<Appointment> findByDoctorIdAndDate(@Param("doctorId") Long doctorId,
                                             @Param("date") java.time.LocalDate date,
                                             Pageable pageable);

    boolean existsByDoctorIdAndAppointmentDateAndTimeSlot(Long doctorId,
                                                            java.time.LocalDate appointmentDate,
                                                            String timeSlot);
}
