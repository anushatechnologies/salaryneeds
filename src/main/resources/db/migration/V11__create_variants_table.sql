-- V11: Create Optional Variants Table and extend Services table for Subcategories (PostgreSQL)

ALTER TABLE SERVICES ADD COLUMN IF NOT EXISTS final_amount DECIMAL(10,2) NULL;
ALTER TABLE SERVICES ADD COLUMN IF NOT EXISTS discount DECIMAL(10,2) NULL DEFAULT 0.00;

UPDATE SERVICES SET final_amount = COALESCE(discount_price, base_price) WHERE final_amount IS NULL;

CREATE TABLE IF NOT EXISTS VARIANTS (
    id BIGSERIAL PRIMARY KEY,
    subcategory_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    amount DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) NULL DEFAULT 0.00,
    final_amount DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_variant_subcategory
        FOREIGN KEY (subcategory_id)
        REFERENCES SERVICES(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT uq_variants_subcategory_name UNIQUE (subcategory_id, name)
);

CREATE INDEX IF NOT EXISTS idx_variants_subcategory_id ON VARIANTS(subcategory_id);
CREATE INDEX IF NOT EXISTS idx_variants_status ON VARIANTS(status);
CREATE INDEX IF NOT EXISTS idx_variants_is_active ON VARIANTS(is_active);
