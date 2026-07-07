package com.example.qltd.exception;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ApiError {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private Map<String, String> errors;
}
