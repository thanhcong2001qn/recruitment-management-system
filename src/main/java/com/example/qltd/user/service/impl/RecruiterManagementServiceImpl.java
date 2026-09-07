package com.example.qltd.user.service.impl;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.user.dto.respone.RecruiterResponse;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import com.example.qltd.user.service.RecruiterManagementService;
import com.example.qltd.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruiterManagementServiceImpl
        implements RecruiterManagementService {

    private final UserRepository userRepository;

    private final CompanyRepository companyRepository;

    private final UserValidator userValidator;

    @Override
    @Transactional
    public RecruiterResponse assignCompany(
            Long recruiterId,
            Long companyId) {

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        userValidator.validateRecruiterCompany(recruiter);

        Company company = companyRepository
                .findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found."));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "Recruiter can only be assigned to an active company.");
        }

        recruiter.setCompany(company);

        User savedRecruiter = userRepository.save(recruiter);

        return toResponse(savedRecruiter);
    }

    private RecruiterResponse toResponse(User recruiter) {
        return RecruiterResponse.builder()
                .id(recruiter.getId())
                .fullName(recruiter.getFullName())
                .email(recruiter.getEmail())
                .phone(recruiter.getPhone())
                .role(recruiter.getRole())
                .status(recruiter.getStatus())
                .companyId(recruiter.getCompany().getId())
                .companyName(recruiter.getCompany().getName())
                .build();
    }
}
