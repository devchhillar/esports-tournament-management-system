package com.esports.view.viewer;

import com.esports.config.ThemeConfig;
import com.esports.dao.*;
import com.esports.model.*;
import com.esports.view.auth.LoginFrame;
import com.esports.view.common.ChartUtil;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Viewer Module - Dedicated public / guest spectating hub.
 * Allows anyone to view live tournaments, schedules, brackets, standings, and visual charts.
 */
public class ViewerDashboard extends JFrame {

    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final StatsDAO statsDAO = new StatsDAOImpl();
    private final TournamentStandingDAO standingDAO = new TournamentStandingDAOImpl();

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel activeTitleLabel;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            ViewerDashboard viewer = new ViewerDashboard();
            viewer.setVisible(true);
        });
    }

    public ViewerDashboard() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("eSports Tournament Spectator Hub - Public Viewer Center");
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

        contentPanel.add(createTournamentsPanel(), "TOURNAMENTS");
        contentPanel.add(createLiveMatchesPanel(), "LIVE");
        contentPanel.add(createUpcomingMatchesPanel(), "UPCOMING");
        contentPanel.add(createCompletedMatchesPanel(), "RESULTS");
        contentPanel.add(createStandingsPanel(), "STANDINGS");
        contentPanel.add(createLeaderboardPanel(), "LEADERBOARDS");
        contentPanel.add(createChartsPanel(), "CHARTS");

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

        JLabel brandTitle = new JLabel("⚡ ESPORTS LIVE");
        brandTitle.setFont(ThemeConfig.FONT_TITLE);
        brandTitle.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        JLabel brandSub = new JLabel("PUBLIC SPECTATOR HUB");
        brandSub.setFont(ThemeConfig.FONT_SMALL);
        brandSub.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);
        sidebar.add(brandPanel);

        sidebar.add(createNavButton("🏆 Browse Tournaments", "TOURNAMENTS", "Explore eSports Tournaments"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🔴 Ongoing & Live Matches", "LIVE", "Live Match Fixtures"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📅 Upcoming Schedule", "UPCOMING", "Upcoming Tournament Fixtures"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📜 Match Results", "RESULTS", "Recent Match Results & Champions"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🏅 Tournament Standings", "STANDINGS", "Tournament Rankings & Points"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⭐ Global Leaderboard", "LEADERBOARDS", "Top Competitive Players"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📊 Statistics & Charts", "CHARTS", "Visual Performance Analytics"));

        sidebar.add(Box.createVerticalGlue());

        JButton loginBtn = UIComponents.createPrimaryButton("🔑 Login / Sign In");
        loginBtn.setMaximumSize(new Dimension(210, 38));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        sidebar.add(loginBtn);
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

        activeTitleLabel = new JLabel("Explore eSports Tournaments");
        activeTitleLabel.setFont(ThemeConfig.FONT_TITLE);
        activeTitleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel guestLabel = new JLabel("👀 Spectator Mode (Public Access)");
        guestLabel.setFont(ThemeConfig.FONT_BOLD);
        guestLabel.setForeground(ThemeConfig.COLOR_ACCENT_PURPLE);

        header.add(activeTitleLabel, BorderLayout.WEST);
        header.add(guestLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createTournamentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel title = new JLabel("Active & Upcoming Tournaments");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JButton viewBracketBtn = UIComponents.createStyledButton("📊 View Tournament Bracket Tree", new Color(123, 44, 191), Color.WHITE, new Color(157, 78, 221));
        topRow.add(title, BorderLayout.WEST);
        topRow.add(viewBracketBtn, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Tournament Title", "Game Discipline", "Format", "Prize Pool", "Entry Fee", "Slots", "Status", "Dates"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        List<Tournament> tourneys = tournamentDAO.findAll();
        for (Tournament t : tourneys) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getGame(),
                    t.getFormat(),
                    "₹ " + t.getPrizePool(),
                    "₹ " + t.getEntryFee(),
                    t.getMaxTeams() + " Teams",
                    t.getStatus(),
                    t.getStartDate() != null ? t.getStartDate().toString().substring(0, 10) : "TBD"
            });
        }

        viewBracketBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int tourneyId = (Integer) model.getValueAt(row, 0);
                com.esports.view.common.BracketViewerDialog.showBracket(this, tourneyId);
            } else if (!tourneys.isEmpty()) {
                com.esports.view.common.BracketViewerDialog.showBracket(this, tourneys.get(0).getId());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tournament.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLiveMatchesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("🔴 Live & High-Priority Fixtures");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_DANGER);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Tournament", "Round", "Team 1", "Team 2", "Status", "Schedule"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        List<Match> liveMatches = matchDAO.findLive();
        if (liveMatches.isEmpty()) {
            liveMatches = matchDAO.findUpcoming();
        }

        for (Match m : liveMatches) {
            model.addRow(new Object[]{
                    m.getTournamentTitle(),
                    m.getRoundName(),
                    m.getTeam1Name(),
                    m.getTeam2Name(),
                    m.getStatus(),
                    m.getScheduledTime() != null ? m.getScheduledTime().toString().substring(0, 16) : "-"
            });
        }

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUpcomingMatchesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("📅 Upcoming Tournament Fixtures");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Tournament", "Round / Stage", "Team 1", "Team 2", "Scheduled Date & Time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        for (Match m : matchDAO.findUpcoming()) {
            model.addRow(new Object[]{
                    m.getTournamentTitle(),
                    m.getRoundName(),
                    m.getTeam1Name(),
                    m.getTeam2Name(),
                    m.getScheduledTime() != null ? m.getScheduledTime().toString().substring(0, 16) : "-"
            });
        }

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCompletedMatchesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("📜 Recent Results & Champions");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Tournament", "Round", "Match Fixture", "Final Score", "Champion Winner", "MVP Player", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        for (Match m : matchDAO.findCompleted()) {
            model.addRow(new Object[]{
                    m.getTournamentTitle(),
                    m.getRoundName(),
                    m.getTeam1Name() + " vs " + m.getTeam2Name(),
                    m.getTeam1Score() + " - " + m.getTeam2Score(),
                    m.getWinnerTeamName() != null ? m.getWinnerTeamName() : "-",
                    m.getMvpPlayerName() != null ? m.getMvpPlayerName() : "-",
                    m.getNotes()
            });
        }

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStandingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setOpaque(false);

        JLabel selectLabel = new JLabel("Select Tournament:");
        selectLabel.setFont(ThemeConfig.FONT_BOLD);
        JComboBox<Tournament> tCombo = new JComboBox<>();
        for (Tournament t : tournamentDAO.findAll()) tCombo.addItem(t);

        topBar.add(selectLabel);
        topBar.add(tCombo);
        card.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Rank", "Team Name", "Tag", "Stage / Group", "Played", "Won", "Lost", "Draws", "Score Diff", "Points"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable loadStandings = () -> {
            model.setRowCount(0);
            Tournament sel = (Tournament) tCombo.getSelectedItem();
            if (sel != null) {
                List<TournamentStanding> list = standingDAO.getStandingsByTournament(sel.getId());
                int rank = 1;
                for (TournamentStanding s : list) {
                    String rankStr = (rank == 1) ? "🥇 1" : (rank == 2) ? "🥈 2" : (rank == 3) ? "🥉 3" : String.valueOf(rank);
                    model.addRow(new Object[]{
                            rankStr, s.getTeamName(), s.getTeamTag(),
                            s.getGroupName() != null ? s.getGroupName() : "Overall",
                            s.getMatchesPlayed(), s.getWins(), s.getLosses(), s.getDraws(),
                            (s.getScoreDiff() > 0 ? "+" : "") + s.getScoreDiff(), s.getPoints()
                    });
                    rank++;
                }
            }
        };
        tCombo.addActionListener(e -> loadStandings.run());
        loadStandings.run();

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("⭐ Public Pro Player Leaderboard");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Rank", "Gamer Tag", "Team", "Primary Game", "Role", "Country", "Points", "Wins", "Kills", "MVPs", "Win Rate %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

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
                    p.getWins(),
                    p.getKills(),
                    p.getMvpCount(),
                    String.format("%.1f%%", p.getWinRate())
            });
            rank++;
        }

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeConfig.FONT_BOLD);
        tabs.setBackground(ThemeConfig.COLOR_CARD_BG);

        Map<String, Integer> tourneysByStatus = statsDAO.getTournamentsByStatus();
        Map<String, Integer> gamesPop = statsDAO.getTournamentsByGame();
        Map<String, Integer> topTeams = statsDAO.getTopTeamsByPoints(5);
        Map<String, Integer> topFraggers = statsDAO.getTopPlayersByKills(5);

        tabs.addTab("🏆 Tournaments by Status", ChartUtil.createPieChart("Tournament Distribution", tourneysByStatus));
        tabs.addTab("🎮 Game Popularity", ChartUtil.createBarChart("Tournaments per Game Title", "Game", "Tournaments", gamesPop, ThemeConfig.COLOR_ACCENT_CYAN));
        tabs.addTab("🛡️ Top Teams (Points)", ChartUtil.createBarChart("Top Esports Teams", "Team", "Points", topTeams, ThemeConfig.COLOR_ACCENT_PURPLE));
        tabs.addTab("🔥 Top Fraggers (Kills)", ChartUtil.createBarChart("Top Eliminations Leaderboard", "Player", "Kills", topFraggers, ThemeConfig.COLOR_DANGER));

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }
}
