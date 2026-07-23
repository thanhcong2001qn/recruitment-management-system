package com.example.qltd.company.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long id);

    Page<CompanyResponse> getCompanies(Pageable pageable);

}
