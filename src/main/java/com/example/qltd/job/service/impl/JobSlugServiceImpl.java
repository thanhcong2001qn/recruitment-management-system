package com.example.qltd.job.service.impl;

import com.example.qltd.common.util.SlugUtil;
import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.JobSlugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobSlugServiceImpl implements JobSlugService {

    private final JobRepository jobRepository;

    @Override
    public String generate(String title) {
        return generate(title, null);
    }

    @Override
    public String generate(
            String title,
            Long excludeJobId) {

        String baseSlug = SlugUtil.toSlug(title);

        String slug = baseSlug;

        int counter = 1;

        while (true) {

            Optional<Job> existingJob = jobRepository.findBySlug(slug);

            if (existingJob.isEmpty()) {
                return slug;
            }

            if (excludeJobId != null
                    && existingJob.get().getId().equals(excludeJobId)) {
                return slug;
            }

            slug = baseSlug + "-" + counter++;
        }
    }
}