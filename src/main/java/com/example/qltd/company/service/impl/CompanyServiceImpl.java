package com.example.qltd.company.service.impl;

import com.example.qltd.common.mapper.PageMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.common.util.SlugUtil;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.mapper.CompanyMapper;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.company.service.CompanyService;
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

        company.setSlug(generateUniqueSlug(request.getName()));

        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);
    }

    private String generateUniqueSlug(String companyName) {

        String slug = SlugUtil.toSlug(companyName);

        if (!companyRepository.existsBySlug(slug)) {
            return slug;
        }

        int index = 1;

        while (companyRepository.existsBySlug(slug + "-" + index)) {
            index++;
        }

        return slug + "-" + index;
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
    public PagedResponse<CompanyResponse> getCompanies(Pageable pageable) {

        Page<Company> companies = companyRepository.findByDeletedFalse(pageable);

        return pageMapper.toPagedResponse(
                companies,
                companyMapper::toResponse);

    }
}
