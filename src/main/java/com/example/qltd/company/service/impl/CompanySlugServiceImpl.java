package com.example.qltd.company.service.impl;

import com.example.qltd.common.util.SlugUtil;
import com.example.qltd.company.entity.Company;
import com.example.qltd.company.repository.CompanyRepository;
import com.example.qltd.company.service.CompanySlugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanySlugServiceImpl implements CompanySlugService {

    private final CompanyRepository companyRepository;

    @Override
    public String generate(String companyName) {
        return generate(companyName, null);
    }

    @Override
    public String generate(String companyName, Long excludeCompanyId) {

        String baseSlug = SlugUtil.toSlug(companyName);

        String slug = baseSlug;

        int counter = 1;

        while (true) {

            Optional<Company> company = companyRepository.findBySlug(slug);

            if (company.isEmpty()) {
                return slug;
            }

            if (excludeCompanyId != null
                    && company.get().getId().equals(excludeCompanyId)) {
                return slug;
            }

            slug = baseSlug + "-" + counter++;

        }

    }

}