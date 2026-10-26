package com.badminton.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.badminton.model.Booking;

public class BookingDAO {

    // Lấy tất cả booking
    public List<Booking> findAll() throws SQLException {

        // TODO N1
        throw new UnsupportedOperationException("N1: findAll");
    }

    // Lấy booking theo ngày
    public List<Booking> findByDate(LocalDate date)
            throws SQLException {

        // TODO N1
        // SELECT * FROM bookings WHERE booking_date = ?

        throw new UnsupportedOperationException("N1: findByDate");
    }

    // Lấy booking theo sân và ngày
    public List<Booking> findByCourtAndDate(
            long courtId,
            LocalDate date
    ) throws SQLException {

        // TODO N1:
        // SELECT * FROM bookings
        // WHERE court_id = ? AND booking_date = ?

        throw new UnsupportedOperationException(
                "N1: findByCourtAndDate"
        );
    }

    // Tìm booking theo ID
    public Optional<Booking> findById(long id)
            throws SQLException {

        // TODO N1
        throw new UnsupportedOperationException("N1: findById");
    }

    // Thêm booking
    public long insert(Booking booking)
            throws SQLException {

        // TODO N1:
        // INSERT INTO bookings ...
        // Trả về ID booking vừa tạo.

        throw new UnsupportedOperationException("N1: insert");
    }

    // Cập nhật booking
    public boolean update(Booking booking)
            throws SQLException {

        // TODO N1
        throw new UnsupportedOperationException("N1: update");
    }

    // Hủy booking
    public boolean cancel(long id)
            throws SQLException {

        // TODO N1:
        // UPDATE bookings
        // SET status = 'CANCELLED'
        // WHERE id = ?

        throw new UnsupportedOperationException("N1: cancel");
    }
}