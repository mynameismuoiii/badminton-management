package com.badminton.service;

import com.badminton.dao.BookingDAO;
import com.badminton.dao.CourtDAO;
import com.badminton.model.Booking;
import com.badminton.model.Court;
import com.badminton.util.TimeSlotUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SuggestionService {

    private final CourtDAO courtDAO;
    private final BookingDAO bookingDAO;

    public SuggestionService() {
        this.courtDAO = new CourtDAO();
        this.bookingDAO = new BookingDAO();
    }

    public List<Court> suggest(
            LocalDate date,
            LocalTime from,
            LocalTime to
    ) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Ngày đặt sân không được để trống."
            );
        }

        if (!TimeSlotUtil.isValidTimeSlot(from, to)) {
            throw new IllegalArgumentException(
                    "Giờ bắt đầu phải nhỏ hơn giờ kết thúc."
            );
        }

        if (!TimeSlotUtil.isWithinOpeningHours(from, to)) {
            throw new IllegalArgumentException(
                    "Giờ đặt sân phải nằm trong khoảng 06:00 - 22:00."
            );
        }

        try {

            List<Court> availableCourts =
                    new ArrayList<>();

            List<Court> courts =
                    courtDAO.findAll();

            for (Court court : courts) {

                // Bỏ qua sân đã ngừng hoạt động
                if (!court.active()) {
                    continue;
                }

                List<Booking> bookings =
                        bookingDAO.findByCourtAndDate(
                                court.id(),
                                date
                        );

                boolean busy = false;

                for (Booking booking : bookings) {

                    // Booking đã hủy không chiếm sân
                    if ("CANCELLED".equalsIgnoreCase(
                            booking.status()
                    )) {
                        continue;
                    }

                    if (TimeSlotUtil.isOverlap(
                            from,
                            to,
                            booking.startTime(),
                            booking.endTime()
                    )) {

                        busy = true;
                        break;
                    }
                }

                if (!busy) {
                    availableCourts.add(court);
                }
            }

            // Giá thấp trước, nếu bằng giá thì sắp theo tên
            availableCourts.sort(
                    Comparator
                            .comparing(Court::pricePerHour)
                            .thenComparing(Court::name)
            );

            return availableCourts;

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể tìm sân trống.",
                    ex
            );
        }
    }
}