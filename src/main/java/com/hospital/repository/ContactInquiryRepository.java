package com.hospital.repository;

import com.hospital.model.ContactInquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {
}
