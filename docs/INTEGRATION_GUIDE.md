
# INTEGRATION GUIDE — BADMINTON MANAGEMENT

## 1. Mục đích

Tài liệu hướng dẫn hai thành viên:
- Cài đặt và chạy project.
- Phát triển trên các branch riêng.
- Ghép code.
- Kiểm thử sau khi tích hợp.
- Xử lý các lỗi thường gặp.

## 2. Yêu cầu môi trường

Cài đặt:
- JDK 21
- Visual Studio Code
- Extension Pack for Java
- Maven
- MySQL
- Git

## 3. Kiểm tra công cụ

Mở Terminal trong VS Code.

Kiểm tra Java:

```bash
java -version
```

Kiểm tra Maven:

```bash
mvn -version
```

Kiểm tra Git:

```bash
git --version
```

## 4. Clone project

```bash
git clone https://github.com/mynameismuoiii/badminton-management.git
```

Di chuyển vào thư mục:

```bash
cd badminton-management
```

Mở project trong VS Code:

```bash
code .
```

## 5. Cấu hình Database

### Bước 1: Tạo Database

Mở MySQL Workbench hoặc MySQL Command Line.

Chạy:

`database/schema.sql`

### Bước 2: Thêm dữ liệu mẫu

Chạy:

`database/seed.sql`

### Bước 3: Tạo file cấu hình

Trong thư mục:

`src/main/resources/`

Copy:

`db.properties.example`

Thành:

`db.properties`

Nội dung mẫu:

```properties
db.url=jdbc:mysql://localhost:3306/badminton_management?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=YOUR_PASSWORD
```

Thay `YOUR_PASSWORD` bằng mật khẩu MySQL trên máy của bạn.

Không push `db.properties` lên GitHub.

## 6. Chạy project

Tại thư mục chứa pom.xml:

```bash
mvn clean test
```

Nếu build thành công, chạy:

```bash
mvn exec:java
```

Project starter sẽ hiển thị MainFrame.

Các màn hình nghiệp vụ chưa hoạt động đầy đủ cho đến khi hoàn thiện Service và DAO.

## 7. Làm việc trên Git

### N1 tạo branch

```bash
git switch main
git pull origin main
git switch -c feature/n1
```

### N2 tạo branch

```bash
git switch main
git pull origin main
git switch -c feature/n2
```

Mỗi người chỉ cần tạo branch một lần.

### Kiểm tra branch

```bash
git branch
```

### Lưu thay đổi

```bash
git add .
git commit -m "Implement assigned feature"
```

### Push lên GitHub

N1:

```bash
git push -u origin feature/n1
```

N2:

```bash
git push -u origin feature/n2
```

Sau lần push đầu tiên, có thể sử dụng `git push`.

## 8. Quy tắc ghép giao diện

MainFrame.java sử dụng CardLayout để chuyển màn hình.

Các Panel được đăng ký bằng:

```java
registerScreen("Sân", new CourtPanel());
registerScreen("Khách hàng", new CustomerPanel());
registerScreen("Đặt sân", new BookingPanel());
registerScreen("Lịch sân", new SchedulePanel());
registerScreen("Gợi ý sân", new SuggestionPanel());
registerScreen("Thanh toán", new PaymentPanel());
registerScreen("Thống kê", new StatisticsPanel());
```

Không tự ý sửa MainFrame nếu không cần thiết.

N1 và N2 phát triển giao diện trong Panel của mình.

## 9. Quy tắc phụ thuộc code

N1 cần:
- Court.java
- CourtDAO.java

N2 cần:
- Booking.java
- BookingDAO.java
- Payment.java
- PaymentDAO.java

Phải giữ đúng các tên phương thức trong TEAM_CONTRACT.md.

Nếu cần đổi phương thức, hai người phải thống nhất trước.

## 10. Merge code

Sau khi hoàn thành chức năng:

1. Push branch lên GitHub.
2. Tạo Pull Request.
3. Kiểm tra các file thay đổi.
4. Kiểm tra Maven build.
5. Review code.
6. Merge vào main.

Không merge khi project còn lỗi compile.

## 11. Kiểm thử tích hợp

### Đăng nhập
- [ ] Tài khoản đúng đăng nhập thành công.
- [ ] Sai mật khẩu bị từ chối.
- [ ] Không cho đăng nhập khi thiếu thông tin.

### Quản lý sân
- [ ] Hiển thị danh sách sân.
- [ ] Thêm sân.
- [ ] Sửa sân.
- [ ] Ngừng hoạt động sân.

### Quản lý khách hàng
- [ ] Hiển thị danh sách khách.
- [ ] Thêm khách.
- [ ] Sửa thông tin khách.
- [ ] Kiểm tra SĐT không hợp lệ.

### Đặt sân
- [ ] Tạo booking thành công.
- [ ] Không đặt trùng giờ.
- [ ] Không đặt sân ngừng hoạt động.
- [ ] Hủy booking.

### Lịch sân
- [ ] Xem booking theo ngày.
- [ ] Phân biệt sân trống và sân đã đặt.
- [ ] Booking đã hủy không chiếm lịch.

### Gợi ý sân
- [ ] Gợi ý sân còn trống.
- [ ] Không gợi ý sân đang bận.
- [ ] Xử lý khi tất cả sân đã được đặt.

### Thanh toán
- [ ] Tính đúng số tiền.
- [ ] Lưu thanh toán.
- [ ] Không thanh toán hai lần.
- [ ] Không thanh toán booking đã hủy.

### Thống kê
- [ ] Đếm số booking.
- [ ] Tính tổng doanh thu.
- [ ] Thống kê theo ngày hoặc tháng.

## 12. Xử lý lỗi thường gặp

### Lỗi Missing mandatory classpath entries

Chạy:

```bash
mvn clean compile
```

Nếu Maven build thành công:

1. Mở Command Palette trong VS Code.
2. Chọn `Java: Clean Java Language Server Workspace`.
3. Chọn Restart and delete.
4. Mở lại project và kiểm tra.

### Lỗi Cannot connect to MySQL

Kiểm tra:
- MySQL đang chạy.
- Database đã tồn tại.
- Username/password đúng.
- Port đúng.
- File db.properties đã được tạo.

### Lỗi Cannot find symbol

Kiểm tra:
- Package khai báo đúng.
- Đường dẫn file đúng.
- Class đã tồn tại.
- Import đúng.
- Hai branch đã được merge đầy đủ.

### Lỗi khi merge Git

Kiểm tra file có conflict.

Thống nhất nội dung đúng rồi mới commit merge.

Không xóa code của thành viên khác khi chưa kiểm tra.

## 13. Hoàn thành project

Trước khi nộp:

- [ ] Maven build thành công.
- [ ] Chạy ứng dụng thành công.
- [ ] Kết nối MySQL thành công.
- [ ] Hoàn thiện chức năng N1.
- [ ] Hoàn thiện chức năng N2.
- [ ] Hoàn thành kiểm thử.
- [ ] README đầy đủ.
- [ ] Không push mật khẩu lên GitHub.
- [ ] Main branch chứa phiên bản ổn định.
