package com.badminton.view;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        super("Đăng nhập");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 230);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(
                new GridLayout(3, 2, 8, 8)
        );

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        JButton button = new JButton("Đăng nhập");

        form.add(new JLabel("Tài khoản"));
        form.add(username);

        form.add(new JLabel("Mật khẩu"));
        form.add(password);

        form.add(new JLabel(""));
        form.add(button);

        add(form);

        button.addActionListener(e -> {

            // TODO N1:
            // Gọi AuthService.login().
            // Đăng nhập thành công thì mở MainFrame.

            JOptionPane.showMessageDialog(
                    this,
                    "TODO N1: kết nối AuthService"
            );
        });
    }
}