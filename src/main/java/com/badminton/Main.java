
package com.badminton;

import com.badminton.view.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
                // Su dung giao dien mac dinh
            }

            new MainFrame().setVisible(true);

        });
    }
}
