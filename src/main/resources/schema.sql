-- ========================================================
-- College Lost & Found Management System Database Schema
-- Database: lost_found_db
-- Compatible with MySQL 8.0+ and H2 MySQL Compatibility Mode
-- ========================================================

-- 1. USERS TABLE
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS handover_records;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 2. CATEGORIES TABLE
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 3. ITEMS TABLE
CREATE TABLE items (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 7) DEFAULT 0.0000000,
    longitude DECIMAL(10, 7) DEFAULT 0.0000000,
    item_date DATE NOT NULL,
    image VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

-- 4. CLAIMS TABLE
CREATE TABLE claims (
    claim_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id BIGINT NOT NULL,
    claimant_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    proof_description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    otp VARCHAR(10),
    otp_expiry TIMESTAMP,
    qr_token VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_claims_item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE CASCADE,
    CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 5. HANDOVER RECORDS TABLE
CREATE TABLE handover_records (
    handover_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    claim_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    owner_id BIGINT,
    receiver_id BIGINT,
    handover_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_method VARCHAR(50),
    remarks TEXT,
    CONSTRAINT fk_handover_claim FOREIGN KEY (claim_id) REFERENCES claims (claim_id),
    CONSTRAINT fk_handover_item FOREIGN KEY (item_id) REFERENCES items (item_id),
    CONSTRAINT fk_handover_owner FOREIGN KEY (owner_id) REFERENCES users (user_id),
    CONSTRAINT fk_handover_receiver FOREIGN KEY (receiver_id) REFERENCES users (user_id)
);

-- 6. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 7. REPORTS TABLE (Flagged Items)
CREATE TABLE reports (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reported_by BIGINT NOT NULL,
    item_id BIGINT,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reported_by) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_item FOREIGN KEY (item_id) REFERENCES items (item_id) ON DELETE SET NULL
);

-- 8. AUDIT LOGS TABLE
CREATE TABLE audit_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
);

-- 9. MATCHES TABLE
CREATE TABLE matches (
    match_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lost_item_id BIGINT NOT NULL,
    found_item_id BIGINT NOT NULL,
    text_score DECIMAL(5, 2) NOT NULL,
    location_score DECIMAL(5, 2) NOT NULL,
    date_score DECIMAL(5, 2) NOT NULL,
    category_score DECIMAL(5, 2) NOT NULL,
    total_score DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_lost FOREIGN KEY (lost_item_id) REFERENCES items (item_id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_found FOREIGN KEY (found_item_id) REFERENCES items (item_id) ON DELETE CASCADE
);

-- ========================================================
-- SEED DATA
-- ========================================================

-- Insert Required Default Categories
INSERT INTO categories (name, description) VALUES
('Wallet', 'Wallets, purses, money clips, cardholders'),
('Mobile', 'Smartphones, feature phones, charging cords'),
('Laptop', 'Laptops, MacBooks, tablets, chargers'),
('ID Card', 'College identity cards, driver licenses, access badges'),
('Keys', 'Room keys, vehicle keys, lock fobs, keychains'),
('Bag', 'Backpacks, handbags, duffels, tote bags'),
('Book', 'Textbooks, notebooks, library books, planners'),
('Documents', 'Passports, marksheets, certificates, files'),
('Watch', 'Wristwatches, smartwatches, fitness trackers'),
('Jewellery', 'Rings, chains, earrings, bracelets'),
('Electronics', 'Headphones, earbuds, power banks, calculators'),
('Other', 'Umbrellas, water bottles, glasses, miscellaneous items');

-- Insert Default Development Users (BCrypt Hashes)
-- Credentials:
-- Admin:   admin@college.com   / Admin@123   (ADMIN)
-- Student: student@college.com / Student@123 (STUDENT)
-- Faculty: faculty@college.com / Faculty@123 (FACULTY)
INSERT INTO users (name, email, password, phone, role, department, status) VALUES
('Campus Administrator', 'admin@college.com', '$2a$12$DVqPlxJjF5BYQhBschxxpe2FCcsvosDaiVTLZ9WxxfuWQIXSXL0c2', '+919876543210', 'ADMIN', 'Administration', 'ACTIVE'),
('Aarav Sharma', 'student@college.com', '$2a$12$XnPZP6EfWf1J87Qvix6W9O5nsItDmcI0co289V/T7O939Wqn3UsR.', '+919876543211', 'STUDENT', 'Computer Science & Engineering', 'ACTIVE'),
('Dr. Rajesh Verma', 'faculty@college.com', '$2a$12$BwWzsxRGHuxVTMf4pH2k7uJSAQ3an6QGza6WL6dPEGbqDZYCEQk1a', '+919876543212', 'FACULTY', 'Electronics & Communication', 'ACTIVE'),
('Priya Patel', 'priya.student@college.com', '$2a$12$XnPZP6EfWf1J87Qvix6W9O5nsItDmcI0co289V/T7O939Wqn3UsR.', '+919876543213', 'STUDENT', 'Information Technology', 'ACTIVE');

-- Insert Sample Items for Immediate Testing
INSERT INTO items (user_id, category_id, title, description, item_type, location, latitude, longitude, item_date, image, status) VALUES
(2, 1, 'Black Leather Tommy Hilfiger Wallet', 'Lost black leather wallet containing college ID card and metro card near Central Library 2nd floor reading hall.', 'LOST', 'Central Library, 2nd Floor', 28.6139200, 77.2090200, '2026-03-20', 'uploads/items/sample-wallet.jpg', 'ACTIVE'),
(2, 3, 'Dell XPS 13 Silver Laptop', 'Left silver Dell laptop inside black sleeve on desk in CS Lab 304 after evening practicals.', 'LOST', 'Computer Science Lab 304', 28.6141000, 77.2093000, '2026-03-22', 'uploads/items/sample-laptop.jpg', 'ACTIVE'),
(4, 4, 'Student ID Card - Priya Patel', 'Lost college identity card with blue lanyard around Academic Block B cafeteria entrance.', 'LOST', 'Academic Block B Cafeteria', 28.6135000, 77.2088000, '2026-03-25', 'uploads/items/sample-idcard.jpg', 'ACTIVE'),
(3, 1, 'Black Leather Men Wallet with Cards', 'Found a men black leather wallet on a wooden table in Central Library reading zone. Has ID card and transit pass inside.', 'FOUND', 'Central Library, Reading Hall', 28.6139300, 77.2090400, '2026-03-20', 'uploads/items/sample-wallet.jpg', 'ACTIVE'),
(1, 5, 'Pair of Honda Bike Keys with Red Keychain', 'Found bike keys on bench near sports ground basketball court pavilion.', 'FOUND', 'Sports Complex, Basketball Court', 28.6150000, 77.2105000, '2026-03-24', 'uploads/items/sample-keys.jpg', 'ACTIVE'),
(3, 11, 'Apple AirPods Pro in White Case', 'Found white AirPods charging case in Seminar Hall auditorium row 4.', 'FOUND', 'Main Auditorium, Row 4', 28.6145000, 77.2091000, '2026-03-26', 'uploads/items/sample-airpods.jpg', 'ACTIVE');

-- Seed Initial Match for the Wallet (lost item 1 & found item 4)
INSERT INTO matches (lost_item_id, found_item_id, text_score, location_score, date_score, category_score, total_score) VALUES
(1, 4, 88.50, 95.00, 100.00, 100.00, 93.90);

-- Seed a Sample Claim
INSERT INTO claims (item_id, claimant_id, reason, proof_description, status, otp, otp_expiry, qr_token) VALUES
(4, 2, 'This is my lost wallet. The college ID inside belongs to Aarav Sharma.', 'College ID Aarav Sharma CS-2023-042 and blue Delhi metro smart card.', 'APPROVED', '482910', CURRENT_TIMESTAMP, 'QR-CLAIM-482910-UUID99');

-- Seed Sample Notifications
INSERT INTO notifications (user_id, title, message, is_read) VALUES
(2, 'Potential Match Found!', 'A FOUND item matching your "Black Leather Tommy Hilfiger Wallet" was reported by Dr. Rajesh Verma with a 93.9% similarity score.', FALSE),
(2, 'Claim Approved!', 'Your claim for item #4 (Black Leather Men Wallet) has been approved! Use OTP or QR code to verify handover.', FALSE),
(3, 'New Claim Submitted', 'Aarav Sharma submitted a claim for your reported found item #4.', TRUE);

-- Seed Sample Audit Logs
INSERT INTO audit_logs (user_id, action, ip_address) VALUES
(1, 'SYSTEM_INITIALIZATION', '127.0.0.1'),
(1, 'SEED_USERS_CREATED', '127.0.0.1'),
(2, 'CREATE_ITEM: Black Leather Tommy Hilfiger Wallet', '127.0.0.1'),
(3, 'CREATE_ITEM: Black Leather Men Wallet with Cards', '127.0.0.1');

-- Seed Sample Report (Flagged Item)
INSERT INTO reports (reported_by, item_id, reason, description, status) VALUES
(4, 5, 'Duplicate Listing', 'This bike keys listing looks duplicated from yesterday post.', 'OPEN');
