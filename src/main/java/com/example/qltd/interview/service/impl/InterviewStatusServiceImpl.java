package com.example.qltd.interview.service.impl;

import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;
import com.example.qltd.interview.service.InterviewStatusService;
import com.example.qltd.interview.validator.InterviewValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewStatusServiceImpl
        implements InterviewStatusService {

    private final InterviewValidator interviewValidator;

    @Override
    public void transition(
            Interview interview,
            InterviewStatus targetStatus) {

        interviewValidator.validateStatusTransition(
                interview,
                targetStatus);

        interview.setStatus(targetStatus);
    }
}
