package com.badminton.ui;

import com.badminton.model.User;
import com.badminton.service.AuthService;
import com.badminton.util.AppFonts;
import com.badminton.view.RoleBasedMainFrame;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginPanel extends JPanel {

    private final JTextField username =
            new MintTextField();

    private final JPasswordField password =
            new MintPasswordField();

    public LoginPanel(
            Runnable onBack,
            Runnable onRegister
    ) {

        setOpaque(false);

        setBorder(
            new EmptyBorder(30, 34, 25, 34)
        );

        setLayout(
            new BoxLayout(this, BoxLayout.Y_AXIS)
        );

        add(title(
            "Chào mừng trở lại!",
            25,
            Color.WHITE,
            true
        ));

        add(space(7));

        add(title(
            "Đăng nhập để tiếp tục đặt sân.",
            14,
            new Color(179, 223, 217),
            false
        ));

        add(space(24));

        add(field(
            "Tên đăng nhập",
            username,
            73
        ));

        add(space(12));

        add(field(
            "Mật khẩu",
            password,
            73
        ));

        add(space(23));

        RoundedButton submit =
                new RoundedButton("Đăng nhập");

        submit.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        submit.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 54)
        );

        submit.setPreferredSize(
            new Dimension(400, 54)
        );

        // Xu ly dang nhap that
        submit.addActionListener(
            e -> loginAccount(submit)
        );

        add(submit);

        add(space(17));

        add(link(
            "Chưa có tài khoản? Đăng ký",
            onRegister
        ));

        add(space(6));

        add(link(
            "Quay lại màn hình đầu",
            onBack
        ));
    }

    // ====================================
    // DANG NHAP VAO MYSQL
    // ====================================

    private void loginAccount(
            RoundedButton submit
    ) {

        String account = username.getText();

        char[] pass = password.getPassword();

        if (account.isBlank()
                || pass.length == 0) {

            Arrays.fill(pass, '\0');

            alert(
                "Vui lòng nhập tên đăng nhập "
                + "và mật khẩu."
            );

            return;
        }

        submit.setEnabled(false);

        // Khong de truy van MySQL lam treo UI
        new SwingWorker<Optional<User>, Void>() {

            @Override
            protected Optional<User> doInBackground()
                    throws Exception {

                try {

                    return new AuthService()
                        .authenticate(
                            account,
                            pass
                        );

                } finally {
                    Arrays.fill(pass, '\0');
                }
            }

            @Override
            protected void done() {

                submit.setEnabled(true);

                try {

                    Optional<User> result = get();

                    // Sai tai khoan hoac mat khau
                    if (result.isEmpty()) {

                        alert(
                            "Sai tên đăng nhập "
                            + "hoặc mật khẩu."
                        );

                        password.setText("");

                        return;
                    }

                    // Dang nhap thanh cong
                    User user = result.get();

                    password.setText("");

                    // Chi cho ADMIN / CUSTOMER vao giao dien theo quyen.
                    // Vai tro khac (vi du STAFF) phai duoc thong nhat sau.
                    if (!"ADMIN".equalsIgnoreCase(user.role())
                            && !"CUSTOMER".equalsIgnoreCase(user.role())) {
                        alert("Tài khoản chưa được cấp quyền truy cập ứng dụng.");
                        return;
                    }

                    // Mo giao dien phu hop voi vai tro da dang nhap.
                    RoleBasedMainFrame mainFrame =
                            new RoleBasedMainFrame(user);

                    mainFrame.setTitle(
                        "TH-MINTON - "
                        + user.username()
                    );

                    mainFrame.setVisible(true);

                    // Dong cua so dang nhap
                    Window authWindow =
                        SwingUtilities.getWindowAncestor(
                            LoginPanel.this
                        );

                    if (authWindow != null) {
                        authWindow.dispose();
                    }

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    alert(
                        "Đăng nhập bị gián đoạn."
                    );

                } catch (ExecutionException ex) {

                    Throwable cause =
                            ex.getCause();

                    if (cause instanceof SQLException) {

                        alert(
                            "Không thể kết nối MySQL. "
                            + "Kiểm tra db.properties."
                        );

                    } else {

                        alert(
                            "Có lỗi khi đăng nhập. "
                            + "Kiểm tra Terminal."
                        );

                        if (cause != null) {
                            cause.printStackTrace();
                        }
                    }
                }
            }

        }.execute();
    }

    private void alert(String message) {

        JOptionPane.showMessageDialog(
            this,
            message,
            "TH-MINTON",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ====================================
    // CAC COMPONENT GIAO DIEN CO SAN
    // ====================================

    static JLabel title(
            String text,
            int size,
            Color color,
            boolean heavy
    ) {

        JLabel label = new JLabel(text);

        label.setFont(
            heavy
                ? AppFonts.bold(size)
                : AppFonts.regular(size)
        );

        label.setForeground(color);

        label.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        return label;
    }

    static Component space(int height) {

        return Box.createRigidArea(
            new Dimension(0, height)
        );
    }

    static JComponent field(
            String text,
            JComponent input,
            int height
    ) {

        JPanel box = new JPanel(
            new BorderLayout(0, 7)
        );

        box.setOpaque(false);

        box.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        JLabel label = new JLabel(text);

        label.setForeground(
            new Color(211, 242, 237)
        );

        label.setFont(
            AppFonts.medium(13f)
        );

        box.add(
            label,
            BorderLayout.NORTH
        );

        box.add(
            input,
            BorderLayout.CENTER
        );

        box.setPreferredSize(
            new Dimension(400, height)
        );

        box.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, height)
        );

        return box;
    }

    static JButton link(
            String text,
            Runnable action
    ) {

        JButton button = new JButton(text);

        button.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        button.setFont(
            AppFonts.regular(13f)
        );

        button.setForeground(
            new Color(139, 240, 211)
        );

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.setBorder(
            new EmptyBorder(2, 0, 2, 0)
        );

        button.setCursor(
            Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
            )
        );

        button.addActionListener(
            e -> action.run()
        );

        return button;
    }
}


// =========================================
// VE BACKGROUND VA VIEN CAC O NHAP
// =========================================

final class MintInputPainter {

    private MintInputPainter() {
    }

    static void background(
            Graphics g,
            int width,
            int height
    ) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(
            new Color(25, 67, 67)
        );

        g2.fillRoundRect(
            2,
            2,
            width - 5,
            height - 5,
            17,
            17
        );

        g2.dispose();
    }

    static void border(
            Graphics g,
            int width,
            int height,
            boolean focused
    ) {

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setStroke(
            new BasicStroke(1.7f)
        );

        g2.setColor(
            focused
                ? new Color(109, 246, 210)
                : new Color(106, 181, 170)
        );

        g2.drawRoundRect(
            2,
            2,
            width - 5,
            height - 5,
            17,
            17
        );

        g2.dispose();
    }
}


// =========================================
// O NHAP VAN BAN
// =========================================

class MintTextField extends JTextField {

    MintTextField() {

        setOpaque(false);

        setFont(
            AppFonts.regular(15f)
        );

        setForeground(Color.WHITE);

        setCaretColor(
            new Color(124, 251, 220)
        );

        setSelectionColor(
            new Color(38, 159, 151)
        );

        setBorder(
            new EmptyBorder(
                10, 16, 10, 16
            )
        );

        addFocusListener(
            new java.awt.event.FocusAdapter() {

                @Override
                public void focusGained(
                    java.awt.event.FocusEvent e
                ) {
                    repaint();
                }

                @Override
                public void focusLost(
                    java.awt.event.FocusEvent e
                ) {
                    repaint();
                }
            }
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        MintInputPainter.background(
            g,
            getWidth(),
            getHeight()
        );

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {

        MintInputPainter.border(
            g,
            getWidth(),
            getHeight(),
            hasFocus()
        );
    }
}


// =========================================
// O NHAP MAT KHAU
// =========================================

class MintPasswordField extends JPasswordField {

    MintPasswordField() {

        setOpaque(false);

        setFont(
            AppFonts.regular(15f)
        );

        setForeground(Color.WHITE);

        setCaretColor(
            new Color(124, 251, 220)
        );

        setSelectionColor(
            new Color(38, 159, 151)
        );

        setBorder(
            new EmptyBorder(
                10, 16, 10, 16
            )
        );

        addFocusListener(
            new java.awt.event.FocusAdapter() {

                @Override
                public void focusGained(
                    java.awt.event.FocusEvent e
                ) {
                    repaint();
                }

                @Override
                public void focusLost(
                    java.awt.event.FocusEvent e
                ) {
                    repaint();
                }
            }
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        MintInputPainter.background(
            g,
            getWidth(),
            getHeight()
        );

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {

        MintInputPainter.border(
            g,
            getWidth(),
            getHeight(),
            hasFocus()
        );
    }
}
