ALTER TABLE users
    ADD COLUMN company_id BIGINT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_user_company
        FOREIGN KEY (company_id)
        REFERENCES companies(id);

CREATE INDEX idx_users_company
    ON users(company_id);