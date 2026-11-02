package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.model.User;
import com.esports.view.auth.LoginFrame;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin Dashboard - Command Center for Tournament Organizers.
 * Features Sidebar Navigation, Live Statistics Cards, and Management Tabs.
 */
public class AdminDashboard extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel activeTitleLabel;
    private AdminOverviewPanel overviewPanel;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }

    public AdminDashboard() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("eSports Tournament Management System - Admin Command Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeConfig.COLOR_BG_DARK);

        // 1. Sidebar Navigation (Left)
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Main Work Area (Center)
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);

        // Top Header Bar
        JPanel topHeader = createTopHeader();
        mainArea.add(topHeader, BorderLayout.NORTH);

        // Dynamic Card Content Panel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(16, 20, 20, 20));

        // Add Module Views to Content Panel
        overviewPanel = new AdminOverviewPanel();
        contentPanel.add(overviewPanel, "OVERVIEW");
        contentPanel.add(new AdminGamesPanel(), "GAMES");
        contentPanel.add(new AdminTournamentsPanel(), "TOURNAMENTS");
        contentPanel.add(new AdminMatchesPanel(), "MATCHES");
        contentPanel.add(new AdminStandingsPanel(), "STANDINGS");
        contentPanel.add(new AdminTeamsPanel(), "TEAMS");
        contentPanel.add(new AdminPlayersPanel(), "PLAYERS");
        contentPanel.add(new AdminRegistrationsPanel(), "REGISTRATIONS");
        contentPanel.add(new AdminPaymentsPanel(), "PAYMENTS");
        contentPanel.add(new AdminReportsPanel(), "REPORTS");
        contentPanel.add(new AdminNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(new AdminProfilePanel(), "PROFILE");

        mainArea.add(contentPanel, BorderLayout.CENTER);
        rootPanel.add(mainArea, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeConfig.COLOR_BORDER));

        // Brand Banner
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(20, 18, 16, 18));

        JLabel brandTitle = new JLabel("⚡ ESPORTS ARENA");
        brandTitle.setFont(ThemeConfig.FONT_TITLE);
        brandTitle.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        JLabel brandSub = new JLabel("ADMIN COMMAND CENTER");
        brandSub.setFont(ThemeConfig.FONT_SMALL);
        brandSub.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);
        sidebar.add(brandPanel);

        // Nav Buttons
        sidebar.add(createNavButton("📊 Dashboard Overview", "OVERVIEW", "Overview & Analytics"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🎮 Game Management", "GAMES", "eSports Titles & Game Rules"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🏆 Tournaments", "TOURNAMENTS", "Tournaments & Automated Fixtures"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⚔️ Match Operations", "MATCHES", "Schedules, Scores & Winners"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🏅 Standings & Rankings", "STANDINGS", "Tournament Standings & Leaderboards"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🛡️ Teams & Rosters", "TEAMS", "Manage eSports Teams"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("👤 Player Profiles", "PLAYERS", "Manage Competitive Players"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📋 Registrations", "REGISTRATIONS", "Approve / Reject Requests"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("💳 Payments & Fees", "PAYMENTS", "Entry Fees & Prize Disbursements"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📄 Reports & Exports", "REPORTS", "PDF & Excel/CSV Reporting Engine"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📢 Announcements", "NOTIFICATIONS", "System Notifications"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⚙️ Admin Profile", "PROFILE", "Account & Security Settings"));

        sidebar.add(Box.createVerticalGlue());

        // Logout Button
        JButton logoutBtn = UIComponents.createDangerButton("🚪 Logout");
        logoutBtn.setMaximumSize(new Dimension(210, 38));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                UserSession.getInstance().logout();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(20));

        return sidebar;
    }

    private JButton createNavButton(String title, String cardKey, String headerTitle) {
        JButton button = new JButton(title);
        button.setFont(ThemeConfig.FONT_BOLD);
        button.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        button.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(230, 38));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardKey);
            activeTitleLabel.setText(headerTitle);
            if ("OVERVIEW".equals(cardKey) && overviewPanel != null) {
                overviewPanel.loadDynamicData();
            }
        });

        return button;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConfig.COLOR_BORDER),
                new EmptyBorder(14, 20, 14, 20)
        ));

        activeTitleLabel = new JLabel("Overview & Analytics");
        activeTitleLabel.setFont(ThemeConfig.FONT_TITLE);
        activeTitleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        User current = UserSession.getInstance().getCurrentUser();
        String userDisplay = current != null ? current.getFullName() + " (ADMIN)" : "Administrator";
        JLabel profileLabel = new JLabel("👤 " + userDisplay);
        profileLabel.setFont(ThemeConfig.FONT_BOLD);
        profileLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        header.add(activeTitleLabel, BorderLayout.WEST);
        header.add(profileLabel, BorderLayout.EAST);

        return header;
    }
}
