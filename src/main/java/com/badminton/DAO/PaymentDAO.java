
package com.badminton.dao;

import com.badminton.config.DatabaseConnection;
import com.badminton.model.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO {

    public List<Payment> findAll()
            throws SQLException {

        List<Payment> result = new ArrayList<>();

        String sql =
            "SELECT id, booking_id, amount, " +
            "method, paid_at FROM payments " +
            "ORDER BY paid_at DESC, id DESC";

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }

        return result;
    }

    public Optional<Payment> findByBookingId(
            long bookingId
    ) throws SQLException {

        String sql =
            "SELECT id, booking_id, amount, " +
            "method, paid_at FROM payments " +
            "WHERE booking_id = ?";

        try (
            Connection conn =
                DatabaseConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {
            ps.setLong(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(map(rs));
                }

                return Optional.empty();
            }
        }
    }

    public long insert(Payment payment)
            throws SQLException {

        if (payment == null ||
            payment.amount() == null ||
            payment.amount().compareTo(BigDecimal.ZERO) <= 0 ||
            payment.method() == null ||
            payment.method().isBlank()) {

            throw new IllegalArgumentException(
                "Thanh toan khong hop le."
            );
        }

        String check =
            "SELECT status FROM bookings " +
            "WHERE id = ? FOR UPDATE";

        String insert =
            "INSERT INTO payments " +
            "(booking_id, amount, method, paid_at) " +
            "VALUES (?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))";

        try (Connection conn =
                 DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Khong thanh toan booking da huy
                try (PreparedStatement ps =
                         conn.prepareStatement(check)) {

                    ps.setLong(1, payment.bookingId());

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                "Khong tim thay booking."
                            );
                        }

                        if ("CANCELLED".equals(
                                rs.getString("status"))) {

                            throw new SQLException(
                                "Booking da huy, " +
                                "khong the thanh toan."
                            );
                        }
                    }
                }

                long id;

                try (PreparedStatement ps =
                         conn.prepareStatement(
                             insert,
                             Statement.RETURN_GENERATED_KEYS
                         )) {

                    ps.setLong(1, payment.bookingId());
                    ps.setBigDecimal(2, payment.amount());
                    ps.setString(3, payment.method());

                    if (payment.paidAt() == null) {
                        ps.setNull(4, Types.TIMESTAMP);
                    } else {
                        ps.setTimestamp(
                            4,
                            Timestamp.valueOf(
                                payment.paidAt()
                            )
                        );
                    }

                    ps.executeUpdate();

                    try (ResultSet keys =
                             ps.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                "Khong lay duoc payment ID"
                            );
                        }

                        id = keys.getLong(1);
                    }
                }

                conn.commit();
                return id;

            } catch (SQLException | RuntimeException ex) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }

                throw ex;
            }
        }
    }

    private static Payment map(ResultSet rs)
            throws SQLException {

        Timestamp ts = rs.getTimestamp("paid_at");

        return new Payment(
            rs.getLong("id"),
            rs.getLong("booking_id"),
            rs.getBigDecimal("amount"),
            rs.getString("method"),
            ts == null ? null : ts.toLocalDateTime()
        );
    }
}
