-- Chạy 1 lần trong MySQL Workbench.
-- Không xóa dữ liệu hiện tại.

USE badminton_management;

CREATE TABLE IF NOT EXISTS payment_requests (
    booking_id BIGINT NOT NULL PRIMARY KEY,

    reported_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_requests_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB;