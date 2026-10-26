package com.badminton.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Booking(
        long id,
        long courtId,
        long customerId,
        LocalDate bookingDate,
        LocalTime startTime,
        LocalTime endTime,
        String status
) {
}