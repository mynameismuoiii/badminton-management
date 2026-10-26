
# Quy uoc chung - Badminton Management

## 1. Quy uoc ten

- Java package: com.badminton
- Class: PascalCase
- Method: camelCase
- Variable: camelCase
- SQL table/column: snake_case

## 2. Kieu du lieu

- ID: long
- Tien: BigDecimal
- Ngay gio: LocalDateTime
- Trang thai: Enum hoac String theo quy uoc

## 3. Model

User:
- id
- username
- passwordHash
- role

Court:
- id
- name
- pricePerHour
- active

Customer:
- id
- fullName
- phone
- email

Booking:
- id
- courtId
- customerId
- startTime
- endTime
- totalPrice
- status

Payment:
- id
- bookingId
- amount
- paidAt
- status

## 4. Trang thai Booking

PENDING
CONFIRMED
CANCELLED
COMPLETED

## 5. Trang thai Payment

UNPAID
PAID
REFUNDED

## 6. Phuong thuc kiem tra san trong

boolean isCourtAvailable(
    long courtId,
    LocalDateTime start,
    LocalDateTime end
);

## 7. Quy tac trung lich

Hai lich trung nhau khi:

newStart < existingEnd
AND
newEnd > existingStart

Khong tinh cac lich da huy.

Khong cho phep start >= end.

Khi sua lich, khong so sanh lich do voi chinh no.

## 8. Luong dat san

BookingPanel
    -> BookingService
    -> ScheduleService
    -> BookingDAO
    -> MySQL

Khi luu phai kiem tra lai lich trong transaction,
dong thoi co co che chong dat trung.

## 9. Git Branch

Nguoi 1:
feature/management

Nguoi 2:
feature/scheduling-payment

Khong commit mat khau database.
