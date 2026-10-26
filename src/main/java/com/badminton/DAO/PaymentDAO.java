package com.badminton.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.badminton.model.Payment;

public class PaymentDAO {

    // Danh sách thanh toán
    public List<Payment> findAll() throws SQLException {

        // TODO N1:
        // N2 cũng sử dụng dữ liệu này để thống kê.

        throw new UnsupportedOperationException("N1: findAll");
    }

    // Thanh toán của booking
    public Optional<Payment> findByBookingId(long bookingId)
            throws SQLException {

        // TODO N1
        throw new UnsupportedOperationException(
                "N1: findByBookingId"
        );
    }

    // Lưu thanh toán
    public long insert(Payment payment)
            throws SQLException {

        // TODO N1:
        // INSERT INTO payments ...
        // Một booking chỉ được thanh toán một lần.

        throw new UnsupportedOperationException("N1: insert");
    }
}