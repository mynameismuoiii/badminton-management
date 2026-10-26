package com.badminton.service;

public class AuthService {

    public boolean login(String username, String password) {

        // TODO N1:
        // 1. Kiểm tra username/password rỗng.
        // 2. Gọi UserDAO.findByUsername().
        // 3. Kiểm tra mật khẩu với password_hash.
        // 4. Trả true nếu hợp lệ, false nếu không.
        //
        // Cần dùng thuật toán băm mật khẩu như BCrypt.
        // Không so sánh mật khẩu dạng plaintext.

        throw new UnsupportedOperationException(
                "N1: implement login"
        );
    }
}