package com.example.qltd.company.service;

public interface CompanySlugService {

    String generate(String companyName);

    String generate(String companyName, Long excludeCompanyId);

}