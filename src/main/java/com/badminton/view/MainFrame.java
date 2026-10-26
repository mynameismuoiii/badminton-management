
package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();

    private final JPanel content = new JPanel(cardLayout);

    private final Map<String, String> screens =
            new LinkedHashMap<>();

    private final Map<String, JPanel> registeredPanels =
            new LinkedHashMap<>();

    public MainFrame() {

        setTitle("Quan ly va toi uu lich dat san cau long");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Cac man hinh cua ung dung
        screens.put("courts", "Quan ly san");
        screens.put("customers", "Khach hang");
        screens.put("bookings", "Dat san");
        screens.put("schedule", "Lich san trong");
        screens.put("suggestions", "Goi y khung gio");
        screens.put("payments", "Thanh toan");
        screens.put("statistics", "Thong ke");

        // Menu ben trai
        JPanel nav = new JPanel(
            new GridLayout(0, 1, 0, 8)
        );

        nav.setBorder(
            BorderFactory.createEmptyBorder(24, 14, 24, 14)
        );

        nav.setPreferredSize(new Dimension(190, 0));

        for (Map.Entry<String, String> entry :
                screens.entrySet()) {

            String key = entry.getKey();

            JButton button = new JButton(entry.getValue());

            button.addActionListener(event -> showScreen(key));

            nav.add(button);

            JPanel placeholder =
                    createPlaceholder(entry.getValue());

            content.add(placeholder, key);

            registeredPanels.put(key, placeholder);
        }

        // Tieu de ung dung
        JLabel header = new JLabel(
            "BADMINTON COURT MANAGEMENT",
            SwingConstants.CENTER
        );

        header.setFont(
            header.getFont().deriveFont(Font.BOLD, 19f)
        );

        header.setBorder(
            BorderFactory.createEmptyBorder(20, 10, 20, 10)
        );

        add(header, BorderLayout.NORTH);

        add(new JScrollPane(nav), BorderLayout.WEST);

        add(content, BorderLayout.CENTER);

        showScreen("courts");
    }

    // Man hinh mau khi chua code chuc nang
    private JPanel createPlaceholder(String title) {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(
            title + " - Chua trien khai",
            SwingConstants.CENTER
        );

        label.setFont(
            label.getFont().deriveFont(Font.BOLD, 22f)
        );

        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    // Dang ky man hinh that
    public void registerScreen(String key, JPanel panel) {

        if (!screens.containsKey(key)) {
            throw new IllegalArgumentException(
                "Unknown screen: " + key
            );
        }

        JPanel old = registeredPanels.get(key);

        if (old != null) {
            content.remove(old);
        }

        content.add(panel, key);

        registeredPanels.put(key, panel);

        content.revalidate();
        content.repaint();

        showScreen(key);
    }

    // Chuyen doi man hinh
    public void showScreen(String key) {
        cardLayout.show(content, key);
    }
}
