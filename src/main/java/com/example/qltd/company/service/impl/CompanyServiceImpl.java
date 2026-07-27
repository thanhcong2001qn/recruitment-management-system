package com.example.qltd.company.service.impl;

import com.example.qltd.common.mapper.PageMapper;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.util.SlugUtil;
import com.example.qltd.company.dto.request.CompanySearchRequest;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.enums.CompanyStatus;
import com.example.qltd.company.mapper.CompanyMapper;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.company.service.CompanyService;
import com.example.qltd.company.specification.CompanySpecification;
import com.example.qltd.company.validator.CompanyValidator;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final PageMapper pageMapper;
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CompanyValidator companyValidator;

    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {

        companyValidator.validate(request);

        Company company = companyMapper.toEntity(request);

        company.setSlug(
                generateUniqueSlug(
                        request.getName(),
                        null));

        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);
    }

    private String generateUniqueSlug(
            String companyName,
            Long excludeCompanyId) {

        String baseSlug = SlugUtil.toSlug(companyName);

        String slug = baseSlug;

        int counter = 1;

        while (true) {

            Optional<Company> company = companyRepository.findBySlug(slug);

            // Chưa có ai dùng slug này
            if (company.isEmpty()) {
                return slug;
            }

            // Nếu đang update và slug này thuộc chính company hiện tại
            if (excludeCompanyId != null
                    && company.get().getId().equals(excludeCompanyId)) {
                return slug;
            }

            slug = baseSlug + "-" + counter++;

        }

    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {

        Company company = companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyResponse> searchCompanies(
            CompanySearchRequest request,
            Pageable pageable) {

        Specification<Company> specification = CompanySpecification.search(request);

        Page<Company> companies = companyRepository.findAll(specification, pageable);

        return pageMapper.toPagedResponse(
                companies,
                companyMapper::toResponse);

    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(
            Long id,
            UpdateCompanyRequest request) {

        Company company = companyRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        companyValidator.validateUpdate(company, request);

        companyMapper.updateEntity(company, request);

        company.setSlug(
                generateUniqueSlug(
                        request.getName(),
                        company.getId()));
        Company updatedCompany = companyRepository.save(company);

        return companyMapper.toResponse(updatedCompany);

    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {

        Company company = companyRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        company.setDeleted(true);

        company.setStatus(CompanyStatus.INACTIVE);

        companyRepository.save(company);

    }

    @Override
    @Transactional
    public CompanyResponse restoreCompany(Long id) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        if (!company.getDeleted()) {
            throw new IllegalStateException("Company is not deleted.");
        }

        company.setDeleted(false);
        company.setStatus(CompanyStatus.ACTIVE);

        Company restoredCompany = companyRepository.save(company);

        return companyMapper.toResponse(restoredCompany);
    }
}
