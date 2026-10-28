# TH-MINTON | Hệ thống quản lý và đặt sân cầu lông

> Ứng dụng **desktop Java Swing** hỗ trợ khách hàng tìm và đặt sân cầu lông, theo dõi lịch và thanh toán chuyển khoản; hỗ trợ quản trị viên quản lý sân, khách hàng và theo dõi hoạt động kinh doanh.
>
> Tài liệu được cập nhật theo **source trong gói `badminton-management (4).zip` ngày 28/10/2026**, thay cho README khởi tạo đã không còn đúng với tình trạng code.

## 1. Giới thiệu

**TH-MINTON** là ứng dụng quản lý sân cầu lông viết bằng Java, sử dụng giao diện **Swing** và cơ sở dữ liệu **MySQL**. Dự án tổ chức theo các tầng `model` – `dao` – `service` – `view/ui`; sử dụng Maven để quản lý build và thư viện.

- **Tên dự án Maven:** `com.badminton:badminton-management:1.0-SNAPSHOT`
- **Điểm khởi chạy:** `com.badminton.Main`
- **Loại ứng dụng:** Java desktop (không phải website, không cần web server)
- **Repository:** <https://github.com/mynameismuoiii/badminton-management>

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java, target **Java 21** (`maven.compiler.release=21`) |
| Giao diện | Java Swing, Java2D, giao diện tùy biến |
| Build / quản lý thư viện | Apache Maven (khuyến nghị 3.9+) |
| Cơ sở dữ liệu | MySQL 8+ (InnoDB, UTF-8) |
| Truy cập dữ liệu | JDBC, `mysql-connector-j` **9.1.0** |
| Kiểm thử | JUnit Jupiter **5.11.4** |
| Bảo mật mật khẩu | PBKDF2-HMAC-SHA256, salt ngẫu nhiên |
| Font giao diện | Fredoka, Lexend (font được đóng gói trong resources) |

## 3. Các chức năng trong source

### 3.1. Xác thực và tài khoản

- Đăng ký tài khoản khách hàng và đăng nhập bằng dữ liệu MySQL.
- Kiểm tra tên, số điện thoại, tên đăng nhập trùng, độ dài mật khẩu; mật khẩu lưu dưới dạng hash, không lưu văn bản gốc.
- Đăng ký tạo liên kết dữ liệu ở cả hai bảng `users` và `customers` trong một giao dịch.
- Phân quyền giao diện theo `ADMIN` và `CUSTOMER`; có chức năng đăng xuất.

### 3.2. Khách hàng (`CUSTOMER`)

- Xem tổng quan, danh sách sân đang hoạt động; chuyển từ sân đã chọn sang màn hình đặt sân.
- Chọn ngày, khung giờ và đặt sân; xem lịch sân, gợi ý sân trống.
- Xem những đơn đặt sân thuộc tài khoản đang đăng nhập; có luồng hủy đơn chưa thanh toán.
- Xem thông tin đơn và số tiền, quét QR chuyển khoản, **báo đã chuyển khoản** và theo dõi trạng thái xác nhận.

### 3.3. Quản trị viên (`ADMIN`)

- Truy cập các màn hình chung: tổng quan, danh sách sân, đặt sân, lịch sân, khám phá sân và thanh toán.
- **Quản lý sân:** xem danh sách, tìm kiếm, thêm, cập nhật, ngừng hoạt động / bật lại sân.
- **Quản lý khách hàng:** xem, thêm, sửa, xóa theo ràng buộc dữ liệu; xem thống kê/hồ sơ và lịch sử đặt sân của khách hàng.
- **Xác nhận thanh toán:** kiểm tra tiền chuyển khoản thực tế trước khi xác nhận thanh toán cho đơn.
- **Thống kê & báo cáo:** các chỉ số đặt sân, sân hoạt động, doanh thu từ khoản đã thanh toán; đồ thị lượt đặt sân và doanh thu theo ngày.

### 3.4. Quy tắc nghiệp vụ chính

- Kiểm tra khung giờ hợp lệ và ngăn đặt trùng sân/giờ với đơn chưa bị hủy; thao tác thêm booking có transaction và khóa hàng sân trong DAO.
- Hai khung giờ chỉ chạm tại thời điểm kết thúc/bắt đầu (ví dụ `08:00–09:00` và `09:00–10:00`) **không được coi là trùng**.
- Chức năng **gợi ý sân trống** kiểm tra giờ trong khoảng `06:00–22:00`, bỏ qua sân ngừng hoạt động / đơn đã hủy; ưu tiên giá rẻ rồi đến tên sân.
- Tiền đặt sân = đơn giá theo giờ × số phút sử dụng / 60, làm tròn 2 chữ số thập phân.
- **Báo đã chuyển khoản không có nghĩa là đã thanh toán thành công**. Chỉ `ADMIN` mới có thể ghi nhận thanh toán sau khi đối chiếu. Source hiện tại dùng QR ảnh tĩnh, **không có tích hợp xác minh giao dịch ngân hàng tự động**.

### 3.5. Phân quyền màn hình

| Màn hình | Customer | Admin |
|---|:---:|:---:|
| Tổng quan | ✅ | ✅ |
| Danh sách sân | ✅ | ✅ |
| Đặt sân | ✅ | ✅ |
| Lịch đặt sân | ✅ | ✅ |
| Khám phá / gợi ý sân | ✅ | ✅ |
| Thanh toán | ✅ (báo chuyển khoản) | ✅ (xác nhận chuyển khoản) |
| Quản lý sân | ❌ | ✅ |
| Quản lý khách hàng | ❌ | ✅ |
| Thống kê & báo cáo | ❌ | ✅ |

> **Lưu ý:** `STAFF` có trong ràng buộc bảng `users`, nhưng `MainFrame` hiện chỉ hỗ trợ hai vai trò `ADMIN` và `CUSTOMER`. Không gán `STAFF` nếu chưa triển khai giao diện tương ứng.

## 4. Cấu trúc thư mục

```text
badminton-management/
├── pom.xml                         # Maven, Java 21, thư viện, main class
├── README.md                       # Tài liệu dự án
├── .gitignore
├── database/
│   ├── schema.sql                  # Tạo database và 5 bảng chính
│   ├── seed.sql                    # Thêm sân và khách hàng mẫu
│   └── migration_payment_requests.sql # Bảng báo đã chuyển khoản
├── docs/
│   ├── RUN_GUIDE.md                # Hướng dẫn cài đặt/chạy chi tiết
│   ├── INTEGRATION_GUIDE.md        # Tích hợp giữa các thành viên
│   ├── TEAM_CONTRACT.md            # Quy ước giao tiếp giữa các module
│   ├── DATABASE_CONTRACT.md        # Quy ước dữ liệu
│   └── TASK_ASSIGNMENT.md          # Phân công công việc
└── src/
    ├── main/
    │   ├── java/com/badminton/
    │   │   ├── Main.java           # Khởi động ứng dụng
    │   │   ├── config/             # Kết nối MySQL
    │   │   ├── model/              # User, Customer, Court, Booking, Payment
    │   │   ├── dao/                # Truy vấn/transaction với database
    │   │   ├── service/            # Xử lý nghiệp vụ, phân quyền
    │   │   ├── util/               # Font, mật khẩu, tính tiền, khung giờ
    │   │   ├── ui/                 # Đăng nhập/đăng ký, dialog, nút tùy biến
    │   │   └── view/               # MainFrame và các màn hình chức năng
    │   └── resources/
    │       ├── db.properties.example # Mẫu cấu hình MySQL
    │       ├── fonts/              # Font Fredoka, Lexend
    │       └── images/             # Ảnh sân, mã QR chuyển khoản
    └── test/java/com/badminton/   # Test JUnit
```

**Một số lớp trung tâm:**

| File / lớp | Vai trò |
|---|---|
| `Main.java` | Thiết lập font/Look & Feel và mở màn hình xác thực |
| `ui/AuthFrame.java`, `ui/AuthView.java` | Cửa sổ và điều hướng đăng nhập/đăng ký |
| `view/MainFrame.java` | Giao diện chính, menu và kiểm tra quyền màn hình |
| `service/AuthService.java` | Đăng ký, xác thực tài khoản |
| `service/BookingService.java` + `dao/BookingDAO.java` | Tạo/hủy đơn, ngăn trùng lịch |
| `service/PaymentService.java` + `dao/PaymentRequestDAO.java` | Báo và xác nhận chuyển khoản |
| `service/CourtService.java`, `service/CustomerService.java` | Quản lý sân và khách hàng |
| `service/ScheduleService.java`, `service/SuggestionService.java` | Tra cứu lịch và tìm sân trống |
| `view/StatisticsPanel.java` | Giao diện báo cáo, biểu đồ |
| `config/DatabaseConnection.java` | Đọc `db.properties` trên classpath và mở JDBC connection |

## 5. Cài đặt và chạy dự án

### Bước 1 — Chuẩn bị

Cài đặt:

1. **JDK 21 hoặc mới hơn** (dự án biên dịch theo API Java 21).
2. **Apache Maven** (khuyến nghị 3.9+).
3. **MySQL Server 8+** (MySQL Workbench là công cụ quản lý, không thay thế MySQL Server).
4. Git và IDE như VS Code/IntelliJ IDEA (tùy chọn).

Kiểm tra trong terminal:

```powershell
java -version
javac -version
mvn -version
```

### Bước 2 — Lấy source

```powershell
git clone https://github.com/mynameismuoiii/badminton-management.git
cd badminton-management
```

Nếu dùng bản ZIP, giải nén và mở terminal ngay tại thư mục **chứa `pom.xml`**.

### Bước 3 — Tạo database

Mở MySQL Workbench hoặc MySQL CLI, thực hiện **theo thứ tự**:

1. Chạy `database/schema.sql` (tạo database `badminton_management`, các bảng `users`, `courts`, `customers`, `bookings`, `payments`).
2. Chạy `database/migration_payment_requests.sql` (**bắt buộc cho luồng báo/xác nhận chuyển khoản**).
3. Chạy `database/seed.sql` (tùy chọn, bổ sung dữ liệu sân/khách hàng mẫu).

Ví dụ với MySQL CLI (chạy từ thư mục gốc dự án):

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/migration_payment_requests.sql
mysql -u root -p < database/seed.sql
```

Kiểm tra:

```sql
USE badminton_management;
SHOW TABLES;
SELECT id, name, price_per_hour, active FROM courts;
```

**Chú ý:** Nếu máy đã có dữ liệu, sao lưu trước khi sửa schema; `CREATE TABLE IF NOT EXISTS` không tự cập nhật cấu trúc bảng cũ.

### Bước 4 — Cấu hình kết nối MySQL

Copy file ví dụ:

```powershell
Copy-Item .\src\main\resources\db.properties.example .\src\main\resources\db.properties
```

Mở `src/main/resources/db.properties` và điền thông tin **của MySQL trên máy bạn**:

```properties
db.url=jdbc:mysql://localhost:3306/badminton_management?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

- Thay `YOUR_MYSQL_PASSWORD` bằng mật khẩu thật; nếu dùng user khác `root` thì đổi `db.username`.
- Nếu MySQL không chạy ở máy hiện tại hoặc dùng port khác, sửa `localhost:3306`.
- **Không commit, gửi kèm hoặc đăng công khai `db.properties` có mật khẩu thật.** File này đã có trong `.gitignore`; chỉ nên chia sẻ `db.properties.example`.
- Dùng đường dẫn **`src/main/resources/db.properties`**, không nhầm với thư mục `src/resources/`.

### Bước 5 — Biên dịch và chạy giao diện

```powershell
mvn clean compile
mvn exec:java
```

Nếu dùng **Windows PowerShell** và báo `mvn is not recognized`, gọi Maven bằng đường dẫn thực tế, ví dụ:

```powershell
$MAVEN = "D:\apache-maven-3.10.0\bin\mvn.cmd"  # Sửa theo máy bạn
& $MAVEN clean compile
& $MAVEN exec:java
```

Nếu muốn chạy hai lệnh liên tiếp và dừng khi build lỗi:

```powershell
& $MAVEN clean compile
if ($LASTEXITCODE -eq 0) { & $MAVEN exec:java }
```

Ứng dụng sẽ khởi động từ `com.badminton.Main`, hiện cửa sổ **TH-MINTON** với màn hình **đăng nhập / đăng ký**. Các chức năng dùng dữ liệu thực tế cần MySQL hoạt động.

## 6. Tài khoản và phân quyền ban đầu

- Tài khoản tạo bằng màn hình **Đăng ký** mặc định nhận quyền `CUSTOMER`.
- Source/`seed.sql` hiện **không tạo sẵn tài khoản ADMIN**.
- Để tạo ADMIN cho môi trường chạy thử: đăng ký một tài khoản bình thường, sau đó **chỉ trên database bạn quản lý**, nâng quyền đúng tài khoản:

```sql
USE badminton_management;
UPDATE users
SET role = 'ADMIN'
WHERE username = 'TEN_DANG_NHAP_ADMIN_DA_DANG_KY';
```

**Đăng xuất và đăng nhập lại** để giao diện nhận quyền vừa thay đổi. Chỉ thực hiện thao tác này với tài khoản quản trị được ủy quyền; không cung cấp quyền cập nhật bảng `users` cho khách hàng thường.

## 7. Quy trình sử dụng cơ bản

**Khách hàng:**

1. Đăng ký → đăng nhập.
2. Mở **Danh sách sân** hoặc **Khám phá sân**, chọn sân muốn đặt.
3. Sang **Đặt sân**, chọn ngày và giờ còn trống, xác nhận tạo đơn.
4. Sang **Thanh toán**: xem số tiền → quét QR → chuyển khoản với nội dung đơn → bấm **đã chuyển khoản**.
5. Theo dõi đơn trong trạng thái chờ; sau khi Admin đối chiếu và xác nhận, đơn được ghi nhận thanh toán.

**Quản trị viên:**

1. Đăng nhập bằng tài khoản `ADMIN`.
2. Quản lý sân/khách hàng và theo dõi lịch đặt sân.
3. Kiểm tra các đơn được báo chuyển khoản, **đối chiếu sao kê thực tế** rồi xác nhận.
4. Xem báo cáo, doanh thu và biểu đồ trên màn hình thống kê.

**Trạng thái đặt sân lưu tại bảng `bookings`:** `PENDING` (chờ), `CONFIRMED` (đã xác nhận), `CANCELLED` (đã hủy). Thông tin **đã báo chuyển khoản** lưu tại `payment_requests`, còn giao dịch đã được quản trị viên xác nhận lưu trong `payments`.

## 8. Kiểm thử

Chạy JUnit qua Maven:

```powershell
mvn test
```

Lưu ý về bộ test **đang có trong source**:

- `PriceCalculatorTest` và test so khớp khung giờ kiểm tra logic không cần database.
- Một số test dưới `src/test/java/com/badminton/service/` truy vấn **MySQL thật**, phụ thuộc việc cấu hình database và có dữ liệu kiểm thử tương ứng.
- `ScheduleServiceTest` hiện có kỳ vọng cứng **1 booking vào ngày 28/10/2026**; kỳ vọng này có thể thất bại nếu database không có dữ liệu đúng như test.
- `BookingServiceTest` còn là **placeholder** (`assertTrue(true)`), chưa kiểm tra các nhánh nghiệp vụ chính.
- Do đó, **`mvn test` chưa phải bằng chứng toàn bộ chức năng đã được kiểm thử đầy đủ**. Nên tách unit test độc lập và integration test có dữ liệu chuẩn.

Khi cần **chỉ biên dịch để chạy UI** trong lúc test phụ thuộc dữ liệu chưa sẵn sàng, dùng `mvn clean compile` và `mvn exec:java` như ở bước 5 (không cần bỏ qua test bằng cách sửa source).

## 9. Xử lý lỗi thường gặp

| Lỗi / biểu hiện | Nguyên nhân thường gặp / cách xử lý |
|---|---|
| `mvn is not recognized` | Chưa thêm Maven vào PATH; gọi trực tiếp `mvn.cmd` bằng đường dẫn cài đặt. |
| `release version 21 not supported` | Maven đang dùng JDK thấp hơn 21; kiểm tra `mvn -version` và `JAVA_HOME`. |
| `Missing db.properties` | Sai vị trí file cấu hình; đặt tại `src/main/resources/db.properties` rồi build lại. |
| `Access denied for user` | Sai tài khoản/mật khẩu hoặc quyền MySQL. |
| `Communications link failure` / `Connection refused` | MySQL Server chưa chạy, sai host/port hoặc bị chặn kết nối. |
| `Unknown database badminton_management` | Chưa chạy `database/schema.sql` trên MySQL đang kết nối. |
| `Table '...payment_requests' doesn't exist` | Chưa chạy `database/migration_payment_requests.sql`. |
| `Duplicate entry` | Trùng username, số điện thoại hoặc dữ liệu có unique constraint. |
| Đăng nhập thành công nhưng không có màn hình quản trị | Đăng ký tạo quyền `CUSTOMER`; kiểm tra `users.role`, đăng xuất rồi đăng nhập lại. |
| `mvn test` lỗi do số lượng booking | Test phụ thuộc dữ liệu MySQL (xem mục 8), không mặc định là lỗi biên dịch. |
| Thiếu ảnh nền trang chủ | Kiểm tra resources ảnh; một số code tham chiếu ảnh tùy chọn `images/home-hero.jpg` chưa nằm trong gói source này. |

## 10. Các lưu ý và phần còn cần hoàn thiện

- **Không phải hệ thống thanh toán ngân hàng tự động.** QR trong `src/main/resources/images/transfer-qr.png` là **ảnh cố định**; cần thay bằng QR/tài khoản của đơn vị vận hành khi triển khai thật và vẫn phải xác minh tiền về.
- Một số class cũ như `view/LoginFrame.java`, `view/RoleBasedMainFrame.java` còn trong source; **luồng khởi động thực tế** là `Main` → `ui/AuthFrame` → `view/MainFrame` sau khi đăng nhập.
- `PaymentService.pay(long, String)` đã được đánh dấu **deprecated** và cố ý ném `UnsupportedOperationException` để ngăn bỏ qua bước Admin xác nhận; không sử dụng API cũ này.
- Tài liệu trong thư mục `docs/` có phần mô tả theo giai đoạn phát triển ban đầu; khi khác biệt, hãy đối chiếu **mã nguồn hiện tại**.
- Bộ test tự động cần bổ sung trường hợp đặt trùng giờ, hủy đặt sân, phân quyền, xử lý lỗi database, quy trình thanh toán và test tích hợp có fixture riêng.
- Khi triển khai thực tế, cần đánh giá bổ sung quyền truy cập ở tầng database/service, bảo mật thông tin cấu hình, sao lưu dữ liệu, xử lý nhiều người đặt sân đồng thời và trải nghiệm lỗi kết nối.

## 11. Tài liệu tham khảo trong repository

- [`docs/RUN_GUIDE.md`](docs/RUN_GUIDE.md): hướng dẫn thiết lập môi trường, PowerShell, MySQL và khắc phục lỗi.
- [`docs/INTEGRATION_GUIDE.md`](docs/INTEGRATION_GUIDE.md): hướng dẫn làm việc và tích hợp source nhóm.
- [`docs/TEAM_CONTRACT.md`](docs/TEAM_CONTRACT.md): quy ước giữa các lớp/module.
- [`docs/DATABASE_CONTRACT.md`](docs/DATABASE_CONTRACT.md): bảng dữ liệu, giao diện DAO và quy ước nghiệp vụ.
- [`docs/TASK_ASSIGNMENT.md`](docs/TASK_ASSIGNMENT.md): phân chia công việc Người 1 / Người 2.

---

**Trạng thái xác minh tài liệu:** Đã đọc cấu trúc source, các màn hình, service/DAO, SQL, Maven và test; **mã Java chính biên dịch thành công với `javac --release 21`** tại thời điểm soạn README. Chưa chạy luồng UI có MySQL và chưa chạy trọn bộ test Maven trong môi trường kiểm tra.
