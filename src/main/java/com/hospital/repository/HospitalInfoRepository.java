package com.hospital.repository;

import com.hospital.model.HospitalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalInfoRepository extends JpaRepository<HospitalInfo, Long> {
}
