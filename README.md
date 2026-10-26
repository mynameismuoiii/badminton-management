# Badminton Management (Java Swing + MySQL)

Starter cho nhóm hai người. Đã có cấu trúc thư mục, model dạng `record`, DAO/service contract, giao diện điều hướng Swing, SQL schema/seed, và util cơ bản. **Chức năng nghiệp vụ chưa triển khai**; những phương thức cần làm ném `UnsupportedOperationException` để tránh hiểu nhầm có thể dùng ngay.

## Yêu cầu
- JDK 21, Maven 3.9+, MySQL 8+

## Chạy UI mẫu
```bash
mvn clean test
mvn exec:java
```
Mở cửa sổ MainFrame và chuyển giữa 7 màn hình placeholder (không cần DB cho UI mẫu).

## Cấu hình database
1. Chạy `database/schema.sql` và `database/seed.sql` trong MySQL.
2. Copy `src/main/resources/db.properties.example` thành `src/main/resources/db.properties`.
3. Đổi username, password trên máy riêng. File thật đã được `.gitignore`.

## Phân công
Xem `docs/TASK_ASSIGNMENT.md`, `docs/TEAM_CONTRACT.md` và `docs/INTEGRATION_GUIDE.md`.

## Điểm cần hoàn thiện
- N1: Login, customer, booking, payment (gồm xác thực mật khẩu an toàn và giao dịch kiểm tra trùng booking).
- N2: Court CRUD, lịch, gợi ý, thống kê.
- CHUNG: kết nối LoginFrame -> MainFrame, tích hợp/test, kiểm tra SQL và constraints.
