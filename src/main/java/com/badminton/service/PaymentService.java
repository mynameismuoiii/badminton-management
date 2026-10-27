package com.badminton.service;

import com.badminton.dao.PaymentDAO;
import com.badminton.dao.PaymentRequestDAO;

import com.badminton.model.Booking;
import com.badminton.model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

public class PaymentService {

    private final BookingService bookingService =
        new BookingService();

    private final PaymentDAO payments =
        new PaymentDAO();

    private final PaymentRequestDAO requests =
        new PaymentRequestDAO();

    public BigDecimal amount(Booking booking)
            throws SQLException {

        return bookingService.total(booking);
    }

    public Set<Long> pendingReports()
            throws SQLException {

        return requests.findPendingBookingIds();
    }

    public boolean isPaid(long bookingId)
            throws SQLException {

        return payments.findByBookingId(
            bookingId
        ).isPresent();
    }

    public Set<Long> paidBookingIds()
            throws SQLException {

        return payments.findAll()
            .stream()
            .map(payment -> payment.bookingId())
            .collect(Collectors.toSet());
    }

    public void reportTransfer(
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
                : bookingService.customerId(user);

        requests.reportTransfer(
            bookingId,
            expectedCustomerId
        );
    }

    public long confirmTransfer(
            User user,
            long bookingId
    ) throws SQLException {

        if (
            user == null ||
            !"ADMIN".equalsIgnoreCase(user.role())
        ) {

            throw new SecurityException(
                "Chi quan tri vien duoc xac nhan tien ve."
            );
        }

        return requests.confirm(bookingId);
    }

    /**
     * Khong duoc tu ghi paid khi
     * moi bam nut bao chuyen khoan.
     */
    @Deprecated
    public long pay(
            long bookingId,
            String method
    ) {

        throw new UnsupportedOperationException(
            "Can admin xac nhan: " +
            "dung confirmTransfer(User, bookingId)."
        );
    }
}