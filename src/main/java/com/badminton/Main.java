package com.badminton;

import com.badminton.ui.AuthFrame;
import com.badminton.util.AppFonts;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
                );
            } catch (Exception ignored) {}

            UIManager.put("Label.font", AppFonts.regular(14f));
            UIManager.put("Button.font", AppFonts.medium(14f));
            UIManager.put("TextField.font", AppFonts.regular(14f));
            UIManager.put("PasswordField.font", AppFonts.regular(14f));

            UIManager.put(
                "OptionPane.messageFont",
                AppFonts.regular(14f)
            );

            UIManager.put(
                "OptionPane.buttonFont",
                AppFonts.medium(13f)
            );

            new AuthFrame().setVisible(true);
        });
    }
}
