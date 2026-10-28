package com.badminton.view;

import com.badminton.ui.MintDialogs;

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
        registerScreen("Khám phá sân", new SuggestionPanel(this::openBookingCourt));
        registerScreen("Thanh toán", paymentPanel);
        

        // QUẢN TRỊ HỆ THỐNG: register only for ADMIN.
        if (admin) {
            registerScreen("Quản lý sân", new CourtPanel());
            registerScreen("Quản lý khách hàng", new CustomerPanel(user));
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

    // Header is a floating, rounded white card with a mint outline.
    // Keep the same outer margin as the sidebar so their top edges line up.
    private JPanel createHeader() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBackground(CONTENT_BG);
        outer.setBorder(new EmptyBorder(9, 9, 8, 10));
        outer.setPreferredSize(new Dimension(0, 96));

        JPanel header = new RoundedHeaderCard();
        header.setLayout(new BorderLayout(12, 0));
        header.setBorder(new EmptyBorder(0, 24, 0, 24));

        pageTitle = new JLabel("Tổng quan");
        pageTitle.setFont(AppFonts.bold(24f));
        pageTitle.setForeground(TEXT);
        header.add(pageTitle, BorderLayout.WEST);

        // The login name is displayed inside a separate rounded pill.
        String roleText = admin ? "Quản trị viên" : "Khách hàng";
        JPanel namePill = new AccountNamePill(currentUser.username() + " (" + roleText + ")");

        // Avatar uses the first letter of the actual login username.
        // E.g. im_muoi -> I. Never use the old fixed initials "TM".
        JComponent avatar = new AccountAvatar(firstLoginLetter(currentUser.username()));

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 10);
        right.add(namePill, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        right.add(avatar, gbc);
        header.add(right, BorderLayout.EAST);

        outer.add(header, BorderLayout.CENTER);
        return outer;
    }

    private static String firstLoginLetter(String username) {
        if (username != null) {
            // Usernames may begin with symbols: use the first actual letter or digit.
            int cp = username.codePoints()
                    .filter(Character::isLetterOrDigit)
                    .findFirst()
                    .orElse(-1);
            if (cp != -1) {
                return new String(Character.toChars(cp))
                        .toUpperCase(java.util.Locale.forLanguageTag("vi-VN"));
            }
        }
        return "?";
    }

    private static final class RoundedHeaderCard extends JPanel {
        RoundedHeaderCard() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (w <= 6 || h <= 6) { g2.dispose(); return; }

            // Subtle shadow, then rounded white fill and visible green border.
            g2.setColor(new Color(24, 107, 91, 20));
            g2.fillRoundRect(2, 4, w - 4, h - 5, 21, 21);
            g2.setColor(new Color(255, 255, 255));
            g2.fillRoundRect(1, 1, w - 4, h - 5, 20, 20);
            g2.setColor(new Color(44, 181, 153));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(2, 2, w - 6, h - 7, 19, 19);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class AccountNamePill extends JPanel {
        AccountNamePill(String name) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(9, 15, 9, 15));
            JLabel text = new JLabel(name);
            text.setFont(AppFonts.medium(13f));
            text.setForeground(TEXT);
            add(text, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(235, 249, 244));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(new Color(192, 229, 218));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h - 2, h - 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class AccountAvatar extends JComponent {
        private final String initial;
        AccountAvatar(String initial) {
            this.initial = initial;
            setPreferredSize(new Dimension(44, 44));
            setMinimumSize(new Dimension(44, 44));
            setMaximumSize(new Dimension(44, 44));
            setOpaque(false);
            setToolTipText("Tài khoản: " + initial);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setColor(new Color(216, 248, 238));
            g2.fillOval(x, y, size, size);
            g2.setColor(new Color(105, 213, 184));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x + 1, y + 1, size - 2, size - 2);
            g2.setFont(AppFonts.bold(17f));
            g2.setColor(new Color(9, 119, 101));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(initial)) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initial, tx, ty);
            g2.dispose();
        }
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


    /**
     * Mo man hinh Dat san voi dung san nguoi dung chon tu Kham pha san.
     * Khong sua BookingPanel hay logic dat san hien co.
     */
    public void openBookingCourt(long courtId) {
        if (courtId <= 0) return;
        bookingPanel.selectCourt(courtId);
        showScreen("Đặt sân");
    }

    // ============================================================
    // HOME / DASHBOARD
    // ============================================================

    // ============================================================
    // HOME: rounded hero, about card, six animated service tiles.
    // This section only changes the overview UI; other routes remain intact.
    // ============================================================
    private JPanel createHomePanel() {
        HomeStack wrapper = new HomeStack();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(CONTENT_BG);
        wrapper.setBorder(new EmptyBorder(18, 19, 28, 19));

        HeroPanel hero = new HeroPanel("/images/home-hero.jpg");
        hero.setLayout(new GridBagLayout());
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setPreferredSize(new Dimension(900, 355));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 355));
        hero.setMinimumSize(new Dimension(640, 355));

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("WELCOME TO TH - MINTON");
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setForeground(new Color(105, 243, 210));
        welcome.setFont(AppFonts.bold(20f));

        JLabel heroTitle = new JLabel("Your Court. Your Game. Your Moment.");
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroTitle.setForeground(Color.WHITE);
        heroTitle.setFont(AppFonts.fredoka(34f));

        JLabel heroDescription = new JLabel("<html><div style='text-align:center'>"
                + "Quản lý sân cầu lông dễ dàng, theo dõi lịch đặt và tìm sân phù hợp"
                + "<br>chỉ trong vài thao tác.</div></html>", SwingConstants.CENTER);
        heroDescription.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroDescription.setForeground(new Color(235, 251, 247));
        heroDescription.setFont(AppFonts.regular(13f));
        heroDescription.setMaximumSize(new Dimension(650, 56));

        JButton bookingButton = new HeroBookingButton("Đặt sân ngay");
        bookingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookingButton.addActionListener(e -> { bookingPanel.refreshData(); showScreen("Đặt sân"); });

        heroContent.add(welcome);
        heroContent.add(Box.createVerticalStrut(11));
        heroContent.add(heroTitle);
        heroContent.add(Box.createVerticalStrut(15));
        heroContent.add(heroDescription);
        heroContent.add(Box.createVerticalStrut(23));
        heroContent.add(bookingButton);
        hero.add(heroContent);
        wrapper.add(hero);
        wrapper.add(Box.createVerticalStrut(17));

        // ABOUT: white rounded card instead of a full-width sharp rectangle.
        RoundedHomeSurface about = new RoundedHomeSurface(Color.WHITE);
        about.setLayout(new BorderLayout());
        about.setBorder(new EmptyBorder(25, 37, 24, 35));
        about.setAlignmentX(Component.LEFT_ALIGNMENT);
        about.setPreferredSize(new Dimension(900, 188));
        about.setMaximumSize(new Dimension(Integer.MAX_VALUE, 188));

        JPanel aboutText = new JPanel();
        aboutText.setOpaque(false);
        aboutText.setLayout(new BoxLayout(aboutText, BoxLayout.Y_AXIS));
        JLabel aboutSmall = new JLabel("ABOUT US");
        aboutSmall.setFont(AppFonts.bold(12f));
        aboutSmall.setForeground(PRIMARY_DARK);
        JLabel aboutTitle = new JLabel("Quản lý sân thông minh hơn");
        // Use the same rounded Fredoka family as the TH-MINTON brand.
        Font aboutFont = AppFonts.fredoka(26f);
        if (aboutFont.canDisplayUpTo(aboutTitle.getText()) != -1) {
            aboutFont = AppFonts.bold(26f);
        }
        aboutTitle.setFont(aboutFont);
        aboutTitle.setForeground(TEXT);
        JLabel aboutDescription = new JLabel("<html>TH - MINTON giúp quản lý sân, khách hàng, lịch đặt "
                + "và thanh toán trong cùng một hệ thống.<br><br>"
                + "Theo dõi trạng thái sân nhanh chóng và tìm khung giờ phù hợp thuận tiện hơn.</html>");
        aboutDescription.setFont(AppFonts.regular(12f));
        aboutDescription.setForeground(MUTED);
        aboutText.add(aboutSmall);
        aboutText.add(Box.createVerticalStrut(7));
        aboutText.add(aboutTitle);
        aboutText.add(Box.createVerticalStrut(10));
        aboutText.add(aboutDescription);
        about.add(aboutText, BorderLayout.CENTER);
        wrapper.add(about);
        wrapper.add(Box.createVerticalStrut(17));

        // SERVICES: 2 rows of 3 compact blended cards, with hover inset.
        RoundedHomeSurface services = new RoundedHomeSurface(new Color(248, 253, 251));
        services.setLayout(new BorderLayout(0, 18));
        services.setBorder(new EmptyBorder(25, 25, 26, 25));
        services.setAlignmentX(Component.LEFT_ALIGNMENT);
        services.setPreferredSize(new Dimension(900, 468));
        services.setMaximumSize(new Dimension(Integer.MAX_VALUE, 468));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel serviceTitle = new JLabel("Khám phá TH - MINTON");
        // Fredoka gives this heading a friendlier appearance; fall back if glyphs are absent.
        Font headingFont = AppFonts.fredoka(27f);
        if (headingFont.canDisplayUpTo(serviceTitle.getText()) != -1) {
            headingFont = AppFonts.bold(27f);
        }
        serviceTitle.setFont(headingFont);
        serviceTitle.setForeground(TEXT);
        JLabel subtitle = new JLabel("Mọi tiện ích cho một buổi cầu lông trọn vẹn");
        subtitle.setFont(AppFonts.regular(12f));
        subtitle.setForeground(MUTED);
        heading.add(serviceTitle);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        services.add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 13, 13));
        cards.setOpaque(false);
        cards.add(createServiceTile("Khám phá nhiều sân", "Tìm sân phù hợp với bạn",
                "Danh sách sân", "list", new Color(11, 91, 83), new Color(17, 139, 122)));
        cards.add(createServiceTile("Đặt sân nhanh chóng", "Chọn sân và giờ mong muốn",
                "Đặt sân", "booking", new Color(13, 107, 98), new Color(20, 155, 143)));
        cards.add(createServiceTile("Tận hưởng trận đấu", "Sẵn sàng cho mỗi cuộc hẹn",
                "Khám phá sân", "explore", new Color(13, 88, 103), new Color(20, 130, 138)));
        cards.add(createServiceTile("Xem lịch dễ dàng", "Theo dõi các lịch đã đặt",
                "Lịch đặt sân", "calendar", new Color(13, 86, 91), new Color(35, 138, 132)));
        cards.add(createServiceTile("Thanh toán tiện lợi", "Kiểm tra và thanh toán nhanh",
                "Thanh toán", "payment", new Color(12, 100, 87), new Color(28, 150, 120)));
        cards.add(createServiceTile("Gợi ý sân phù hợp", "Khám phá thêm lựa chọn mới",
                "Khám phá sân", "explore", new Color(17, 90, 108), new Color(31, 143, 147)));
        services.add(cards, BorderLayout.CENTER);
        wrapper.add(services);

        return wrapper;
    }

    private JButton createServiceTile(String title, String description,
                                      String screen, String iconName,
                                      Color top, Color bottom) {
        ServiceTile tile = new ServiceTile(title, description, iconName, top, bottom);
        tile.setToolTipText("Mở trang: " + screen);
        tile.addActionListener(e -> {
            if ("Đặt sân".equals(screen)) bookingPanel.refreshData();
            if ("Thanh toán".equals(screen)) paymentPanel.refreshData();
            showScreen(screen);
        });
        return tile;
    }

    // Keep the overview full width when the window is resized; scroll vertically only.
    private static final class HomeStack extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(850, 700);
        }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int axis, int direction) {
            return 20;
        }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int axis, int direction) {
            return Math.max(100, visible.height - 60);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static final class RoundedHomeSurface extends JPanel {
        private final Color fill;
        RoundedHomeSurface(Color fill) { this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            gg.setColor(new Color(9, 70, 61, 12));
            gg.fillRoundRect(2, 3, w - 4, h - 5, 24, 24);
            gg.setColor(fill);
            gg.fillRoundRect(0, 0, w - 3, h - 5, 24, 24);
            gg.setColor(new Color(211, 232, 224));
            gg.setStroke(new BasicStroke(1f));
            gg.drawRoundRect(1, 1, w - 5, h - 7, 23, 23);
            gg.dispose();
            super.paintComponent(g);
        }
    }

    private static final class ServiceTile extends JButton {
        private boolean hovered;
        private final String title;
        private final String description;
        private final String iconName;
        private final Color start;
        private final Color end;

        ServiceTile(String title, String description, String iconName, Color start, Color end) {
            super("");
            this.title = title;
            this.description = description;
            this.iconName = iconName;
            this.start = start;
            this.end = end;
            setForeground(Color.WHITE);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleName(title);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int inset = getModel().isPressed() ? 10 : hovered ? 6 : 2;
            int x = inset;
            int y = inset;
            int w = Math.max(2, getWidth() - inset * 2);
            int h = Math.max(2, getHeight() - inset * 2 - 2);

            // The gradient and border shrink symmetrically when hovered / pressed.
            gg.setColor(new Color(10, 64, 56, hovered ? 14 : 22));
            gg.fillRoundRect(x, y + 3, w, h, 23, 23);
            gg.setClip(new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, 22, 22));
            gg.setPaint(new GradientPaint(x, y, hovered ? start.brighter() : start,
                    x + w, y + h, hovered ? end.brighter() : end));
            gg.fillRoundRect(x, y, w, h, 22, 22);
            gg.setPaint(new RadialGradientPaint(new Point(x + w * 3 / 4, y + 15),
                    Math.max(80, w * 3 / 4), new float[]{0f, 1f},
                    new Color[]{new Color(186, 255, 235, hovered ? 85 : 55),
                            new Color(186, 255, 235, 0)}));
            gg.fillRoundRect(x, y, w, h, 22, 22);
            gg.setClip(null);
            gg.setColor(new Color(207, 255, 241, hovered ? 150 : 65));
            gg.setStroke(new BasicStroke(1f));
            gg.drawRoundRect(x, y, w - 1, h - 1, 22, 22);

            // All content is painted around the ACTUAL middle of the tile.
            // This avoids the off-center icon caused by BoxLayout stretching a JComponent.
            Font titleFont = AppFonts.bold(16f);
            Font hintFont = AppFonts.regular(11f);
            int availableWidth = Math.max(70, getWidth() - 42);
            for (float size = 16f; size >= 12f; size -= 1f) {
                Font candidate = AppFonts.bold(size);
                titleFont = candidate;
                if (gg.getFontMetrics(candidate).stringWidth(title) <= availableWidth) break;
            }
            FontMetrics titleMetrics = gg.getFontMetrics(titleFont);
            FontMetrics hintMetrics = gg.getFontMetrics(hintFont);
            int blockHeight = 40 + 12 + titleMetrics.getHeight() + 6 + hintMetrics.getHeight();
            int top = (getHeight() - blockHeight) / 2;
            int centerX = getWidth() / 2;

            // Icon circle and vector icon: both centered horizontally.
            int iconX = centerX - 20;
            gg.setColor(new Color(219, 255, 244, 40));
            gg.fillOval(iconX + 1, top + 1, 38, 38);
            gg.setColor(new Color(228, 255, 247));
            gg.setStroke(new BasicStroke(1.5f));
            gg.drawOval(iconX + 1, top + 1, 38, 38);
            new MenuIcon(iconName).paintIcon(this, gg, centerX - 9, top + 10);

            int titleY = top + 40 + 12 + titleMetrics.getAscent();
            gg.setColor(Color.WHITE);
            gg.setFont(titleFont);
            gg.drawString(title, centerX - titleMetrics.stringWidth(title) / 2, titleY);

            int hintY = top + 40 + 12 + titleMetrics.getHeight() + 6 + hintMetrics.getAscent();
            gg.setFont(hintFont);
            gg.setColor(new Color(215, 249, 239));
            gg.drawString(description, centerX - hintMetrics.stringWidth(description) / 2, hintY);
            gg.dispose();
        }
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    private JButton createLogoutButton() {
        NavButton logout = new NavButton("Đăng xuất", "logout");
        logout.setPreferredSize(new Dimension(240, 48));
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        logout.addActionListener(e -> {
            int choice = MintDialogs.showConfirmDialog(this,
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

            // Draw text using FontMetrics so it is exactly centered in both axes,
            // independent of the active Swing Look & Feel / button insets.
            String label = getText();
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = (getWidth() - fm.stringWidth(label)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.setFont(getFont());
            g2.drawString(label, textX, textY);
            g2.dispose();
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
            // All hero effects are clipped, including the image/glow and court arcs.
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                    0, 0, w - 1, h - 1, 27, 27));

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