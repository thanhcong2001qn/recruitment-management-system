package com.example.qltd.interview.service;

import java.util.List;

import com.example.qltd.interview.dto.request.ChangeInterviewStatusRequest;
import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.dto.request.UpdateInterviewFeedbackRequest;
import com.example.qltd.interview.dto.response.InterviewResponse;

public interface InterviewService {

    List<InterviewResponse> getApplicationInterviews(
            Long applicationId,
            String managerEmail);

    InterviewResponse scheduleInterview(
            Long applicationId,
            String managerEmail,
            CreateInterviewRequest request);

    InterviewResponse changeStatus(
            Long interviewId,
            String managerEmail,
            ChangeInterviewStatusRequest request);

    InterviewResponse updateFeedback(
            Long interviewId,
            String managerEmail,
            UpdateInterviewFeedbackRequest request);
}
