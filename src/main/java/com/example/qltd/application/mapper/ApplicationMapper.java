package com.example.qltd.application.mapper;

import com.example.qltd.application.dto.response.ApplicationResponse;
import com.example.qltd.application.entity.Application;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(
            Application application) {

        return ApplicationResponse.builder()
                .id(application.getId())

                .jobId(
                        application.getJob().getId())

                .jobTitle(
                        application.getJob().getTitle())

                .candidateId(
                        application.getCandidate().getId())

                .candidateName(
                        application.getCandidate().getFullName())

                .candidateEmail(
                        application.getCandidate().getEmail())

                .coverLetter(
                        application.getCoverLetter())

                .resumeUrl(
                        application.getResumeUrl())

                .status(
                        application.getStatus())

                .appliedAt(
                        application.getAppliedAt())

                .createdAt(
                        application.getCreatedAt())

                .updatedAt(
                        application.getUpdatedAt())

                .build();
    }
}