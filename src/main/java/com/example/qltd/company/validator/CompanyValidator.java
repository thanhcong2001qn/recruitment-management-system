package com.example.qltd.company.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
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
                    "Founded year cannot be greater than current year."
            );
        }

    }

}
