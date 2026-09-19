-- V8: Create TESTIMONIALS table
-- customer_id is NOT NULL — every testimonial is linked to a real customer
-- worker_id  is NOT NULL — every testimonial is linked to a real worker
-- status: PENDING → APPROVED / REJECTED  (moderation workflow)

CREATE TABLE IF NOT EXISTS TESTIMONIALS (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BINARY(16)   NOT NULL,
    worker_id   BINARY(16)   NOT NULL,
    content     TEXT         NOT NULL,
    rating      INT          NOT NULL DEFAULT 5,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_testimonial_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMERS(id) ON DELETE CASCADE,
    CONSTRAINT fk_testimonial_worker   FOREIGN KEY (worker_id)   REFERENCES WORKER_PROFILES(id) ON DELETE CASCADE,
    CONSTRAINT chk_testimonial_rating  CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_testimonial_status  CHECK (status IN ('PENDING','APPROVED','REJECTED'))
);

CREATE INDEX idx_testimonial_customer ON TESTIMONIALS(customer_id);
CREATE INDEX idx_testimonial_worker   ON TESTIMONIALS(worker_id);
CREATE INDEX idx_testimonial_status   ON TESTIMONIALS(status);
