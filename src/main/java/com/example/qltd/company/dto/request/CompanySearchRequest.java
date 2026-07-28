package com.example.qltd.company.dto.request;

import com.example.qltd.company.enums.CompanySize;
import com.example.qltd.company.enums.CompanyStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanySearchRequest {

    /**
     * Search theo tên hoặc mô tả
     */
    private String keyword;

    /**
     * Search theo thành phố
     */
    private String city;

    /**
     * ACTIVE / INACTIVE
     */
    private CompanyStatus status;

    /**
     * SMALL / MEDIUM / LARGE...
     */
    private CompanySize companySize;

}
