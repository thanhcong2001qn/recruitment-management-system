package com.example.qltd.company.service;

import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

}
