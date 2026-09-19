-- Migration V14: Add image_url to VARIANTS table (PostgreSQL)

ALTER TABLE VARIANTS 
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500) NULL;
