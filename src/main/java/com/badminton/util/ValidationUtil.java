package com.badminton.util;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    // Kiểm tra chuỗi không rỗng
    public static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    // Kiểm tra số điện thoại VN dạng 10 chữ số
    public static boolean validPhone(String phone) {
        return phone != null
                && phone.matches("0[0-9]{9}");
    }
}