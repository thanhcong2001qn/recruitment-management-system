package com.example.qltd.company.service;

import org.springframework.data.domain.Pageable;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse getCompanyById(Long id);

    PagedResponse<CompanyResponse> getCompanies(Pageable pageable);

}
