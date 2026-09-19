-- V10: Create worker locations table for real-time tracking during trips
CREATE TABLE IF NOT EXISTS worker_locations (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    worker_id VARCHAR(36) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_worker_locations_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_worker_locations_booking_time ON worker_locations(booking_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_worker_locations_worker ON worker_locations(worker_id);
