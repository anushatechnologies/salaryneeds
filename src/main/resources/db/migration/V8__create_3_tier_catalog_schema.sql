-- V8: 3-Tier Catalog Schema (Service -> Category -> Sub-Category)

-- 1. Create Top-Level Services Master Table
CREATE TABLE IF NOT EXISTS CATALOG_SERVICES (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed default Top-Level Services if empty
INSERT IGNORE INTO CATALOG_SERVICES (id, name, description, image_url, display_order, is_active)
VALUES
(UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')), 'home services', 'Home cleaning, plumbing, repairs and maintenance', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952', 1, TRUE),
(UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')), 'appliance care', 'AC servicing, refrigerator, and appliance repairs', 'https://images.unsplash.com/photo-1581092160607-ee22621dd758', 2, TRUE),
(UNHEX(REPLACE('00000000-0000-0000-0000-000000000003', '-', '')), 'personal care & salon', 'Women salon, spa, grooming at doorstep', 'https://images.unsplash.com/photo-1560066984-138dadb4c035', 3, TRUE);

-- 2. Add service_id to CATEGORIES using safe prepared statement
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'CATEGORIES' AND column_name = 'service_id');
SET @sql_add_col = IF(@col_exists = 0, 'ALTER TABLE CATEGORIES ADD COLUMN service_id BINARY(16)', 'SELECT 1');
PREPARE stmt1 FROM @sql_add_col;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Link existing categories to default top services
UPDATE CATEGORIES SET service_id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')) WHERE service_id IS NULL AND name IN ('home cleaning', 'plumbing', 'electrical');
UPDATE CATEGORIES SET service_id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')) WHERE service_id IS NULL AND name IN ('appliance repair');
UPDATE CATEGORIES SET service_id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000003', '-', '')) WHERE service_id IS NULL;
