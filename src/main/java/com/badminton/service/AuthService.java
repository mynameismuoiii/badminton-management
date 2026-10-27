package com.badminton.service;

import com.badminton.dao.UserDAO;
import com.badminton.model.User;
import com.badminton.util.PasswordUtil;
import com.badminton.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // =============================
    // DANG KY TAI KHOAN
    // =============================

    public long register(
            String fullName,
            String username,
            String phone,
            char[] password
    ) throws SQLException {

        // Kiem tra ho ten
        if (fullName == null
                || fullName.isBlank()
                || fullName.length() > 120) {

            throw new IllegalArgumentException(
                "Họ tên phải có từ 1 đến 120 ký tự."
            );
        }

        // Kiem tra ten dang nhap
        if (username == null
                || !username.matches(
                    "[A-Za-z0-9._-]{3,80}"
                )) {

            throw new IllegalArgumentException(
                "Tên đăng nhập gồm 3-80 ký tự "
                + "(chữ, số, dấu . _ -)."
            );
        }

        // Kiem tra so dien thoai
        if (!ValidationUtil.validPhone(phone)) {

            throw new IllegalArgumentException(
                "Số điện thoại phải có 10 chữ số, "
                + "bắt đầu bằng 0."
            );
        }

        // Kiem tra mat khau
        if (password == null
                || password.length < 8
                || password.length > 128) {

            throw new IllegalArgumentException(
                "Mật khẩu phải dài 8-128 ký tự."
            );
        }

        // Bam mat khau truoc khi luu
        String passwordHash =
                PasswordUtil.hash(password);

        try {
            // UserDAO tu dong luu 2 bang
            // users va customers trong transaction
            return userDAO.createCustomerAccount(
                fullName.strip(),
                username,
                phone,
                passwordHash
            );

        } catch (SQLException ex) {

            // Ma loi trung UNIQUE cua MySQL
            if (ex.getErrorCode() == 1062) {

                throw new IllegalArgumentException(
                    "Tên đăng nhập hoặc số điện thoại "
                    + "đã được sử dụng."
                );
            }

            throw ex;
        }
    }

    // =============================
    // XAC THUC TAI KHOAN
    // =============================

    public Optional<User> authenticate(
            String username,
            char[] password
    ) throws SQLException {

        if (username == null
                || username.isBlank()
                || password == null
                || password.length == 0) {

            return Optional.empty();
        }

        // Tim user trong MySQL
        return userDAO.findByUsername(username)

            // So sanh voi mat khau da bam
            .filter(user ->
                PasswordUtil.verify(
                    password,
                    user.passwordHash()
                )
            );
    }

    // =============================
    // GIU HAM LOGIN CU
    // =============================

    public boolean login(
            String username,
            String password
    ) {

        char[] chars = password == null
                ? new char[0]
                : password.toCharArray();

        try {
            return authenticate(
                username,
                chars
            ).isPresent();

        } catch (SQLException ex) {

            throw new IllegalStateException(
                "Không thể kết nối MySQL.",
                ex
            );

        } finally {
            Arrays.fill(chars, '\0');
        }
    }
}
