CREATE TABLE jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    title VARCHAR(255) NOT NULL,

    slug VARCHAR(255) NOT NULL,

    description TEXT,

    requirements TEXT,

    responsibilities TEXT,

    salary_min DECIMAL(15, 2),

    salary_max DECIMAL(15, 2),

    currency VARCHAR(10) NOT NULL DEFAULT 'VND',

    location VARCHAR(255),

    working_type VARCHAR(30) NOT NULL,

    experience_level VARCHAR(30) NOT NULL,

    employment_type VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    deadline DATE NOT NULL,

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    company_id BIGINT NOT NULL,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_jobs
        PRIMARY KEY (id),

    CONSTRAINT uk_job_slug
        UNIQUE (slug),

    CONSTRAINT fk_job_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id)
);