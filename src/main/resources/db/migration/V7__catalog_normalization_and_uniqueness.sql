-- V7: Lowercase names and unique constraints for Sub-Category duplicate prevention (PostgreSQL)

UPDATE CATEGORIES SET name = LOWER(TRIM(REGEXP_REPLACE(name, '\s+', ' ', 'g')));
UPDATE SERVICES SET name = LOWER(TRIM(REGEXP_REPLACE(name, '\s+', ' ', 'g')));

ALTER TABLE SERVICES ADD CONSTRAINT uq_services_category_name UNIQUE (category_id, name);
