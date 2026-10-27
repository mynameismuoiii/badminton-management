# TH-MINTON — HƯỚNG DẪN CÀI ĐẶT VÀ CHẠY DỰ ÁN

> Dành cho cả Người 1 và Người 2. Hai máy **không cần cùng ổ đĩa, thư mục cài Java/Maven, hay mật khẩu MySQL**. Chỉ cần dùng cùng phiên bản source và **cùng cấu trúc database**.

## 1. Tổng quan

- Ứng dụng: **Java Swing Desktop** (không phải website).
- Mã nguồn: `https://github.com/mynameismuoiii/badminton-management`
- Điểm chạy: `com.badminton.Main` (đã khai báo trong `pom.xml`).
- Java: **JDK 21 trở lên** (trong `pom.xml`: `maven.compiler.release=21`).
- Maven: **3.9+** (đề xuất).
- CSDL: **MySQL Server** trên máy riêng, cổng mặc định `3306`.
- MySQL Workbench: công cụ thao tác SQL; **không bắt buộc phải mở khi chạy ứng dụng**, nhưng MySQL Server phải đang chạy.
- Thư viện JDBC đã có trong Maven: `com.mysql:mysql-connector-j`.

### NHỮNG GIÁ TRỊ MỖI NGƯỜI PHẢI TỰ THAY

| Giá trị | Ví dụ máy Hương | Thảo phải làm gì? |
|---|---|---|
| Thư mục dự án | `D:\badminton-management` | Thay bằng nơi clone source trên máy mình. |
| Thư mục Maven | `D:\apache-maven-3.10.0` | Thay bằng thư mục Maven **thực tế** của mình; hoặc thêm Maven `bin` vào PATH để dùng `mvn`. |
| Java/JDK | Có thể là JDK 21, 26... | Cài JDK 21+, cấu hình `JAVA_HOME`/PATH nếu chưa nhận `java`. |
| MySQL hostname | `localhost` | Nếu MySQL chạy trên chính máy mình: **giữ `localhost`**. |
| MySQL port | `3306` | Nếu server ở port khác, sửa **port** trong cấu hình JDBC. |
| MySQL username | `root` (chạy thử) | Điền tài khoản MySQL trên máy mình. |
| MySQL password | **Không chia sẻ** | Điền mật khẩu MySQL riêng, không dùng mật khẩu của Người 1. |
| Database name | `badminton_management` | **Giữ tên này** để trùng SQL và cấu hình dự án (trừ khi nhóm thống nhất đổi). |
| Git branch | `feature/n1`, `feature/n2` | Chọn branch đúng nhiệm vụ; không sửa chung trực tiếp trên `main`. |

**Không copy nguyên đường dẫn `D:\...` từ máy Người 1 sang máy Người 2. Không commit `db.properties` lên GitHub.**

## 2. Chuẩn bị công cụ (Windows / PowerShell)

Cài Git, JDK 21+, Apache Maven, MySQL Server và VS Code (khuyến nghị extension **Extension Pack for Java**).

Mở **Terminal → New Terminal** trong VS Code, kiểm tra:

```powershell
java -version
javac -version
mvn -version
git --version
```

- Nếu `mvn` báo **not recognized**, Maven chưa ở `PATH`. Có thể gọi `mvn.cmd` bằng đường dẫn tuyệt đối (xem mục 5).
- Nếu `java`/`javac` không nhận, kiểm tra `JAVA_HOME` trỏ tới **thư mục JDK** và biến `Path` có `%JAVA_HOME%\bin`. Đóng và mở lại VS Code sau khi sửa PATH.
- Lệnh `mvn -version` cũng cho biết Maven đang dùng phiên bản Java nào.

## 3. Lấy đúng source từ GitHub

**Cách A — Người 2 clone lần đầu:**

```powershell
# THAY duong dan nay bang thu muc muon luu project
Set-Location "C:\Users\YourName\Documents"

git clone https://github.com/mynameismuoiii/badminton-management.git
Set-Location .\badminton-management
code .
```

**Cách B — Đã clone và muốn cập nhật code từ main:**

```powershell
# Chay ben trong thu muc project
 git status
 git switch main
 git pull origin main
```

**Quan trọng:** Trước khi Người 2 clone/pull, Người 1 phải **push phiên bản đã sửa đăng ký/đăng nhập** và merge (hoặc tạo Pull Request rồi merge) vào `main`. Nếu code mới chỉ nằm trên `feature/n1`, clone `main` sẽ chưa có chức năng mới.

Người 2 tạo nhánh làm việc **chỉ một lần**, sau khi cập nhật `main`:

```powershell
git switch -c feature/n2
```

Nếu nhánh `feature/n2` đã tồn tại ở máy mình, dùng `git switch feature/n2` thay vì tạo lại.

## 4. Tạo database MySQL trên từng máy

Hai người đều tạo **database riêng trên máy mình** và dùng chung `database/schema.sql`; dữ liệu ở hai máy **không tự đồng bộ** khi `git pull`.

1. Cài và khởi động **MySQL Server**; ghi lại **host, port, username, password** của máy mình.
2. Mở **MySQL Workbench**, tạo connection kết nối đến server của **máy mình**, chọn **Test Connection**.
3. Chọn **File → Open SQL Script...**, mở file **`database/schema.sql` trong thư mục dự án vừa clone**.
4. Chạy toàn bộ script (**Execute All**). Nó tạo database `badminton_management` và các bảng `users`, `customers`, `courts`, `bookings`, `payments`.
5. **Tuỳ chọn:** Chạy `database/seed.sql` để có sân và khách hàng mẫu. File `seed.sql` chỉ cần cho demo/test.
6. Kiểm tra bằng SQL:

```sql
USE badminton_management;
SHOW TABLES;
SELECT * FROM courts;
```

> **Không chạy lại SQL xóa bảng, không xóa database có dữ liệu.** Nếu database đã tồn tại với schema cũ, hãy đối chiếu và chạy migration của nhóm thay vì tưởng `CREATE TABLE IF NOT EXISTS` sẽ tự bổ sung cột mới.

## 5. Tạo cấu hình `db.properties` RIÊNG trên mỗi máy

Trong **thư mục dự án** (thư mục chứa `pom.xml`), mở Terminal PowerShell:

```powershell
# Tao file cau hinh tu file mau (chi can lam mot lan)
Copy-Item .\src\main\resources\db.properties.example .\src\main\resources\db.properties
```

Mở **`src/main/resources/db.properties`**, sửa thành:

```properties
db.url=jdbc:mysql://localhost:3306/badminton_management?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
db.username=root
db.password=THAY_BANG_MAT_KHAU_MYSQL_CUA_CHINH_BAN
```

**Cần sửa:**

- `localhost`: chỉ đổi nếu MySQL Server không nằm trên máy đang chạy Java.
- `3306`: đổi nếu MySQL Server máy bạn dùng port khác (ví dụ `3307`).
- `db.username`: điền tài khoản MySQL trên **máy bạn**, có thể khác `root`.
- `db.password`: **bắt buộc thay** bằng mật khẩu trên **máy bạn** (không thêm dấu ngoặc kép; ký tự `@` dùng bình thường).
- `badminton_management`: giữ nguyên nếu đã chạy `database/schema.sql` chuẩn.

**Không đổi tên file thành `db.properties.txt`**. Không đặt nhầm vào `src/resources` hoặc `src/main/java`. `DatabaseConnection.java` đọc chính xác file `db.properties` từ **classpath**.

Kiểm tra file tồn tại:

```powershell
Test-Path .\src\main\resources\db.properties
```

Kết quả phải là `True`.

`.gitignore` của dự án đã có `src/main/resources/db.properties`. Tuyệt đối không chia sẻ mật khẩu MySQL trong README, commit, chat hoặc ảnh chụp file cấu hình.

## 6. Biên dịch và chạy dự án

Mở Terminal trong **đúng thư mục chứa `pom.xml`**.

**Cách A — Maven đã ở PATH (dùng được `mvn -version`):**

```powershell
mvn clean compile
mvn exec:java
```

**Cách B — Maven chưa ở PATH:**

```powershell
# THAY duong dan nay bang noi ban DA GIAI NEN Maven
$MAVEN = "D:\apache-maven-3.10.0\bin\mvn.cmd"

# Kiem tra duong dan Maven; phai tra ve True
Test-Path $MAVEN

# Build va chay
& $MAVEN clean compile
& $MAVEN exec:java
```

Người 2 **không bắt buộc** phải cài Maven ở ổ D:; ví dụ nếu Maven nằm ở `C:\Tools\apache-maven-3.10.0\bin\mvn.cmd`, chỉ cần thay biến `$MAVEN`.

Sau `clean compile`, kiểm tra file cấu hình đã vào `target/classes`:

```powershell
Test-Path .\target\classes\db.properties
```

Kết quả phải là `True`. Nếu `False`, kiểm tra lại mục 5 và chạy lại `clean compile`.

Tuỳ chọn chạy test:

```powershell
mvn test
```

hoặc (khi dùng `$MAVEN`):

```powershell
& $MAVEN test
```

Màn hình **TH-MINTON Java Swing** sẽ mở lên. Đây là ứng dụng **Desktop**, không phải một trang web chạy trong trình duyệt.

## 7. Test đăng ký / đăng nhập thật

1. Trong TH-MINTON, đăng ký một tài khoản **test mới** (username và số điện thoại chưa tồn tại trên máy mình).
2. Nếu có thông báo thành công, mở MySQL Workbench và chạy:

```sql
USE badminton_management;
SELECT u.id, u.username, u.full_name, u.phone,
       c.id AS customer_id
FROM users u
LEFT JOIN customers c ON c.user_id = u.id
ORDER BY u.id DESC;
```

3. Tài khoản vừa tạo phải xuất hiện ở `users`; nếu chức năng liên kết đã tích hợp, `customer_id` phải khác `NULL`.
4. Thử đăng nhập bằng tài khoản vừa đăng ký; thử thêm mật khẩu sai và username trùng để kiểm tra thông báo.

**Lưu ý:** Chức năng đăng ký/đăng nhập thật chỉ có khi branch đã chứa code `AuthService`, `UserDAO`, `LoginPanel`, `RegisterPanel`, `PasswordUtil` mới nhất. Bản starter/nhánh cũ có thể vẫn còn logic demo hoặc TODO.

## 8. Xử lý lỗi thường gặp

| Hiện tượng | Cần kiểm tra |
|---|---|
| `mvn is not recognized` | Maven `bin` chưa ở PATH; dùng cách B với đường dẫn riêng. |
| `java is not recognized` / sai JDK | Cài JDK 21+, cấu hình `JAVA_HOME` và PATH. |
| `Missing db.properties` | File phải nằm `src/main/resources/db.properties`; chạy `clean compile`, kiểm tra `target/classes/db.properties`. |
| `Access denied for user` | Sai tên user/mật khẩu/host hoặc thiếu quyền trên MySQL của **máy đó**. |
| `Communications link failure` | MySQL Server chưa chạy, hoặc sai host/port. |
| `Unknown database badminton_management` | Chưa chạy `database/schema.sql` trên server đang kết nối. |
| `Table ... doesn't exist` | Chưa chạy hết schema, hoặc đang kết nối nhầm database. |
| `Duplicate entry` | Username/số điện thoại đã tồn tại; dùng dữ liệu đăng ký khác. |
| Đăng ký vẫn báo `demo` | Đang chạy source chưa cập nhật logic xác thực. |
| `BUILD FAILURE` | Xem lỗi đầu tiên ở Terminal, kiểm tra tên class/file (ví dụ `CourtDAO.java` và `public class CourtDAO`). |

Kiểm tra dịch vụ MySQL trên Windows:

```powershell
Get-Service -Name "*mysql*"
```

Nếu không có dịch vụ, cần cấu hình hoặc cài MySQL Server; **Workbench không phải Server**.

## 9. Git và phân chia công việc

- Người 1 (`feature/n1`): xác thực, khách hàng, đặt sân/thanh toán theo phân công.
- Người 2 (`feature/n2`): quản lý sân, lịch sân, gợi ý, thống kê theo phân công.
- Các file dùng chung gồm `pom.xml`, `database/schema.sql`, model và DAO contract: **thống nhất trước khi thay đổi**, tránh merge xung đột.
- Code Java và file SQL có thể push/merge bằng Git; **dữ liệu trong MySQL cục bộ không tự chuyển sang máy người khác**.

Khi làm xong thay đổi, kiểm tra và push nhánh đang đứng:

```powershell
git status
git branch --show-current
git add docs/RUN_GUIDE.md
git commit -m "docs: add environment-specific run guide"
git push -u origin feature/n1
```

Lệnh push ví dụ trên **chỉ đúng khi đang ở `feature/n1`**. Người 2 đổi thành `feature/n2` khi push nhánh của mình. Sau đó mở Pull Request và merge vào `main` khi build/test đạt.

## 10. Checklist trước khi báo đã chạy được

- [ ] `java -version`, `javac -version`, Maven, Git hoạt động.
- [ ] Đã clone/pull **đúng branch có source mới nhất**.
- [ ] `schema.sql` tạo đủ 5 bảng MySQL trên **máy mình**.
- [ ] `db.properties` nằm ở `src/main/resources`, mật khẩu đã thay.
- [ ] `Test-Path .\target\classes\db.properties` là `True` sau compile.
- [ ] `mvn clean compile` (hoặc `& $MAVEN clean compile`) báo `BUILD SUCCESS`.
- [ ] `mvn exec:java` (hoặc `& $MAVEN exec:java`) mở được TH-MINTON.
- [ ] Đăng ký lưu được `users` và `customers`; đăng nhập được bằng tài khoản vừa tạo.
- [ ] `git status` không liệt kê file chứa mật khẩu để chuẩn bị commit.
