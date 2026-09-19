-- V6: Seed Initial Categories, Services, Coupons, Workers, and Customers

-- 1. Insert Categories
INSERT IGNORE INTO CATEGORIES (id, name, description, icon_url, display_order, is_active)
VALUES 
(UNHEX(REPLACE('11111111-1111-1111-1111-111111111111', '-', '')), 'Home Cleaning', 'Full house, kitchen, and bathroom deep cleaning services', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952', 1, TRUE),
(UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), 'Plumbing', 'Pipe repairs, leak fixing, tap installation, and drainage', 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39', 2, TRUE),
(UNHEX(REPLACE('33333333-3333-3333-3333-333333333333', '-', '')), 'Electrical', 'Wiring, switch repair, appliance setup, and short-circuit repair', 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e', 3, TRUE),
(UNHEX(REPLACE('44444444-4444-4444-4444-444444444444', '-', '')), 'Appliance Repair', 'AC servicing, refrigerator repair, washing machine repair', 'https://images.unsplash.com/photo-1581092160607-ee22621dd758', 4, TRUE),
(UNHEX(REPLACE('55555555-5555-5555-5555-555555555555', '-', '')), 'Salon for Women', 'Facial, manicure, pedicure, hair spa at doorstep', 'https://images.unsplash.com/photo-1560066984-138dadb4c035', 5, TRUE);

-- 2. Insert Services
INSERT IGNORE INTO SERVICES (id, category_id, name, description, base_price, discount_price, duration_minutes, inclusions, exclusions, image_url, rating_avg, is_active)
VALUES
(1, UNHEX(REPLACE('11111111-1111-1111-1111-111111111111', '-', '')), 'Deep Home Cleaning', 'Complete apartment deep cleaning with mechanized scrubbing', 1499.00, 1199.00, 180, 'Floor scrubbing, dusting, bathroom sanitization, kitchen degreasing', 'Inside appliances, wall painting', 'https://images.unsplash.com/photo-1581578731548-c64695cc6952', 4.85, TRUE),
(2, UNHEX(REPLACE('11111111-1111-1111-1111-111111111111', '-', '')), 'Bathroom Deep Cleaning', 'Intensive stain removal and tile scrubbing for 1 bathroom', 599.00, 499.00, 60, 'Stain removal, tile scrub, toilet disinfection, mirror polish', 'Exhaust fan deep motor repair', 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a', 4.90, TRUE),
(3, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), 'Tap Repair & Installation', 'Fix leaking faucets, showerheads, or install new fittings', 299.00, 249.00, 45, 'Inspection, seal replacement, faucet fitting', 'Cost of new tap or major line alterations', 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39', 4.75, TRUE),
(4, UNHEX(REPLACE('33333333-3333-3333-3333-333333333333', '-', '')), 'Switchboard Repair & Installation', 'Repair spark issues, replace modular switches and sockets', 349.00, 299.00, 45, 'Fault diagnosis, socket replacement, safety check', 'Cost of spare switches/boards', 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e', 4.80, TRUE),
(5, UNHEX(REPLACE('44444444-4444-4444-4444-444444444444', '-', '')), 'Split AC Regular Service', 'Foam jet cleaning of indoor and outdoor AC units', 699.00, 549.00, 60, 'Filter cleaning, cooling coil foam jet wash, drain pipe flush', 'Gas refilling, spare parts', 'https://images.unsplash.com/photo-1621905252507-b35492cc74b4', 4.92, TRUE);

-- 3. Insert Coupons
INSERT IGNORE INTO COUPONS (id, code, description, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_until, usage_limit, used_count, is_active)
VALUES
(1, 'WELCOME50', 'Get 50% off on your first home service booking', 'PERCENTAGE', 50.00, 400.00, 250.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 5000, 12, TRUE),
(2, 'FESTIVE100', 'Flat Rs 100 off on all services above Rs 500', 'FLAT', 100.00, 500.00, 100.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 2000, 45, TRUE),
(3, 'CLEAN20', '20% off on Home Cleaning services', 'PERCENTAGE', 20.00, 600.00, 300.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 1000, 8, TRUE),
(4, 'SAVE10', '10% instant discount on any booking', 'PERCENTAGE', 10.00, 200.00, 150.00, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 10000, 120, TRUE);

-- 4. Insert Demo Workers
INSERT IGNORE INTO WORKER_PROFILES (id, name, email, phone, password_hash, category_id, service, skills, experience_years, pincode, verified, rating_avg, completed_jobs_count, duty_online, last_lat, last_lng, last_seen_at, email_verified, phone_verified, account_status)
VALUES
(UNHEX(REPLACE('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '-', '')), 'Ramesh Sharma', 'ramesh.cleaner@example.com', '9876500001', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.e1q0YgX5Qh1mQ1Vj5T9mGz2pZ7XJvKO', UNHEX(REPLACE('11111111-1111-1111-1111-111111111111', '-', '')), 'Deep Home Cleaning', 'Deep scrubbing, Sanitization, Kitchen Degreasing', 6, '500081', TRUE, 4.95, 340, TRUE, 17.4485, 78.3758, NOW(), TRUE, TRUE, 'ACTIVE'),
(UNHEX(REPLACE('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '-', '')), 'Suresh Kumar', 'suresh.plumber@example.com', '9876500002', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.e1q0YgX5Qh1mQ1Vj5T9mGz2pZ7XJvKO', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), 'Tap Repair & Installation', 'Leak fix, Pipe fitting, Valve repair', 8, '500081', TRUE, 4.88, 512, TRUE, 17.4420, 78.3800, NOW(), TRUE, TRUE, 'ACTIVE'),
(UNHEX(REPLACE('cccccccc-cccc-cccc-cccc-cccccccccccc', '-', '')), 'Mahesh Reddy', 'mahesh.electrician@example.com', '9876500003', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.e1q0YgX5Qh1mQ1Vj5T9mGz2pZ7XJvKO', UNHEX(REPLACE('33333333-3333-3333-3333-333333333333', '-', '')), 'Switchboard Repair & Installation', 'Wiring, MCB setup, Appliance connections', 5, '500081', TRUE, 4.78, 220, TRUE, 17.4500, 78.3700, NOW(), TRUE, TRUE, 'ACTIVE'),
(UNHEX(REPLACE('dddddddd-dddd-dddd-dddd-dddddddddddd', '-', '')), 'Vikas Verma', 'vikas.ac@example.com', '9876500004', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.e1q0YgX5Qh1mQ1Vj5T9mGz2pZ7XJvKO', UNHEX(REPLACE('44444444-4444-4444-4444-444444444444', '-', '')), 'Split AC Regular Service', 'AC Jet pump wash, Gas check, PCB repair', 9, '500072', TRUE, 4.92, 640, TRUE, 17.4900, 78.4000, NOW(), TRUE, TRUE, 'ACTIVE');

-- 5. Insert Sample Customer
INSERT IGNORE INTO CUSTOMERS (id, name, email, phone, password_hash, default_address, email_verified, phone_verified, account_status)
VALUES
(UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')), 'Pavan Kumar', 'pavan@example.com', '9876543210', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.e1q0YgX5Qh1mQ1Vj5T9mGz2pZ7XJvKO', 'Flat 402, Lotus Heights, Madhapur, Hyderabad - 500081', TRUE, TRUE, 'ACTIVE');

-- 6. Insert Sample Address for Customer
INSERT IGNORE INTO ADDRESSES (id, customer_id, label, address_line, house, street, city, pincode, lat, lng, is_default)
VALUES
(UNHEX(REPLACE('a1111111-1111-1111-1111-111111111111', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')), 'Home', 'Flat 402, Lotus Heights, Madhapur', 'Flat 402, Lotus Heights', 'Ayyappa Society Main Road', 'Hyderabad', '500081', 17.4485, 78.3758, TRUE),
(UNHEX(REPLACE('a2222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')), 'Work', 'Cyber Towers, 5th Floor, Hitech City', '5th Floor', 'Cyber Towers Road', 'Hyderabad', '500081', 17.4504, 78.3808, FALSE);
