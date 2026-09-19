-- V7: Create ADMINS table

CREATE TABLE IF NOT EXISTS ADMINS (
    id         BINARY(16) PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    email      VARCHAR(254)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed one default admin  (password: Admin@1234)
INSERT IGNORE INTO ADMINS (id, name, email, password_hash)
VALUES (
    UNHEX(REPLACE('a0000000-0000-0000-0000-000000000001', '-', '')),
    'Demo Administrator',
    'admin@salaryneeds.com',
    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.e1q0YgX5Qh1mQ1Vj5T9mGz2pZ7XJvKO'
);
