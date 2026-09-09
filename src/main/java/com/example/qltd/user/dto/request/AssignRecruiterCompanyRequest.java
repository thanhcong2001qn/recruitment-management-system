package com.example.qltd.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRecruiterCompanyRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;
}
