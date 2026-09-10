package com.example.qltd.interview.service.impl;

import com.example.qltd.application.entity.Application;
import com.example.qltd.application.repository.ApplicationRepository;
import com.example.qltd.application.service.ApplicationAuthorizationService;
import com.example.qltd.common.exception.DuplicateResourceException;
import com.example.qltd.common.exception.ResourceNotFoundException;
import com.example.qltd.interview.dto.request.ChangeInterviewStatusRequest;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.dto.request.UpdateInterviewFeedbackRequest;
import com.example.qltd.interview.dto.response.InterviewResponse;
import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.mapper.InterviewMapper;
import com.example.qltd.interview.repository.InterviewRepository;
import com.example.qltd.interview.service.InterviewService;
import com.example.qltd.interview.service.InterviewStatusService;
import com.example.qltd.interview.validator.InterviewValidator;
import com.example.qltd.user.entity.User;
import com.example.qltd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;

    private final ApplicationRepository applicationRepository;

    private final UserRepository userRepository;

    private final ApplicationAuthorizationService authorizationService;

    private final InterviewValidator interviewValidator;

    private final InterviewStatusService interviewStatusService;

    private final InterviewMapper interviewMapper;

    @Override
    public InterviewResponse scheduleInterview(
            Long applicationId,
            String managerEmail,
            CreateInterviewRequest request) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found."));

        User manager = userRepository
                .findByEmailIgnoreCase(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        authorizationService.checkCanManage(
                application,
                manager);

        interviewValidator.validateSchedule(
                application,
                request);

        if (interviewRepository
                .existsByApplicationIdAndRoundNumber(
                        applicationId,
                        request.getRoundNumber())) {
            throw new DuplicateResourceException(
                    "Interview round already exists for this application.");
        }

        Interview interview = Interview.builder()
                .application(application)
                .roundNumber(request.getRoundNumber())
                .interviewType(request.getInterviewType())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .location(request.getLocation())
                .meetingUrl(request.getMeetingUrl())
                .notes(request.getNotes())
                .build();

        Interview savedInterview = interviewRepository.save(
                interview);

        return interviewMapper.toResponse(
                savedInterview);
    }

    @Override
    public InterviewResponse changeStatus(
            Long interviewId,
            String managerEmail,
            ChangeInterviewStatusRequest request) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview not found."));

        User manager = userRepository
                .findByEmailIgnoreCase(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        authorizationService.checkCanManage(
                interview.getApplication(),
                manager);

        interviewStatusService.transition(
                interview,
                request.getStatus());

        Interview updatedInterview = interviewRepository.save(
                interview);

        return interviewMapper.toResponse(
                updatedInterview);
    }

    @Override
    public InterviewResponse updateFeedback(
            Long interviewId,
            String managerEmail,
            UpdateInterviewFeedbackRequest request) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview not found."));

        User manager = userRepository
                .findByEmailIgnoreCase(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found."));

        authorizationService.checkCanManage(
                interview.getApplication(),
                manager);

        interviewValidator.validateFeedback(interview);

        interview.setFeedback(request.getFeedback());
        interview.setRating(request.getRating());

        Interview updatedInterview = interviewRepository.save(
                interview);

        return interviewMapper.toResponse(
                updatedInterview);
    }
}
