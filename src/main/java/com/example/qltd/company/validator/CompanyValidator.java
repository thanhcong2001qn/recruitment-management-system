package com.example.qltd.company.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class CompanyValidator {

    private final CompanyRepository companyRepository;

    public void validate(CreateCompanyRequest request) {

        validateDuplicateName(request);

        validateDuplicateEmail(request);

        validateFoundedYear(request);

    }

    public void validateUpdate(
            Company company,
            UpdateCompanyRequest request) {

        validateCompanyName(company, request.getName());

        validateCompanyEmail(company, request.getEmail());

        validateFoundedYear(request);

    }

    private void validateDuplicateName(CreateCompanyRequest request) {

        if (companyRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Company name already exists.");
        }

    }

    private void validateDuplicateEmail(CreateCompanyRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return;
        }

        if (companyRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new DuplicateResourceException("Company email already exists.");
        }

    }

    private void validateFoundedYear(CreateCompanyRequest request) {

        if (request.getFoundedYear() == null) {
            return;
        }

        int currentYear = Year.now().getValue();

        if (request.getFoundedYear() > currentYear) {
            throw new IllegalArgumentException(
                    "Founded year cannot be greater than current year.");
        }

    }

    private void validateCompanyName(
            Company company,
            String newName) {

        if (newName == null || newName.isBlank()) {
            return;
        }

        String normalizedName = newName.trim();

        // Không đổi tên
        if (company.getName().equalsIgnoreCase(normalizedName)) {
            return;
        }

        if (companyRepository.existsByNameIgnoreCaseAndIdNot(
                normalizedName,
                company.getId())) {
            throw new DuplicateResourceException("Company name already exists.");
        }

    }

    private void validateCompanyEmail(
            Company company,
            String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim();

        if (company.getEmail() != null &&
                company.getEmail().equalsIgnoreCase(normalizedEmail)) {
            return;
        }

        if (companyRepository.existsByEmailIgnoreCaseAndIdNot(
                normalizedEmail,
                company.getId())) {
            throw new DuplicateResourceException("Company email already exists.");
        }

    }
}
