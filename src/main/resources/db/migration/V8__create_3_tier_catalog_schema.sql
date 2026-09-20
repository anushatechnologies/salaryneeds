-- V8: 3-Tier Catalog Schema (Service -> Category -> Sub-Category) (PostgreSQL)

CREATE TABLE IF NOT EXISTS CATALOG_SERVICES (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO CATALOG_SERVICES (id, name, description, image_url, display_order, is_active)
VALUES
('00000000-0000-0000-0000-000000000001', 'home services', 'Home cleaning, plumbing, repairs and maintenance', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952', 1, TRUE),
('00000000-0000-0000-0000-000000000002', 'appliance care', 'AC servicing, refrigerator, and appliance repairs', 'https://images.unsplash.com/photo-1581092160607-ee22621dd758', 2, TRUE),
('00000000-0000-0000-0000-000000000003', 'personal care & salon', 'Women salon, spa, grooming at doorstep', 'https://images.unsplash.com/photo-1560066984-138dadb4c035', 3, TRUE)
ON CONFLICT (name) DO NOTHING;

ALTER TABLE CATEGORIES ADD COLUMN IF NOT EXISTS service_id UUID;

UPDATE CATEGORIES SET service_id = '00000000-0000-0000-0000-000000000001' WHERE service_id IS NULL AND name IN ('home cleaning', 'plumbing', 'electrical');
UPDATE CATEGORIES SET service_id = '00000000-0000-0000-0000-000000000002' WHERE service_id IS NULL AND name IN ('appliance repair');
UPDATE CATEGORIES SET service_id = '00000000-0000-0000-0000-000000000003' WHERE service_id IS NULL;
