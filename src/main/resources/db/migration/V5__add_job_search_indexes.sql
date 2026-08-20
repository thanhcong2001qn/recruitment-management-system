CREATE INDEX idx_jobs_deleted_company
    ON jobs (deleted, company_id);

CREATE INDEX idx_jobs_deleted_status
    ON jobs (deleted, status);

CREATE INDEX idx_jobs_deleted_working_type
    ON jobs (deleted, working_type);

CREATE INDEX idx_jobs_deleted_experience_level
    ON jobs (deleted, experience_level);

CREATE INDEX idx_jobs_deleted_employment_type
    ON jobs (deleted, employment_type);

CREATE INDEX idx_jobs_deleted_created_at
    ON jobs (deleted, created_at);