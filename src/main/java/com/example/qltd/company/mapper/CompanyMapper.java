package com.example.qltd.company.mapper;

import org.springframework.stereotype.Component;

import com.example.qltd.common.util.SlugUtil;
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
                .website(request.getWebsite())
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

    public void updateEntity(Company company,
                             UpdateCompanyRequest request) {

        company.setName(request.getName());
        company.setSlug(SlugUtil.toSlug(request.getName()));
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setLogoUrl(request.getLogoUrl());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setCountry(request.getCountry());
        company.setCompanySize(request.getCompanySize());
        company.setFoundedYear(request.getFoundedYear());
        company.setEmployeeCount(request.getEmployeeCount());
    }

}
