package com.example.qltd.common.security;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.common.exception.ForbiddenException;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CompanyAuthorizationService {

    public void checkCompanyAccess(
            User user,
            Long companyId) {

        if (user == null) {
            throw new BusinessRuleException(
                    "User is required.");
        }

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (user.getRole() != Role.RECRUITER) {
            throw new BusinessRuleException(
                    "User is not allowed to manage company resources.");
        }

        if (user.getCompany() == null) {
            throw new BusinessRuleException(
                    "Recruiter is not assigned to a company.");
        }

        if (!user.getCompany()
                .getId()
                .equals(companyId)) {

            throw new BusinessRuleException(
                    "Recruiter cannot manage resources of another company.");
        }
    }
}