package com.badminton.ui;

import com.badminton.service.AuthService;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RegisterPanel extends JPanel {

    private final JTextField fullName =
            new MintTextField();

    private final JTextField username =
            new MintTextField();

    private final JTextField phone =
            new MintTextField();

    private final JPasswordField password =
            new MintPasswordField();

    private final JPasswordField confirmPassword =
            new MintPasswordField();

    public RegisterPanel(
            Runnable onBack,
            Runnable onLogin
    ) {

        setOpaque(false);

        setBorder(
            new EmptyBorder(24, 34, 20, 34)
        );

        setLayout(
            new BoxLayout(this, BoxLayout.Y_AXIS)
        );

        add(LoginPanel.title(
            "Tạo tài khoản",
            26,
            Color.WHITE,
            true
        ));

        add(LoginPanel.space(4));

        add(LoginPanel.title(
            "Tham gia TH-MINTON ngay hôm nay.",
            13,
            new Color(179, 223, 217),
            false
        ));

        add(LoginPanel.space(15));

        add(LoginPanel.field(
            "Họ và tên",
            fullName,
            67
        ));

        add(LoginPanel.space(7));

        add(LoginPanel.field(
            "Tên đăng nhập",
            username,
            67
        ));

        add(LoginPanel.space(7));

        add(LoginPanel.field(
            "Số điện thoại",
            phone,
            67
        ));

        add(LoginPanel.space(7));

        add(LoginPanel.field(
            "Mật khẩu",
            password,
            67
        ));

        add(LoginPanel.space(7));

        add(LoginPanel.field(
            "Xác nhận mật khẩu",
            confirmPassword,
            67
        ));

        add(LoginPanel.space(14));

        RoundedButton submit =
                new RoundedButton("Đăng ký");

        submit.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        submit.setPreferredSize(
            new Dimension(400, 49)
        );

        submit.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 49)
        );

        // Goi ham dang ky
        submit.addActionListener(
            e -> registerAccount(
                submit,
                onLogin
            )
        );

        add(submit);

        add(LoginPanel.space(9));

        add(LoginPanel.link(
            "Đã có tài khoản? Đăng nhập",
            onLogin
        ));

        add(LoginPanel.space(4));

        add(LoginPanel.link(
            "Quay lại màn hình đầu",
            onBack
        ));
    }

    // =====================================
    // XU LY DANG KY
    // =====================================

    private void registerAccount(
            RoundedButton submit,
            Runnable onLogin
    ) {

        String name = fullName.getText();
        String account = username.getText();
        String mobile = phone.getText();

        char[] pass = password.getPassword();
        char[] confirm =
                confirmPassword.getPassword();

        try {

            // 1. Kiem tra thong tin trong
            if (name.isBlank()
                    || account.isBlank()
                    || mobile.isBlank()
                    || pass.length == 0) {

                alert(
                    "Vui lòng nhập đầy đủ thông tin."
                );

                return;
            }

            // 2. Cho phep moi loai ky tu
            // Chi kiem tra do dai username
            if (account.codePointCount(
                    0, account.length()) > 80) {

                alert(
                    "Tên đăng nhập tối đa 80 ký tự."
                );

                return;
            }

            // 3. Kiem tra so dien thoai
            if (!mobile.matches("0[0-9]{9}")) {

                alert(
                    "Số điện thoại phải có 10 chữ số "
                    + "và bắt đầu bằng 0."
                );

                return;
            }

            // 4. Kiem tra do dai ho ten
            // Khong gioi han loai ky tu
            if (name.codePointCount(
                    0, name.length()) > 120) {

                alert(
                    "Họ và tên tối đa 120 ký tự."
                );

                return;
            }

            // 5. Kiem tra do dai mat khau
            if (pass.length < 8
                    || pass.length > 128) {

                alert(
                    "Mật khẩu phải dài 8-128 ký tự."
                );

                return;
            }

            // 6. Xac nhan mat khau
            if (!Arrays.equals(pass, confirm)) {

                alert(
                    "Hai mật khẩu không trùng nhau."
                );

                return;
            }

            // 7. Khoa nut khi luu MySQL
            submit.setEnabled(false);

            // Chay ngam de khong treo Swing
            new SwingWorker<Long, Void>() {

                @Override
                protected Long doInBackground()
                        throws Exception {

                    try {
                        return new AuthService()
                            .register(
                                name,
                                account,
                                mobile,
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

                        // Lay ket qua dang ky
                        get();

                        password.setText("");
                        confirmPassword.setText("");

                        alert(
                            "Đăng ký thành công! "
                            + "Hãy đăng nhập."
                        );

                        // Chuyen sang dang nhap
                        onLogin.run();

                    } catch (InterruptedException ex) {

                        Thread.currentThread().interrupt();

                        alert(
                            "Đăng ký bị gián đoạn."
                        );

                    } catch (ExecutionException ex) {

                        Throwable cause =
                                ex.getCause();

                        if (cause instanceof IllegalArgumentException) {

                            alert(cause.getMessage());

                        } else if (cause instanceof SQLException sqlEx) {

                            System.err.println("===== MYSQL ERROR =====");

                            System.err.println(
                                "SQL State: " + sqlEx.getSQLState()
                            );

                            System.err.println(
                                "Error Code: " + sqlEx.getErrorCode()
                            );

                            System.err.println(
                                "Message: " + sqlEx.getMessage()
                            );

                            sqlEx.printStackTrace();

                            alert(
                                "Lỗi MySQL:\n"
                                + sqlEx.getMessage()
                            );

                        } else {

                            alert(
                                "Đăng ký thất bại. "
                                + "Kiểm tra Terminal "
                                + "để biết chi tiết."
                            );

                            if (cause != null) {
                                cause.printStackTrace();
                            }
                        }
                    }
                }

            }.execute();

        } finally {

            // Xoa mang mat khau xac nhan
            Arrays.fill(confirm, '\0');

            // Xoa mat khau neu validate that bai
            if (submit.isEnabled()) {
                Arrays.fill(pass, '\0');
            }
        }
    }

    // =====================================
    // HIEN THONG BAO
    // =====================================

    private void alert(String message) {

        MintDialogs.showMessageDialog(
            this,
            message,
            "TH-MINTON",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
