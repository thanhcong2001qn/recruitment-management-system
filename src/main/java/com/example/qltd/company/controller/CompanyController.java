package com.example.qltd.company.controller;

import com.example.qltd.common.dto.ApiResponse;
import com.example.qltd.common.dto.PagedResponse;
import com.example.qltd.company.dto.request.CompanySearchRequest;
import com.example.qltd.company.dto.request.CreateCompanyRequest;
import com.example.qltd.company.dto.request.UpdateCompanyRequest;
import com.example.qltd.company.dto.response.CompanyResponse;
import com.example.qltd.company.service.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company", description = "APIs for managing companies")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "Create company", description = "Create a new company. Accessible by ADMIN and RECRUITER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Company created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Company already exists")
    })
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

    @Operation(summary = "Get company by ID", description = "Retrieve an active company by its ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(

            @Parameter(description = "Company ID", example = "1", required = true) @PathVariable Long id) {

        CompanyResponse response = companyService.getCompanyById(id);

        return ResponseEntity.ok(
                ApiResponse.<CompanyResponse>builder()
                        .success(true)
                        .message("Company retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Search companies", description = "Search and paginate companies using filter criteria.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company list retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search or pagination parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<PagedResponse<CompanyResponse>>> searchCompanies(

            @Parameter(description = "Company search filters") CompanySearchRequest request,

            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PagedResponse<CompanyResponse> response = companyService.searchCompanies(
                request,
                pageable);

        return ResponseEntity.ok(
                ApiResponse.<PagedResponse<CompanyResponse>>builder()
                        .success(true)
                        .message("Company list retrieved successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Update company", description = "Update an existing company. Accessible by ADMIN and RECRUITER.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Company already exists")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(

            @Parameter(description = "Company ID", example = "1", required = true) @PathVariable Long id,

            @Valid @RequestBody UpdateCompanyRequest request) {

        CompanyResponse response = companyService.updateCompany(
                id,
                request);

        return ResponseEntity.ok(
                ApiResponse.<CompanyResponse>builder()
                        .success(true)
                        .message("Company updated successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Delete company", description = "Soft delete a company. Only ADMIN can perform this operation.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only ADMIN can delete a company"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(

            @Parameter(description = "Company ID", example = "1", required = true) @PathVariable Long id) {

        companyService.deleteCompany(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Company deleted successfully")
                        .build());
    }

    @Operation(summary = "Restore company", description = "Restore a soft-deleted company. Only ADMIN can perform this operation.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company restored successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Company is not deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only ADMIN can restore a company"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> restoreCompany(

            @Parameter(description = "Company ID", example = "1", required = true) @PathVariable Long id) {

        CompanyResponse response = companyService.restoreCompany(id);

        return ResponseEntity.ok(
                ApiResponse.<CompanyResponse>builder()
                        .success(true)
                        .message("Company restored successfully")
                        .data(response)
                        .build());
    }
}