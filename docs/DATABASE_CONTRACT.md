
# TH-MINTON - DATABASE CONTRACT

## 1. Cong nghe

- Database: MySQL 8+
- Java: JDK 21+
- Ket noi: JDBC
- Model: Java record
- ID: long
- Tien: BigDecimal
- Ngay: LocalDate
- Gio: LocalTime

## 2. Bang database

| Bang | Phu trach |
|---|---|
| users | N1 |
| customers | N1 |
| courts | N2 |
| bookings | N1, N2 su dung |
| payments | N1, N2 su dung |

## 3. Quan he

- users.id -> customers.user_id
- customers.id -> bookings.customer_id
- courts.id -> bookings.court_id
- bookings.id -> payments.booking_id

Khach vang lai co customers.user_id = NULL.

Mot tai khoan CUSTOMER co toi da mot ho so customer.

## 4. API danh cho N2

CourtDAO do N2 phat trien:
- findAll()
- findById(long id)
- insert(Court court)
- update(Court court)
- delete(long id)

BookingDAO do N1 phat trien, N2 su dung:
- findAll()
- findByDate(LocalDate date)
- findByCourtAndDate(long courtId, LocalDate date)
- findById(long id)

PaymentDAO do N1 phat trien, N2 su dung:
- findAll()
- findByBookingId(long bookingId)

## 5. Booking status

- PENDING
- CONFIRMED
- CANCELLED

CANCELLED khong chiem khung gio.

Khoang thoi gian su dung [startTime, endTime).

## 6. Quy uoc

- Khong viet SQL trong Swing View.
- Khong thay doi Java record neu chua thong nhat.
- Khong doi ten bang/cot dang su dung.
- Khong thay doi chu ky ham DAO dung chung.
- Khong hardcode mat khau database.
- Password phai duoc hash truoc khi luu.
- Booking moi phai duoc kiem tra trung lich.
- N2 chi goi BookingDAO va PaymentDAO cua N1.
- N1 khong sua CourtDAO cua N2.

## 7. Quy tac merge Git

N1 lam tren feature/n1.
N2 lam tren feature/n2.

File schema.sql la file chung, N1 quan ly
thay doi theo database contract.

Neu can them cot vao bang:
1. Thong bao cho thanh vien con lai.
2. Bo sung vao schema.sql.
3. Bo sung migration khi can.
4. Khong xoa cot dang duoc su dung.
5. Kiem tra Maven compile truoc khi merge.

## 8. Luu y trien khai

- BookingDAO va PaymentDAO la tang truy cap
  du lieu, khong thay the hoan toan Service.
- Service can xu ly phan quyen, tinh tien,
  kiem tra nghiep vu va bao loi cho UI.
- Khi huy booking da thanh toan can quy
  dinh chinh sach hoan tien rieng.
