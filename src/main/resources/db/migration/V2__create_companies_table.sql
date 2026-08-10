CREATE TABLE companies (
    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(255) NOT NULL,

    slug VARCHAR(255) NOT NULL,

    description TEXT,

    website VARCHAR(255),

    email VARCHAR(255),

    phone VARCHAR(255),

    logo_url VARCHAR(255),

    address VARCHAR(255),

    city VARCHAR(255),

    country VARCHAR(255),

    founded_year INT,

    employee_count INT,

    company_size VARCHAR(50),

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME NOT NULL,

    updated_at DATETIME NOT NULL,

    CONSTRAINT pk_companies
        PRIMARY KEY (id),

    CONSTRAINT uk_company_slug
        UNIQUE (slug)
);