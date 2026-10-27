package com.badminton.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final String ALGORITHM =
            "PBKDF2WithHmacSHA256";

    private static final String FORMAT =
            "pbkdf2_sha256";

    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private PasswordUtil() {
    }

    // Bam mat khau truoc khi luu database
    public static String hash(char[] password) {

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                "Mật khẩu không được rỗng."
            );
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] derived = derive(
            password,
            salt,
            ITERATIONS
        );

        return FORMAT + "$"
                + ITERATIONS + "$"
                + Base64.getEncoder()
                        .encodeToString(salt)
                + "$"
                + Base64.getEncoder()
                        .encodeToString(derived);
    }

    // Kiem tra mat khau khi dang nhap
    public static boolean verify(
            char[] password,
            String storedHash
    ) {

        if (password == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split(
                "\\$", -1
            );

            if (parts.length != 4
                    || !FORMAT.equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(
                parts[1]
            );

            if (iterations < 100_000
                    || iterations > 1_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder()
                    .decode(parts[2]);

            byte[] expected = Base64.getDecoder()
                    .decode(parts[3]);

            if (salt.length != SALT_BYTES
                    || expected.length != KEY_BITS / 8) {
                return false;
            }

            byte[] actual = derive(
                password,
                salt,
                iterations
            );

            return MessageDigest.isEqual(
                expected,
                actual
            );

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(
            char[] password,
            byte[] salt,
            int iterations
    ) {

        PBEKeySpec spec = new PBEKeySpec(
            password,
            salt,
            iterations,
            KEY_BITS
        );

        try {
            return SecretKeyFactory
                .getInstance(ALGORITHM)
                .generateSecret(spec)
                .getEncoded();

        } catch (
            java.security.GeneralSecurityException ex
        ) {
            throw new IllegalStateException(
                "Không thể mã hóa mật khẩu.",
                ex
            );

        } finally {
            spec.clearPassword();
        }
    }
}
