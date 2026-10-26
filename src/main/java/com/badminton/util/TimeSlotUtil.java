package com.badminton.util;

import java.time.LocalTime;

public final class TimeSlotUtil {

    private TimeSlotUtil() {
    }

    public static boolean overlaps(
            LocalTime fromA,
            LocalTime toA,
            LocalTime fromB,
            LocalTime toB
    ) {

        if (!fromA.isBefore(toA) || !fromB.isBefore(toB)) {
            throw new IllegalArgumentException(
                    "Invalid time interval"
            );
        }

        return fromA.isBefore(toB) && fromB.isBefore(toA);
    }
}