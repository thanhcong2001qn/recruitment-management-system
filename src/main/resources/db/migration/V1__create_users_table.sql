CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    full_name VARCHAR(100) NOT NULL,

    email VARCHAR(100) NOT NULL,

    password VARCHAR(255) NOT NULL,

    phone VARCHAR(20),

    role VARCHAR(20) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL,

    updated_at DATETIME,

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uk_users_email
        UNIQUE (email)
);