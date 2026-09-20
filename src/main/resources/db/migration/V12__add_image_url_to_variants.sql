-- Migration V12: Add image_url to VARIANTS table (PostgreSQL)

ALTER TABLE VARIANTS 
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) NULL;
