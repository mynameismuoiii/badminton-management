package com.badminton.dao;

import com.badminton.config.DatabaseConnection;
import com.badminton.util.PriceCalculator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Read-only reports for ADMIN customer management. No schema migration is required. */
public final class CustomerManagementDAO {

    public record Summary(long id, String fullName, String phone,
                          String email, boolean hasAccount,
                          long bookingCount, BigDecimal paidTotal) { }

    public record BookingHistory(long id, String courtName,
                                 LocalDate date, LocalTime start, LocalTime end,
                                 String status, BigDecimal currentHourlyPrice,
                                 BigDecimal estimateAtCurrentPrice,
                                 BigDecimal actuallyPaid, String paymentMethod,
                                 LocalDateTime paidAt) { }

    /**
     * One row/customer. Paid total is taken ONLY from recorded payments;
     * a booking without a payment contributes zero to the paid total.
     */
    public List<Summary> findSummaries() throws SQLException {
        String sql = """
                SELECT c.id, c.full_name, c.phone, c.email, c.user_id,
                       COUNT(b.id) AS booking_count,
                       COALESCE(SUM(p.amount), 0) AS paid_total
                FROM customers c
                LEFT JOIN bookings b ON b.customer_id = c.id
                LEFT JOIN payments p ON p.booking_id = b.id
                GROUP BY c.id, c.full_name, c.phone, c.email, c.user_id
                ORDER BY c.id DESC
                """;
        List<Summary> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Summary(
                        rs.getLong("id"), rs.getString("full_name"),
                        rs.getString("phone"), rs.getString("email"),
                        rs.getObject("user_id") != null,
                        rs.getLong("booking_count"),
                        rs.getBigDecimal("paid_total")));
            }
        }
        return rows;
    }

    /**
     * Current hourly price is a REFERENCE PRICE, not a historical price lock.
     * Actual paid amount comes from payments.amount, never an estimate.
     */
    public List<BookingHistory> findBookings(long customerId) throws SQLException {
        String sql = """
                SELECT b.id, b.booking_date, b.start_time, b.end_time, b.status,
                       ct.name AS court_name, ct.price_per_hour,
                       p.amount AS paid_amount, p.method AS payment_method, p.paid_at
                FROM bookings b
                JOIN courts ct ON ct.id = b.court_id
                LEFT JOIN payments p ON p.booking_id = b.id
                WHERE b.customer_id = ?
                ORDER BY b.booking_date DESC, b.start_time DESC, b.id DESC
                """;
        List<BookingHistory> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time fromSql = rs.getTime("start_time");
                    Time toSql = rs.getTime("end_time");
                    LocalTime from = fromSql.toLocalTime();
                    LocalTime to = toSql.toLocalTime();
                    BigDecimal hourlyPrice = rs.getBigDecimal("price_per_hour");
                    BigDecimal estimated = PriceCalculator.calculate(hourlyPrice, from, to);
                    Timestamp paidAt = rs.getTimestamp("paid_at");
                    rows.add(new BookingHistory(
                            rs.getLong("id"), rs.getString("court_name"),
                            rs.getDate("booking_date").toLocalDate(), from, to,
                            rs.getString("status"), hourlyPrice, estimated,
                            rs.getBigDecimal("paid_amount"),
                            rs.getString("payment_method"),
                            paidAt == null ? null : paidAt.toLocalDateTime()));
                }
            }
        }
        return rows;
    }
}
