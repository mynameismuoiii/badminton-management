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
import java.util.Set;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MainFrame extends JFrame {

    // =========================
    // COLORS
    // =========================

    // Same palette as AuthView's welcoming / loading background.
    private static final Color SPLASH_START = new Color(7, 39, 37);
    private static final Color SPLASH_END = new Color(10, 86, 76);

    private static final Color SIDEBAR = SPLASH_START;
    private static final Color SIDEBAR_DARK = SPLASH_START;
    private static final Color PRIMARY = new Color(72, 222, 190);
    private static final Color PRIMARY_DARK = new Color(22, 147, 151);
    private static final Color CONTENT_BG = new Color(240, 248, 245);
    private static final Color TEXT = new Color(9, 52, 48);
    private static final Color MUTED = new Color(105, 135, 130);


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

    // Screens in this group are never registered or opened for CUSTOMER.
    // This is a Swing-side access check, not a replacement for DB/server authorization.
    private static final Set<String> ADMIN_SCREENS = Set.of(
            "Quản lý sân", "Quản lý khách hàng", "Thống kê & báo cáo"
    );
    private BookingPanel bookingPanel;
    private PaymentPanel paymentPanel;

    public MainFrame(User user) {

        super("TH - MINTON");
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
        // SCREENS - two menu groups, with an independent read-only court list.
        // =========================

        paymentPanel = new PaymentPanel(currentUser);
        bookingPanel = new BookingPanel(currentUser, bookingId -> {
            paymentPanel.focusBooking(bookingId);
            showScreen("Thanh toán");
        });

        // KHÁM PHÁ & ĐẶT SÂN: visible to ADMIN and CUSTOMER.
        registerScreen("Tổng quan", createHomePanel());
        registerScreen("Danh sách sân", new CustomerCourtListPanel(courtId -> {
            bookingPanel.selectCourt(courtId);
            showScreen("Đặt sân");
        }));
        registerScreen("Đặt sân", bookingPanel);
        registerScreen("Lịch đặt sân", new SchedulePanel());
        registerScreen("Khám phá sân", new SuggestionPanel());
        registerScreen("Thanh toán", paymentPanel);

        // QUẢN TRỊ HỆ THỐNG: register only for ADMIN.
        if (admin) {
            registerScreen("Quản lý sân", new CourtPanel());
            registerScreen("Quản lý khách hàng", new CustomerPanel());
            registerScreen("Thống kê & báo cáo", new StatisticsPanel());
        }

        addMenuHeading("KHÁM PHÁ & ĐẶT SÂN", false);
        for (String name : new String[]{
                "Tổng quan", "Danh sách sân", "Đặt sân", "Lịch đặt sân",
                "Khám phá sân", "Thanh toán"
        }) {
            addMenuButton(name);
        }

        if (admin) {
            addMenuHeading("QUẢN TRỊ HỆ THỐNG", true);
            for (String name : new String[]{
                    "Quản lý sân", "Quản lý khách hàng", "Thống kê & báo cáo"
            }) {
                addMenuButton(name);
            }
        }

        showScreen("Tổng quan");
    }


    // ============================================================
    // SIDEBAR
    // ============================================================

    private JPanel createSidebar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(CONTENT_BG);
        outer.setBorder(new EmptyBorder(9, 10, 9, 8));
        outer.setPreferredSize(new Dimension(300, 0));

        JPanel card = new SidebarCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(4, 0, 4, 0));
        outer.add(card, BorderLayout.CENTER);

        JPanel brand = new JPanel(new GridBagLayout());
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(23, 10, 19, 10));
        JLabel logo = new JLabel("TH - MINTON", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(AppFonts.fredoka(25f));
        brand.add(logo);
        card.add(brand, BorderLayout.NORTH);

        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(0, 12, 10, 12));

        JScrollPane menuScroll = new JScrollPane(menuPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setBorder(null);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.getVerticalScrollBar().setUnitIncrement(14);
        menuScroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        menuScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(72, 169, 151);
                trackColor = SIDEBAR;
            }
            @Override protected JButton createDecreaseButton(int orientation) {
                return zeroButton();
            }
            @Override protected JButton createIncreaseButton(int orientation) {
                return zeroButton();
            }
            private JButton zeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        card.add(menuScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 11));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 12, 15, 12));
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(82, 151, 138));
        separator.setBackground(new Color(82, 151, 138));
        footer.add(separator, BorderLayout.NORTH);
        footer.add(createLogoutButton(), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return outer;
    }

    private void addMenuHeading(String title, boolean withDivider) {
        if (withDivider) {
            menuPanel.add(Box.createVerticalStrut(9));
            JSeparator separator = new JSeparator();
            separator.setForeground(new Color(82, 151, 138));
            separator.setBackground(new Color(82, 151, 138));
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuPanel.add(separator);
            menuPanel.add(Box.createVerticalStrut(15));
        } else {
            menuPanel.add(Box.createVerticalStrut(4));
        }

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.setBorder(new EmptyBorder(0, 10, 7, 0));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        heading.setPreferredSize(new Dimension(240, 30));
        JLabel text = new JLabel(title);
        text.setForeground(new Color(165, 203, 193));
        text.setFont(AppFonts.medium(11.5f));
        heading.add(text, BorderLayout.WEST);
        menuPanel.add(heading);
    }

    // Match AuthView: deep forest green, soft mint glow, rounded shell.
    private static class SidebarCard extends JPanel {
        SidebarCard() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                    0, 0, w, h, 20, 20));
            g2.setPaint(new GradientPaint(0, 0, SPLASH_START,
                    w * 0.70f, h, SPLASH_END));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new RadialGradientPaint(
                    new Point(w / 2, h / 3), Math.max(240, h / 2),
                    new float[]{0f, 1f},
                    new Color[]{new Color(78, 216, 190, 37),
                            new Color(78, 216, 190, 0)}));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
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
                new Color(250, 253, 251)
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
                new Color(217, 249, 239)
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

    private void addMenuButton(String name) {
        if (!screens.containsKey(name)) {
            throw new IllegalArgumentException("Menu without screen: " + name);
        }
        NavButton button = new NavButton(name, iconForScreen(name));
        button.addActionListener(e -> {
            if ("Đặt sân".equals(name)) {
                bookingPanel.refreshData();
            } else if ("Thanh toán".equals(name)) {
                paymentPanel.refreshData();
            }
            showScreen(name);
        });
        navButtons.put(name, button);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuPanel.add(button);
        menuPanel.add(Box.createVerticalStrut(5));
    }

    private static String iconForScreen(String name) {
        return switch (name) {
            case "Tổng quan" -> "overview";
            case "Danh sách sân" -> "list";
            case "Đặt sân" -> "booking";
            case "Lịch đặt sân" -> "calendar";
            case "Khám phá sân" -> "explore";
            case "Thanh toán" -> "payment";
            case "Quản lý sân" -> "court";
            case "Quản lý khách hàng" -> "users";
            case "Thống kê & báo cáo" -> "stats";
            default -> "overview";
        };
    }

    // ============================================================
    // REGISTER SCREEN
    // ============================================================

    public void registerScreen(
        String name,
        JPanel panel
    ) {

        if (!admin && ADMIN_SCREENS.contains(name)) {
            throw new SecurityException("CUSTOMER không được đăng ký: " + name);
        }

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

        // Validate access on navigation, even when called directly in code.
        if (!admin && ADMIN_SCREENS.contains(name)) {
            throw new SecurityException("Bạn không có quyền truy cập: " + name);
        }

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
                        "WELCOME TO TH - MINTON"
                );

        smallTitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        smallTitle.setForeground(new Color(105, 243, 210));

        // Bold that remains visible even with variable font files.
        smallTitle.setFont(
                AppFonts.bold(22f).deriveFont(Font.BOLD, 22f)
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

        heroDescription.setForeground(new Color(232, 249, 243));

        heroDescription.setFont(
                AppFonts.regular(14f)
        );

        heroDescription.setMaximumSize(
                new Dimension(
                        600,
                        60
                )
        );


        // Button with rounded corners and a gentle inset hover effect.
        JButton bookingButton = new HeroBookingButton("Đặt sân ngay");
        bookingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookingButton.addActionListener(e -> showScreen("Đặt sân"));

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
                        + "TH - MINTON giúp quản lý sân, khách hàng, "
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
                        "Khám phá TH - MINTON"
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
        NavButton logout = new NavButton("Đăng xuất", "logout");
        logout.setPreferredSize(new Dimension(240, 48));
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn đăng xuất?", "TH - MINTON",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new AuthFrame().setVisible(true);
            }
        });
        return logout;
    }

    // ============================================================
    // HOME HERO BOOKING BUTTON
    // ============================================================

    private static class HeroBookingButton extends JButton {
        private boolean hovered;

        HeroBookingButton(String text) {
            super(text);

            setFont(AppFonts.bold(15f).deriveFont(Font.BOLD, 15f));
            setForeground(SIDEBAR_DARK);
            setHorizontalAlignment(SwingConstants.CENTER);
            setPreferredSize(new Dimension(174, 52));
            setMaximumSize(new Dimension(174, 52));
            setMinimumSize(new Dimension(174, 52));

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // When the pointer enters, the shape moves inwards a little
            // without moving the layout or changing the click target.
            boolean pressed = getModel().isPressed();
            int insetX = pressed ? 8 : hovered ? 6 : 2;
            int insetY = pressed ? 7 : hovered ? 5 : 2;
            int width = Math.max(1, getWidth() - insetX * 2);
            int height = Math.max(1, getHeight() - insetY * 2 - 2);
            int radius = hovered ? 26 : 20;

            g2.setColor(new Color(0, 37, 31, hovered ? 36 : 54));
            g2.fillRoundRect(insetX, insetY + 2, width, height,
                    radius, radius);

            g2.setColor(pressed ? PRIMARY_DARK
                    : hovered ? new Color(112, 237, 203) : PRIMARY);
            g2.fillRoundRect(insetX, insetY, width, height,
                    radius, radius);

            if (hovered) {
                g2.setColor(new Color(183, 255, 239, 175));
                g2.drawRoundRect(insetX, insetY, width - 1, height - 1,
                        radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }


    // ============================================================
    // NAV BUTTON
    // ============================================================

    private static class NavButton extends JButton {
        private boolean active;
        private boolean hovered;

        NavButton(String text, String iconName) {
            super(text);
            setIcon(new MenuIcon(iconName));
            setIconTextGap(13);
            setHorizontalAlignment(SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setFont(AppFonts.bold(15f));
            setForeground(new Color(224, 242, 237));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));
            setPreferredSize(new Dimension(240, 47));
            setMinimumSize(new Dimension(0, 47));
            setBorder(new EmptyBorder(0, 13, 0, 6));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    refreshAppearance();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hovered = false;
                    refreshAppearance();
                }
            });
        }

        void setActive(boolean value) {
            active = value;
            refreshAppearance();
        }

        private void refreshAppearance() {
            setFont(AppFonts.bold(hovered ? 15.7f : 15f));
            setForeground(active || hovered ? SIDEBAR_DARK
                    : new Color(224, 242, 237));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (active || hovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? new Color(112, 237, 203) : PRIMARY);
                int inset = hovered ? 3 : 1;
                g2.fillRoundRect(inset, 1, getWidth() - inset * 2,
                        getHeight() - 2, 14, 14);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // Small vector icons avoid dependency on platform fonts and extra libraries.
    private static class MenuIcon implements Icon {
        private final String type;
        MenuIcon(String type) { this.type = type; }
        @Override public int getIconWidth() { return 19; }
        @Override public int getIconHeight() { return 19; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D p = (Graphics2D) g.create();
            p.translate(x, y);
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            p.setColor(c.getForeground());
            p.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            switch (type) {
                case "overview" -> {
                    p.drawRoundRect(2, 2, 6, 6, 1, 1);
                    p.drawRoundRect(11, 2, 6, 6, 1, 1);
                    p.drawRoundRect(2, 11, 6, 6, 1, 1);
                    p.drawRoundRect(11, 11, 6, 6, 1, 1);
                }
                case "list" -> {
                    p.drawRoundRect(2, 2, 15, 15, 3, 3);
                    for (int y0 : new int[]{6, 10, 14}) {
                        p.fillOval(5, y0 - 1, 2, 2);
                        p.drawLine(9, y0, 14, y0);
                    }
                }
                case "booking" -> {
                    p.drawRoundRect(2, 4, 15, 13, 2, 2);
                    p.drawLine(2, 8, 17, 8);
                    p.drawLine(6, 2, 6, 6);
                    p.drawLine(13, 2, 13, 6);
                    p.drawLine(6, 12, 9, 15);
                    p.drawLine(9, 15, 14, 10);
                }
                case "calendar" -> {
                    p.drawRoundRect(2, 4, 15, 13, 2, 2);
                    p.drawLine(2, 8, 17, 8);
                    p.drawLine(6, 2, 6, 6);
                    p.drawLine(13, 2, 13, 6);
                    for (int x0 : new int[]{6, 10, 14}) {
                        p.fillOval(x0 - 1, 10, 2, 2);
                        p.fillOval(x0 - 1, 14, 2, 2);
                    }
                }
                case "explore" -> {
                    p.drawOval(2, 2, 15, 15);
                    p.drawLine(6, 13, 12, 6);
                    p.drawLine(12, 6, 10, 12);
                    p.drawLine(10, 12, 6, 13);
                }
                case "payment" -> {
                    p.drawRoundRect(2, 5, 15, 11, 2, 2);
                    p.drawLine(2, 8, 17, 8);
                    p.drawRoundRect(11, 10, 6, 4, 2, 2);
                }
                case "court" -> {
                    p.drawRoundRect(2, 4, 15, 13, 2, 2);
                    p.drawLine(4, 4, 9, 2);
                    p.drawLine(9, 2, 15, 4);
                    for (int y0 : new int[]{8, 11, 14}) p.drawLine(6, y0, 14, y0);
                }
                case "users" -> {
                    p.drawOval(4, 3, 5, 5);
                    p.drawOval(12, 5, 4, 4);
                    p.drawArc(2, 9, 10, 8, 0, 180);
                    p.drawArc(10, 10, 7, 7, 0, 180);
                }
                case "stats" -> {
                    p.drawLine(2, 17, 17, 17);
                    p.drawRect(4, 11, 2, 6);
                    p.drawRect(9, 7, 2, 10);
                    p.drawRect(14, 3, 2, 14);
                }
                case "logout" -> {
                    p.drawLine(11, 3, 4, 3);
                    p.drawLine(4, 3, 4, 16);
                    p.drawLine(4, 16, 11, 16);
                    p.drawLine(9, 9, 17, 9);
                    p.drawLine(14, 6, 17, 9);
                    p.drawLine(14, 12, 17, 9);
                }
                default -> p.drawOval(3, 3, 12, 12);
            }
            p.dispose();
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
                                PRIMARY;

                        trackColor =
                                CONTENT_BG;
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
                                PRIMARY
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
                                CONTENT_BG
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
    private static class HeroPanel extends JPanel {
        // Image resource remains optional; a light texture can be drawn below
        // the glow, but the banner palette always matches AuthView.
        private final Image backgroundImage;

        HeroPanel(String imagePath) {
            setOpaque(false);
            java.net.URL url = MainFrame.class.getResource(imagePath);
            backgroundImage = url == null ? null : new ImageIcon(url).getImage();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            // Exact gradient endpoints from the splash/login screen.
            g2.setPaint(new GradientPaint(0, 0, SPLASH_START,
                    w, h, SPLASH_END));
            g2.fillRect(0, 0, w, h);

            // Keep the existing banner image as a barely visible texture.
            if (backgroundImage != null && backgroundImage.getWidth(this) > 0
                    && backgroundImage.getHeight(this) > 0) {
                double scale = Math.max((double) w / backgroundImage.getWidth(this),
                        (double) h / backgroundImage.getHeight(this));
                int dw = (int) Math.ceil(backgroundImage.getWidth(this) * scale);
                int dh = (int) Math.ceil(backgroundImage.getHeight(this) * scale);
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.08f));
                g2.drawImage(backgroundImage, (w - dw) / 2,
                        (h - dh) / 2, dw, dh, this);
                g2.setComposite(AlphaComposite.SrcOver);
            }

            // Soft radial light at the center, as in AuthView.
            g2.setPaint(new RadialGradientPaint(
                    new Point(w / 2, h / 3), Math.max(320, w / 3),
                    new float[]{0f, 1f},
                    new Color[]{new Color(78, 216, 190, 90),
                            new Color(78, 216, 190, 0)}));
            g2.fillRect(0, 0, w, h);

            // The same delicate court arcs as on the initial welcome screen.
            g2.setColor(new Color(217, 255, 243, 33));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g2.drawArc(-210, 20, 660, 570, 18, 110);
            g2.drawArc(w - 500, -150, 660, 570, 65, 110);

            g2.setColor(new Color(146, 243, 212, 16));
            g2.fillOval(-110, h - 105, w + 220, 240);
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