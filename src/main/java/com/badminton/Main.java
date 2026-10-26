package com.badminton;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.badminton.view.MainFrame;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
            }

            // TODO N1:
            // Sau khi hoàn thiện đăng nhập,
            // đổi thành new LoginFrame().setVisible(true);

            new MainFrame().setVisible(true);
        });
    }
}