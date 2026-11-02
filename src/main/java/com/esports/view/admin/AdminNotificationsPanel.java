package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.dao.NotificationDAO;
import com.esports.dao.NotificationDAOImpl;
import com.esports.dao.UserDAO;
import com.esports.dao.UserDAOImpl;
import com.esports.model.Notification;
import com.esports.model.User;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Panel for System Notifications & Announcements.
 */
public class AdminNotificationsPanel extends JPanel {

    private final NotificationDAO notificationDAO = new NotificationDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    private DefaultTableModel tableModel;
    private JTable notificationTable;

    public AdminNotificationsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadNotifications();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLabel = new JLabel("📢 System Announcements & User Notifications");
        titleLabel.setFont(ThemeConfig.FONT_SUBTITLE);
        titleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton broadcastBtn = UIComponents.createPrimaryButton("📣 Broadcast Announcement");
        JButton sendBtn = UIComponents.createAccentButton("✉️ Send Direct Alert");
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        broadcastBtn.addActionListener(e -> showBroadcastDialog());
        sendBtn.addActionListener(e -> showDirectDialog());
        refreshBtn.addActionListener(e -> loadNotifications());

        btnPanel.add(broadcastBtn);
        btnPanel.add(sendBtn);
        btnPanel.add(refreshBtn);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(btnPanel, BorderLayout.EAST);
        mainCard.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Target User ID", "Title", "Message", "Read Status", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        notificationTable = new JTable(tableModel);
        notificationTable.setShowGrid(true);
        notificationTable.setFillsViewportHeight(true);
        notificationTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        notificationTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        notificationTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        notificationTable.getColumnModel().getColumn(3).setPreferredWidth(350);
        notificationTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        notificationTable.getColumnModel().getColumn(5).setPreferredWidth(130);

        JScrollPane scrollPane = new JScrollPane(notificationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        int userId = (UserSession.getInstance().getCurrentUser() != null) ? UserSession.getInstance().getCurrentUser().getId() : 1;
        List<Notification> list = notificationDAO.getByUser(userId);
        for (Notification n : list) {
            tableModel.addRow(new Object[]{
                    n.getId(),
                    n.getUserId(),
                    n.getTitle(),
                    n.getMessage(),
                    n.isRead() ? "Read" : "Unread",
                    n.getCreatedAt() != null ? n.getCreatedAt().toString().substring(0, 16) : "-"
            });
        }
    }

    private void showBroadcastDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Broadcast System Announcement", true);
        dialog.setSize(480, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField titleField = UIComponents.createTextField(15);
        JTextArea msgArea = new JTextArea(4, 15);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBackground(ThemeConfig.COLOR_CARD_BG);
        msgArea.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        panel.add(new JLabel("Broadcast Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Message Body:"));
        panel.add(new JScrollPane(msgArea));

        JButton sendBtn = UIComponents.createPrimaryButton("Broadcast to All Users");
        sendBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String msg = msgArea.getText().trim();
            if (title.isEmpty() || msg.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and message are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (notificationDAO.broadcast(title, msg)) {
                JOptionPane.showMessageDialog(dialog, "Announcement broadcast to all active users!", "Broadcast Sent", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadNotifications();
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(sendBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showDirectDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Send Direct Alert", true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JComboBox<User> userCombo = new JComboBox<>();
        for (User u : userDAO.findAll()) {
            userCombo.addItem(u);
        }

        JTextField titleField = UIComponents.createTextField(15);
        JTextArea msgArea = new JTextArea(3, 15);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBackground(ThemeConfig.COLOR_CARD_BG);
        msgArea.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        panel.add(new JLabel("Recipient User:"));
        panel.add(userCombo);
        panel.add(new JLabel("Alert Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Alert Message:"));
        panel.add(new JScrollPane(msgArea));

        JButton sendBtn = UIComponents.createPrimaryButton("Send Alert");
        sendBtn.addActionListener(e -> {
            User target = (User) userCombo.getSelectedItem();
            String title = titleField.getText().trim();
            String msg = msgArea.getText().trim();

            if (target != null && !title.isEmpty() && !msg.isEmpty()) {
                if (notificationDAO.sendNotification(target.getId(), title, msg)) {
                    JOptionPane.showMessageDialog(dialog, "Alert sent to " + target.getFullName(), "Sent", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadNotifications();
                }
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(sendBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
