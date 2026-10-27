package com.badminton.ui;

import java.awt.Dimension;
import javax.swing.JFrame;

public class AuthFrame extends JFrame {

    public AuthFrame() {

        setTitle(
            "TH-MINTON | Your Court. Your Game. Your Moment."
        );

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setContentPane(new AuthView());

        setSize(1180, 780);

        setMinimumSize(new Dimension(1000, 720));

        setLocationRelativeTo(null);
    }
}
