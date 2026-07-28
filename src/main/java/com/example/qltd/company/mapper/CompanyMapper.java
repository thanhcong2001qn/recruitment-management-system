package com.example.qltd.company.mapper;

import org.springframework.stereotype.Component;

import com.example.qltd.common.util.SlugUtil;
import com.example.qltd.common.util.WebsiteUtil;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;

@Component
public class CompanyMapper {

    public Company toEntity(CreateCompanyRequest request) {

        return Company.builder()
                .name(request.getName())
                .slug(SlugUtil.toSlug(request.getName()))
                .description(request.getDescription())
                .website(WebsiteUtil.normalize(request.getWebsite()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .logoUrl(request.getLogoUrl())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .companySize(request.getCompanySize())
                .foundedYear(request.getFoundedYear())
                .employeeCount(request.getEmployeeCount())
                .status(CompanyStatus.ACTIVE)
                .deleted(false)
                .build();
    }

    public CompanyResponse toResponse(Company company) {

        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .slug(company.getSlug())
                .description(company.getDescription())
                .website(company.getWebsite())
                .email(company.getEmail())
                .phone(company.getPhone())
                .logoUrl(company.getLogoUrl())
                .address(company.getAddress())
                .city(company.getCity())
                .country(company.getCountry())
                .companySize(company.getCompanySize())
                .foundedYear(company.getFoundedYear())
                .employeeCount(company.getEmployeeCount())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    public void updateEntity(Company company, UpdateCompanyRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            company.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }

        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }

        if (request.getEmail() != null) {
            company.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }

        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }

        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }

        if (request.getCity() != null) {
            company.setCity(request.getCity());
        }

        if (request.getCountry() != null) {
            company.setCountry(request.getCountry());
        }

        if (request.getCompanySize() != null) {
            company.setCompanySize(request.getCompanySize());
        }

        if (request.getFoundedYear() != null) {
            company.setFoundedYear(request.getFoundedYear());
        }

        if (request.getEmployeeCount() != null) {
            company.setEmployeeCount(request.getEmployeeCount());
        }
    }

}
