-- ============================================================
-- Company search indexes
-- ============================================================

CREATE INDEX idx_companies_deleted_city
    ON companies (deleted, city);

CREATE INDEX idx_companies_deleted_status_size
    ON companies (deleted, status, company_size);

CREATE INDEX idx_companies_deleted_created_at
    ON companies (deleted, created_at);