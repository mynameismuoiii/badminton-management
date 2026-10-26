package com.badminton.service;

import com.badminton.model.Booking;

public class BookingService {

    public long createBooking(Booking booking) {

        // TODO N1:
        // 1. Kiểm tra booking có hợp lệ không.
        // 2. Kiểm tra sân còn hoạt động không.
        // 3. Kiểm tra startTime < endTime.
        // 4. Kiểm tra có booking nào trùng giờ không.
        // 5. Lưu booking xuống MySQL.
        // 6. Trả về ID booking.
        //
        // Cần dùng transaction/locking phù hợp
        // để tránh hai yêu cầu đặt trùng sân đồng thời.

        throw new UnsupportedOperationException(
                "N1: createBooking"
        );
    }
}