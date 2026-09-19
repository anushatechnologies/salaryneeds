-- V15: Reset Sequences for PostgreSQL to prevent ID collisions with seeded data

SELECT setval(pg_get_serial_sequence('services', 'id'), COALESCE((SELECT MAX(id) FROM services), 0) + 100, true);
SELECT setval(pg_get_serial_sequence('variants', 'id'), COALESCE((SELECT MAX(id) FROM variants), 0) + 100, true);
SELECT setval(pg_get_serial_sequence('coupons', 'id'), COALESCE((SELECT MAX(id) FROM coupons), 0) + 100, true);
SELECT setval(pg_get_serial_sequence('bookings', 'id'), COALESCE((SELECT MAX(id) FROM bookings), 0) + 100, true);
