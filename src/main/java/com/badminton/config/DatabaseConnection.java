package com.badminton.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws SQLException {

        Properties properties = new Properties();

        try (InputStream input =
                DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new SQLException(
                    "Missing db.properties"
                );
            }

            properties.load(input);

        } catch (IOException ex) {
            throw new SQLException(
                "Cannot read db.properties", ex
            );
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || user == null || password == null) {
            throw new SQLException("Missing database settings");
        }

        return DriverManager.getConnection(
                url, user, password
        );
    }
}