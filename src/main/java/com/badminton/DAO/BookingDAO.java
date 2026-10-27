package com.badminton.dao;

import com.badminton.config.DatabaseConnection;
import com.badminton.model.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO {

    private static final String COLS =
        "id, court_id, customer_id, booking_date, " +
        "start_time, end_time, status";

    public List<Booking> findAll() throws SQLException {
        return list(
            "SELECT " + COLS +
            " FROM bookings " +
            "ORDER BY booking_date, start_time, id",
            null, null
        );
    }

    // N2 su dung ham nay de hien thi lich san
    public List<Booking> findByDate(LocalDate date)
            throws SQLException {

        return list(
            "SELECT " + COLS +
            " FROM bookings WHERE booking_date = ? " +
            "ORDER BY start_time, court_id",
            date, null
        );
    }

    // N2 su dung de kiem tra khung gio tung san
    public List<Booking> findByCourtAndDate(
            long courtId,
            LocalDate date
    ) throws SQLException {

        return list(
            "SELECT " + COLS +
            " FROM bookings WHERE court_id = ? " +
            "AND booking_date = ? ORDER BY start_time",
            date, courtId
        );
    }

    // N1: chi tra ve don cua khach dang dang nhap.
    public List<Booking> findByCustomerId(long customerId)
            throws SQLException {

        List<Booking> result = new ArrayList<>();

        String sql =
            "SELECT " + COLS +
            " FROM bookings WHERE customer_id = ? " +
            "ORDER BY booking_date DESC, " +
            "start_time DESC, id DESC";

        try (
            Connection conn =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    private List<Booking> list(
            String sql,
            LocalDate date,
            Long courtId
    ) throws SQLException {

        List<Booking> result = new ArrayList<>();

        try (
            Connection conn =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {

            if (courtId != null) {

                ps.setLong(1, courtId);
                ps.setDate(2, Date.valueOf(date));

            } else if (date != null) {

                ps.setDate(1, Date.valueOf(date));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public Optional<Booking> findById(long id)
            throws SQLException {

        String sql =
            "SELECT " + COLS +
            " FROM bookings WHERE id = ?";

        try (
            Connection conn =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(map(rs));
                }

                return Optional.empty();
            }
        }
    }

    // Tao don dat san
    public long insert(Booking booking)
            throws SQLException {

        validate(booking);

        String sql =
            "INSERT INTO bookings " +
            "(court_id, customer_id, booking_date, " +
            "start_time, end_time, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conn =
                DatabaseConnection.getConnection()
        ) {

            conn.setAutoCommit(false);

            try {

                // Khoa san trong transaction
                lockCourt(conn, booking.courtId());

                if (
                    !"CANCELLED".equals(booking.status())
                    && overlaps(conn, booking, 0)
                ) {

                    throw new SQLException(
                        "Khung gio nay da co nguoi dat.",
                        "23000"
                    );
                }

                long id;

                try (
                    PreparedStatement ps =
                        conn.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                        )
                ) {

                    fill(ps, booking);
                    ps.executeUpdate();

                    try (
                        ResultSet keys =
                            ps.getGeneratedKeys()
                    ) {

                        if (!keys.next()) {
                            throw new SQLException(
                                "Khong lay duoc booking ID"
                            );
                        }

                        id = keys.getLong(1);
                    }
                }

                conn.commit();

                return id;

            } catch (SQLException | RuntimeException ex) {

                rollback(conn, ex);
                throw ex;
            }
        }
    }

    // Cap nhat don dat san
    public boolean update(Booking booking)
            throws SQLException {

        validate(booking);

        String sql =
            "UPDATE bookings SET " +
            "court_id = ?, customer_id = ?, " +
            "booking_date = ?, start_time = ?, " +
            "end_time = ?, status = ? WHERE id = ?";

        try (
            Connection conn =
                DatabaseConnection.getConnection()
        ) {

            conn.setAutoCommit(false);

            try {

                lockCourt(conn, booking.courtId());

                if (
                    !"CANCELLED".equals(booking.status())
                    && overlaps(
                        conn, booking, booking.id()
                    )
                ) {

                    throw new SQLException(
                        "Khung gio nay da co nguoi dat.",
                        "23000"
                    );
                }

                int count;

                try (
                    PreparedStatement ps =
                        conn.prepareStatement(sql)
                ) {

                    fill(ps, booking);
                    ps.setLong(7, booking.id());

                    count = ps.executeUpdate();
                }

                conn.commit();

                return count > 0;

            } catch (SQLException | RuntimeException ex) {

                rollback(conn, ex);
                throw ex;
            }
        }
    }

    public boolean cancel(long id)
            throws SQLException {

        String sql =
            "UPDATE bookings " +
            "SET status = 'CANCELLED' " +
            "WHERE id = ? AND status <> 'CANCELLED'";

        try (
            Connection conn =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    // Lock san de tranh 2 yeu cau dat cung gio
    private static void lockCourt(
            Connection conn,
            long courtId
    ) throws SQLException {

        String sql =
            "SELECT id FROM courts " +
            "WHERE id = ? AND active = TRUE FOR UPDATE";

        try (
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {

            ps.setLong(1, courtId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                        "San khong ton tai " +
                        "hoac da ngung hoat dong."
                    );
                }
            }
        }
    }

    // Hai khoang thoi gian giao nhau neu:
    // startA < endB va endA > startB
    private static boolean overlaps(
            Connection conn,
            Booking b,
            long excludedId
    ) throws SQLException {

        String sql =
            "SELECT id FROM bookings " +
            "WHERE court_id = ? " +
            "AND booking_date = ? " +
            "AND status <> 'CANCELLED' " +
            "AND start_time < ? " +
            "AND end_time > ? " +
            "AND id <> ? LIMIT 1";

        try (
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {

            ps.setLong(1, b.courtId());
            ps.setDate(
                2,
                Date.valueOf(b.bookingDate())
            );
            ps.setTime(
                3,
                Time.valueOf(b.endTime())
            );
            ps.setTime(
                4,
                Time.valueOf(b.startTime())
            );
            ps.setLong(5, excludedId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void validate(Booking b) {

        if (
            b == null ||
            b.bookingDate() == null ||
            b.startTime() == null ||
            b.endTime() == null ||
            !b.startTime().isBefore(b.endTime())
        ) {

            throw new IllegalArgumentException(
                "Ngay/gio dat san khong hop le."
            );
        }

        if (
            !"PENDING".equals(b.status()) &&
            !"CONFIRMED".equals(b.status()) &&
            !"CANCELLED".equals(b.status())
        ) {

            throw new IllegalArgumentException(
                "Trang thai booking khong hop le."
            );
        }
    }

    private static void fill(
            PreparedStatement ps,
            Booking b
    ) throws SQLException {

        ps.setLong(1, b.courtId());
        ps.setLong(2, b.customerId());
        ps.setDate(
            3,
            Date.valueOf(b.bookingDate())
        );
        ps.setTime(
            4,
            Time.valueOf(b.startTime())
        );
        ps.setTime(
            5,
            Time.valueOf(b.endTime())
        );
        ps.setString(6, b.status());
    }

    private static Booking map(ResultSet rs)
            throws SQLException {

        return new Booking(
            rs.getLong("id"),
            rs.getLong("court_id"),
            rs.getLong("customer_id"),
            rs.getDate("booking_date")
                .toLocalDate(),
            rs.getTime("start_time")
                .toLocalTime(),
            rs.getTime("end_time")
                .toLocalTime(),
            rs.getString("status")
        );
    }

    private static void rollback(
            Connection conn,
            Exception original
    ) {

        try {

            conn.rollback();

        } catch (SQLException ex) {

            original.addSuppressed(ex);
        }
    }
}