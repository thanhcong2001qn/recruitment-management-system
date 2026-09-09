CREATE TABLE interviews (

    id BIGINT NOT NULL AUTO_INCREMENT,

    application_id BIGINT NOT NULL,

    round_number INT NOT NULL,

    interview_type VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',

    scheduled_at DATETIME NOT NULL,

    duration_minutes INT NOT NULL,

    location VARCHAR(500),

    meeting_url VARCHAR(500),

    notes TEXT,

    feedback TEXT,

    rating INT,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_interviews
        PRIMARY KEY (id),

    CONSTRAINT uk_interview_application_round
        UNIQUE (application_id, round_number),

    CONSTRAINT fk_interview_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id),

    CONSTRAINT chk_interview_round_number
        CHECK (round_number > 0),

    CONSTRAINT chk_interview_duration
        CHECK (duration_minutes BETWEEN 15 AND 480),

    CONSTRAINT chk_interview_rating
        CHECK (rating IS NULL OR rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_interviews_application_schedule
    ON interviews (application_id, scheduled_at);

CREATE INDEX idx_interviews_status_schedule
    ON interviews (status, scheduled_at);
