package com.hospital.controller;

import com.hospital.dto.ContactInquiryRequest;
import com.hospital.dto.HospitalInfoResponse;
import com.hospital.model.ContactInquiry;
import com.hospital.repository.ContactInquiryRepository;
import com.hospital.repository.HospitalInfoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Everything here is intentionally public (matches "/api/public/**" in
 * SecurityConfig) - visitors submitting the Contact Us form, or the
 * Directions/Contact pages reading the hospital's own address & phone,
 * haven't signed in yet.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ContactInquiryRepository contactInquiryRepository;
    private final HospitalInfoRepository hospitalInfoRepository;

    public PublicController(ContactInquiryRepository contactInquiryRepository,
                             HospitalInfoRepository hospitalInfoRepository) {
        this.contactInquiryRepository = contactInquiryRepository;
        this.hospitalInfoRepository = hospitalInfoRepository;
    }

    @PostMapping("/contact")
    public ResponseEntity<Void> submitContactForm(@Valid @RequestBody ContactInquiryRequest request) {
        ContactInquiry inquiry = ContactInquiry.builder()
                .name(request.getName())
                .email(request.getEmail())
                .message(request.getMessage())
                .status(ContactInquiry.Status.NEW)
                .build();
        contactInquiryRepository.save(inquiry);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/hospital-info")
    public ResponseEntity<HospitalInfoResponse> getHospitalInfo() {
        return hospitalInfoRepository.findAll().stream().findFirst()
                .map(HospitalInfoResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital info not configured"));
    }
}
