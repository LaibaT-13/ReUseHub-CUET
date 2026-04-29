-- ============================================
-- REUSEHUB DATABASE - TABLE CREATION SCRIPT
-- Generated from Java JPA Entity Models
-- Date: October 8, 2025
-- ============================================

-- Create database (optional - uncomment if needed)
-- CREATE DATABASE IF NOT EXISTS reusehub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE reusehub_db;

-- ============================================
-- 1. USER TABLE
-- Main users table for authenticated users
-- ============================================
CREATE TABLE user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Basic user information
    u_name VARCHAR(255) NOT NULL UNIQUE COMMENT 'Username (unique)',
    u_phone VARCHAR(20) NOT NULL COMMENT 'Phone number',
    u_cus_mail VARCHAR(255) NOT NULL UNIQUE COMMENT 'CUET email address',
    u_password VARCHAR(255) NOT NULL COMMENT 'Hashed password',
    date_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation date',
    
    -- User role and status
    role ENUM('BUYER', 'SELLER') DEFAULT 'BUYER' COMMENT 'User role',
    status ENUM('ACTIVE', 'WARNED', 'BANNED') DEFAULT 'ACTIVE' COMMENT 'Account status',
    
    -- Additional information
    address VARCHAR(500) COMMENT 'User address',
    email_verified BOOLEAN DEFAULT TRUE COMMENT 'Email verification status',
    
    -- Ban/Warning fields
    ban_reason VARCHAR(500) COMMENT 'Reason for ban if banned',
    warning_reason VARCHAR(500) COMMENT 'Reason for warning if warned',
    banned_date TIMESTAMP NULL COMMENT 'Date when user was banned',
    warned_date TIMESTAMP NULL COMMENT 'Date when user was warned',
    banned_by BIGINT NULL COMMENT 'User ID of admin who banned this user',
    warned_by BIGINT NULL COMMENT 'User ID of admin who warned this user',
    
    -- Password reset fields
    reset_otp VARCHAR(6) COMMENT 'OTP for password reset',
    otp_expiry TIMESTAMP NULL COMMENT 'OTP expiration time',
    
    -- Self-referencing foreign keys for ban/warn tracking
    FOREIGN KEY (banned_by) REFERENCES user(user_id) ON DELETE SET NULL,
    FOREIGN KEY (warned_by) REFERENCES user(user_id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_user_email (u_cus_mail),
    INDEX idx_user_status (status),
    INDEX idx_user_role (role),
    INDEX idx_user_name (u_name)
);

-- ============================================
-- 2. ADMIN TABLE
-- Separate admin accounts for system administration
-- ============================================
CREATE TABLE admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Admin information
    a_name VARCHAR(255) NOT NULL COMMENT 'Admin name',
    a_email VARCHAR(255) NOT NULL UNIQUE COMMENT 'Admin email address',
    a_password VARCHAR(255) NOT NULL COMMENT 'Hashed admin password',
    a_phone VARCHAR(20) NOT NULL COMMENT 'Admin phone number',
    date_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Admin account creation date',
    
    -- Admin status and department
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT 'Admin account status',
    department VARCHAR(255) COMMENT 'Admin department or role',
    
    -- Indexes
    INDEX idx_admin_email (a_email),
    INDEX idx_admin_status (status)
);

-- ============================================
-- 3. ITEM TABLE
-- Items posted by users for sale/trade
-- ============================================
CREATE TABLE item (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Item basic information
    i_name VARCHAR(255) NOT NULL COMMENT 'Item name',
    description TEXT COMMENT 'Item description',
    image VARCHAR(500) COMMENT 'Item image file path/URL',
    price DECIMAL(10,2) COMMENT 'Item price in currency',
    item_condition VARCHAR(50) COMMENT 'Item condition (like-new, good, fair, needs-fixing)',
    location VARCHAR(255) DEFAULT 'CUET Campus' COMMENT 'Item location',
    
    -- Timestamps
    post_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Item posting date',
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update date',
    
    -- Item categorization and status
    category VARCHAR(100) COMMENT 'Item category',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SOLD', 'UNSOLD') NOT NULL DEFAULT 'PENDING' COMMENT 'Item approval status',
    available BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Item availability for purchase',
    
    -- Admin approval fields
    approved_by BIGINT NULL COMMENT 'Admin who approved the item',
    approved_date TIMESTAMP NULL COMMENT 'Date when item was approved',
    rejection_reason TEXT COMMENT 'Reason for rejection if rejected',
    
    -- User relationship
    u_id BIGINT NOT NULL COMMENT 'User who posted the item',
    
    -- Foreign key constraints
    FOREIGN KEY (approved_by) REFERENCES admin(admin_id) ON DELETE SET NULL,
    FOREIGN KEY (u_id) REFERENCES user(user_id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_item_status (status),
    INDEX idx_item_category (category),
    INDEX idx_item_user (u_id),
    INDEX idx_item_available (available),
    INDEX idx_item_post_date (post_date),
    INDEX idx_item_price (price)
);

-- ============================================
-- 4. MESSAGE TABLE
-- Chat messages between users
-- ============================================
CREATE TABLE message (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Message participants
    sender_id BIGINT NOT NULL COMMENT 'User who sent the message',
    receiver_id BIGINT NOT NULL COMMENT 'User who received the message',
    item_id BIGINT NULL COMMENT 'Related item (optional)',
    
    -- Message content and metadata
    content TEXT NOT NULL COMMENT 'Message content',
    sent_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Message sent timestamp',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Message read status',
    
    -- Foreign key constraints
    FOREIGN KEY (sender_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_message_sender (sender_id),
    INDEX idx_message_receiver (receiver_id),
    INDEX idx_message_item (item_id),
    INDEX idx_message_read (is_read),
    INDEX idx_message_date (sent_date),
    INDEX idx_message_conversation (sender_id, receiver_id, sent_date)
);

-- ============================================
-- 5. REPORT TABLE
-- User reports for moderation and admin review
-- ============================================
CREATE TABLE report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Report participants
    reporter_id BIGINT NOT NULL COMMENT 'User who made the report',
    reported_user_id BIGINT NOT NULL COMMENT 'User being reported',
    item_id BIGINT NULL COMMENT 'Related item (optional)',
    
    -- Report details
    reason VARCHAR(255) NOT NULL COMMENT 'Reason for reporting',
    description TEXT NOT NULL COMMENT 'Detailed description of the issue',
    
    -- Report status and priority
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'PENDING' COMMENT 'Report processing status',
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM' COMMENT 'Report severity level',
    
    -- Timestamps
    report_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Report submission date',
    review_date TIMESTAMP NULL COMMENT 'Report review date',
    
    -- Admin review fields
    reviewed_by BIGINT NULL COMMENT 'Admin who reviewed the report',
    admin_notes TEXT COMMENT 'Admin notes about the report',
    
    -- Foreign key constraints
    FOREIGN KEY (reporter_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reported_user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE SET NULL,
    FOREIGN KEY (reviewed_by) REFERENCES admin(admin_id) ON DELETE SET NULL,
    
    -- Indexes for performance
    INDEX idx_report_status (status),
    INDEX idx_report_severity (severity),
    INDEX idx_report_reporter (reporter_id),
    INDEX idx_report_reported (reported_user_id),
    INDEX idx_report_date (report_date),
    INDEX idx_report_admin (reviewed_by)
);

-- ============================================
-- TRIGGERS AND CONSTRAINTS
-- ============================================

-- Trigger to automatically update item update_date
DELIMITER //
CREATE TRIGGER item_update_timestamp 
    BEFORE UPDATE ON item 
    FOR EACH ROW 
BEGIN 
    SET NEW.update_date = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- ============================================
-- VIEWS FOR COMMON QUERIES
-- ============================================

-- Active items view (approved and available items with seller info)
CREATE VIEW active_items_view AS
SELECT 
    i.item_id,
    i.i_name,
    i.description,
    i.price,
    i.item_condition,
    i.location,
    i.category,
    i.post_date,
    i.image,
    u.u_name AS seller_name,
    u.u_cus_mail AS seller_email,
    u.u_phone AS seller_phone
FROM item i
JOIN user u ON i.u_id = u.user_id
WHERE i.status = 'APPROVED' 
  AND i.available = TRUE 
  AND u.status = 'ACTIVE';

-- Pending reports summary for admin dashboard
CREATE VIEW pending_reports_view AS
SELECT 
    r.report_id,
    r.reason,
    r.severity,
    r.report_date,
    r.description,
    reporter.u_name AS reporter_name,
    reporter.u_cus_mail AS reporter_email,
    reported.u_name AS reported_user_name,
    reported.u_cus_mail AS reported_user_email,
    COALESCE(i.i_name, 'No item') AS item_name
FROM report r
JOIN user reporter ON r.reporter_id = reporter.user_id
JOIN user reported ON r.reported_user_id = reported.user_id
LEFT JOIN item i ON r.item_id = i.item_id
WHERE r.status = 'PENDING'
ORDER BY r.severity DESC, r.report_date ASC;

-- User statistics view
CREATE VIEW user_stats_view AS
SELECT 
    u.user_id,
    u.u_name,
    u.u_cus_mail,
    u.date_joined,
    u.status,
    COUNT(i.item_id) AS total_items_posted,
    COUNT(CASE WHEN i.status = 'SOLD' THEN 1 END) AS items_sold,
    COUNT(CASE WHEN i.status = 'APPROVED' AND i.available = TRUE THEN 1 END) AS active_items
FROM user u
LEFT JOIN item i ON u.user_id = i.u_id
GROUP BY u.user_id, u.u_name, u.u_cus_mail, u.date_joined, u.status;

-- Admin activity summary
CREATE VIEW admin_activity_view AS
SELECT 
    a.admin_id,
    a.a_name,
    a.a_email,
    a.status,
    COUNT(DISTINCT i.item_id) AS items_approved,
    COUNT(DISTINCT r.report_id) AS reports_reviewed,
    MAX(COALESCE(i.approved_date, r.review_date)) AS last_activity
FROM admin a
LEFT JOIN item i ON a.admin_id = i.approved_by
LEFT JOIN report r ON a.admin_id = r.reviewed_by
GROUP BY a.admin_id, a.a_name, a.a_email, a.status;

-- ============================================
-- STORED PROCEDURES
-- ============================================

-- Procedure to get user conversation list
DELIMITER //
CREATE PROCEDURE GetUserConversations(IN user_id BIGINT)
BEGIN
    SELECT DISTINCT
        CASE WHEN m.sender_id = user_id THEN m.receiver_id ELSE m.sender_id END AS conversation_user_id,
        CASE WHEN m.sender_id = user_id THEN receiver.u_name ELSE sender.u_name END AS conversation_user_name,
        MAX(m.sent_date) AS last_message_date,
        COUNT(CASE WHEN m.receiver_id = user_id AND m.is_read = FALSE THEN 1 END) AS unread_count
    FROM message m
    JOIN user sender ON m.sender_id = sender.user_id
    JOIN user receiver ON m.receiver_id = receiver.user_id
    WHERE m.sender_id = user_id OR m.receiver_id = user_id
    GROUP BY conversation_user_id, conversation_user_name
    ORDER BY last_message_date DESC;
END//
DELIMITER ;

-- Procedure to mark messages as read
DELIMITER //
CREATE PROCEDURE MarkMessagesAsRead(IN user_id BIGINT, IN other_user_id BIGINT)
BEGIN
    UPDATE message 
    SET is_read = TRUE 
    WHERE receiver_id = user_id AND sender_id = other_user_id AND is_read = FALSE;
END//
DELIMITER ;

-- ============================================
-- SAMPLE DATA (OPTIONAL - UNCOMMENT TO USE)
-- ============================================

/*
-- Sample admin user
INSERT INTO admin (a_name, a_email, a_password, a_phone, department) 
VALUES ('System Admin', 'admin@cuet.ac.bd', '$2a$10$example_hashed_password', '+8801700000000', 'IT Department');

-- Sample users
INSERT INTO user (u_name, u_phone, u_cus_mail, u_password, address, role) VALUES
('John Doe', '+8801711111111', 'john.doe@student.cuet.ac.bd', '$2a$10$example_hashed_password', 'CUET Campus, Chittagong', 'SELLER'),
('Jane Smith', '+8801722222222', 'jane.smith@cuet.ac.bd', '$2a$10$example_hashed_password', 'Faculty Quarter, CUET', 'BUYER');

-- Sample items
INSERT INTO item (i_name, description, price, item_condition, category, u_id, status) VALUES
('Programming Textbook', 'Introduction to Algorithms by CLRS. Good condition.', 1500.00, 'good', 'Books', 1, 'APPROVED'),
('Laptop Stand', 'Adjustable laptop stand, barely used.', 800.00, 'like-new', 'Electronics', 1, 'APPROVED');
*/

-- ============================================
-- DATABASE MAINTENANCE COMMANDS
-- ============================================

-- Enable event scheduler for automated tasks (optional)
-- SET GLOBAL event_scheduler = ON;

-- Create event to clean up old OTP entries (runs daily)
/*
CREATE EVENT IF NOT EXISTS cleanup_expired_otp
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
UPDATE user SET reset_otp = NULL, otp_expiry = NULL 
WHERE otp_expiry IS NOT NULL AND otp_expiry < NOW();
*/

-- ============================================
-- PERFORMANCE RECOMMENDATIONS
-- ============================================

/*
Performance Tips:
1. Regularly analyze table performance: ANALYZE TABLE table_name;
2. Monitor slow queries: Enable slow query log
3. Consider partitioning for large tables (items, messages)
4. Add composite indexes for common query patterns
5. Regular maintenance: OPTIMIZE TABLE table_name;

Security Tips:
1. Use prepared statements in application code
2. Implement proper input validation
3. Use SSL connections for database
4. Regular security audits and updates
5. Implement rate limiting for API endpoints
*/

-- ============================================
-- END OF TABLE CREATION SCRIPT
-- ============================================