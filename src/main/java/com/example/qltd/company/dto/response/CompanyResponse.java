package com.example.qltd.company.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.qltd.company.enums.CompanySize;
import com.example.qltd.company.enums.CompanyStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private String website;

    private String email;

    private String phone;

    private String logoUrl;

    private String address;

    private String city;

    private String country;

    private CompanySize companySize;

    private Integer foundedYear;

    private Integer employeeCount;

    private CompanyStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
