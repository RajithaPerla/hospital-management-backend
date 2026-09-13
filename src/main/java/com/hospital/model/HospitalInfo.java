package com.hospital.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "hospital_info")
@Getter
@Setter
public class HospitalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "parking_info")
    private String parkingInfo;

    @Column(name = "transit_info")
    private String transitInfo;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
