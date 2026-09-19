-- V7: Lowercase names and unique constraints for Sub-Category duplicate prevention (PostgreSQL)

ALTER TABLE CATEGORIES DROP COLUMN IF EXISTS normalized_name;
ALTER TABLE SERVICES DROP COLUMN IF EXISTS normalized_name;

-- Convert existing CATEGORIES name to lowercase and collapsed spaces
UPDATE CATEGORIES SET name = LOWER(TRIM(REGEXP_REPLACE(name, '\s+', ' ', 'g')));

-- Convert existing SERVICES name to lowercase and collapsed spaces
UPDATE SERVICES SET name = LOWER(TRIM(REGEXP_REPLACE(name, '\s+', ' ', 'g')));

-- Ensure unique constraint on (category_id, name) in SERVICES
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_services_category_name'
    ) THEN
        ALTER TABLE SERVICES ADD CONSTRAINT uq_services_category_name UNIQUE (category_id, name);
    END IF;
END $$;
