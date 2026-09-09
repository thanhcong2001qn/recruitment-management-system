package com.example.qltd.job.dto.request;

import com.example.qltd.job.enums.EmploymentType;
import com.example.qltd.job.enums.ExperienceLevel;
import com.example.qltd.job.enums.JobStatus;
import com.example.qltd.job.enums.WorkingType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobSearchRequest {

    /**
     * Search theo title hoặc description.
     */
    private String keyword;

    /**
     * Filter theo company.
     */
    private Long companyId;

    /**
     * Filter theo location.
     */
    private String location;

    /**
     * Filter theo trạng thái Job.
     */
    private JobStatus status;

    /**
     * Filter theo hình thức làm việc.
     */
    private WorkingType workingType;

    /**
     * Filter theo level kinh nghiệm.
     */
    private ExperienceLevel experienceLevel;

    /**
     * Filter theo loại việc làm.
     */
    private EmploymentType employmentType;
}