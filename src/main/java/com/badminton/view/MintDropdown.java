package com.badminton.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * TH-MINTON: a reusable mint-themed Swing JComboBox UI.
 *
 * Apply using MintDropdown.install(existingComboBox); AFTER any other setUI call.
 * Does not replace the combo model, selected value or action listeners.
 */
public final class MintDropdown {
    private static final Color INK = new Color(12, 65, 57);
    private static final Color ACCENT = new Color(17, 159, 133);
    private static final Color MINT = new Color(220, 250, 241);
    private static final Color LINE = new Color(204, 232, 224);
    private static final Color SHADOW = new Color(28, 81, 72, 30);

    private MintDropdown() {}

    /** True when the mint popup UI (not merely the rounded outer border) is active. */
    public static boolean isInstalled(JComboBox<?> combo) {
        return combo != null && combo.getUI() instanceof MintComboUI;
    }

    public static void install(JComboBox<?> combo) {
        if (combo == null) return;
        // addNotify may call install again; do not nest renderer wrappers or reset UI.
        if (isInstalled(combo)) return;
        // Preserve each screen's label formatting, e.g. "#2 - Nguyen Van A".
        ListCellRenderer<?> previousRenderer = combo.getRenderer();
        if (previousRenderer instanceof OptionRenderer option) {
            previousRenderer = option.original;
        }
        // Must run after the custom combo constructor's setUI(...) call.
        combo.setUI(new MintComboUI());
        combo.setOpaque(false);
        combo.setForeground(INK);
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setMaximumRowCount(7);
        combo.setRenderer(new OptionRenderer(previousRenderer));
    }

    private static final class MintComboUI extends BasicComboBoxUI {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override protected ComboPopup createPopup() {
            return new MintPopup((JComboBox) comboBox);
        }

        @Override protected JButton createArrowButton() {
            JButton arrow = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D p = (Graphics2D) g.create();
                    p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    if (getModel().isRollover() || getModel().isPressed()) {
                        p.setColor(MINT);
                        p.fillRoundRect(cx - 15, cy - 15, 30, 30, 11, 11);
                    }
                    p.setColor(isEnabled() ? ACCENT : new Color(155, 172, 167));
                    p.setStroke(new BasicStroke(2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    p.drawLine(cx - 5, cy - 2, cx, cy + 3);
                    p.drawLine(cx, cy + 3, cx + 5, cy - 2);
                    p.dispose();
                }
            };
            arrow.setPreferredSize(new Dimension(39, 38));
            arrow.setOpaque(false);
            arrow.setBorderPainted(false);
            arrow.setContentAreaFilled(false);
            arrow.setFocusPainted(false);
            arrow.setRolloverEnabled(true);
            arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return arrow;
        }

        @Override public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) {
            // Existing rounded combo/wrapper paints the background.
        }
    }

    private static final class MintPopup extends BasicComboPopup {
        @SuppressWarnings("rawtypes")
        MintPopup(JComboBox combo) {
            super(combo);
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            setBorder(new EmptyBorder(7, 7, 8, 7));
            setLightWeightPopupEnabled(true);
            list.setOpaque(false);
            list.setBackground(Color.WHITE);
            list.setSelectionBackground(MINT);
            list.setSelectionForeground(INK);
            list.setBorder(null);
            list.setFixedCellHeight(-1);
            list.setCellRenderer(new OptionRenderer(combo.getRenderer()));
            scroller.setBorder(null);
            scroller.setOpaque(false);
            scroller.getViewport().setOpaque(false);
            scroller.getViewport().setBackground(Color.WHITE);
            scroller.getVerticalScrollBar().setUnitIncrement(12);
            scroller.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override protected void configureScrollBarColors() {
                    thumbColor = new Color(192, 224, 214);
                    trackColor = Color.WHITE;
                }
                @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
                @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
                private JButton zeroButton() {
                    JButton button = new JButton();
                    button.setPreferredSize(new Dimension(0, 0));
                    button.setMinimumSize(new Dimension(0, 0));
                    return button;
                }
                @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                    if (r.isEmpty()) return;
                    Graphics2D p = (Graphics2D)g.create();
                    p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    p.setColor(thumbColor);
                    p.fillRoundRect(r.x + 3, r.y + 3, Math.max(2, r.width - 6), Math.max(4, r.height - 6), 8, 8);
                    p.dispose();
                }
            });
            scroller.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            p.setColor(SHADOW);
            p.fillRoundRect(4, 5, Math.max(1, w - 7), Math.max(1, h - 7), 20, 20);
            p.setColor(Color.WHITE);
            p.fillRoundRect(2, 1, Math.max(1, w - 6), Math.max(1, h - 6), 18, 18);
            p.setColor(LINE);
            p.setStroke(new BasicStroke(1.1f));
            p.drawRoundRect(2, 1, Math.max(1, w - 7), Math.max(1, h - 7), 18, 18);
            p.dispose();
            super.paintComponent(g);
        }

        @Override public void show() {
            // Keep Swing's native keyboard navigation and selection semantics.
            super.show();
            repaint();
        }
    }

    private static final class OptionRenderer extends JLabel implements ListCellRenderer<Object> {
        private boolean highlight;
        private boolean visibleRow;
        private final ListCellRenderer<?> original;

        OptionRenderer(ListCellRenderer<?> original) {
            this.original = original;
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                 int index, boolean selected, boolean focus) {
            String display = value == null ? "" : value.toString();
            if (original != null && original != this) {
                @SuppressWarnings("unchecked") ListCellRenderer<Object> delegate =
                        (ListCellRenderer<Object>) original;
                Component formatted = delegate.getListCellRendererComponent(
                        (JList<Object>) list, value, index, selected, focus);
                if (formatted instanceof JLabel label) display = label.getText();
            }
            setText(display);
            setFont(list.getFont() == null ? new Font("SansSerif", Font.PLAIN, 13) : list.getFont());
            setForeground(INK);
            highlight = index >= 0 && selected;
            visibleRow = index >= 0;
            if (visibleRow) {
                setBorder(new EmptyBorder(9, 14, 9, 12));
                setPreferredSize(null);
            } else {
                setBorder(new EmptyBorder(2, 5, 2, 0));
            }
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            if (visibleRow && highlight) {
                Graphics2D p = (Graphics2D) g.create();
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                p.setColor(MINT);
                p.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 12, 12);
                p.setColor(ACCENT);
                p.fillRoundRect(4, 7, 3, getHeight() - 14, 3, 3);
                p.dispose();
            }
            super.paintComponent(g);
        }
    }
}
