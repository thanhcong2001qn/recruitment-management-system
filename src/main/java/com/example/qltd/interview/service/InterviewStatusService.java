package com.example.qltd.interview.service;

import com.example.qltd.interview.entity.Interview;
import com.example.qltd.interview.enums.InterviewStatus;

public interface InterviewStatusService {

    void transition(
            Interview interview,
            InterviewStatus targetStatus);
}
