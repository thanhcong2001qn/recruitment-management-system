package com.example.qltd.job.service;

public interface JobSlugService {

    String generate(String title);

    String generate(String title, Long excludeJobId);
}