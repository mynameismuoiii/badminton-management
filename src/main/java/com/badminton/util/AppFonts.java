
package com.badminton.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * TH-MINTON fonts.
 *
 * Merriweather: Font cho toan bo giao dien.
 * Fredoka SemiBold: Chi dung cho logo TH-MINTON.
 *
 * Tu dong tim font trong resources/fonts va thu muc con.
 */
public final class AppFonts {

    private AppFonts() {
    }

    private static final Font REGULAR =
            loadUi("Regular", Font.PLAIN);

    private static final Font MEDIUM =
            loadUi("Medium", Font.PLAIN);

    private static final Font BOLD =
            loadUi("Bold", Font.BOLD);

    private static final Font FREDOKA = loadLogo();

    // ==============================
    // LOAD MERRIWEATHER
    // ==============================

    private static Font loadUi(String weight, int style) {

        String standardName =
                "Merriweather-" + weight + ".ttf";

        Font font = loadResource(
                "Merriweather " + weight,
                "merriweather",

                "/fonts/Merriweather/static/" + standardName,
                "/fonts/Merriweather/" + standardName,

                "/fonts/Merriweather/static/Merriweather_24pt-"
                        + weight + ".ttf",

                "/fonts/Merriweather/static/Merriweather_18pt-"
                        + weight + ".ttf",

                "/fonts/Merriweather/static/Merriweather_36pt-"
                        + weight + ".ttf",

                "/fonts/Merriweather/Merriweather-VariableFont_opsz,wdth,wght.ttf",

                "/fonts/Merriweather/Merriweather-VariableFont_wght.ttf"
        );

        if (font == null) {
            font = findRecursively(
                    "Merriweather",
                    "merriweather",
                    weight
            );
        }

        if (font != null) {
            return style == Font.BOLD
                    ? font.deriveFont(Font.BOLD, 14f)
                    : font;
        }

        Font fallback = installedOrFallback(
                "Merriweather",
                style
        );

        System.err.println(
                "[AppFonts] WARNING: Khong thay Merriweather "
                        + weight
                        + ". Dang dung "
                        + fallback.getFontName()
        );

        return fallback;
    }

    // ==============================
    // LOAD FREDOKA - LOGO
    // ==============================

    private static Font loadLogo() {

        Font font = loadResource(
                "Fredoka Logo",
                "fredoka",

                "/fonts/Fredoka/static/Fredoka-SemiBold.ttf",

                "/fonts/Fredoka/Fredoka-SemiBold.ttf",

                "/fonts/Fredoka/static/Fredoka_Expanded-SemiBold.ttf",

                "/fonts/Fredoka/static/Fredoka-Bold.ttf",

                "/fonts/Fredoka/Fredoka-VariableFont_wdth,wght.ttf",

                "/fonts/static/Fredoka-SemiBold.ttf"
        );

        if (font == null) {
            font = findRecursively(
                    "Fredoka",
                    "fredoka",
                    "SemiBold"
            );
        }

        if (font != null) {
            return font;
        }

        Font fallback = installedOrFallback(
                "Fredoka",
                Font.BOLD
        );

        System.err.println(
                "[AppFonts] WARNING: Khong thay Fredoka. "
                        + "Dang dung "
                        + fallback.getFontName()
        );

        return fallback;
    }

    // ==============================
    // LOAD FONT TU RESOURCES
    // ==============================

    private static Font loadResource(
            String label,
            String familyToken,
            String... paths
    ) {

        for (String path : paths) {

            try (InputStream stream =
                         AppFonts.class.getResourceAsStream(path)) {

                if (stream == null) {
                    continue;
                }

                Font font = Font.createFont(
                        Font.TRUETYPE_FONT,
                        stream
                ).deriveFont(14f);

                if (!matchesFamily(font, familyToken)) {

                    System.err.println(
                            "[AppFonts] Sai font: "
                                    + path
                                    + " ("
                                    + font.getFontName()
                                    + ")"
                    );

                    continue;
                }

                System.out.println(
                        "[AppFonts] "
                                + label
                                + ": "
                                + font.getFontName()
                                + " <- "
                                + path
                );

                return font;

            } catch (Exception ex) {

                System.err.println(
                        "[AppFonts] Khong doc duoc "
                                + path
                                + ": "
                                + ex.getMessage()
                );
            }
        }

        return null;
    }

    // ==============================
    // TIM FONT TRONG THU MUC CON
    // ==============================

    private static Font findRecursively(
            String folderName,
            String familyToken,
            String weight
    ) {

        List<Path> folders = List.of(

                Paths.get(
                        "src",
                        "main",
                        "resources",
                        "fonts",
                        folderName
                ),

                Paths.get(
                        "target",
                        "classes",
                        "fonts",
                        folderName
                )
        );

        for (Path folder : folders) {

            Font font = scan(
                    folder,
                    familyToken,
                    weight
            );

            if (font != null) {
                return font;
            }
        }

        URL classpathFolder =
                AppFonts.class.getResource(
                        "/fonts/" + folderName
                );

        if (classpathFolder != null
                && "file".equals(classpathFolder.getProtocol())) {

            try {

                return scan(
                        Paths.get(classpathFolder.toURI()),
                        familyToken,
                        weight
                );

            } catch (Exception ex) {

                System.err.println(
                        "[AppFonts] Loi duong dan "
                                + folderName
                                + ": "
                                + ex.getMessage()
                );
            }
        }

        return null;
    }

    // ==============================
    // QUET FILE FONT
    // ==============================

    private static Font scan(
            Path folder,
            String familyToken,
            String weight
    ) {

        if (!Files.isDirectory(folder)) {
            return null;
        }

        try (Stream<Path> paths = Files.walk(folder, 8)) {

            List<Path> candidates = paths

                    .filter(Files::isRegularFile)

                    .filter(path -> {

                        String file = path
                                .getFileName()
                                .toString()
                                .toLowerCase(Locale.ROOT);

                        return file.contains(familyToken)
                                && (
                                file.endsWith(".ttf")
                                        || file.endsWith(".otf")
                        );
                    })

                    .sorted(
                            Comparator.comparingInt(
                                    path -> priority(path, weight)
                            )
                    )

                    .toList();

            for (Path path : candidates) {

                try (InputStream stream =
                             Files.newInputStream(path)) {

                    Font font = Font.createFont(
                            Font.TRUETYPE_FONT,
                            stream
                    ).deriveFont(14f);

                    if (!matchesFamily(font, familyToken)) {
                        continue;
                    }

                    System.out.println(
                            "[AppFonts] "
                                    + folder.getFileName()
                                    + " "
                                    + weight
                                    + ": "
                                    + font.getFontName()
                                    + " <- "
                                    + path
                    );

                    return font;

                } catch (Exception ex) {

                    System.err.println(
                            "[AppFonts] Khong doc duoc "
                                    + path
                                    + ": "
                                    + ex.getMessage()
                    );
                }
            }

        } catch (Exception ex) {

            System.err.println(
                    "[AppFonts] Khong quet duoc "
                            + folder
                            + ": "
                            + ex.getMessage()
            );
        }

        return null;
    }

    // ==============================
    // KIEM TRA TEN FONT
    // ==============================

    private static boolean matchesFamily(
            Font font,
            String familyToken
    ) {

        return font.getFamily(Locale.ROOT)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z]", "")
                .contains(familyToken);
    }

    // ==============================
    // UU TIEN FONT DUNG WEIGHT
    // ==============================

    private static int priority(
            Path path,
            String weight
    ) {

        String name = path
                .getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        String requested =
                weight.toLowerCase(Locale.ROOT);

        if (name.contains("-" + requested + ".")
                || name.contains("_" + requested + ".")) {
            return 0;
        }

        if (requested.equals("medium")
                && name.contains("semibold")) {
            return 1;
        }

        if (name.contains("variable")
                || name.contains("[wght]")) {
            return 2;
        }

        if (requested.equals("medium")
                && name.contains("regular")) {
            return 3;
        }

        if (requested.equals("semibold")
                && name.contains("bold")) {
            return 3;
        }

        if (requested.equals("bold")
                && name.contains("regular")) {
            return 4;
        }

        if (name.contains("regular")) {
            return 5;
        }

        return 6;
    }

    // ==============================
    // FONT DU PHONG
    // ==============================

    private static Font installedOrFallback(
            String family,
            int style
    ) {

        for (String name :
                GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames()) {

            if (name.equalsIgnoreCase(family)) {
                return new Font(name, style, 14);
            }
        }

        return new Font("Segoe UI", style, 14);
    }

    // ==============================
    // CAC HAM SU DUNG FONT
    // ==============================

    public static Font regular(float size) {
        return REGULAR.deriveFont(size);
    }

    public static Font medium(float size) {
        return MEDIUM.deriveFont(size);
    }

    public static Font bold(float size) {
        return BOLD.deriveFont(Font.BOLD, size);
    }

    // Giu nguyen Fredoka SemiBold cho logo
    public static Font fredoka(float size) {
        return FREDOKA.deriveFont(size);
    }
}
