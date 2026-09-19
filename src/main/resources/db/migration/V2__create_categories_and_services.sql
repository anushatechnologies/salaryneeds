-- V2: Create Categories and Services Tables (PostgreSQL)

CREATE TABLE IF NOT EXISTS CATEGORIES (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon_url VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS SERVICES (
    id BIGSERIAL PRIMARY KEY,
    category_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    discount_price DECIMAL(10,2),
    image_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_services_category FOREIGN KEY (category_id) REFERENCES CATEGORIES(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_services_category_id ON SERVICES(category_id);
CREATE INDEX IF NOT EXISTS idx_services_is_active ON SERVICES(is_active);
