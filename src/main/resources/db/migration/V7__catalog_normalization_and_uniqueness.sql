-- V7: Lowercase names and unique constraints for Service and Sub-Category duplicate prevention

-- Drop normalized_name columns if they exist from any previous migration
SET @exist_cat_norm := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'CATEGORIES' AND column_name = 'normalized_name');
SET @sql_cat_norm = IF(@exist_cat_norm > 0, 'ALTER TABLE CATEGORIES DROP COLUMN normalized_name', 'SELECT 1');
PREPARE stmt1 FROM @sql_cat_norm;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @exist_srv_norm := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'SERVICES' AND column_name = 'normalized_name');
SET @sql_srv_norm = IF(@exist_srv_norm > 0, 'ALTER TABLE SERVICES DROP COLUMN normalized_name', 'SELECT 1');
PREPARE stmt2 FROM @sql_srv_norm;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Convert existing CATEGORIES name to lowercase and collapsed spaces
UPDATE CATEGORIES SET name = LOWER(TRIM(REGEXP_REPLACE(name, '[[:space:]]+', ' ')));

-- Convert existing SERVICES name to lowercase and collapsed spaces
UPDATE SERVICES SET name = LOWER(TRIM(REGEXP_REPLACE(name, '[[:space:]]+', ' ')));

-- Ensure unique constraint on (category_id, name) in SERVICES
ALTER TABLE SERVICES ADD CONSTRAINT uq_services_category_name UNIQUE (category_id, name);
