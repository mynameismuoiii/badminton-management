package com.badminton.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Payment(
        long id,
        long bookingId,
        BigDecimal amount,
        String method,
        LocalDateTime paidAt
) {
}