package com.badminton.service;

import com.badminton.dao.BookingDAO;
import com.badminton.dao.CourtDAO;
import com.badminton.dao.CustomerDAO;
import com.badminton.dao.PaymentRequestDAO;
import com.badminton.dao.UserDAO;

import com.badminton.model.Booking;
import com.badminton.model.Court;
import com.badminton.model.User;

import com.badminton.util.PriceCalculator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService {

    private final BookingDAO bookings =
        new BookingDAO();

    private final CourtDAO courts =
        new CourtDAO();

    private final CustomerDAO customers =
        new CustomerDAO();

    private final UserDAO users =
        new UserDAO();

    private final PaymentRequestDAO paymentRequests =
        new PaymentRequestDAO();

    public long customerId(User user)
            throws SQLException {

        if (
            user == null ||
            !"CUSTOMER".equalsIgnoreCase(user.role())
        ) {

            throw new IllegalArgumentException(
                "Yeu cau tai khoan khach hang."
            );
        }

        return users.findCustomerIdByUserId(
            user.id()
        ).orElseThrow(
            () -> new IllegalArgumentException(
                "Tai khoan chua co ho so khach hang."
            )
        );
    }

    public List<Booking> listVisible(User user)
            throws SQLException {

        if (user == null) {

            throw new SecurityException(
                "Vui long dang nhap."
            );
        }

        if ("ADMIN".equalsIgnoreCase(user.role())) {

            return bookings.findAll();
        }

        return bookings.findByCustomerId(
            customerId(user)
        );
    }

    public BigDecimal total(Booking booking)
            throws SQLException {

        Court court = courts.findById(
            booking.courtId()
        ).orElseThrow(
            () -> new IllegalArgumentException(
                "Khong tim thay san."
            )
        );

        return PriceCalculator.calculate(
            court.pricePerHour(),
            booking.startTime(),
            booking.endTime()
        );
    }

    public long createBooking(
            User user,
            Booking booking
    ) throws SQLException {

        if (user == null) {

            throw new SecurityException(
                "Vui long dang nhap."
            );
        }

        boolean admin =
            "ADMIN".equalsIgnoreCase(user.role());

        if (
            booking == null ||
            booking.bookingDate() == null ||
            booking.startTime() == null ||
            booking.endTime() == null ||
            !booking.startTime().isBefore(
                booking.endTime()
            )
        ) {

            throw new IllegalArgumentException(
                "Ngay hoac gio khong hop le."
            );
        }

        if (
            booking.bookingDate()
                .atTime(booking.startTime())
                .isBefore(LocalDateTime.now())
        ) {

            throw new IllegalArgumentException(
                "Khong duoc dat san o thoi diem da qua."
            );
        }

        if (
            !admin &&
            booking.customerId() != customerId(user)
        ) {

            throw new SecurityException(
                "Khong duoc dat san cho khach hang khac."
            );
        }

        if (
            customers.findById(
                booking.customerId()
            ).isEmpty()
        ) {

            throw new IllegalArgumentException(
                "Khong tim thay khach hang."
            );
        }

        Court court = courts.findById(
            booking.courtId()
        ).orElseThrow(
            () -> new IllegalArgumentException(
                "Khong tim thay san."
            )
        );

        if (!court.active()) {

            throw new IllegalArgumentException(
                "San da ngung hoat dong."
            );
        }

        if (
            PriceCalculator.calculate(
                court.pricePerHour(),
                booking.startTime(),
                booking.endTime()
            ).signum() <= 0
        ) {

            throw new IllegalArgumentException(
                "So tien phai lon hon 0."
            );
        }

        Booking newBooking = new Booking(
            0,
            booking.courtId(),
            booking.customerId(),
            booking.bookingDate(),
            booking.startTime(),
            booking.endTime(),
            "PENDING"
        );

        // BookingDAO.insert khoa court row
        // de tranh trung gio.
        return bookings.insert(newBooking);
    }

    public boolean cancelBooking(
            User user,
            long bookingId
    ) throws SQLException {

        if (user == null) {

            throw new SecurityException(
                "Vui long dang nhap."
            );
        }

        long expectedCustomerId =
            "ADMIN".equalsIgnoreCase(user.role())
                ? -1
                : customerId(user);

        return paymentRequests.cancelUnpaidBooking(
            bookingId,
            expectedCustomerId
        );
    }

    // Giu API cu de code khac co the su dung.
    // Khong co user context nen khong cho tao.
    public long createBooking(Booking booking) {

        throw new SecurityException(
            "Hay dung createBooking(User, Booking) " +
            "de kiem tra quyen."
        );
    }
}