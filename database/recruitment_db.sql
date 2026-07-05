CREATE DATABASE recruitment_db;
USE recruitment_db;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'RECRUITER', 'CANDIDATE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    website VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT,
    salary_min DECIMAL(12,2),
    salary_max DECIMAL(12,2),
    location VARCHAR(150),
    job_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'REMOTE') NOT NULL,
    level ENUM('INTERN', 'FRESHER', 'JUNIOR', 'MIDDLE', 'SENIOR') NOT NULL,
    status ENUM('DRAFT', 'OPEN', 'CLOSED') DEFAULT 'DRAFT',

    company_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_jobs_company
        FOREIGN KEY (company_id) REFERENCES companies(id),

    CONSTRAINT fk_jobs_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE candidate_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    dob DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    address VARCHAR(255),
    education TEXT,
    experience TEXT,
    skills TEXT,
    cv_url VARCHAR(255),

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_candidate_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    cv_url VARCHAR(255),
    cover_letter TEXT,
    status ENUM('APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'REJECTED', 'HIRED') DEFAULT 'APPLIED',

    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_applications_candidate
        FOREIGN KEY (candidate_id) REFERENCES users(id),

    CONSTRAINT fk_applications_job
        FOREIGN KEY (job_id) REFERENCES jobs(id),

    CONSTRAINT unique_candidate_job
        UNIQUE (candidate_id, job_id)
);

CREATE TABLE interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    interviewer_id BIGINT NOT NULL,
    interview_time DATETIME NOT NULL,
    location VARCHAR(255),
    meeting_link VARCHAR(255),
    note TEXT,
    result ENUM('PENDING', 'PASSED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_interviews_application
        FOREIGN KEY (application_id) REFERENCES applications(id),

    CONSTRAINT fk_interviews_interviewer
        FOREIGN KEY (interviewer_id) REFERENCES users(id)
);