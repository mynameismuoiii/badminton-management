
CREATE DATABASE IF NOT EXISTS badminton_management
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE badminton_management;

-- 1. TAI KHOAN (N1)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    full_name VARCHAR(120),
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',

    CONSTRAINT chk_users_role
        CHECK (role IN ('CUSTOMER', 'STAFF', 'ADMIN'))
) ENGINE=InnoDB;


-- 2. SAN CAU LONG (N2)
CREATE TABLE IF NOT EXISTS courts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    price_per_hour DECIMAL(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_courts_price
        CHECK (price_per_hour >= 0)
) ENGINE=InnoDB;


-- 3. KHACH HANG (N1)
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(120),

    CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB;


-- 4. DAT SAN (N1 - N2 CUNG SU DUNG)
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    court_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_bookings_court
        FOREIGN KEY (court_id) REFERENCES courts(id),

    CONSTRAINT fk_bookings_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT chk_bookings_time
        CHECK (start_time < end_time),

    CONSTRAINT chk_bookings_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),

    INDEX idx_bookings_court_date (
        court_id, booking_date
    ),

    INDEX idx_bookings_date_status (
        booking_date, status
    )
) ENGINE=InnoDB;


-- 5. THANH TOAN (N1 - N2 CUNG SU DUNG)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id),

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0),

    INDEX idx_payments_paid_at (paid_at)
) ENGINE=InnoDB;
