package com.badminton.dao;

import java.sql.SQLException;
import java.util.Optional;

import com.badminton.model.User;

public class UserDAO {

    public Optional<User> findByUsername(String username)
            throws SQLException {

        // TODO N1:
        // 1. Kết nối MySQL.
        // 2. SELECT * FROM users WHERE username = ?
        // 3. Dùng PreparedStatement.
        // 4. Chuyển ResultSet thành User.
        // 5. Trả Optional<User>.

        throw new UnsupportedOperationException(
                "N1: implement findByUsername"
        );
    }
}