
package com.badminton.ui;

import com.badminton.util.AppFonts;

import java.awt.*;
import javax.swing.*;

public class AuthView extends JPanel {

    private enum Page {
        SPLASH, WELCOME, LOGIN, REGISTER
    }

    private Page page = Page.SPLASH;
    private int activeDot = 0;

    private final RoundedButton loginChoice =
        new RoundedButton("Đăng nhập", true);

    private final RoundedButton registerChoice =
        new RoundedButton("Đăng ký", false);

    // =========================
    // CARD DANG NHAP / DANG KY
    // =========================

    private final JPanel formCard = new JPanel(new CardLayout()) {

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Bong do
            g2.setColor(new Color(0, 18, 21, 100));

            g2.fillRoundRect(
                6, 9,
                getWidth() - 8,
                getHeight() - 12,
                32, 32
            );

            // Nen card
            g2.setColor(new Color(10, 42, 46, 242));

            g2.fillRoundRect(
                0, 0,
                getWidth() - 9,
                getHeight() - 13,
                32, 32
            );

            // Vien card
            g2.setColor(new Color(112, 237, 203, 95));

            g2.drawRoundRect(
                0, 0,
                getWidth() - 10,
                getHeight() - 14,
                32, 32
            );

            g2.dispose();

            super.paintComponent(g);
        }
    };

    // =========================
    // CONSTRUCTOR
    // =========================

    public AuthView() {

        setLayout(null);
        setBackground(new Color(7, 42, 40));

        formCard.setOpaque(false);

        formCard.add(
            new LoginPanel(
                this::showWelcome,
                this::showRegister
            ),
            "LOGIN"
        );

        formCard.add(
            new RegisterPanel(
                this::showWelcome,
                this::showLogin
            ),
            "REGISTER"
        );

        add(formCard);
        add(loginChoice);
        add(registerChoice);

        loginChoice.addActionListener(
            e -> showLogin()
        );

        registerChoice.addActionListener(
            e -> showRegister()
        );

        updatePage();

        // Animation cho 3 cham loading
        Timer loadingDots = new Timer(310, e -> {

            activeDot = (activeDot + 1) % 3;

            if (page != Page.SPLASH) {
                ((Timer) e.getSource()).stop();
            }

            repaint();
        });

        loadingDots.start();

        // Loading 1.9 giay
        Timer splash = new Timer(
            1900,
            e -> showWelcome()
        );

        splash.setRepeats(false);
        splash.start();
    }

    // =========================
    // CHUYEN MAN HINH
    // =========================

    private void showWelcome() {

        page = Page.WELCOME;
        updatePage();
    }

    private void showLogin() {

        page = Page.LOGIN;

        ((CardLayout) formCard.getLayout()).show(
            formCard,
            "LOGIN"
        );

        updatePage();
    }

    private void showRegister() {

        page = Page.REGISTER;

        ((CardLayout) formCard.getLayout()).show(
            formCard,
            "REGISTER"
        );

        updatePage();
    }

    private void updatePage() {

        boolean welcome = page == Page.WELCOME;

        loginChoice.setVisible(welcome);
        registerChoice.setVisible(welcome);

        formCard.setVisible(
            page == Page.LOGIN || page == Page.REGISTER
        );

        doLayout();
        revalidate();
        repaint();
    }

    // =========================
    // BO CUC
    // =========================

    @Override
    public void doLayout() {

        super.doLayout();

        int w = getWidth();
        int h = getHeight();

        int btnW = 225;
        int gap = 16;

        int left = (w - 2 * btnW - gap) / 2;

        // Nut xuat hien gan dung vi tri 3 cham
        // sau khi loading xong.
        int y = getLoadingDotsY() - 3;

        loginChoice.setBounds(
            left,
            y,
            btnW,
            64
        );

        registerChoice.setBounds(
            left + btnW + gap,
            y,
            btnW,
            64
        );

        // Dat logo va form thanh MOT CUM can giua.
        // Khong tach xa khi maximize.
        int cardW = getFormWidth();
        int cardH = page == Page.REGISTER ? 610 : 465;

        int cardX = getAuthLeftX()
            + getBrandColumnWidth()
            + AUTH_GAP;

        formCard.setBounds(
            cardX,
            Math.max(25, (h - cardH) / 2),
            cardW,
            cardH
        );
    }

    // Khoang cach giua hai cot
    private static final int AUTH_GAP = 28;

    // Chieu rong cot logo
    private static final int BRAND_COLUMN_WIDTH = 340;

    // Khoang cach toi thieu voi hai canh
    private static final int AUTH_SIDE_PADDING = 24;

    private int getFormWidth() {

        return Math.min(
            480,
            Math.max(420, getWidth() / 2 - 68)
        );
    }

    private int getBrandColumnWidth() {

        int available = getWidth()
            - 2 * AUTH_SIDE_PADDING
            - getFormWidth()
            - AUTH_GAP;

        return Math.max(
            245,
            Math.min(BRAND_COLUMN_WIDTH, available)
        );
    }

    private int getAuthLeftX() {

        int groupW = getBrandColumnWidth()
            + AUTH_GAP
            + getFormWidth();

        return Math.max(
            12,
            (getWidth() - groupW) / 2
        );
    }

    private int getBrandCenterX() {

        if (page == Page.SPLASH || page == Page.WELCOME) {
            return getWidth() / 2;
        }

        return getAuthLeftX()
            + getBrandColumnWidth() / 2;
    }

    // =========================
    // VE GIAO DIEN
    // =========================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        drawBackground(g2);
        drawBrand(g2);

        // Chi hien loading khi SPLASH
        if (page == Page.SPLASH) {
            drawDots(g2);
        }

        g2.dispose();
    }

    // =========================
    // BACKGROUND DARK MINT
    // =========================

    private void drawBackground(Graphics2D g) {

        int w = getWidth();
        int h = getHeight();

        // Nen gradient
        g.setPaint(
            new GradientPaint(
                0, 0,
                new Color(7, 39, 37),
                w, h,
                new Color(10, 86, 76)
            )
        );

        g.fillRect(0, 0, w, h);

        // Anh sang trung tam
        g.setPaint(
            new RadialGradientPaint(
                new Point(w / 2, h / 3),
                Math.max(320, w / 3),
                new float[]{0f, 1f},
                new Color[]{
                    new Color(78, 216, 190, 90),
                    new Color(78, 216, 190, 0)
                }
            )
        );

        g.fillRect(0, 0, w, h);

        // Cac duong cong mo trang tri
        g.setStroke(new BasicStroke(2f));

        g.setColor(
            new Color(217, 255, 243, 38)
        );

        g.drawArc(
            -220, 42,
            830, 620,
            18, 110
        );

        g.drawArc(
            w - 610, 14,
            810, 650,
            65, 110
        );

        // Hai duong san
        g.drawLine(
            92,
            h - 37,
            w / 2 - 110,
            h - 166
        );

        g.drawLine(
            w - 92,
            h - 37,
            w / 2 + 110,
            h - 166
        );

        // Lop sang mo phia duoi
        g.setColor(
            new Color(146, 243, 212, 16)
        );

        g.fillOval(
            -100,
            h - 218,
            w + 200,
            390
        );
    }

    // =========================
    // LOGO TRON + TH-MINTON
    // =========================

    private void drawBrand(Graphics2D g) {

        boolean centered =
            page == Page.SPLASH
            || page == Page.WELCOME;

        int h = getHeight();

        // Khi hien form:
        // Logo can giua trong cot ben trai.
        // Hai phan di chuyen cung nhau.
        int cx = getBrandCenterX();

        int size = centered ? 126 : 116;

        int logoY = centered
            ? Math.max(90, h / 2 - 232)
            : h / 2 - 185;

        int logoX = cx - size / 2;

        // Glow xung quanh logo
        g.setColor(
            new Color(96, 229, 200, 35)
        );

        g.fillOval(
            logoX - 24,
            logoY - 24,
            size + 48,
            size + 48
        );

        // Vien trang hinh tron
        g.setColor(
            new Color(246, 255, 251)
        );

        g.fillOval(
            logoX,
            logoY,
            size,
            size
        );

        // Gradient ben trong logo
        g.setPaint(
            new GradientPaint(
                logoX + 8,
                logoY + 12,
                new Color(72, 222, 190),
                logoX + size,
                logoY + size,
                new Color(22, 147, 151)
            )
        );

        g.fillOval(
            logoX + 11,
            logoY + 11,
            size - 22,
            size - 22
        );

        // Bieu tuong cach dieu
        g.setColor(Color.WHITE);

        g.setStroke(
            new BasicStroke(
                7f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
            )
        );

        g.drawArc(
            logoX + size / 2 - 26,
            logoY + 28,
            52, 52,
            38, 268
        );

        g.drawLine(
            logoX + size / 2 + 11,
            logoY + 73,
            logoX + size / 2 + 32,
            logoY + 94
        );

        // =========================
        // TEN TH-MINTON
        // =========================

        String title = "TH - MINTON";

        // Giu nguyen font Fredoka
        g.setFont(
            AppFonts.fredoka(centered ? 56f : 43f)
        );

        g.setColor(Color.WHITE);

        int titleY = logoY
            + size
            + (centered ? 91 : 86);

        g.drawString(
            title,
            cx - g.getFontMetrics().stringWidth(title) / 2,
            titleY
        );

        // =========================
        // SLOGAN
        // =========================

        String slogan =
            "Your Court. Your Game. Your Moment.";

        // Merriweather (tu AppFonts)
        g.setFont(
            AppFonts.regular(centered ? 19f : 16f)
        );

        g.setColor(
            new Color(215, 247, 239)
        );

        int sloganY = titleY + 41;

        g.drawString(
            slogan,
            cx - g.getFontMetrics().stringWidth(slogan) / 2,
            sloganY
        );
    }

    // =========================
    // LOADING DOTS
    // =========================

    private int getLoadingDotsY() {

        int logoY = Math.max(
            90,
            getHeight() / 2 - 232
        );

        return logoY
            + 126
            + 91
            + 41
            + 49;
    }

    private void drawDots(Graphics2D g) {

        int cx = getWidth() / 2;
        int y = getLoadingDotsY();

        for (int i = 0; i < 3; i++) {

            g.setColor(
                i == activeDot
                    ? new Color(79, 240, 208)
                    : new Color(231, 253, 248, 100)
            );

            g.fillOval(
                cx - 29 + i * 23,
                y,
                11, 11
            );
        }
    }
}
