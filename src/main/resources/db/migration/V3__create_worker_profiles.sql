-- V3: Create Worker Profiles Table

CREATE TABLE IF NOT EXISTS WORKER_PROFILES (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    category_id BINARY(16) NOT NULL,
    service VARCHAR(100),
    skills VARCHAR(255),
    experience_years INT DEFAULT 0,
    pincode VARCHAR(10),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    rating_avg DECIMAL(3,2) DEFAULT 5.00,
    completed_jobs_count INT DEFAULT 0,
    duty_online BOOLEAN NOT NULL DEFAULT TRUE,
    last_lat DOUBLE,
    last_lng DOUBLE,
    last_seen_at TIMESTAMP NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_worker_category FOREIGN KEY (category_id) REFERENCES CATEGORIES(id) ON DELETE RESTRICT
);

CREATE INDEX idx_worker_category_id ON WORKER_PROFILES(category_id);
CREATE INDEX idx_worker_pincode ON WORKER_PROFILES(pincode);
CREATE INDEX idx_worker_rating_avg ON WORKER_PROFILES(rating_avg);
CREATE INDEX idx_worker_duty_online ON WORKER_PROFILES(duty_online);
