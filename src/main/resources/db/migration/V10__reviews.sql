-- V10: Rename TESTIMONIALS table to REVIEWS
--      Drops the old table created in V8 and creates the new one under the correct name.
--      Also updates the status constraint values to match ReviewStatus enum.

-- Create the new REVIEWS table
CREATE TABLE IF NOT EXISTS REVIEWS (
    id          BIGSERIAL PRIMARY KEY,
    customer_id UUID         NOT NULL,
    worker_id   UUID         NOT NULL,
    content     TEXT         NOT NULL,
    rating      INT          NOT NULL DEFAULT 5,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ  DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  DEFAULT NOW(),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMERS(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_worker   FOREIGN KEY (worker_id)   REFERENCES WORKER_PROFILES(id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating  CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_review_status  CHECK (status IN ('PENDING','APPROVED','REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_review_customer ON REVIEWS(customer_id);
CREATE INDEX IF NOT EXISTS idx_review_worker   ON REVIEWS(worker_id);
CREATE INDEX IF NOT EXISTS idx_review_status   ON REVIEWS(status);

-- Migrate existing data from TESTIMONIALS (if any rows exist)
INSERT INTO REVIEWS (id, customer_id, worker_id, content, rating, status, created_at, updated_at)
SELECT id, customer_id, worker_id, content, rating, status, created_at, updated_at
FROM TESTIMONIALS
ON CONFLICT DO NOTHING;

-- Drop the old TESTIMONIALS table
DROP TABLE IF EXISTS TESTIMONIALS;
