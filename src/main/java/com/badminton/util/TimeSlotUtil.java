package com.badminton.util;

import java.time.LocalTime;

public final class TimeSlotUtil {

    private TimeSlotUtil() {
    }

    public static boolean isValidTimeSlot(
            LocalTime start,
            LocalTime end
    ) {
        return start != null
                && end != null
                && start.isBefore(end);
    }

    public static boolean isOverlap(
            LocalTime start1,
            LocalTime end1,
            LocalTime start2,
            LocalTime end2
    ) {
        if (!isValidTimeSlot(start1, end1)
                || !isValidTimeSlot(start2, end2)) {
            return false;
        }

        return start1.isBefore(end2)
                && start2.isBefore(end1);
    }

    public static boolean isWithinOpeningHours(
            LocalTime start,
            LocalTime end
    ) {
        if (!isValidTimeSlot(start, end)) {
            return false;
        }

        LocalTime openingTime =
                LocalTime.of(6, 0);

        LocalTime closingTime =
                LocalTime.of(22, 0);

        return !start.isBefore(openingTime)
                && !end.isAfter(closingTime);
    }
}