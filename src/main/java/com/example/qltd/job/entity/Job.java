package com.example.qltd.job.entity;

import com.example.qltd.common.base.BaseEntity;
import com.example.qltd.company.entity.Company;
import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jobs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_job_slug", columnNames = "slug")
})
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(precision = 15, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 15, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 10)
    @Builder.Default
    private String currency = "VND";

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkingType workingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_company"))
    private Company company;
}