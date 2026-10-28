package com.badminton.view;

import com.badminton.util.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;

/**
 * TH-MINTON's rounded, keyboard-editable date input with a calendar popup.
 * Deliberately keeps the original Date + ChangeListener API used by the booking flow.
 * No external date picker dependency is necessary.
 */
public final class RoundedDatePicker extends JPanel {
    private static final Color INK = new Color(18, 62, 55);
    private static final Color MUTED = new Color(108, 136, 129);
    private static final Color GREEN = new Color(22, 160, 141);
    private static final Color BORDER = new Color(220, 233, 229);
    private static final Color PALE = new Color(228, 249, 243);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String[] DOW = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private final JTextField input = new JTextField();
    private final JButton calendarButton = new JButton();
    private final EventListenerList listeners = new EventListenerList();
    private LocalDate selectedDate = LocalDate.now();
    private YearMonth visibleMonth = YearMonth.now();
    private boolean invalid;

    public RoundedDatePicker() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(3, 12, 3, 5));
        setPreferredSize(new Dimension(190, 42));
        setMinimumSize(new Dimension(166, 39));
        setToolTipText("Nhập ngày dd/MM/yyyy hoặc nhấn biểu tượng lịch");

        input.setText(selectedDate.format(FORMAT));
        input.setFont(AppFonts.medium(13f));
        input.setForeground(INK);
        input.setCaretColor(GREEN);
        input.setOpaque(false);
        input.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 3));
        input.setToolTipText("Nhập theo định dạng dd/MM/yyyy");
        input.addActionListener(e -> {
            try { commitEdit(); } catch (ParseException ex) { showInvalid(); }
        });
        input.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                try { commitEdit(); } catch (ParseException ex) { showInvalid(); }
            }
        });
        add(input, BorderLayout.CENTER);

        calendarButton.setPreferredSize(new Dimension(35, 34));
        calendarButton.setToolTipText("Chọn ngày từ lịch");
        calendarButton.setBorder(BorderFactory.createEmptyBorder());
        calendarButton.setContentAreaFilled(false);
        calendarButton.setFocusPainted(false);
        calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarButton.addActionListener(e -> openCalendar());
        calendarButton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        calendarButton.setLayout(new BorderLayout());
        // Vector icon: independent of Unicode glyph availability in custom fonts.
        calendarButton.add(new IconCanvas(), BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }

    /** Match JSpinner.getValue() / setValue(Date) used by the existing code. */
    public Object getValue() {
        return Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void setValue(Object value) {
        if (!(value instanceof Date date)) throw new IllegalArgumentException("Expected Date");
        setSelectedDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public void addChangeListener(ChangeListener l) { listeners.add(ChangeListener.class, l); }
    public void removeChangeListener(ChangeListener l) { listeners.remove(ChangeListener.class, l); }

    public void commitEdit() throws ParseException {
        try {
            LocalDate parsed = LocalDate.parse(input.getText().trim(), FORMAT);
            setSelectedDate(parsed);
        } catch (DateTimeParseException ex) {
            showInvalid();
            throw new ParseException("Ngay phai co dang dd/MM/yyyy", ex.getErrorIndex());
        }
    }

    private void setSelectedDate(LocalDate date) {
        LocalDate old = selectedDate;
        selectedDate = date;
        visibleMonth = YearMonth.from(date);
        invalid = false;
        input.setForeground(INK);
        input.setText(date.format(FORMAT));
        repaint();
        if (!old.equals(date)) {
            ChangeEvent event = new ChangeEvent(this);
            for (ChangeListener l : listeners.getListeners(ChangeListener.class)) l.stateChanged(event);
        }
    }

    private void showInvalid() {
        invalid = true;
        input.setForeground(new Color(199, 72, 73));
        input.setToolTipText("Ngày không hợp lệ. Hãy nhập dd/MM/yyyy.");
        repaint();
    }

    private void openCalendar() {
        // If a valid date has been typed, use it as the initial calendar month.
        try { commitEdit(); } catch (ParseException ignored) { /* Keep last valid selection. */ }
        visibleMonth = YearMonth.from(selectedDate);
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 10, 10, 10)));
        popup.setBackground(Color.WHITE);
        CalendarMonth content = new CalendarMonth(popup);
        popup.add(content);
        popup.show(this, 0, getHeight() + 5);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
        g2.setColor(invalid ? new Color(223, 95, 95) : input.isFocusOwner() ? GREEN : BORDER);
        g2.setStroke(new BasicStroke(input.isFocusOwner() ? 1.7f : 1f));
        g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);
        g2.dispose();
        super.paintComponent(g);
    }

    private static final class IconCanvas extends JComponent {
        @Override public Dimension getPreferredSize() { return new Dimension(34, 32); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            p.setColor(PALE);
            p.fillRoundRect(cx-16, cy-15, 32, 30, 10, 10);
            p.setColor(GREEN);
            p.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            p.drawRoundRect(cx-9, cy-8, 18, 17, 4, 4);
            p.drawLine(cx-9, cy-3, cx+9, cy-3);
            p.drawLine(cx-5, cy-11, cx-5, cy-6);
            p.drawLine(cx+5, cy-11, cx+5, cy-6);
            p.fillRect(cx-5, cy+1, 3, 3);
            p.fillRect(cx+2, cy+1, 3, 3);
            p.dispose();
        }
    }

    private final class CalendarMonth extends JPanel {
        private final JPopupMenu popup;
        private final JPanel days = new JPanel(new GridLayout(0, 7, 3, 3));
        private final JLabel monthTitle = new JLabel();

        CalendarMonth(JPopupMenu popup) {
            this.popup = popup;
            setOpaque(true);
            setBackground(Color.WHITE);
            setLayout(new BorderLayout(0, 10));
            setPreferredSize(new Dimension(282, 282));

            JPanel top = new JPanel(new BorderLayout(6, 0));
            top.setOpaque(false);
            JButton back = smallButton("‹");
            JButton next = smallButton("›");
            back.addActionListener(e -> { visibleMonth = visibleMonth.minusMonths(1); rebuild(); });
            next.addActionListener(e -> { visibleMonth = visibleMonth.plusMonths(1); rebuild(); });
            monthTitle.setFont(AppFonts.bold(13f));
            monthTitle.setForeground(INK);
            monthTitle.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(back, BorderLayout.WEST);
            top.add(monthTitle, BorderLayout.CENTER);
            top.add(next, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);

            JPanel calendar = new JPanel(new BorderLayout(0, 4));
            calendar.setOpaque(false);
            JPanel weekLabels = new JPanel(new GridLayout(1, 7, 3, 3));
            weekLabels.setOpaque(false);
            for (String dow : DOW) {
                JLabel title = new JLabel(dow, SwingConstants.CENTER);
                title.setFont(AppFonts.medium(10.5f));
                title.setForeground(MUTED);
                weekLabels.add(title);
            }
            calendar.add(weekLabels, BorderLayout.NORTH);
            days.setOpaque(false);
            calendar.add(days, BorderLayout.CENTER);
            add(calendar, BorderLayout.CENTER);

            JButton today = new JButton("Hôm nay");
            today.setFont(AppFonts.medium(12f));
            today.setForeground(GREEN);
            today.setBackground(PALE);
            today.setFocusPainted(false);
            today.setBorder(new EmptyBorder(7, 10, 7, 10));
            today.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            today.addActionListener(e -> { setSelectedDate(LocalDate.now()); popup.setVisible(false); });
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            foot.setOpaque(false);
            foot.add(today);
            add(foot, BorderLayout.SOUTH);
            rebuild();
        }

        private JButton smallButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(AppFonts.bold(19f));
            btn.setForeground(GREEN);
            btn.setBackground(PALE);
            btn.setPreferredSize(new Dimension(31, 29));
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(1, 5, 1, 5));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private void rebuild() {
            monthTitle.setText("Tháng " + visibleMonth.getMonthValue() + "/" + visibleMonth.getYear());
            days.removeAll();
            int offset = visibleMonth.atDay(1).getDayOfWeek().getValue() - 1;
            int total = visibleMonth.lengthOfMonth();
            for (int i = 0; i < offset; i++) days.add(new JLabel());
            for (int day = 1; day <= total; day++) {
                LocalDate date = visibleMonth.atDay(day);
                JButton b = new JButton(String.valueOf(day));
                b.setFont(AppFonts.medium(11.5f));
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(true);
                b.setOpaque(true);
                boolean chosen = date.equals(selectedDate);
                b.setBackground(chosen ? GREEN : date.equals(LocalDate.now()) ? PALE : Color.WHITE);
                b.setForeground(chosen ? Color.WHITE : INK);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e -> { setSelectedDate(date); popup.setVisible(false); });
                days.add(b);
            }
            int filled = offset + total;
            for (int i = filled; i < 42; i++) days.add(new JLabel());
            days.revalidate();
            days.repaint();
        }
    }
}
