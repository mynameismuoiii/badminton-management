USE badminton_management;

INSERT INTO courts (name, price_per_hour, active)
VALUES
    ('Sân 1', 100000, TRUE),
    ('Sân 2', 120000, TRUE)
ON DUPLICATE KEY UPDATE
    price_per_hour = VALUES(price_per_hour);

INSERT INTO customers (full_name, phone, email)
VALUES
    ('Nguyễn Văn A', '0900000001', 'example@example.com')
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name);