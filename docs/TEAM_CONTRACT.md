# API Contract (CHUNG)

Dùng kiểu `long` cho ID, `LocalDate` cho ngày, `LocalTime` cho giờ, `BigDecimal` cho tiền.

| Chủ sở hữu | API được các nhóm khác gọi |
|---|---|
| N2 | CourtDAO.findAll(), CourtDAO.findById(long) |
| N1 | BookingDAO.findAll(), BookingDAO.findByDate(LocalDate), BookingDAO.findByCourtAndDate(long,LocalDate) |
| N1 | PaymentDAO.findAll(), PaymentDAO.findByBookingId(long) |
| N1 | CustomerDAO.findAll(), CustomerDAO.findById(long) |

- Booking status: `PENDING`, `CONFIRMED`, `CANCELLED`; thống nhất thêm trạng thái nếu cần.
- Khoảng thời gian booking dùng `[startTime,endTime)`: 08:00-09:00 không trùng 09:00-10:00.
- Booking `CANCELLED` không chiếm slot. Kiểm tra trùng lịch phải an toàn khi nhiều yêu cầu cùng lúc: dùng transaction và lock phù hợp, không chỉ SELECT rồi INSERT tùy tiện.
- Payment có 1 thanh toán/booking theo UNIQUE; nếu cần hoàn tiền/đa giao dịch hãy thảo luận và đổi schema.
- Password phải lưu dạng hash, không so sánh plaintext.
- DAO có thể ném SQLException, service phải chuyển thành thông báo phù hợp cho UI.
- Những DAO/service hiện tại cố ý `UnsupportedOperationException` cho nghiệp vụ chưa viết: không phải code hoàn thiện.

# TEAM CONTRACT — BADMINTON MANAGEMENT

## 1. Mục đích

Tài liệu này quy định các quy ước lập trình chung giữa hai thành viên.

Mục tiêu:
- Thống nhất cấu trúc dữ liệu.
- Thống nhất tên các phương thức.
- Tránh xung đột khi ghép code.
- Đảm bảo N1 và N2 có thể phát triển song song.

## 2. Công nghệ sử dụng

| Công nghệ | Phiên bản / Mục đích |
|---|---|
| Java | JDK 21 |
| Java Swing | Xây dựng giao diện Desktop |
| Maven | Quản lý thư viện và build |
| MySQL | Lưu trữ dữ liệu |
| JDBC | Kết nối Java với MySQL |
| JUnit 5 | Kiểm thử |
| Git & GitHub | Quản lý source code |

## 3. Quy ước cấu trúc project

Package gốc:

`com.badminton`

Các package con:

| Package | Chức năng |
|---|---|
| config | Cấu hình kết nối Database |
| model | Định nghĩa đối tượng dữ liệu |
| dao | Truy vấn Database |
| service | Xử lý nghiệp vụ |
| view | Xây dựng giao diện Swing |
| util | Các hàm hỗ trợ |

Luồng xử lý:

View -> Service -> DAO -> Database

Không viết câu lệnh SQL trực tiếp trong View.

## 4. Quy ước kiểu dữ liệu

| Dữ liệu | Kiểu Java |
|---|---|
| ID | long |
| Ngày | LocalDate |
| Giờ | LocalTime |
| Ngày và giờ | LocalDateTime |
| Tiền | BigDecimal |
| Danh sách | List<T> |
| Đúng/Sai | boolean |

Các Model hiện tại được khai báo bằng Java record.

Ví dụ:

```java
public record Court(
    long id,
    String name,
    BigDecimal pricePerHour,
    boolean active
) {
}
```

Truy cập thuộc tính bằng:

```java
court.id();
court.name();
court.pricePerHour();
```

Không sử dụng `getId()` nếu Model vẫn dùng record.

## 5. Quy ước Model

### User.java — N1

- id
- username
- passwordHash
- role

### Court.java — N2

- id
- name
- pricePerHour
- active

### Customer.java — N1

- id
- fullName
- phone
- email

### Booking.java — N1

- id
- courtId
- customerId
- bookingDate
- startTime
- endTime
- status

### Payment.java — N1

- id
- bookingId
- amount
- method
- paidAt

Không tự ý thay đổi tên hoặc kiểu dữ liệu của các thuộc tính khi chưa thống nhất với thành viên còn lại.

## 6. Quy ước DAO

### CourtDAO.java — N2

Các phương thức:

```java
List<Court> findAll();

Optional<Court> findById(long id);

long insert(Court court);

boolean update(Court court);

boolean delete(long id);
```

N1 sử dụng CourtDAO để lấy thông tin sân khi đặt sân và thanh toán.

### BookingDAO.java — N1

Các phương thức:

```java
List<Booking> findAll();

List<Booking> findByDate(LocalDate date);

List<Booking> findByCourtAndDate(
    long courtId,
    LocalDate date
);

Optional<Booking> findById(long id);

long insert(Booking booking);

boolean update(Booking booking);

boolean cancel(long id);
```

N2 sử dụng BookingDAO để:
- Xem lịch sân.
- Kiểm tra sân trống.
- Gợi ý sân.
- Thống kê lượt đặt.

### CustomerDAO.java — N1

```java
List<Customer> findAll();

Optional<Customer> findById(long id);

long insert(Customer customer);

boolean update(Customer customer);

boolean delete(long id);
```

### PaymentDAO.java — N1

```java
List<Payment> findAll();

Optional<Payment> findByBookingId(long bookingId);

long insert(Payment payment);
```

N2 sử dụng PaymentDAO để thống kê doanh thu.

### UserDAO.java — N1

```java
Optional<User> findByUsername(String username);
```

Các phương thức DAO có thể khai báo `throws SQLException` theo code starter.

## 7. Quy tắc đặt sân

Một booking chỉ hợp lệ khi:
- Khách hàng tồn tại.
- Sân tồn tại và đang hoạt động.
- Ngày đặt hợp lệ.
- Giờ bắt đầu nhỏ hơn giờ kết thúc.
- Không trùng thời gian với booking còn hiệu lực.

Ví dụ:

Booking A: 08:00 - 09:00

Booking B: 08:30 - 09:30

=> Trùng lịch.

Booking C: 09:00 - 10:00

=> Không trùng lịch với Booking A.

Không tính booking có trạng thái CANCELLED là đang chiếm sân.

Việc kiểm tra và tạo booking cần được bảo vệ bằng transaction/locking để tránh đặt trùng khi nhiều yêu cầu được thực hiện đồng thời.

## 8. Quy ước trạng thái Booking

| Trạng thái | Ý nghĩa |
|---|---|
| PENDING | Chờ xác nhận |
| CONFIRMED | Đã xác nhận |
| CANCELLED | Đã hủy |

Sử dụng thống nhất chữ in hoa.

## 9. Quy tắc thanh toán

- Mỗi booking chỉ có một bản ghi Payment.
- Không thanh toán booking đã hủy.
- Không thanh toán trùng.
- Số tiền được tính từ thời gian đặt và giá sân.
- Dùng BigDecimal để tính toán tiền.
- Không dùng double để lưu số tiền.

Công thức:

Total = PricePerHour * DurationMinutes / 60

## 10. Quy ước Service

Service chịu trách nhiệm:
- Kiểm tra dữ liệu.
- Kiểm tra quy tắc nghiệp vụ.
- Gọi DAO.
- Trả kết quả cho View.

View không gọi trực tiếp SQL.

## 11. Quy ước Git

N1 sử dụng branch:

`feature/n1`

N2 sử dụng branch:

`feature/n2`

Quy tắc:
- Không code trực tiếp trên main.
- Không tự ý sửa file người khác phụ trách.
- Nếu cần sửa Model hoặc DAO dùng chung, phải thông báo.
- Commit theo chức năng.
- Kiểm tra compile trước khi push.
- Tạo Pull Request trước khi merge.

## 12. Quy ước TODO

Những phương thức có:

```java
throw new UnsupportedOperationException("TODO");
```

là các phương thức chưa hoàn thiện.

Người phụ trách phải:
1. Viết logic thực tế.
2. Xử lý ngoại lệ.
3. Kiểm thử.
4. Xóa TODO sau khi hoàn thành.
