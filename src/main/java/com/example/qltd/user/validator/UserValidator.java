package com.example.qltd.user.validator;

import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validateRecruiterCompany(
            User user) {

        if (user == null) {
            throw new BusinessRuleException(
                    "User is required.");
        }

        if (user.getRole() != Role.RECRUITER) {
            throw new BusinessRuleException(
                    "User is not a recruiter.");
        }

        if (user.getCompany() == null) {
            throw new BusinessRuleException(
                    "Recruiter must belong to a company.");
        }
    }
}