package com.example.qltd.application.dto.request;

import com.example.qltd.application.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationSearchRequest {

    private String keyword;

    private ApplicationStatus status;
}