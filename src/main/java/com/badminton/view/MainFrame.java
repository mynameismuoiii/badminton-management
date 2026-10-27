package com.badminton.view;

import com.badminton.ui.AuthFrame;
import com.badminton.model.User;
import com.badminton.util.AppFonts;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MainFrame extends JFrame {

    // =========================
    // COLORS
    // =========================

    private static final Color SIDEBAR =
            new Color(7, 57, 51);

    private static final Color SIDEBAR_DARK =
            new Color(5, 45, 41);

    private static final Color PRIMARY =
            new Color(47, 201, 174);

    private static final Color PRIMARY_DARK =
            new Color(24, 156, 139);

    private static final Color CONTENT_BG =
            new Color(243, 248, 247);

    private static final Color TEXT =
            new Color(28, 55, 50);

    private static final Color MUTED =
            new Color(116, 137, 133);


    // =========================
    // LAYOUT
    // =========================

    private final CardLayout cardLayout =
            new CardLayout();

    private final JPanel content =
            new JPanel(cardLayout);

    private final JPanel menuPanel =
            new JPanel();

    private final Map<String, JPanel> screens =
            new LinkedHashMap<>();

    private final Map<String, NavButton> navButtons =
            new LinkedHashMap<>();

    private JLabel pageTitle;

    private String currentScreen;
    private final User currentUser;
    private final boolean admin;
    private BookingPanel bookingPanel;
    private PaymentPanel paymentPanel;

    public MainFrame(User user) {

        super("TH-MINTON");
        this.currentUser = Objects.requireNonNull(user, "Login required");
        this.admin = "ADMIN".equalsIgnoreCase(user.role());
        if (!admin && !"CUSTOMER".equalsIgnoreCase(user.role())) {
            throw new SecurityException("Vai trò chưa được hỗ trợ: " + user.role());
        }

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setMinimumSize(
                new Dimension(1100, 720)
        );

        setSize(1280, 800);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        getContentPane().setBackground(
                CONTENT_BG
        );


        // =========================
        // SIDEBAR
        // =========================

        JPanel sidebar =
                createSidebar();

        add(
                sidebar,
                BorderLayout.WEST
        );


        // =========================
        // MAIN AREA
        // =========================

        JPanel mainArea =
                new JPanel(
                        new BorderLayout()
                );

        mainArea.setBackground(
                CONTENT_BG
        );

        mainArea.add(
                createHeader(),
                BorderLayout.NORTH
        );

        content.setBackground(
                CONTENT_BG
        );

        mainArea.add(
                content,
                BorderLayout.CENTER
        );

        add(
                mainArea,
                BorderLayout.CENTER
        );


        // =========================
        // SCREENS: THEME shell + COMBINED business functions
        // =========================

        paymentPanel = new PaymentPanel(currentUser);
        bookingPanel = new BookingPanel(currentUser, bookingId -> {
            paymentPanel.focusBooking(bookingId);
            showScreen("Thanh toán");
        });

        registerScreen("Tổng quan", createHomePanel());
        if (admin) {
            // Admin co quyen quan ly san va khach hang.
            registerScreen("Sân", new CourtPanel());
            registerScreen("Khách hàng", new CustomerPanel());
        } else {
            // Customer chi xem san, va dat tren san da chon.
            registerScreen("Sân", new CustomerCourtListPanel(courtId -> {
                bookingPanel.selectCourt(courtId);
                showScreen("Đặt sân");
            }));
        }
        registerScreen("Đặt sân", bookingPanel);
        registerScreen("Lịch sân", new SchedulePanel());
        registerScreen("Gợi ý sân", new SuggestionPanel());
        registerScreen("Thanh toán", paymentPanel);
        if (admin) {
            registerScreen("Thống kê", new StatisticsPanel());
        }

        // Chi hien menu cho cac man hinh duoc cap quyen.
        for (String screenName : screens.keySet()) {
            addMenuButton(screenName);
        }

        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(createLogoutButton());

        showScreen("Tổng quan");
    }


    // ============================================================
    // SIDEBAR
    // ============================================================

    private JPanel createSidebar() {

        JPanel sidebar =
                new JPanel(
                        new BorderLayout()
                );

        sidebar.setPreferredSize(
                new Dimension(235, 0)
        );

        sidebar.setBackground(
                SIDEBAR
        );


        // ===== BRAND =====

        JPanel brand =
                new JPanel();

        brand.setOpaque(false);

        brand.setLayout(
                new BoxLayout(
                        brand,
                        BoxLayout.Y_AXIS
                )
        );

        brand.setBorder(
                new EmptyBorder(
                        30,
                        25,
                        25,
                        20
                )
        );


        JLabel logo =
                new JLabel("TH-MINTON");

        logo.setForeground(
                Color.WHITE
        );

        logo.setFont(
                AppFonts.fredoka(27f)
        );

        logo.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        JLabel subtitle =
                new JLabel(
                        "Your Court. Your Game."
                );

        subtitle.setForeground(
                new Color(
                        155,
                        213,
                        204
                )
        );

        subtitle.setFont(
                AppFonts.regular(11f)
        );

        subtitle.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        brand.add(logo);

        brand.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                4
                        )
                )
        );

        brand.add(subtitle);


        sidebar.add(
                brand,
                BorderLayout.NORTH
        );


        // ===== MENU =====

        menuPanel.setOpaque(false);

        menuPanel.setLayout(
                new BoxLayout(
                        menuPanel,
                        BoxLayout.Y_AXIS
                )
        );

        menuPanel.setBorder(
                new EmptyBorder(
                        10,
                        14,
                        20,
                        14
                )
        );


        sidebar.add(
                menuPanel,
                BorderLayout.CENTER
        );

        return sidebar;
    }


    // ============================================================
    // HEADER
    // ============================================================

    private JPanel createHeader() {

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setBackground(
                Color.WHITE
        );

        header.setPreferredSize(
                new Dimension(
                        0,
                        78
                )
        );

        header.setBorder(
                new EmptyBorder(
                        0,
                        28,
                        0,
                        28
                )
        );


        pageTitle =
                new JLabel("Tổng quan");

        pageTitle.setFont(
                AppFonts.bold(24f)
        );

        pageTitle.setForeground(
                TEXT
        );


        JPanel right =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                18
                        )
                );

        right.setOpaque(false);


        JLabel userLabel =
                new JLabel(
                        currentUser.username() + " (" +
                        (admin ? "Quản trị viên" : "Khách hàng") + ")"
                );

        userLabel.setFont(
                AppFonts.medium(13f)
        );

        userLabel.setForeground(
                MUTED
        );


        JLabel avatar =
                new JLabel(
                        "TM",
                        SwingConstants.CENTER
                );

        avatar.setOpaque(true);

        avatar.setBackground(
                new Color(
                        220,
                        248,
                        241
                )
        );

        avatar.setForeground(
                PRIMARY_DARK
        );

        avatar.setFont(
                AppFonts.bold(12f)
        );

        avatar.setPreferredSize(
                new Dimension(
                        40,
                        40
                )
        );


        right.add(userLabel);
        right.add(avatar);


        header.add(
                pageTitle,
                BorderLayout.WEST
        );

        header.add(
                right,
                BorderLayout.EAST
        );

        return header;
    }


    // ============================================================
    // MENU BUTTON
    // ============================================================

    private void addMenuButton(
            String name
    ) {

        NavButton button =
                new NavButton(name);

        button.addActionListener(e -> {
            if ("Đặt sân".equals(name)) {
                bookingPanel.refreshData();
            } else if ("Thanh toán".equals(name)) {
                paymentPanel.refreshData();
            }
            showScreen(name);
        });

        navButtons.put(
                name,
                button
        );

        menuPanel.add(button);

        menuPanel.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                6
                        )
                )
        );
    }


    // ============================================================
    // REGISTER SCREEN
    // ============================================================

    public void registerScreen(
        String name,
        JPanel panel
    ) {

        if (screens.containsKey(name)) {

            throw new IllegalArgumentException(
                    "Duplicate screen: " + name
            );
        }

        screens.put(
                name,
                panel
        );

        JScrollPane scrollPane =
                createScrollPane(panel);

        content.add(
                scrollPane,
                name
        );
    }


    // ============================================================
    // CHANGE SCREEN
    // ============================================================

    public void showScreen(
            String name
    ) {

        if (!screens.containsKey(name)) {

            throw new IllegalArgumentException(
                    "Unknown screen: "
                    + name
            );
        }

        currentScreen = name;

        cardLayout.show(
                content,
                name
        );

        pageTitle.setText(name);

        navButtons.forEach(
                (screenName, button) ->
                        button.setActive(
                                screenName.equals(
                                        name
                                )
                        )
        );
    }


    // ============================================================
    // HOME / DASHBOARD
    // ============================================================

    private JPanel createHomePanel() {

        JPanel wrapper = new JPanel();

        wrapper.setLayout(
                new BoxLayout(
                        wrapper,
                        BoxLayout.Y_AXIS
                )
        );

        wrapper.setBackground(CONTENT_BG);

        wrapper.setBorder(
                new EmptyBorder(
                        0,
                        0,
                        40,
                        0
                )
        );

        // Để trang đủ dài cho JScrollPane cuộn như web
        wrapper.setPreferredSize(
                new Dimension(
                        1000,
                        1050
                )
        );


        // =====================================================
        // HERO SECTION
        // =====================================================

        HeroPanel hero = new HeroPanel(
                "/images/home-hero.jpg" // <-- đổi ảnh ở đây
        );

        hero.setPreferredSize(
                new Dimension(
                        1000,
                        440
                )
        );

        hero.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        440
                )
        );

        hero.setLayout(
                new GridBagLayout()
        );


        JPanel heroContent = new JPanel();

        heroContent.setOpaque(false);

        heroContent.setLayout(
                new BoxLayout(
                        heroContent,
                        BoxLayout.Y_AXIS
                )
        );


        JLabel smallTitle =
                new JLabel(
                        "WELCOME TO TH-MINTON"
                );

        smallTitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        smallTitle.setForeground(
                new Color(
                        110,
                        232,
                        209
                )
        );

        smallTitle.setFont(
                AppFonts.medium(13f)
        );


        JLabel heroTitle =
                new JLabel(
                        "Your Court. Your Game. Your Moment."
                );

        heroTitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        heroTitle.setForeground(
                Color.WHITE
        );

        heroTitle.setFont(
                AppFonts.fredoka(36f)
        );


        JLabel heroDescription =
                new JLabel(
                        "<html><div style='text-align:center;'>"
                        + "Quản lý sân cầu lông dễ dàng, "
                        + "theo dõi lịch đặt và tìm sân phù hợp "
                        + "chỉ trong vài thao tác."
                        + "</div></html>"
                );

        heroDescription.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        heroDescription.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        heroDescription.setForeground(
                new Color(
                        225,
                        240,
                        237
                )
        );

        heroDescription.setFont(
                AppFonts.regular(14f)
        );

        heroDescription.setMaximumSize(
                new Dimension(
                        600,
                        60
                )
        );


        JButton bookingButton =
                new JButton(
                        "Đặt sân ngay"
                );

        bookingButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        bookingButton.setFont(
                AppFonts.medium(13f)
        );

        bookingButton.setForeground(
                SIDEBAR_DARK
        );

        bookingButton.setBackground(
                PRIMARY
        );

        bookingButton.setFocusPainted(false);
        bookingButton.setBorderPainted(false);

        bookingButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        bookingButton.setPreferredSize(
                new Dimension(
                        145,
                        42
                )
        );

        bookingButton.setMaximumSize(
                new Dimension(
                        145,
                        42
                )
        );

        bookingButton.addActionListener(
                e -> showScreen("Đặt sân")
        );


        heroContent.add(smallTitle);

        heroContent.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                12
                        )
                )
        );

        heroContent.add(heroTitle);

        heroContent.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                16
                        )
                )
        );

        heroContent.add(heroDescription);

        heroContent.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                24
                        )
                )
        );

        heroContent.add(bookingButton);


        hero.add(heroContent);


        wrapper.add(hero);


        // =====================================================
        // INTRO / ABOUT SECTION
        // =====================================================

        JPanel aboutSection =
                new JPanel(
                        new BorderLayout(
                                35,
                                0
                        )
                );

        aboutSection.setBackground(
                Color.WHITE
        );

        aboutSection.setBorder(
                new EmptyBorder(
                        45,
                        55,
                        45,
                        55
                )
        );

        aboutSection.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        260
                )
        );


        // ------------------
        // LEFT
        // ------------------

        JPanel aboutText =
                new JPanel();

        aboutText.setOpaque(false);

        aboutText.setLayout(
                new BoxLayout(
                        aboutText,
                        BoxLayout.Y_AXIS
                )
        );


        JLabel aboutSmall =
                new JLabel(
                        "ABOUT US"
                );

        aboutSmall.setForeground(
                PRIMARY_DARK
        );

        aboutSmall.setFont(
                AppFonts.medium(12f)
        );


        JLabel aboutTitle =
                new JLabel(
                        "Quản lý sân thông minh hơn"
                );

        aboutTitle.setForeground(
                TEXT
        );

        aboutTitle.setFont(
                AppFonts.bold(25f)
        );


        JLabel aboutDescription =
                new JLabel(
                        "<html>"
                        + "TH-MINTON giúp quản lý sân, khách hàng, "
                        + "lịch đặt và thanh toán trong cùng một hệ thống."
                        + "<br><br>"
                        + "Theo dõi trạng thái sân nhanh chóng và "
                        + "tìm khung giờ phù hợp thuận tiện hơn."
                        + "</html>"
                );

        aboutDescription.setForeground(
                MUTED
        );

        aboutDescription.setFont(
                AppFonts.regular(13f)
        );


        aboutText.add(aboutSmall);

        aboutText.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                8
                        )
                )
        );

        aboutText.add(aboutTitle);

        aboutText.add(
                Box.createRigidArea(
                        new Dimension(
                                0,
                                14
                        )
                )
        );

        aboutText.add(aboutDescription);


        // ------------------
        // RIGHT
        // ------------------




        aboutSection.add(
                aboutText,
                BorderLayout.CENTER
        );


        wrapper.add(aboutSection);


        // =====================================================
        // QUICK SERVICES
        // =====================================================

        JPanel services =
                new JPanel(
                        new BorderLayout()
                );

        services.setBackground(
                CONTENT_BG
        );

        services.setBorder(
                new EmptyBorder(
                        40,
                        55,
                        40,
                        55
                )
        );


        JLabel serviceTitle =
                new JLabel(
                        "Khám phá TH-MINTON"
                );

        serviceTitle.setFont(
                AppFonts.bold(24f)
        );

        serviceTitle.setForeground(
                TEXT
        );


        JPanel serviceCards =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                18,
                                0
                        )
                );

        serviceCards.setOpaque(false);

        serviceCards.setPreferredSize(
            new Dimension(
                    1000,
                    420
            )
    );

        serviceCards.setBorder(
                new EmptyBorder(
                        25,
                        0,
                        0,
                        0
                )
        );


        serviceCards.add(
            createImageCard(
                    "Có sân trên toàn Việt Nam",
                    "/images/service-1.jpg"
            )
    );

    serviceCards.add(
            createImageCard(
                    "Đặt sân nhanh chóng",
                    "/images/service-2.jpg"
            )
    );

    serviceCards.add(
            createImageCard(
                    "Tận hưởng từng trận đấu",
                    "/images/service-3.jpg"
            )
    );


        services.add(
                serviceTitle,
                BorderLayout.NORTH
        );

        services.add(
                serviceCards,
                BorderLayout.CENTER
        );


        wrapper.add(services);

        return wrapper;
    }
    private JPanel createImageCard(
            String title,
            String imagePath
    ) {

        ImageCard card =
                new ImageCard(
                        imagePath,
                        24
                );

        card.setLayout(
                new BorderLayout()
        );

        card.setBorder(
                new EmptyBorder(
                        22,
                        22,
                        22,
                        22
                )
        );

        JLabel titleLabel =
                new JLabel(
                        "<html>"
                        + title
                        + "</html>"
                );

        titleLabel.setFont(
                AppFonts.bold(22f)
        );

        titleLabel.setForeground(
                Color.WHITE
        );

        titleLabel.setVerticalAlignment(
                SwingConstants.TOP
        );

        card.add(
                titleLabel,
                BorderLayout.NORTH
        );

        return card;
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    private JButton createLogoutButton() {

        JButton logout =
                new JButton(
                        "Đăng xuất"
                );

        logout.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        46
                )
        );

        logout.setPreferredSize(
                new Dimension(
                        200,
                        46
                )
        );

        logout.setFont(
                AppFonts.medium(13f)
        );

        logout.setForeground(
                new Color(
                        221,
                        238,
                        235
                )
        );

        logout.setBackground(
                SIDEBAR_DARK
        );

        logout.setFocusPainted(
                false
        );

        logout.setBorderPainted(
                false
        );

        logout.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        logout.addActionListener(
                e -> {

                    int choice =
                            JOptionPane
                                    .showConfirmDialog(
                                            this,
                                            "Bạn có muốn đăng xuất?",
                                            "TH-MINTON",
                                            JOptionPane.YES_NO_OPTION
                                    );

                    if (
                            choice
                            == JOptionPane.YES_OPTION
                    ) {

                        dispose();

                        new AuthFrame()
                                .setVisible(true);
                    }
                }
        );

        return logout;
    }


    // ============================================================
    // NAV BUTTON
    // ============================================================

    private static class NavButton
            extends JButton {

        private boolean active;

        NavButton(String text) {

            super(text);

            setHorizontalAlignment(
                    SwingConstants.LEFT
            );

            setFont(
                    AppFonts.medium(13f)
            );

            setForeground(
                    new Color(
                            218,
                            238,
                            234
                    )
            );

            setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            46
                    )
            );

            setPreferredSize(
                    new Dimension(
                            200,
                            46
                    )
            );

            setBorder(
                    new EmptyBorder(
                            0,
                            18,
                            0,
                            12
                    )
            );

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {

                            if (!active) {

                                setForeground(
                                        Color.WHITE
                                );

                                setBackground(
                                        new Color(
                                                18,
                                                83,
                                                75
                                        )
                                );

                                setOpaque(true);
                            }
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {

                            if (!active) {

                                setForeground(
                                        new Color(
                                                218,
                                                238,
                                                234
                                        )
                                );

                                setOpaque(false);
                            }
                        }
                    }
            );
        }


        void setActive(
                boolean active
        ) {

            this.active = active;

            if (active) {

                setOpaque(true);

                setBackground(
                        PRIMARY
                );

                setForeground(
                        SIDEBAR_DARK
                );

            } else {

                setOpaque(false);

                setForeground(
                        new Color(
                                218,
                                238,
                                234
                        )
                );
            }

            repaint();
        }
    }


    // ============================================================
    // ROUNDED PANEL
    // ============================================================

    private static class RoundedPanel
            extends JPanel {

        private final int radius;
        private final Color background;

        RoundedPanel(
                int radius,
                Color background
        ) {

            this.radius = radius;
            this.background = background;

            setOpaque(false);
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // shadow
            g2.setColor(
                    new Color(
                            0,
                            40,
                            33,
                            18
                    )
            );

            g2.fillRoundRect(
                    3,
                    5,
                    getWidth() - 6,
                    getHeight() - 6,
                    radius,
                    radius
            );


            // card
            g2.setColor(
                    background
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 6,
                    getHeight() - 8,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }
    private JScrollPane createScrollPane(JPanel panel) {

    JScrollPane scrollPane =
            new JScrollPane(panel);

    scrollPane.setBorder(null);

    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );

    scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    );

    scrollPane.getVerticalScrollBar()
            .setUnitIncrement(16);

    scrollPane.getViewport()
            .setBackground(CONTENT_BG);
    styleScrollBar(scrollPane);

    return scrollPane;
    }
    private void styleScrollBar(JScrollPane scrollPane) {

        JScrollBar verticalBar =
                scrollPane.getVerticalScrollBar();

        verticalBar.setPreferredSize(
                new Dimension(10, 0)
        );

        verticalBar.setUnitIncrement(16);

        verticalBar.setUI(
                new BasicScrollBarUI() {

                    @Override
                    protected void configureScrollBarColors() {

                        thumbColor =
                                new Color(
                                        47,
                                        201,
                                        174
                                );

                        trackColor =
                                new Color(
                                        236,
                                        244,
                                        242
                                );
                    }

                    @Override
                    protected JButton createDecreaseButton(
                            int orientation
                    ) {

                        return createZeroButton();
                    }

                    @Override
                    protected JButton createIncreaseButton(
                            int orientation
                    ) {

                        return createZeroButton();
                    }

                    private JButton createZeroButton() {

                        JButton button =
                                new JButton();

                        button.setPreferredSize(
                                new Dimension(0, 0)
                        );

                        button.setMinimumSize(
                                new Dimension(0, 0)
                        );

                        button.setMaximumSize(
                                new Dimension(0, 0)
                        );

                        return button;
                    }

                    @Override
                    protected void paintThumb(
                            Graphics g,
                            JComponent c,
                            Rectangle thumbBounds
                    ) {

                        Graphics2D g2 =
                                (Graphics2D) g.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        g2.setColor(
                                new Color(
                                        47,
                                        201,
                                        174
                                )
                        );

                        g2.fillRoundRect(
                                thumbBounds.x + 2,
                                thumbBounds.y + 2,
                                thumbBounds.width - 4,
                                thumbBounds.height - 4,
                                10,
                                10
                        );

                        g2.dispose();
                    }

                    @Override
                    protected void paintTrack(
                            Graphics g,
                            JComponent c,
                            Rectangle trackBounds
                    ) {

                        Graphics2D g2 =
                                (Graphics2D) g.create();

                        g2.setColor(
                                new Color(
                                        243,
                                        248,
                                        247
                                )
                        );

                        g2.fillRect(
                                trackBounds.x,
                                trackBounds.y,
                                trackBounds.width,
                                trackBounds.height
                        );

                        g2.dispose();
                    }
                }
        );
    }
    private static class HeroPanel
        extends JPanel {

        private final Image backgroundImage;

        HeroPanel(String imagePath) {

            setOpaque(false);

            java.net.URL url =
                    MainFrame.class.getResource(
                            imagePath
                    );

            if (url != null) {

                backgroundImage =
                        new ImageIcon(url)
                                .getImage();

            } else {

                backgroundImage = null;
            }
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );


            // =========================
            // BACKGROUND IMAGE
            // =========================

            if (backgroundImage != null) {

                int panelWidth =
                        getWidth();

                int panelHeight =
                        getHeight();

                double imageRatio =
                        (double) backgroundImage.getWidth(null)
                        / backgroundImage.getHeight(null);

                double panelRatio =
                        (double) panelWidth
                        / panelHeight;


                int drawWidth;
                int drawHeight;
                int x;
                int y;


                if (panelRatio > imageRatio) {

                    drawWidth =
                            panelWidth;

                    drawHeight =
                            (int) (
                                    panelWidth
                                    / imageRatio
                            );

                    x = 0;

                    y =
                            (
                                    panelHeight
                                    - drawHeight
                            )
                            / 2;

                } else {

                    drawHeight =
                            panelHeight;

                    drawWidth =
                            (int) (
                                    panelHeight
                                    * imageRatio
                            );

                    y = 0;

                    x =
                            (
                                    panelWidth
                                    - drawWidth
                            )
                            / 2;
                }


                g2.drawImage(
                        backgroundImage,
                        x,
                        y,
                        drawWidth,
                        drawHeight,
                        this
                );

            } else {

                // Nếu chưa add ảnh thì vẫn có nền xanh
                g2.setColor(
                        new Color(
                                9,
                                73,
                                65
                        )
                );

                g2.fillRect(
                        0,
                        0,
                        getWidth(),
                        getHeight()
                );
            }


            // =========================
            // DARK GREEN OVERLAY
            // =========================

            g2.setColor(
                    new Color(
                            3,
                            45,
                            40,
                            145
                    )
            );

            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );


            // =========================
            // SUBTLE GREEN GRADIENT
            // =========================

            GradientPaint gradient =
                    new GradientPaint(
                            0,
                            0,
                            new Color(
                                    10,
                                    107,
                                    92,
                                    70
                            ),
                            getWidth(),
                            getHeight(),
                            new Color(
                                    0,
                                    25,
                                    22,
                                    80
                            )
                    );

            g2.setPaint(gradient);

            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );


            g2.dispose();
        }
    }
    private static class ImageCard
        extends JPanel {

        private final Image image;
        private final int radius;

        ImageCard(
                String imagePath,
                int radius
        ) {

            this.radius = radius;

            java.net.URL url =
                    MainFrame.class.getResource(
                            imagePath
                    );

            if (url != null) {

                image =
                        new ImageIcon(url)
                                .getImage();

            } else {

                image = null;
            }

            setOpaque(false);
        }


        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width =
                    getWidth();

            int height =
                    getHeight();


            // =========================
            // BO GÓC
            // =========================

            g2.setClip(
                    new java.awt.geom.RoundRectangle2D.Float(
                            0,
                            0,
                            width,
                            height,
                            radius,
                            radius
                    )
            );


            // =========================
            // ẢNH
            // =========================

            if (image != null) {

                int imageWidth =
                        image.getWidth(null);

                int imageHeight =
                        image.getHeight(null);

                double imageRatio =
                        (double) imageWidth
                        / imageHeight;

                double panelRatio =
                        (double) width
                        / height;

                int drawWidth;
                int drawHeight;
                int x;
                int y;


                if (panelRatio > imageRatio) {

                    drawWidth = width;

                    drawHeight =
                            (int) (
                                    width
                                    / imageRatio
                            );

                    x = 0;

                    y =
                            (height - drawHeight)
                            / 2;

                } else {

                    drawHeight = height;

                    drawWidth =
                            (int) (
                                    height
                                    * imageRatio
                            );

                    y = 0;

                    x =
                            (width - drawWidth)
                            / 2;
                }


                g2.drawImage(
                        image,
                        x,
                        y,
                        drawWidth,
                        drawHeight,
                        this
                );

            } else {

                // chưa có ảnh → placeholder xanh
                GradientPaint placeholder =
                        new GradientPaint(
                                0,
                                0,
                                new Color(
                                        31,
                                        154,
                                        135
                                ),
                                width,
                                height,
                                new Color(
                                        7,
                                        57,
                                        51
                                )
                        );

                g2.setPaint(
                        placeholder
                );

                g2.fillRect(
                        0,
                        0,
                        width,
                        height
                );
            }


            // =========================
            // OVERLAY NHẸ
            // để chữ nhìn rõ
            // =========================

            GradientPaint overlay =
                    new GradientPaint(
                            0,
                            0,
                            new Color(
                                    0,
                                    30,
                                    25,
                                    125
                            ),
                            0,
                            height,
                            new Color(
                                    0,
                                    20,
                                    18,
                                    25
                            )
                    );

            g2.setPaint(
                    overlay
            );

            g2.fillRect(
                    0,
                    0,
                    width,
                    height
            );


            g2.dispose();

            super.paintComponent(g);
        }
    }
}