package com.example.qltd.user.service;

import com.example.qltd.user.dto.respone.RecruiterResponse;

public interface RecruiterManagementService {
    public RecruiterResponse assignCompany(
            Long recruiterId,
            Long companyId);
}
