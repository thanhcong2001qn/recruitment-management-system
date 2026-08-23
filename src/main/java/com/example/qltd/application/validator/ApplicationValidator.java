package com.example.qltd.application.validator;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.enums.ApplicationStatus;
import com.example.qltd.common.exception.BusinessRuleException;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.shared.enums.Role;
import com.example.qltd.shared.enums.UserStatus;
import com.example.qltd.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ApplicationValidator {

    public void validateCreate(
            Job job,
            User candidate) {

        if (job == null) {
            throw new BusinessRuleException(
                    "Job is required.");
        }

        if (Boolean.TRUE.equals(
                job.getDeleted())) {
            throw new BusinessRuleException(
                    "Cannot apply to a deleted job.");
        }

        if (job.getStatus() != JobStatus.PUBLISHED) {

            throw new BusinessRuleException(
                    "Only published jobs can receive applications.");
        }

        if (candidate == null) {
            throw new BusinessRuleException(
                    "Candidate is required.");
        }

        if (candidate.getRole() != Role.CANDIDATE) {

            throw new BusinessRuleException(
                    "Only candidates can apply for jobs.");
        }

        if (candidate.getStatus() != UserStatus.ACTIVE) {

            throw new BusinessRuleException(
                    "Candidate account is not active.");
        }
    }

    public void validateStatusTransition(
            Application application,
            ApplicationStatus targetStatus) {

        if (application == null) {
            throw new BusinessRuleException(
                    "Application is required.");
        }

        if (targetStatus == null) {
            throw new BusinessRuleException(
                    "Target status is required.");
        }

        if (application.getStatus() == targetStatus) {
            return;
        }

        switch (application.getStatus()) {

            case APPLIED ->
                validateFromApplied(
                        targetStatus);

            case SCREENING ->
                validateFromScreening(
                        targetStatus);

            case SHORTLISTED ->
                validateFromShortlisted(
                        targetStatus);

            case INTERVIEW ->
                validateFromInterview(
                        targetStatus);

            case OFFERED ->
                validateFromOffered(
                        targetStatus);

            case HIRED,
                    REJECTED,
                    WITHDRAWN ->
                throw new BusinessRuleException(
                        "Application is already in a terminal status.");
        }
    }

    private void validateFromApplied(
            ApplicationStatus target) {

        if (target != ApplicationStatus.SCREENING
                && target != ApplicationStatus.REJECTED
                && target != ApplicationStatus.WITHDRAWN) {

            invalidTransition(
                    ApplicationStatus.APPLIED,
                    target);
        }
    }

    private void validateFromScreening(
            ApplicationStatus target) {

        if (target != ApplicationStatus.SHORTLISTED
                && target != ApplicationStatus.REJECTED
                && target != ApplicationStatus.WITHDRAWN) {

            invalidTransition(
                    ApplicationStatus.SCREENING,
                    target);
        }
    }

    private void validateFromShortlisted(
            ApplicationStatus target) {

        if (target != ApplicationStatus.INTERVIEW
                && target != ApplicationStatus.REJECTED
                && target != ApplicationStatus.WITHDRAWN) {

            invalidTransition(
                    ApplicationStatus.SHORTLISTED,
                    target);
        }
    }

    private void validateFromInterview(
            ApplicationStatus target) {

        if (target != ApplicationStatus.OFFERED
                && target != ApplicationStatus.REJECTED
                && target != ApplicationStatus.WITHDRAWN) {

            invalidTransition(
                    ApplicationStatus.INTERVIEW,
                    target);
        }
    }

    private void validateFromOffered(
            ApplicationStatus target) {

        if (target != ApplicationStatus.HIRED
                && target != ApplicationStatus.REJECTED
                && target != ApplicationStatus.WITHDRAWN) {

            invalidTransition(
                    ApplicationStatus.OFFERED,
                    target);
        }
    }

    private void invalidTransition(
            ApplicationStatus from,
            ApplicationStatus to) {

        throw new BusinessRuleException(
                "Invalid application status transition: "
                        + from
                        + " -> "
                        + to);
    }
}