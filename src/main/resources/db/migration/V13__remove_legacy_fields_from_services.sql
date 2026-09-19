-- Migration V13: Drop legacy fields from SERVICES table (PostgreSQL)

ALTER TABLE SERVICES 
    DROP COLUMN IF EXISTS duration_minutes,
    DROP COLUMN IF EXISTS inclusions,
    DROP COLUMN IF EXISTS exclusions,
    DROP COLUMN IF EXISTS rating_avg;
