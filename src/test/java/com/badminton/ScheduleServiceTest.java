package com.badminton;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.badminton.util.TimeSlotUtil;

class ScheduleServiceTest {

    @Test
    void overlap() {

        boolean result = TimeSlotUtil.overlaps(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalTime.of(8, 30),
                LocalTime.of(10, 0)
        );

        assertTrue(result);
    }
}