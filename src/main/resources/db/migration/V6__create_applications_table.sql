CREATE TABLE applications (

    id BIGINT NOT NULL AUTO_INCREMENT,

    job_id BIGINT NOT NULL,

    candidate_id BIGINT NOT NULL,

    cover_letter TEXT,

    resume_url VARCHAR(500),

    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',

    applied_at DATETIME NOT NULL,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_applications
        PRIMARY KEY (id),

    CONSTRAINT uk_application_job_candidate
        UNIQUE (job_id, candidate_id),

    CONSTRAINT fk_application_job
        FOREIGN KEY (job_id)
        REFERENCES jobs(id),

    CONSTRAINT fk_application_candidate
        FOREIGN KEY (candidate_id)
        REFERENCES users(id)
);

CREATE INDEX idx_applications_job_status
    ON applications (job_id, status);

CREATE INDEX idx_applications_candidate_status
    ON applications (candidate_id, status);

CREATE INDEX idx_applications_created_at
    ON applications (created_at);