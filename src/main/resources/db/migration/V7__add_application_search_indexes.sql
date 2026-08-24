CREATE INDEX idx_applications_job_status_created
    ON applications (
        job_id,
        status,
        created_at
    );