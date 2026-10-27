package com.badminton.dao;

import com.badminton.config.DatabaseConnection;
import com.badminton.util.PriceCalculator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * N1: Bao da chuyen khoan KHONG phai
 * xac nhan da nhan tien.
 */
public class PaymentRequestDAO {

    public Set<Long> findPendingBookingIds()
            throws SQLException {

        Set<Long> result = new HashSet<>();

        try (
            Connection c =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                c.prepareStatement(
                    "SELECT booking_id FROM payment_requests"
                );

            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                result.add(rs.getLong(1));
            }
        }

        return result;
    }

    // Khach hang bao da chuyen khoan
    public void reportTransfer(
            long bookingId,
            long expectedCustomerId
    ) throws SQLException {

        try (
            Connection c =
                DatabaseConnection.getConnection()
        ) {

            c.setAutoCommit(false);

            try {

                long actualCustomerId;
                String status;

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "SELECT customer_id, status " +
                            "FROM bookings " +
                            "WHERE id=? FOR UPDATE"
                        )
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                "Khong tim thay don."
                            );
                        }

                        actualCustomerId = rs.getLong(1);
                        status = rs.getString(2);
                    }
                }

                if (
                    expectedCustomerId > 0 &&
                    actualCustomerId != expectedCustomerId
                ) {

                    throw new SecurityException(
                        "Khong co quyen voi don nay."
                    );
                }

                if (!"PENDING".equals(status)) {

                    throw new IllegalArgumentException(
                        "Don khong o trang thai cho thanh toan."
                    );
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "SELECT id FROM payments " +
                            "WHERE booking_id=?"
                        )
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (rs.next()) {
                            throw new IllegalArgumentException(
                                "Don da thanh toan."
                            );
                        }
                    }
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "INSERT INTO payment_requests" +
                            "(booking_id) VALUES (?)"
                        )
                ) {

                    ps.setLong(1, bookingId);
                    ps.executeUpdate();
                }

                c.commit();

            } catch (SQLException | RuntimeException ex) {

                rollback(c, ex);
                throw ex;
            }
        }
    }

    // Huy don chua bao chuyen khoan / chua nhan tien
    public boolean cancelUnpaidBooking(
            long bookingId,
            long expectedCustomerId
    ) throws SQLException {

        try (
            Connection c =
                DatabaseConnection.getConnection()
        ) {

            c.setAutoCommit(false);

            try {

                long owner;
                String status;

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "SELECT customer_id, status " +
                            "FROM bookings " +
                            "WHERE id=? FOR UPDATE"
                        )
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                "Khong tim thay don."
                            );
                        }

                        owner = rs.getLong(1);
                        status = rs.getString(2);
                    }
                }

                if (
                    expectedCustomerId > 0 &&
                    expectedCustomerId != owner
                ) {

                    throw new SecurityException(
                        "Khong co quyen huy don nay."
                    );
                }

                if (!"PENDING".equals(status)) {

                    throw new IllegalArgumentException(
                        "Chi duoc huy don dang cho thanh toan."
                    );
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "SELECT booking_id " +
                            "FROM payment_requests " +
                            "WHERE booking_id=?"
                        )
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (rs.next()) {

                            throw new IllegalArgumentException(
                                "Da bao chuyen khoan, " +
                                "hay lien he admin de xu ly."
                            );
                        }
                    }
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "SELECT id FROM payments " +
                            "WHERE booking_id=?"
                        )
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (rs.next()) {

                            throw new IllegalArgumentException(
                                "Don da thanh toan."
                            );
                        }
                    }
                }

                int changed;

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "UPDATE bookings " +
                            "SET status='CANCELLED' " +
                            "WHERE id=? AND status='PENDING'"
                        )
                ) {

                    ps.setLong(1, bookingId);
                    changed = ps.executeUpdate();
                }

                c.commit();

                return changed > 0;

            } catch (SQLException | RuntimeException ex) {

                rollback(c, ex);
                throw ex;
            }
        }
    }

    /**
     * Admin xac nhan sau khi kiem tra sao ke.
     * Su dung transaction.
     */
    public long confirm(long bookingId)
            throws SQLException {

        try (
            Connection c =
                DatabaseConnection.getConnection()
        ) {

            c.setAutoCommit(false);

            try {

                String status;
                BigDecimal hourlyPrice;

                java.time.LocalTime from;
                java.time.LocalTime to;

                String read =
                    "SELECT b.status, b.start_time, " +
                    "b.end_time, c.price_per_hour " +
                    "FROM bookings b JOIN courts c " +
                    "ON c.id=b.court_id " +
                    "WHERE b.id=? FOR UPDATE";

                try (
                    PreparedStatement ps =
                        c.prepareStatement(read)
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                "Khong tim thay don."
                            );
                        }

                        status = rs.getString(1);
                        from = rs.getTime(2).toLocalTime();
                        to = rs.getTime(3).toLocalTime();
                        hourlyPrice = rs.getBigDecimal(4);
                    }
                }

                if (!"PENDING".equals(status)) {

                    throw new IllegalArgumentException(
                        "Don khong con cho thanh toan."
                    );
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "SELECT booking_id " +
                            "FROM payment_requests " +
                            "WHERE booking_id=? FOR UPDATE"
                        )
                ) {

                    ps.setLong(1, bookingId);

                    try (
                        ResultSet rs = ps.executeQuery()
                    ) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                "Khach chua bao chuyen khoan."
                            );
                        }
                    }
                }

                BigDecimal amount =
                    PriceCalculator.calculate(
                        hourlyPrice,
                        from,
                        to
                    );

                long paymentId;

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "INSERT INTO payments" +
                            "(booking_id, amount, method) " +
                            "VALUES (?, ?, 'BANK_TRANSFER')",
                            Statement.RETURN_GENERATED_KEYS
                        )
                ) {

                    ps.setLong(1, bookingId);
                    ps.setBigDecimal(2, amount);

                    ps.executeUpdate();

                    try (
                        ResultSet keys =
                            ps.getGeneratedKeys()
                    ) {

                        if (!keys.next()) {

                            throw new SQLException(
                                "Khong lay duoc ma thanh toan."
                            );
                        }

                        paymentId = keys.getLong(1);
                    }
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "UPDATE bookings " +
                            "SET status='CONFIRMED' " +
                            "WHERE id=?"
                        )
                ) {

                    ps.setLong(1, bookingId);
                    ps.executeUpdate();
                }

                try (
                    PreparedStatement ps =
                        c.prepareStatement(
                            "DELETE FROM payment_requests " +
                            "WHERE booking_id=?"
                        )
                ) {

                    ps.setLong(1, bookingId);
                    ps.executeUpdate();
                }

                c.commit();

                return paymentId;

            } catch (SQLException | RuntimeException ex) {

                rollback(c, ex);
                throw ex;
            }
        }
    }

    private static void rollback(
            Connection c,
            Exception original
    ) {

        try {
            c.rollback();

        } catch (SQLException ex) {
            original.addSuppressed(ex);
        }
    }
}