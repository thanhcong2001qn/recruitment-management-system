package com.example.qltd.company.service;

import org.springframework.data.domain.Pageable;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.company.dto.request.CompanySearchRequest;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long id);

    PagedResponse<CompanyResponse> searchCompanies(
            CompanySearchRequest request,
            Pageable pageable);

    CompanyResponse updateCompany(
            Long id,
            UpdateCompanyRequest request);

}
