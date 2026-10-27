package com.badminton.service;

import com.badminton.model.Booking;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleServiceTest {

    @Test
    void testScheduleByDate() {

        ScheduleService service =
                new ScheduleService();

        LocalDate date =
                LocalDate.of(
                        2026,
                        10,
                        28
                );

        List<Booking> bookings =
                service.schedule(date);

        assertNotNull(bookings);

        assertEquals(
                1,
                bookings.size()
        );
    }


    @Test
    void testScheduleByCourt() {

        ScheduleService service =
                new ScheduleService();

        long courtId = 1;

        LocalDate date =
                LocalDate.of(
                        2026,
                        10,
                        28
                );

        List<Booking> bookings =
                service.scheduleByCourt(
                        courtId,
                        date
                );

        assertNotNull(bookings);

        for (Booking booking : bookings) {

            assertEquals(
                    courtId,
                    booking.courtId()
            );

            assertEquals(
                    date,
                    booking.bookingDate()
            );
        }
    }
}