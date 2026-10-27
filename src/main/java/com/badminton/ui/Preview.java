
package com.badminton.ui;

import javax.swing.*;
import java.awt.*;

public class Preview {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // Tao cua so xem truoc
            JFrame frame = new JFrame("TH-MINTON | UI Preview");

            frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
            );

            // Hien giao dien TH-MINTON
            frame.setContentPane(new AuthView());

            frame.setSize(1180, 780);
            frame.setMinimumSize(new Dimension(1000, 720));

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
