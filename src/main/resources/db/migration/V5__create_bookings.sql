-- V5: Create Bookings Table

CREATE TABLE IF NOT EXISTS BOOKINGS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    worker_id VARCHAR(36),
    service_id BIGINT NOT NULL,
    service_name VARCHAR(100),
    category_id VARCHAR(50),
    booking_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    payable_amount DECIMAL(10,2) NOT NULL,
    address_id VARCHAR(36),
    address_summary VARCHAR(500),
    slot_id VARCHAR(50),
    scheduled_time VARCHAR(50),
    coupon_code VARCHAR(30),
    notes VARCHAR(500),
    cancellation_reason VARCHAR(255),
    cancellation_fee DECIMAL(10,2) DEFAULT 0.00,
    refund_amount DECIMAL(10,2) DEFAULT 0.00,
    cancelled_at TIMESTAMP NULL,
    start_pin_hash VARCHAR(255),
    start_pin_encrypted VARCHAR(255),
    start_pin_verified BOOLEAN DEFAULT FALSE,
    pin_attempts INT DEFAULT 0,
    pin_expires_at TIMESTAMP NULL,
    service_started_at TIMESTAMP NULL,
    service_completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_customer_id ON BOOKINGS(customer_id);
CREATE INDEX idx_booking_worker_id ON BOOKINGS(worker_id);
CREATE INDEX idx_booking_status ON BOOKINGS(status);
CREATE INDEX idx_booking_date ON BOOKINGS(booking_date);
CREATE INDEX idx_booking_created_at ON BOOKINGS(created_at);
