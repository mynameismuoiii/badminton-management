package com.badminton.model;

public record Customer(
        long id,
        String fullName,
        String phone,
        String email
) {
}