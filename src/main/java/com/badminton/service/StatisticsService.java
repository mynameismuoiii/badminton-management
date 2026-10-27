package com.badminton.service;

import com.badminton.dao.BookingDAO;
import com.badminton.dao.CourtDAO;
import com.badminton.dao.PaymentDAO;
import com.badminton.model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class StatisticsService {

    private final BookingDAO bookingDAO;
    private final CourtDAO courtDAO;
    private final PaymentDAO paymentDAO;


    public StatisticsService() {

        bookingDAO =
                new BookingDAO();

        courtDAO =
                new CourtDAO();

        paymentDAO =
                new PaymentDAO();
    }


    // ============================================================
    // TOTAL REVENUE
    // ============================================================

    public BigDecimal revenue() {

        try {

            List<Payment> payments =
                    paymentDAO.findAll();

            BigDecimal total =
                    BigDecimal.ZERO;


            for (Payment payment : payments) {

                if (payment.amount() != null) {

                    total =
                            total.add(
                                    payment.amount()
                            );
                }
            }


            return total;

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể tính doanh thu.",
                    ex
            );
        }
    }


    // ============================================================
    // TOTAL BOOKINGS
    // ============================================================

    public int totalBookings() {

        try {

            return bookingDAO
                    .findAll()
                    .size();

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể thống kê booking.",
                    ex
            );
        }
    }


    // ============================================================
    // ACTIVE COURTS
    // ============================================================

    public long activeCourts() {

        try {

            return courtDAO
                    .findAll()
                    .stream()
                    .filter(
                            court ->
                                    court.active()
                    )
                    .count();

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể thống kê sân.",
                    ex
            );
        }
    }


    // ============================================================
    // TOTAL PAYMENTS
    // ============================================================

    public int totalPayments() {

        try {

            return paymentDAO
                    .findAll()
                    .size();

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Không thể thống kê thanh toán.",
                    ex
            );
        }
    }
}