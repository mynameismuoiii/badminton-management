package com.badminton.view;

import com.badminton.model.User;
import com.badminton.ui.AuthFrame;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public final class RoleBasedMainFrame extends JFrame {

    // =============================
    // THONG TIN TAI KHOAN
    // =============================

    private final User currentUser;
    private final boolean admin;

    // =============================
    // CAC THANH PHAN GIAO DIEN
    // =============================

    private final JPanel content =
        new JPanel(new CardLayout());

    private final JPanel menu =
        new JPanel(
            new GridLayout(0, 1, 4, 4)
        );

    private final Map<String, JPanel> allowedScreens =
        new LinkedHashMap<>();

    // =============================
    // CONSTRUCTOR
    // =============================

    public RoleBasedMainFrame(User user) {

        super("TH-MINTON");

        currentUser = Objects.requireNonNull(
            user,
            "User must not be null"
        );

        admin = "ADMIN".equalsIgnoreCase(
            currentUser.role()
        );

        boolean customer =
            "CUSTOMER".equalsIgnoreCase(
                currentUser.role()
            );

        if (!admin && !customer) {

            throw new IllegalArgumentException(
                "Vai trò tài khoản chưa được hỗ trợ."
            );
        }

        setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        setSize(1120, 720);

        setMinimumSize(
            new Dimension(900, 600)
        );

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // =============================
        // HEADER
        // =============================

        JPanel top = new JPanel(
            new BorderLayout(12, 8)
        );

        top.setBorder(
            BorderFactory.createEmptyBorder(
                12, 16, 12, 16
            )
        );

        String roleText = admin
            ? "Quản trị viên"
            : "Khách hàng";

        JLabel welcome = new JLabel(
            "TH-MINTON  |  "
                + currentUser.username()
                + "  ("
                + roleText
                + ")",
            SwingConstants.LEFT
        );

        top.add(
            welcome,
            BorderLayout.CENTER
        );

        JButton logout =
            new JButton("Đăng xuất");

        logout.addActionListener(
            e -> logout()
        );

        top.add(
            logout,
            BorderLayout.EAST
        );

        add(
            top,
            BorderLayout.NORTH
        );

        // =============================
        // MENU BEN TRAI
        // =============================

        JPanel side = new JPanel(
            new BorderLayout()
        );

        side.setBorder(
            BorderFactory.createEmptyBorder(
                8, 8, 8, 8
            )
        );

        JLabel menuTitle = new JLabel(
            "CHỨC NĂNG",
            SwingConstants.CENTER
        );

        side.add(
            menuTitle,
            BorderLayout.NORTH
        );

        side.add(
            menu,
            BorderLayout.CENTER
        );

        JPanel sidebar = new JPanel(
            new BorderLayout()
        );

        sidebar.add(
            side,
            BorderLayout.NORTH
        );

        JScrollPane scrollMenu =
            new JScrollPane(sidebar);

        scrollMenu.setPreferredSize(
            new Dimension(205, 0)
        );

        scrollMenu.setBorder(
            BorderFactory.createEmptyBorder()
        );

        add(
            scrollMenu,
            BorderLayout.WEST
        );

        add(
            content,
            BorderLayout.CENTER
        );

        // =============================
        // KHOI TAO MAN HINH THANH TOAN
        // =============================

        PaymentPanel paymentPanel =
            new PaymentPanel(currentUser);

        // =============================
        // KHOI TAO MAN HINH DAT SAN
        // =============================

        BookingPanel bookingPanel =
            new BookingPanel(
                currentUser,

                bookingId -> {

                    // Sau khi tao don thanh cong
                    // chuyen qua man hinh thanh toan

                    paymentPanel.focusBooking(
                        bookingId
                    );

                    showScreen("Thanh toán");
                }
            );

        // =============================
        // CHUC NANG RIENG CUA ADMIN
        // =============================

        if (admin) {

            // Nguoi 2: Quan ly san
            addScreen(
                "Quản lý sân",
                new CourtPanel()
            );

            // Nguoi 1: Quan ly khach hang
            addScreen(
                "Quản lý khách hàng",
                new CustomerPanel(user)
            );

        } else {

            // =============================
            // CHUC NANG DANH SACH SAN
            // DANH CHO CUSTOMER
            // =============================

            CustomerCourtListPanel courtListPanel =
                new CustomerCourtListPanel(
                    courtId -> {

                        // 1. Truyen ID san da chon
                        // sang BookingPanel
                        bookingPanel.selectCourt(
                            courtId
                        );

                        // 2. Chuyen sang giao dien
                        // Dat san
                        showScreen("Đặt sân");
                    }
                );

            addScreen(
                "Danh sách sân",
                courtListPanel
            );
        }

        // =============================
        // CHUC NANG DAT SAN
        // =============================

        addScreen(
            "Đặt sân",
            bookingPanel
        );

        // =============================
        // CHUC NANG CUA NGUOI 2
        // =============================

        addScreen(
            "Xem lịch sân",
            new SchedulePanel()
        );

        addScreen(
            "Gợi ý sân trống",
            new SuggestionPanel()
        );

        // =============================
        // CHUC NANG THANH TOAN
        // =============================

        addScreen(
            "Thanh toán",
            paymentPanel
        );

        // =============================
        // THONG KE ADMIN
        // =============================

        if (admin) {

            addScreen(
                "Thống kê",
                new StatisticsPanel()
            );
        }

        // =============================
        // HIEN MAN HINH DAU TIEN
        // =============================

        showScreen(
            allowedScreens.keySet()
                .iterator()
                .next()
        );
    }

    // =============================
    // THEM MAN HINH VAO MENU
    // =============================

    private void addScreen(
            String name,
            JPanel panel
    ) {

        if (allowedScreens.containsKey(name)) {

            throw new IllegalArgumentException(
                "Duplicate screen: " + name
            );
        }

        allowedScreens.put(
            name,
            panel
        );

        content.add(
            panel,
            name
        );

        // Tao nut menu
        JButton button =
            new JButton(name);

        button.setHorizontalAlignment(
            SwingConstants.LEFT
        );

        button.addActionListener(e -> {

            // Khi bam menu Thanh toan
            if ("Thanh toán".equals(name)) {

                PaymentPanel payment =
                    (PaymentPanel) allowedScreens.get(name);

                payment.refreshData();
            }

            // Khi bam menu Dat san
            if ("Đặt sân".equals(name)) {

                BookingPanel booking =
                    (BookingPanel) allowedScreens.get(name);

                booking.refreshData();
            }

            // Hien man hinh duoc chon
            showScreen(name);
        });

        menu.add(button);
    }

    // =============================
    // CHUYEN DOI MAN HINH
    // =============================

    private void showScreen(String name) {

        if (!allowedScreens.containsKey(name)) {

            throw new SecurityException(
                "Không có quyền truy cập màn hình: "
                    + name
            );
        }

        CardLayout layout =
            (CardLayout) content.getLayout();

        layout.show(
            content,
            name
        );
    }

    // =============================
    // DANG XUAT
    // =============================

    private void logout() {

        dispose();

        new AuthFrame().setVisible(true);
    }
}