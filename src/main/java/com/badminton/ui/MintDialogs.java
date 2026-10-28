package com.badminton.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * TH-MINTON notification dialogs. Only the UI is changed; JOptionPane result
 * values (YES/NO, OK/CANCEL, CLOSED) are retained for existing business logic.
 *
 * This class provides drop-in replacements for JOptionPane.showMessageDialog
 * and showConfirmDialog used by the current project.
 */
public final class MintDialogs {
    private MintDialogs() { }

    private static final Color DEEP = new Color(6, 57, 51);
    private static final Color MINT = new Color(9, 139, 112);
    private static final Color MINT_DARK = new Color(6, 108, 86);
    private static final Color PALE = new Color(237, 248, 245);
    private static final Color INK = new Color(23, 44, 48);
    private static final Color MUTED = new Color(103, 118, 124);
    private static final Color LIGHT = new Color(243, 246, 247);
    private static final Color RED = new Color(217, 55, 72);
    private static final Color AMBER = new Color(205, 133, 12);
    private static final Color BLUE = new Color(41, 116, 199);

    private enum Kind { BOOKING, SUCCESS, INFO, WARNING, ERROR, QUESTION, DANGER }

    public static void showMessageDialog(Component parent, Object message) {
        showMessageDialog(parent, message, "TH-MINTON", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int type) {
        show(parent, message, title, type, JOptionPane.DEFAULT_OPTION, false);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, Object message, String title,
                                        int optionType, int type) {
        return show(parent, message, title, type, optionType, true);
    }

    private static int show(Component parent, Object message, String title, int type,
                            int options, boolean confirmation) {
        if (!SwingUtilities.isEventDispatchThread()) {
            final int[] result = { JOptionPane.CLOSED_OPTION };
            try {
                SwingUtilities.invokeAndWait(() -> result[0] = showOnEdt(parent, message, title, type, options, confirmation));
                return result[0];
            } catch (Exception ex) {
                throw new IllegalStateException("Không thể mở hộp thoại TH-MINTON", ex);
            }
        }
        return showOnEdt(parent, message, title, type, options, confirmation);
    }

    private static int showOnEdt(Component parent, Object message, String title, int type,
                                 int options, boolean confirmation) {
        String text = message instanceof String s ? s : "";
        Kind kind = detectKind(text, title, type, confirmation);
        DialogWindow dialog = new DialogWindow(parent, message, text, title, kind, options, confirmation);
        dialog.setVisible(true);
        return dialog.result;
    }

    private static Kind detectKind(String message, String title, int type, boolean confirm) {
        String value = ((title == null ? "" : title) + " " + message).toLowerCase(Locale.ROOT);
        if (confirm) {
            if (value.contains("xác nhận đặt sân") && value.contains("tổng tiền:")) return Kind.BOOKING;
            if (value.contains("xóa") || value.contains("hủy đơn") || value.contains("ngừng hoạt động")) return Kind.DANGER;
            if (value.contains("đăng xuất")) return Kind.QUESTION;
            if (type == JOptionPane.WARNING_MESSAGE) return Kind.WARNING;
            return Kind.QUESTION;
        }
        if (type == JOptionPane.ERROR_MESSAGE) return Kind.ERROR;
        if (value.contains("đăng nhập thất bại") || value.contains("đăng ký thất bại") ||
                value.contains("sai mật khẩu") || value.contains("sai tên đăng nhập") ||
                value.contains("sai tài khoản") || value.contains("không kết nối") ||
                value.contains("lỗi hệ thống") || value.contains("lỗi mysql") ||
                value.contains("có lỗi khi") || value.contains("không thực hiện được") ||
                value.contains("lỗi khi") || value.contains("bị gián đoạn")) return Kind.ERROR;
        if (value.contains("thành công") || value.contains("đã hủy đơn") ||
                value.contains("đã ngừng hoạt động") || value.contains("đã sao chép") ||
                value.contains("đã xác nhận thanh toán") || value.contains("đã thêm khách") ||
                value.contains("đã cập nhật khách") || value.contains("đã xóa khách") || value.contains("đã xác nhận tiền") ||
                value.contains("thanh toán thành công")) return Kind.SUCCESS;
        if (type == JOptionPane.WARNING_MESSAGE || value.contains("vui lòng") ||
                value.contains("hãy chọn") || value.contains("chưa chọn") ||
                value.contains("không hợp lệ") || value.contains("không thể") ||
                value.contains("đã được đặt") || value.contains("chỉ được") ||
                value.contains("không còn hoạt động") || value.contains("thiếu") || value.contains("không được phép") ||
                value.contains("không trùng") || value.contains("tối đa") ||
                value.contains("phải có") || value.contains("phải dài") ||
                value.contains("đã hủy") || value.contains("đã qua") ||
                value.contains("chưa được cấp quyền")) return Kind.WARNING;
        return Kind.INFO;
    }

    private static final Font LEXEND_REGULAR = loadFont("/fonts/static/Lexend-Regular.ttf", Font.PLAIN);
    private static final Font LEXEND_BOLD = loadFont("/fonts/static/Lexend-SemiBold.ttf", Font.BOLD);

    private static Font loadFont(String resource, int style) {
        try (InputStream input = MintDialogs.class.getResourceAsStream(resource)) {
            if (input != null) return Font.createFont(Font.TRUETYPE_FONT, input);
        } catch (Exception ignored) { /* Font fallback is safe. */ }
        return new Font("SansSerif", style, 14);
    }

    private static Font font(int size, boolean bold) {
        return (bold ? LEXEND_BOLD : LEXEND_REGULAR).deriveFont((float) size);
    }

    private static JLabel label(String text, int size, boolean bold, Color fg) {
        JLabel result = new JLabel(text);
        result.setFont(font(size, bold));
        result.setForeground(fg);
        return result;
    }

    private static JPanel clearPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    private static class DialogWindow extends JDialog {
        private int result = JOptionPane.CLOSED_OPTION;

        DialogWindow(Component parent, Object message, String text, String givenTitle, Kind kind,
                     int options, boolean confirm) {
            super(SwingUtilities.getWindowAncestor(parent), Dialog.ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setBackground(Color.WHITE); // Opaque window avoids black transparency artifacts on Windows/Linux.

            final boolean booking = kind == Kind.BOOKING;
            final boolean customComponent = !(message instanceof String) && message instanceof Component;
            boolean imagePreview = message instanceof JLabel imageLabel && imageLabel.getIcon() != null;
            int dialogWidth = booking ? 560 : imagePreview ? 605 : customComponent ? 565 : 510;
            JPanel outer = clearPanel(new BorderLayout());
            outer.setOpaque(true);
            outer.setBackground(Color.WHITE);
            outer.setBorder(new EmptyBorder(1, 1, 1, 1));
            RoundedSurface surface = new RoundedSurface();
            surface.setLayout(new BorderLayout());
            surface.setBorder(new EmptyBorder(19, 25, 23, 25));
            outer.add(surface, BorderLayout.CENTER);
            setContentPane(outer);

            JPanel top = clearPanel(new BorderLayout());
            JLabel brand = label("TH-MINTON   /   " + shortHeader(givenTitle, kind), 12, true, DEEP);
            top.add(brand, BorderLayout.WEST);
            JButton close = new JButton("×");
            close.setForeground(MUTED);
            close.setFont(font(23, false));
            close.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 2));
            close.setOpaque(false); close.setContentAreaFilled(false);
            close.setFocusPainted(false); close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            close.addActionListener(e -> dismiss(JOptionPane.CLOSED_OPTION));
            top.add(close, BorderLayout.EAST);
            surface.add(top, BorderLayout.NORTH);

            JPanel contents = clearPanel(new BorderLayout(0, 13));
            contents.setBorder(new EmptyBorder(17, 0, 0, 0));
            JPanel lead = createLead(kind, customComponent, text, givenTitle);
            contents.add(lead, BorderLayout.NORTH);

            JComponent body;
            if (booking) body = bookingSummary(text);
            else if (customComponent) body = componentBody((Component) message);
            else body = messageBody(text, kind);
            contents.add(body, BorderLayout.CENTER);
            surface.add(contents, BorderLayout.CENTER);

            JPanel actions = clearPanel(new FlowLayout(FlowLayout.RIGHT, 11, 0));
            actions.setBorder(new EmptyBorder(21, 0, 0, 0));
            JButton defaultButton;
            if (confirm) {
                String context = ((givenTitle == null ? "" : givenTitle) + " " + text)
                        .toLowerCase(Locale.ROOT);
                String noText = options == JOptionPane.OK_CANCEL_OPTION ? "Hủy" : "Ở lại";
                if (kind == Kind.BOOKING || kind == Kind.DANGER) noText = "Quay lại";
                else if (context.contains("đăng xuất")) noText = "Ở lại";
                else if (context.contains("chuyển khoản")) noText = "Để sau";
                JButton cancel = new PillButton(noText, false, MINT);
                cancel.addActionListener(e -> dismiss(options == JOptionPane.OK_CANCEL_OPTION
                        ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION));
                actions.add(cancel);
                String yesText;
                if (kind == Kind.BOOKING) yesText = "Đặt sân và thanh toán";
                else if (context.contains("đăng xuất")) yesText = "Đăng xuất";
                else if (kind == Kind.DANGER) {
                    yesText = context.contains("xóa khách") ? "Xóa khách hàng"
                            : context.contains("hủy đơn") ? "Hủy đơn"
                            : context.contains("ngừng hoạt động") ? "Ngừng hoạt động"
                            : "Xác nhận thay đổi";
                } else if (options == JOptionPane.OK_CANCEL_OPTION) yesText = "Lưu thông tin";
                else if (context.contains("admin xác nhận tiền")) yesText = "Đã nhận tiền";
                else if (context.contains("đã chuyển khoản")) yesText = "Tôi đã chuyển khoản";
                else yesText = "Xác nhận";
                JButton approve = new PillButton(yesText, true, kind == Kind.DANGER || context.contains("đăng xuất") ? RED : MINT);
                approve.addActionListener(e -> dismiss(options == JOptionPane.OK_CANCEL_OPTION
                        ? JOptionPane.OK_OPTION : JOptionPane.YES_OPTION));
                actions.add(approve);
                defaultButton = kind == Kind.DANGER ? cancel : approve;
            } else {
                String buttonText = kind == Kind.SUCCESS && text.contains("Đặt sân thành công")
                        ? "Tiếp tục thanh toán" : kind == Kind.ERROR ? "Đã hiểu" : "Đóng";
                JButton ok = new PillButton(buttonText, true, MINT);
                ok.addActionListener(e -> dismiss(JOptionPane.OK_OPTION));
                actions.add(ok);
                defaultButton = ok;
            }
            surface.add(actions, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(defaultButton);
            getRootPane().registerKeyboardAction(e -> dismiss(JOptionPane.CLOSED_OPTION),
                    KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

            setSize(dialogWidth, computeHeight(lead, body, customComponent, booking) + 2);
            try { setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20)); }
            catch (UnsupportedOperationException ignored) { /* Rectangular dialog fallback. */ }
            setLocationRelativeTo(parent);
        }

        private int computeHeight(JPanel lead, JComponent body, boolean custom, boolean booking) {
            int leadHeight = lead.getPreferredSize().height;
            int bodyHeight = body.getPreferredSize().height;
            return Math.min(700, Math.max(booking ? 455 : 295, 170 + leadHeight + bodyHeight + (custom ? 34 : 0)));
        }

        private void dismiss(int selected) {
            result = selected;
            dispose();
        }

        private String shortHeader(String title, Kind kind) {
            if (title == null || title.isBlank() || title.equalsIgnoreCase("TH-MINTON") ||
                    title.equalsIgnoreCase("TH - MINTON")) {
                return switch (kind) {
                    case SUCCESS -> "Thông báo thành công";
                    case ERROR -> "Thông báo lỗi";
                    case WARNING -> "Cảnh báo";
                    case DANGER -> "Xác nhận thao tác";
                    default -> "Thông báo";
                };
            }
            return title.replace("TH-MINTON | ", "").replace("TH - MINTON | ", "");
        }

        private JPanel createLead(Kind kind, boolean component, String msg, String title) {
            JPanel lead = clearPanel(new BorderLayout(16, 0));
            if (component) {
                lead.add(new StatusIcon(Kind.INFO, 54), BorderLayout.WEST);
                JPanel lines = clearPanel(new GridLayout(2, 1, 0, 3));
                lines.add(label(title == null ? "Thông tin" : title, 20, true, INK));
                lines.add(label("Kiểm tra nội dung bên dưới", 13, false, MUTED));
                lead.add(lines, BorderLayout.CENTER);
            } else if (kind == Kind.SUCCESS || kind == Kind.ERROR || kind == Kind.WARNING) {
                lead.setLayout(new BoxLayout(lead, BoxLayout.Y_AXIS));
                StatusIcon icon = new StatusIcon(kind, 64);
                icon.setAlignmentX(Component.CENTER_ALIGNMENT);
                lead.add(icon);
                lead.add(Box.createVerticalStrut(9));
                JLabel heading = label(titleFor(kind, msg, title), 21, true, kind == Kind.ERROR ? RED : INK);
                heading.setAlignmentX(Component.CENTER_ALIGNMENT);
                lead.add(heading);
            } else {
                lead.add(new StatusIcon(kind, 61), BorderLayout.WEST);
                JPanel lines = clearPanel(new GridLayout(2, 1, 0, 3));
                lines.add(label(titleFor(kind, msg, title), 21, true, INK));
                lines.add(label(kind == Kind.BOOKING
                        ? "Vui lòng kiểm tra thông tin trước khi tiếp tục"
                        : kind == Kind.DANGER ? "Hãy kiểm tra kỹ trước khi xác nhận"
                        : "Vui lòng xác nhận thao tác của bạn", 13, false, MUTED));
                lead.add(lines, BorderLayout.CENTER);
            }
            return lead;
        }

        private String titleFor(Kind kind, String msg, String title) {
            if (kind == Kind.BOOKING) return "Xác nhận đặt sân";
            String context = ((title == null ? "" : title) + " " + msg).toLowerCase(Locale.ROOT);
            if (context.contains("đăng xuất")) return "Bạn muốn đăng xuất?";
            if (kind == Kind.DANGER) {
                return context.contains("xóa") ? "Xác nhận xóa"
                        : context.contains("hủy") ? "Xác nhận hủy đơn"
                        : context.contains("ngừng hoạt động") ? "Ngừng hoạt động sân?"
                        : "Xác nhận thay đổi";
            }
            if (context.contains("admin xác nhận tiền")) return "Xác nhận đã nhận tiền";
            if (context.contains("đã chuyển khoản")) return "Xác nhận chuyển khoản";
            if (kind == Kind.SUCCESS) {
                if (msg.toLowerCase(Locale.ROOT).contains("đặt sân thành công")) return "Đặt sân thành công!";
                if (msg.toLowerCase(Locale.ROOT).contains("đăng ký thành công")) return "Chào mừng đến TH-MINTON!";
                if (msg.toLowerCase(Locale.ROOT).contains("đã hủy đơn")) return "Hủy đơn thành công!";
                if (msg.toLowerCase(Locale.ROOT).contains("đã xác nhận thanh toán")) return "Thanh toán đã được xác nhận!";
                return "Thao tác thành công!";
            }
            if (kind == Kind.ERROR) return "Đã xảy ra lỗi";
            if (kind == Kind.WARNING) return "Cảnh báo";
            if (kind == Kind.INFO) return "Thông báo";
            return "Xác nhận";
        }

        private JComponent componentBody(Component content) {
            JPanel body = clearPanel(new BorderLayout());
            body.setBorder(new EmptyBorder(7, 0, 0, 0));
            if (content instanceof JComponent jc) {
                Dimension size = jc.getPreferredSize();
                // A QR preview is a complete image: don't crop it to a 350px scroll viewport.
                if (jc instanceof JLabel image && image.getIcon() != null && size.height <= 500) {
                    JPanel centered = clearPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                    centered.add(jc);
                    body.add(centered, BorderLayout.CENTER);
                } else if (size.height > 390) {
                    JScrollPane pane = new JScrollPane(jc);
                    pane.setPreferredSize(new Dimension(480, 350));
                    pane.setBorder(BorderFactory.createEmptyBorder());
                    pane.getVerticalScrollBar().setUnitIncrement(18);
                    body.add(pane, BorderLayout.CENTER);
                } else body.add(jc, BorderLayout.CENTER);
            } else body.add(content, BorderLayout.CENTER);
            return body;
        }

        private JComponent messageBody(String message, Kind kind) {
            JPanel frame = clearPanel(new BorderLayout());
            frame.setBorder(new EmptyBorder(7, 8, 0, 8));
            String visibleMessage = message == null ? "" : message;
            if (kind == Kind.SUCCESS && visibleMessage.startsWith("Đặt sân thành công!")) {
                String details = visibleMessage.substring("Đặt sân thành công!".length()).trim();
                if (!details.isEmpty()) visibleMessage = details;
            }
            JTextArea desc = new JTextArea(visibleMessage);
            desc.setFont(font(14, false));
            desc.setForeground(INK);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setOpaque(false);
            desc.setEditable(false);
            desc.setFocusable(false);
            desc.setBorder(BorderFactory.createEmptyBorder());
            int width = 425;
            desc.setSize(width, Short.MAX_VALUE);
            int height = Math.max(45, Math.min(218, desc.getPreferredSize().height + 14));
            desc.setPreferredSize(new Dimension(width, height));
            if (message != null && message.length() > 520) {
                JScrollPane sc = new JScrollPane(desc);
                sc.setBorder(BorderFactory.createEmptyBorder());
                sc.setPreferredSize(new Dimension(width, Math.min(height, 180)));
                frame.add(sc, BorderLayout.CENTER);
            } else {
                frame.add(desc, BorderLayout.CENTER);
            }
            return frame;
        }

        private JComponent bookingSummary(String message) {
            LinkedHashMap<String, String> summary = new LinkedHashMap<>();
            for (String line : message.split("\\R")) {
                int c = line.indexOf(':');
                if (c < 0) continue;
                String key = line.substring(0, c).trim();
                if (key.equals("Sân") || key.equals("Ngày") || key.equals("Giờ") || key.equals("Tổng tiền"))
                    summary.put(key, line.substring(c + 1).trim());
            }
            JPanel wrapper = new RoundedSurface(PALE, 14);
            wrapper.setLayout(new GridLayout(4, 1, 0, 0));
            wrapper.setBorder(new EmptyBorder(13, 17, 13, 17));
            for (Map.Entry<String, String> e : summary.entrySet()) {
                JPanel row = clearPanel(new BorderLayout(12, 0));
                JLabel key = label(e.getKey() + ":", 14, false, MUTED);
                JLabel value = label(e.getValue(), 14, e.getKey().equals("Tổng tiền"),
                        e.getKey().equals("Tổng tiền") ? MINT_DARK : INK);
                row.add(key, BorderLayout.WEST);
                row.add(value, BorderLayout.EAST);
                wrapper.add(row);
            }
            return wrapper;
        }

    }

    private static final class RoundedSurface extends JPanel {
        private final Color fill;
        private final int arc;
        RoundedSurface() { this(Color.WHITE, 20); }
        RoundedSurface(Color fill, int arc) {
            super(); this.fill = fill; this.arc = arc; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (fill.equals(Color.WHITE)) {
                for (int i = 10; i >= 2; i -= 2) {
                    gg.setColor(new Color(10, 48, 43, 4));
                    gg.fillRoundRect(i / 2, i / 2, getWidth() - i, getHeight() - i,
                            arc + 1, arc + 1);
                }
            }
            gg.setColor(fill);
            gg.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            gg.dispose();
            super.paintComponent(g);
        }
    }

    private static final class StatusIcon extends JComponent {
        private final Kind kind;
        private final int dimension;
        StatusIcon(Kind kind, int dimension) {
            this.kind = kind; this.dimension = dimension;
            setPreferredSize(new Dimension(dimension, dimension));
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color accent = switch (kind) {
                case SUCCESS, BOOKING, QUESTION -> MINT;
                case ERROR, DANGER -> RED;
                case WARNING -> AMBER;
                case INFO -> BLUE;
            };
            gg.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24));
            gg.fillOval(0, 0, dimension, dimension);
            int mid = dimension / 2;
            gg.setColor(accent);
            gg.setStroke(new BasicStroke(3.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (kind == Kind.SUCCESS) {
                gg.drawLine(mid - 13, mid, mid - 3, mid + 9);
                gg.drawLine(mid - 3, mid + 9, mid + 15, mid - 12);
            } else if (kind == Kind.ERROR) {
                gg.drawLine(mid - 10, mid - 10, mid + 10, mid + 10);
                gg.drawLine(mid + 10, mid - 10, mid - 10, mid + 10);
            } else if (kind == Kind.WARNING || kind == Kind.DANGER) {
                if (kind == Kind.DANGER) {
                    gg.drawRoundRect(mid - 10, mid - 9, 20, 22, 3, 3);
                    gg.drawLine(mid - 13, mid - 12, mid + 13, mid - 12);
                    gg.drawLine(mid - 4, mid - 17, mid + 4, mid - 17);
                    gg.drawLine(mid - 4, mid - 4, mid - 4, mid + 6);
                    gg.drawLine(mid + 4, mid - 4, mid + 4, mid + 6);
                } else {
                    gg.drawLine(mid, mid - 12, mid, mid + 4);
                    gg.fillOval(mid - 2, mid + 9, 4, 4);
                }
            } else if (kind == Kind.BOOKING) {
                gg.drawRoundRect(mid - 15, mid - 13, 30, 29, 4, 4);
                gg.drawLine(mid - 15, mid - 6, mid + 15, mid - 6);
                gg.drawLine(mid - 8, mid - 17, mid - 8, mid - 10);
                gg.drawLine(mid + 8, mid - 17, mid + 8, mid - 10);
                gg.drawLine(mid - 8, mid + 1, mid - 3, mid + 6);
                gg.drawLine(mid - 3, mid + 6, mid + 8, mid - 2);
            } else if (kind == Kind.INFO) {
                gg.fillOval(mid - 2, mid - 13, 5, 5);
                gg.drawLine(mid, mid - 2, mid, mid + 12);
            } else {
                gg.drawOval(mid - 13, mid - 13, 26, 26);
                gg.drawLine(mid, mid - 7, mid, mid + 3);
                gg.fillOval(mid - 2, mid + 7, 4, 4);
            }
            gg.dispose();
        }
    }

    private static final class PillButton extends JButton {
        private final boolean primary;
        private final Color accent;
        PillButton(String caption, boolean primary, Color accent) {
            super(caption); this.primary = primary; this.accent = accent;
            setFont(font(13, true));
            setForeground(primary ? Color.WHITE : DEEP);
            setBorder(new EmptyBorder(12, 19, 12, 19));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(Math.max(125, getFontMetrics(getFont()).stringWidth(caption) + 43), 44));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D gg = (Graphics2D) g.create();
            gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = primary ? accent : LIGHT;
            if (getModel().isPressed()) bg = primary ? accent.darker() : new Color(222, 232, 231);
            else if (getModel().isRollover()) bg = primary ? accent.brighter() : new Color(230, 238, 236);
            gg.setColor(bg);
            gg.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (isFocusOwner()) {
                gg.setColor(new Color(37, 197, 162));
                gg.setStroke(new BasicStroke(2f));
                gg.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            }
            gg.dispose();
            super.paintComponent(g);
        }
    }
}
