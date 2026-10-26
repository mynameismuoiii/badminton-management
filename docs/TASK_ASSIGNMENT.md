
# TASK ASSIGNMENT — BADMINTON MANAGEMENT

## 1. Tổng quan

Dự án: Badminton Management

Ngôn ngữ: Java 21

Giao diện: Java Swing

Database: MySQL

Quản lý thư viện: Maven

Số thành viên: 2

## 2. Bảng phân chia chức năng

| STT | Chức năng | Phụ trách |
|---|---|---|
| 1 | Thiết kế Database | CHUNG |
| 2 | Kết nối MySQL | CHUNG |
| 3 | Đăng nhập | N1 |
| 4 | Quản lý khách hàng | N1 |
| 5 | Đặt sân | N1 |
| 6 | Thanh toán | N1 |
| 7 | Quản lý sân | N2 |
| 8 | Xem lịch sân | N2 |
| 9 | Gợi ý sân trống | N2 |
| 10 | Thống kê | N2 |
| 11 | Giao diện chính | CHUNG |
| 12 | Kiểm thử tích hợp | CHUNG |

## 3. Người 1 — N1

Phụ trách:
- Đăng nhập.
- Quản lý khách hàng.
- Đặt sân.
- Thanh toán.

### Model

- [ ] User.java
- [ ] Customer.java
- [ ] Booking.java
- [ ] Payment.java

### DAO

- [ ] UserDAO.java
- [ ] CustomerDAO.java
- [ ] BookingDAO.java
- [ ] PaymentDAO.java

### Service

- [ ] AuthService.java
- [ ] CustomerService.java
- [ ] BookingService.java
- [ ] PaymentService.java

### View

- [ ] LoginFrame.java
- [ ] CustomerPanel.java
- [ ] BookingPanel.java
- [ ] PaymentPanel.java

### Util

- [ ] ValidationUtil.java
- [ ] PriceCalculator.java

### Test

- [ ] BookingServiceTest.java
- [ ] PriceCalculatorTest.java

### Kết quả cần đạt

- Đăng nhập bằng tài khoản hợp lệ.
- Quản lý danh sách khách hàng.
- Tạo và hủy booking.
- Không cho đặt sân trùng giờ.
- Tính tiền theo thời lượng sử dụng.
- Lưu thông tin thanh toán.
- Không cho thanh toán trùng.

## 4. Người 2 — N2

Phụ trách:
- Quản lý sân.
- Xem lịch sân.
- Gợi ý sân trống.
- Thống kê.

### Model

- [ ] Court.java

### DAO

- [ ] CourtDAO.java

### Service

- [ ] CourtService.java
- [ ] ScheduleService.java
- [ ] SuggestionService.java
- [ ] StatisticsService.java

### View

- [ ] CourtPanel.java
- [ ] SchedulePanel.java
- [ ] SuggestionPanel.java
- [ ] StatisticsPanel.java

### Util

- [ ] TimeSlotUtil.java

### Test

- [ ] ScheduleServiceTest.java
- [ ] SuggestionServiceTest.java

### Kết quả cần đạt

- Thêm, sửa, xóa hoặc ngừng hoạt động sân.
- Hiển thị danh sách sân.
- Xem lịch đặt sân theo ngày.
- Xác định sân đang trống.
- Gợi ý sân phù hợp theo thời gian.
- Thống kê số lượt đặt sân.
- Thống kê tổng doanh thu.

## 5. Công việc chung

### Cấu hình

- [ ] pom.xml
- [ ] .gitignore
- [ ] db.properties.example
- [ ] DatabaseConnection.java

### Database

- [ ] schema.sql
- [ ] seed.sql

### Giao diện chính

- [ ] Main.java
- [ ] MainFrame.java

### Tài liệu

- [ ] README.md
- [ ] TEAM_CONTRACT.md
- [ ] TASK_ASSIGNMENT.md
- [ ] INTEGRATION_GUIDE.md

### Tích hợp

- [ ] Ghép code N1 và N2.
- [ ] Kiểm tra database.
- [ ] Kiểm tra giao diện.
- [ ] Kiểm thử toàn bộ luồng.
- [ ] Sửa lỗi sau khi merge.

## 6. Phụ thuộc giữa hai thành viên

| Người cung cấp | File | Người sử dụng |
|---|---|---|
| N1 | Booking.java | N2 |
| N1 | BookingDAO.java | N2 |
| N1 | Payment.java | N2 |
| N1 | PaymentDAO.java | N2 |
| N2 | Court.java | N1 |
| N2 | CourtDAO.java | N1 |

Hai thành viên cần giữ nguyên các phương thức đã thống nhất.

## 7. Quy tắc hoàn thành công việc

Một chức năng chỉ được đánh dấu hoàn thành khi:
- Code không còn lỗi compile.
- Chức năng hoạt động thực tế.
- Có xử lý dữ liệu không hợp lệ.
- Kết nối được database nếu chức năng cần.
- Đã kiểm thử các trường hợp cơ bản.
- Đã push code lên branch.
- Không làm lỗi chức năng của thành viên khác.

## 8. Tiến độ đề xuất

| Ngày | N1 | N2 |
|---|---|---|
| 1 | Model, thống nhất DB | Court Model, thống nhất DAO |
| 2 | UserDAO, CustomerDAO, BookingDAO | CourtDAO, TimeSlotUtil |
| 3 | AuthService, CustomerService, BookingService | CourtService, ScheduleService, SuggestionService |
| 4 | Payment, các Panel N1 | Statistics, các Panel N2 |
| 5 | Unit Test, tích hợp | Unit Test, tích hợp |

Tiến độ có thể điều chỉnh tùy tình hình thực tế.
