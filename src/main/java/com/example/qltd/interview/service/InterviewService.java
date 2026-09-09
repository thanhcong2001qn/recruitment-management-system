package com.example.qltd.interview.service;

import com.example.qltd.interview.dto.request.CreateInterviewRequest;
import com.example.qltd.interview.dto.response.InterviewResponse;

public interface InterviewService {

    InterviewResponse scheduleInterview(
            Long applicationId,
            String managerEmail,
            CreateInterviewRequest request);
}
