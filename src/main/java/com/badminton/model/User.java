package com.badminton.model;

public record User(
        long id,
        String username,
        String passwordHash,
        String role
) {
}