package com.example.qltd.job.service;

import com.example.qltd.job.entity.Job;
import com.example.qltd.job.repository.JobRepository;
import com.example.qltd.job.service.impl.JobSlugServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobSlugService")
class JobSlugServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobSlugServiceImpl jobSlugService;

    @Test
    @DisplayName("Should generate base slug when slug is available")
    void shouldGenerateBaseSlug() {

        when(
                jobRepository.findBySlug(
                        "java-backend-developer"))
                .thenReturn(Optional.empty());

        String result = jobSlugService.generate(
                "Java Backend Developer");

        assertThat(result)
                .isEqualTo(
                        "java-backend-developer");
    }

    @Test
    @DisplayName("Should append suffix when slug already exists")
    void shouldAppendSuffixWhenSlugExists() {

        Job existing = Job.builder()
                .id(10L)
                .slug(
                        "java-backend-developer")
                .build();

        when(
                jobRepository.findBySlug(
                        "java-backend-developer"))
                .thenReturn(
                        Optional.of(existing));

        when(
                jobRepository.findBySlug(
                        "java-backend-developer-1"))
                .thenReturn(
                        Optional.empty());

        String result = jobSlugService.generate(
                "Java Backend Developer");

        assertThat(result)
                .isEqualTo(
                        "java-backend-developer-1");
    }

    @Test
    @DisplayName("Should keep existing slug when excluding current job")
    void shouldKeepExistingSlugWhenExcludingCurrentJob() {

        Job existing = Job.builder()
                .id(10L)
                .slug(
                        "java-backend-developer")
                .build();

        when(
                jobRepository.findBySlug(
                        "java-backend-developer"))
                .thenReturn(
                        Optional.of(existing));

        String result = jobSlugService.generate(
                "Java Backend Developer",
                10L);

        assertThat(result)
                .isEqualTo(
                        "java-backend-developer");
    }
}