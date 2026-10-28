package com.badminton.service;

import com.badminton.dao.BookingDAO;
import com.badminton.model.Booking;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ScheduleService {

    private final BookingDAO bookingDAO;

    public ScheduleService() {
        this.bookingDAO = new BookingDAO();
    }

    // Lấy toàn bộ lịch đặt sân của một ngày
    public List<Booking> schedule(LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Ngày xem lịch không được để trống."
            );
        }

        try {

            return bookingDAO.findByDate(date);

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể tải lịch sân.",
                    ex
            );
        }
    }

    // Lấy lịch của một sân cụ thể trong ngày
    public List<Booking> scheduleByCourt(
            long courtId,
            LocalDate date
    ) {

        if (courtId <= 0) {
            throw new IllegalArgumentException(
                    "Sân không hợp lệ."
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Ngày xem lịch không được để trống."
            );
        }

        try {

            return bookingDAO.findByCourtAndDate(
                    courtId,
                    date
            );

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể tải lịch của sân.",
                    ex
            );
        }
    }
}