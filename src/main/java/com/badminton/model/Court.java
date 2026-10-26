package com.badminton.model;

import java.math.BigDecimal;

public record Court(
        long id,
        String name,
        BigDecimal pricePerHour,
        boolean active
) {
}