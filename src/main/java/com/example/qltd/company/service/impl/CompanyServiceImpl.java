package com.example.qltd.company.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.util.SlugUtil;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.mapper.CompanyMapper;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.company.service.CompanyService;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {

        validateCompany(request);

        Company company = companyMapper.toEntity(request);

        company.setSlug(generateUniqueSlug(request.getName()));

        Company savedCompany = companyRepository.save(company);

        return companyMapper.toResponse(savedCompany);
    }

    private void validateCompany(CreateCompanyRequest request) {

        if (companyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Company name already exists");
        }
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Company email already exists");
        }
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

}
