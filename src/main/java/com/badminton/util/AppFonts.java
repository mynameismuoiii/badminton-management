package com.badminton.util;

import java.awt.Font;
import java.io.InputStream;

public final class AppFonts {

    private AppFonts() {}

    // ======================
    // FONT LEXEND
    // ======================

    private static final Font REGULAR = load(
        "/fonts/static/Lexend-Regular.ttf",
        "/fonts/Lexend.ttf"
    );

    private static final Font MEDIUM = load(
        "/fonts/static/Lexend-Medium.ttf",
        "/fonts/Lexend.ttf"
    );

    private static final Font BOLD = load(
        "/fonts/static/Lexend-Bold.ttf",
        "/fonts/Lexend.ttf"
    );

    // ======================
    // FONT FREDOKA
    // ======================

    private static final Font FREDOKA = load(
        "/fonts/static/Fredoka-SemiBold.ttf",
        "/fonts/Fredoka-VariableFont_wdth,wght.ttf"
    );

    private static Font load(
            String preferred,
            String fallback
    ) {
        for (String path : new String[]{preferred, fallback}) {

            try (InputStream is =
                    AppFonts.class.getResourceAsStream(path)) {

                if (is == null) {
                    continue;
                }

                Font font = Font.createFont(
                    Font.TRUETYPE_FONT,
                    is
                );

                System.out.println(
                    "Loaded font: " + font.getFontName()
                    + " from " + path
                );

                return font;

            } catch (Exception e) {
                System.err.println(
                    "Loi tai font: " + path
                    + " - " + e.getMessage()
                );
            }
        }

        return new Font("SansSerif", Font.PLAIN, 14);
    }

    public static Font regular(float size) {
        return REGULAR.deriveFont(size);
    }

    public static Font medium(float size) {
        return MEDIUM.deriveFont(size);
    }

    public static Font bold(float size) {
        return BOLD.deriveFont(size);
    }

    // Font Fredoka cho ten thuong hieu
    public static Font fredoka(float size) {
        return FREDOKA.deriveFont(size);
    }
}
