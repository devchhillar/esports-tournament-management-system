package com.esports.view.player;

import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.dao.*;
import com.esports.model.*;
import com.esports.view.auth.LoginFrame;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Player Dashboard - Complete portal for registered competitive gamers.
 */
public class PlayerDashboard extends JFrame {

    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final RegistrationDAO registrationDAO = new RegistrationDAOImpl();
    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final GameDAO gameDAO = new GameDAOImpl();

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel activeTitleLabel;
    private Player currentPlayer;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            PlayerDashboard dashboard = new PlayerDashboard();
            dashboard.setVisible(true);
        });
    }

    public PlayerDashboard() {
        loadPlayerData();
        initializeUI();
    }

    private void loadPlayerData() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null) {
            currentPlayer = playerDAO.findByUserId(user.getId());
            if (currentPlayer != null) {
                UserSession.getInstance().setPlayerId(currentPlayer.getId());
            }
        }
    }

    private void initializeUI() {
        setTitle("eSports Tournament Management System - Player Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeConfig.COLOR_BG_DARK);

        // Sidebar
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // Main Area
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);

        // Header
        JPanel topHeader = createTopHeader();
        mainArea.add(topHeader, BorderLayout.NORTH);

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(16, 20, 20, 20));

        contentPanel.add(createProfilePanel(), "PROFILE");
        contentPanel.add(createTournamentsPanel(), "TOURNAMENTS");
        contentPanel.add(createMyTeamPanel(), "MY_TEAM");
        contentPanel.add(createMatchesPanel(), "MATCHES");
        contentPanel.add(createRankingsPanel(), "RANKINGS");

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

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(20, 18, 16, 18));

        JLabel brandTitle = new JLabel("⚡ ESPORTS ARENA");
        brandTitle.setFont(ThemeConfig.FONT_TITLE);
        brandTitle.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        JLabel brandSub = new JLabel("PLAYER HEADQUARTERS");
        brandSub.setFont(ThemeConfig.FONT_SMALL);
        brandSub.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);
        sidebar.add(brandPanel);

        sidebar.add(createNavButton("👤 My Profile & Career", "PROFILE", "Player Profile & Career Statistics"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🏆 Browse Tournaments", "TOURNAMENTS", "Available Tournaments & Events"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🛡️ My Squad & Team", "MY_TEAM", "My Team & Teammates"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⚔️ Match Schedules", "MATCHES", "Competitive Match Schedules & History"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⭐ Global Leaderboard", "RANKINGS", "Competitive Player Rankings"));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = UIComponents.createDangerButton("🚪 Logout");
        logoutBtn.setMaximumSize(new Dimension(210, 38));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            UserSession.getInstance().logout();
            new LoginFrame().setVisible(true);
            dispose();
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

        activeTitleLabel = new JLabel("Player Profile & Career Statistics");
        activeTitleLabel.setFont(ThemeConfig.FONT_TITLE);
        activeTitleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        String tag = currentPlayer != null ? currentPlayer.getGamerTag() : "Pro Gamer";
        int pts = currentPlayer != null ? currentPlayer.getRankingPoints() : 1000;
        JLabel profileLabel = new JLabel("🎮 " + tag + " | ⭐ " + pts + " RP");
        profileLabel.setFont(ThemeConfig.FONT_BOLD);
        profileLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        header.add(activeTitleLabel, BorderLayout.WEST);
        header.add(profileLabel, BorderLayout.EAST);

        return header;
    }

    // --- Tab 1: Profile Panel ---
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Stats Row 1
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);

        int pts = currentPlayer != null ? currentPlayer.getRankingPoints() : 1000;
        int played = currentPlayer != null ? currentPlayer.getMatchesPlayed() : 0;
        int wins = currentPlayer != null ? currentPlayer.getWins() : 0;
        int kills = currentPlayer != null ? currentPlayer.getKills() : 0;
        int mvps = currentPlayer != null ? currentPlayer.getMvpCount() : 0;
        double winRate = currentPlayer != null ? currentPlayer.getWinRate() : 0.0;

        statsRow.add(createCardStat("⭐ Ranking Points", String.valueOf(pts), ThemeConfig.COLOR_ACCENT_CYAN));
        statsRow.add(createCardStat("🎮 Matches Played", String.valueOf(played), ThemeConfig.COLOR_ACCENT_BLUE));
        statsRow.add(createCardStat("🏆 Victories / Losses", wins + " / " + (played - wins), ThemeConfig.COLOR_SUCCESS));
        statsRow.add(createCardStat("🎯 Win Rate %", String.format("%.1f%%", winRate), ThemeConfig.COLOR_ACCENT_PURPLE));

        // Stats Row 2 (Kills & MVPs)
        JPanel statsRow2 = new JPanel(new GridLayout(1, 2, 14, 0));
        statsRow2.setOpaque(false);
        statsRow2.add(createCardStat("🔥 Total Career Kills", String.valueOf(kills), ThemeConfig.COLOR_DANGER));
        statsRow2.add(createCardStat("🎖️ MVP Awards", String.valueOf(mvps), new Color(245, 158, 11)));

        JPanel topStats = new JPanel(new GridLayout(2, 1, 0, 10));
        topStats.setOpaque(false);
        topStats.add(statsRow);
        topStats.add(statsRow2);
        panel.add(topStats, BorderLayout.NORTH);

        // Bio and Details Card
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new GridLayout(0, 2, 16, 12));

        User user = UserSession.getInstance().getCurrentUser();
        JTextField tagField = UIComponents.createTextField(20);
        if (currentPlayer != null) tagField.setText(currentPlayer.getGamerTag());

        JComboBox<String> gameCombo = new JComboBox<>();
        for (Game g : gameDAO.findAll()) {
            gameCombo.addItem(g.getName());
        }
        if (currentPlayer != null) gameCombo.setSelectedItem(currentPlayer.getPrimaryGame());

        JTextField roleField = UIComponents.createTextField(20);
        if (currentPlayer != null) roleField.setText(currentPlayer.getInGameRole());

        JTextField countryField = UIComponents.createTextField(20);
        if (currentPlayer != null) countryField.setText(currentPlayer.getCountry());

        JTextField bioField = UIComponents.createTextField(20);
        if (currentPlayer != null && currentPlayer.getBio() != null) bioField.setText(currentPlayer.getBio());

        final String[] avatarPath = {currentPlayer != null ? currentPlayer.getProfilePicture() : null};
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        avatarPanel.setOpaque(false);
        JLabel avatarStatus = new JLabel(avatarPath[0] != null ? new java.io.File(avatarPath[0]).getName() : "No avatar uploaded");
        avatarStatus.setFont(ThemeConfig.FONT_SMALL);
        avatarStatus.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadAvatarBtn = UIComponents.createSecondaryButton("📁 Upload Avatar");
        uploadAvatarBtn.addActionListener(e -> {
            String savedPath = com.esports.util.FileUploadUtil.chooseAndSaveImage(this, "avatars");
            if (savedPath != null) {
                avatarPath[0] = savedPath;
                avatarStatus.setText("Uploaded: " + new java.io.File(savedPath).getName());
                avatarStatus.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        avatarPanel.add(uploadAvatarBtn);
        avatarPanel.add(avatarStatus);

        card.add(new JLabel("Full Name:"));
        card.add(new JLabel(user != null ? user.getFullName() : "-"));
        card.add(new JLabel("Account Username:"));
        card.add(new JLabel(user != null ? "@" + user.getUsername() : "-"));
        card.add(new JLabel("Profile Avatar:"));
        card.add(avatarPanel);
        card.add(new JLabel("Current Squad:"));
        card.add(new JLabel(currentPlayer != null && currentPlayer.getCurrentTeamName() != null ? currentPlayer.getCurrentTeamName() : "Free Agent"));
        card.add(new JLabel("Gamer Tag / In-Game Name:"));
        card.add(tagField);
        card.add(new JLabel("Primary Game Discipline:"));
        card.add(gameCombo);
        card.add(new JLabel("In-Game Role / Position:"));
        card.add(roleField);
        card.add(new JLabel("Country / Region:"));
        card.add(countryField);
        card.add(new JLabel("Player Bio / Status:"));
        card.add(bioField);

        JButton saveProfileBtn = UIComponents.createPrimaryButton("💾 Save Profile Changes");
        saveProfileBtn.addActionListener(e -> {
            if (currentPlayer != null) {
                currentPlayer.setGamerTag(tagField.getText().trim());
                currentPlayer.setPrimaryGame((String) gameCombo.getSelectedItem());
                currentPlayer.setInGameRole(roleField.getText().trim());
                currentPlayer.setCountry(countryField.getText().trim());
                currentPlayer.setBio(bioField.getText().trim());
                currentPlayer.setProfilePicture(avatarPath[0]);

                if (playerDAO.updateProfile(currentPlayer)) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        wrapper.add(saveProfileBtn, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCardStat(String title, String val, Color col) {
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel t = new JLabel(title);
        t.setFont(ThemeConfig.FONT_SMALL);
        t.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(col);

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, col),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }

    // --- Tab 2: Tournaments Panel ---
    private JPanel createTournamentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel topBar = UIComponents.createCardPanel();
        topBar.setLayout(new BorderLayout());

        JLabel title = new JLabel("Open & Upcoming Tournaments");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");
        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Tournament", "Game", "Format", "Prize Pool", "Entry Fee", "Slots", "Status", "Start Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable load = () -> {
            model.setRowCount(0);
            for (Tournament t : tournamentDAO.findAll()) {
                model.addRow(new Object[]{
                        t.getId(), t.getTitle(), t.getGame(), t.getFormat(),
                        "₹ " + t.getPrizePool(), "₹ " + t.getEntryFee(), t.getMaxTeams(),
                        t.getStatus(), t.getStartDate() != null ? t.getStartDate().toString().substring(0, 16) : ""
                });
            }
        };
        refreshBtn.addActionListener(e -> load.run());
        load.run();

        return panel;
    }

    // --- Tab 3: My Squad & Team ---
    private JPanel createMyTeamPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        String teamName = (currentPlayer != null && currentPlayer.getCurrentTeamName() != null) ? currentPlayer.getCurrentTeamName() : "No Current Team (Free Agent)";
        JLabel title = new JLabel("🛡️ Squad Roster: " + teamName);
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Player ID", "Gamer Tag", "Full Name", "Primary Game", "Role", "Country", "Points", "Kills"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        if (currentPlayer != null && currentPlayer.getCurrentTeamId() != null) {
            List<Player> teammates = playerDAO.findByTeamId(currentPlayer.getCurrentTeamId());
            for (Player p : teammates) {
                model.addRow(new Object[]{
                        p.getId(), p.getGamerTag(), p.getFullName(), p.getPrimaryGame(),
                        p.getInGameRole(), p.getCountry(), p.getRankingPoints(), p.getKills()
                });
            }
        }

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 4: Matches Panel ---
    private JPanel createMatchesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("⚔️ Tournament Matches & Fixtures");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Match ID", "Tournament", "Round", "Team 1", "Score", "Team 2", "Schedule", "Status", "Winner", "MVP"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        List<Match> matches = (currentPlayer != null && currentPlayer.getCurrentTeamId() != null)
                ? matchDAO.findByTeam(currentPlayer.getCurrentTeamId())
                : matchDAO.findAll();

        for (Match m : matches) {
            String score = m.getStatus().equalsIgnoreCase("COMPLETED") ? m.getTeam1Score() + " - " + m.getTeam2Score() : "VS";
            model.addRow(new Object[]{
                    m.getId(), m.getTournamentTitle(), m.getRoundName(),
                    m.getTeam1Name(), score, m.getTeam2Name(),
                    m.getScheduledTime() != null ? m.getScheduledTime().toString().substring(0, 16) : "-",
                    m.getStatus(),
                    m.getWinnerTeamName() != null ? m.getWinnerTeamName() : "-",
                    m.getMvpPlayerName() != null ? m.getMvpPlayerName() : "-"
            });
        }

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 5: Rankings Panel (Privacy Protected) ---
    private JPanel createRankingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("⭐ Global Competitive Player Leaderboard");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Rank", "Gamer Tag", "Current Team", "Primary Game", "Role", "Country", "Points", "Matches", "Wins", "Kills", "MVPs", "Win %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        int rank = 1;
        for (Player p : playerDAO.findAll()) {
            String rankStr = (rank == 1) ? "🥇 1" : (rank == 2) ? "🥈 2" : (rank == 3) ? "🥉 3" : String.valueOf(rank);
            model.addRow(new Object[]{
                    rankStr,
                    p.getGamerTag(),
                    p.getCurrentTeamName() != null ? p.getCurrentTeamName() : "Free Agent",
                    p.getPrimaryGame(),
                    p.getInGameRole(),
                    p.getCountry(),
                    p.getRankingPoints(),
                    p.getMatchesPlayed(),
                    p.getWins(),
                    p.getKills(),
                    p.getMvpCount(),
                    String.format("%.1f%%", p.getWinRate())
            });
            rank++;
        }

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }
}
