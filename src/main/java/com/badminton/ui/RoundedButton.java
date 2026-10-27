package com.badminton.ui;

import com.badminton.util.AppFonts;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.Timer;

public class RoundedButton extends JButton {

    private final boolean primary;

    private float shrink = 0f;
    private float target = 0f;

    private final Timer animator;

    public RoundedButton(String text) {
        this(text, true);
    }

    public RoundedButton(String text, boolean primary) {
        super(text);

        this.primary = primary;

        setFont(AppFonts.medium(18f));
        setForeground(Color.WHITE);

        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);

        setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        setPreferredSize(new Dimension(225, 60));

        // Animation thu nho va phong to
        animator = new Timer(15, e -> {

            shrink += (target - shrink) * .32f;

            if (Math.abs(shrink - target) < .15f) {
                shrink = target;
                ((Timer) e.getSource()).stop();
            }

            repaint();
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                animateTo(5f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animateTo(0f);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                animateTo(8f);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                animateTo(
                    contains(e.getPoint()) ? 5f : 0f
                );
            }
        });
    }

    private void animateTo(float value) {

        target = value;

        if (!animator.isRunning()) {
            animator.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        int p = Math.round(shrink);

        int w = Math.max(
            1,
            getWidth() - p * 2 - 2
        );

        int h = Math.max(
            1,
            getHeight() - p * 2 - 4
        );

        Color c1 = primary
            ? new Color(60, 223, 186)
            : new Color(68, 119, 125);

        Color c2 = primary
            ? new Color(22, 155, 143)
            : new Color(38, 81, 92);

        // Bong do
        g2.setColor(new Color(0, 12, 17, 75));

        g2.fillRoundRect(
            p + 2, p + 5,
            w, h,
            32, 32
        );

        // Nen gradient
        g2.setPaint(
            new GradientPaint(
                p, p, c1,
                p + w, p + h, c2
            )
        );

        g2.fillRoundRect(
            p, p,
            w, h,
            32, 32
        );

        // Vien nut
        g2.setColor(
            new Color(224, 255, 246, 100)
        );

        g2.drawRoundRect(
            p, p,
            w, h,
            32, 32
        );

        // Ve text bang font Lexend
        g2.setFont(getFont());
        g2.setColor(getForeground());

        FontMetrics fm = g2.getFontMetrics();

        int tx = (
            getWidth() - fm.stringWidth(getText())
        ) / 2;

        int ty = (
            getHeight() - fm.getHeight()
        ) / 2 + fm.getAscent();

        g2.drawString(
            getText(),
            tx,
            ty
        );

        g2.dispose();
    }
}
