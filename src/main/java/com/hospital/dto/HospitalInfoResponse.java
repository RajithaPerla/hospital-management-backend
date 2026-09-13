package com.hospital.dto;

import com.hospital.model.HospitalInfo;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class HospitalInfoResponse {
    String name;
    String addressLine1;
    String addressLine2;
    String city;
    String state;
    String postalCode;
    String phoneNumber;
    String email;
    String operatingHours;
    String parkingInfo;
    String transitInfo;
    BigDecimal latitude;
    BigDecimal longitude;

    public static HospitalInfoResponse fromEntity(HospitalInfo h) {
        return HospitalInfoResponse.builder()
                .name(h.getName())
                .addressLine1(h.getAddressLine1())
                .addressLine2(h.getAddressLine2())
                .city(h.getCity())
                .state(h.getState())
                .postalCode(h.getPostalCode())
                .phoneNumber(h.getPhoneNumber())
                .email(h.getEmail())
                .operatingHours(h.getOperatingHours())
                .parkingInfo(h.getParkingInfo())
                .transitInfo(h.getTransitInfo())
                .latitude(h.getLatitude())
                .longitude(h.getLongitude())
                .build();
    }
}
