package com.badminton.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MainFrame extends JFrame {

    private final JPanel content =
            new JPanel(new CardLayout());

    private final JPanel menu =
            new JPanel(new GridLayout(0, 1, 4, 4));

    private final Map<String, JPanel> screens =
            new LinkedHashMap<>();

    public MainFrame() {

        super("Badminton Management");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JScrollPane left = new JScrollPane(menu);
        left.setPreferredSize(new Dimension(195, 0));

        add(left, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        // N2
        registerScreen("Sân", new CourtPanel());

        // N1
        registerScreen("Khách hàng", new CustomerPanel());
        registerScreen("Đặt sân", new BookingPanel());

        // N2
        registerScreen("Lịch sân", new SchedulePanel());
        registerScreen("Gợi ý sân", new SuggestionPanel());

        // N1
        registerScreen("Thanh toán", new PaymentPanel());

        // N2
        registerScreen("Thống kê", new StatisticsPanel());

        showScreen("Sân");
    }

    public void registerScreen(String name, JPanel panel) {

        if (screens.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Duplicate screen: " + name
            );
        }

        screens.put(name, panel);
        content.add(panel, name);

        JButton button = new JButton(name);

        button.addActionListener(e -> showScreen(name));

        menu.add(button);
    }

    public void showScreen(String name) {

        if (!screens.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Unknown screen: " + name
            );
        }

        CardLayout layout =
                (CardLayout) content.getLayout();

        layout.show(content, name);
    }
}