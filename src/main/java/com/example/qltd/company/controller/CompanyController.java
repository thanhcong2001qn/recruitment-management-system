package com.example.qltd.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.company.dto.request.CompanySearchRequest;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.service.CompanyService;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CreateCompanyRequest request) {

        CompanyResponse response = companyService.createCompany(request);

        ApiResponse<CompanyResponse> apiResponse = ApiResponse.<CompanyResponse>builder()
                .success(true)
                .message("Company created successfully")
                .data(response)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(
            @PathVariable Long id) {

        CompanyResponse response = companyService.getCompanyById(id);

        return ResponseEntity.ok(
                ApiResponse.<CompanyResponse>builder()
                        .success(true)
                        .message("Company retrieved successfully")
                        .data(response)
                        .build());

    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyResponse>>> searchCompanies(

            CompanySearchRequest request,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<CompanyResponse> response = companyService.searchCompanies(request, pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<CompanyResponse>>builder()
                        .success(true)
                        .message("Company list retrieved successfully")
                        .data(response)
                        .build());

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(

            @PathVariable Long id,

            @Valid @RequestBody UpdateCompanyRequest request) {

        CompanyResponse response = companyService.updateCompany(id, request);

        return ResponseEntity.ok(

                ApiResponse.<CompanyResponse>builder()
                        .success(true)
                        .message("Company updated successfully")
                        .data(response)
                        .build()

        );

    }
}
