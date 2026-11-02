package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.MatchDAO;
import com.esports.dao.MatchDAOImpl;
import com.esports.dao.StatsDAO;
import com.esports.dao.StatsDAOImpl;
import com.esports.model.Match;
import com.esports.view.common.ChartUtil;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Main eSports Dashboard Overview Panel.
 * Dynamically visualizes KPI metrics, JFreeChart graphs, live matches, and recent results.
 */
public class AdminOverviewPanel extends JPanel {

    private final StatsDAO statsDAO = new StatsDAOImpl();
    private final MatchDAO matchDAO = new MatchDAOImpl();

    // Header Refresh button
    private JButton refreshBtn;

    // KPI labels
    private JLabel totalTourneysLabel;
    private JLabel activeTourneysLabel;
    private JLabel upcomingTourneysLabel;
    private JLabel completedTourneysLabel;

    private JLabel totalTeamsLabel;
    private JLabel totalPlayersLabel;
    private JLabel totalMatchesLabel;
    private JLabel pendingRegsLabel;

    // Charts & Tables
    private JPanel chartsContainer;
    private DefaultListModel<String> activityListModel;
    private DefaultTableModel upcomingMatchesModel;

    public AdminOverviewPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadDynamicData();
    }

    private void initializeUI() {
        JPanel scrollRoot = new JPanel();
        scrollRoot.setLayout(new BoxLayout(scrollRoot, BoxLayout.Y_AXIS));
        scrollRoot.setOpaque(false);
        scrollRoot.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Header with Refresh Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel titleLabel = new JLabel("⚡ TOURNAMENT OPERATIONS OVERVIEW");
        titleLabel.setFont(ThemeConfig.FONT_TITLE);
        titleLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh Live Analytics");
        refreshBtn.addActionListener(e -> loadDynamicData());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        scrollRoot.add(headerPanel);
        scrollRoot.add(Box.createVerticalStrut(14));

        // 2. Row 1 KPI Metrics: Tournaments
        JPanel row1 = new JPanel(new GridLayout(1, 4, 14, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        totalTourneysLabel = new JLabel("0", SwingConstants.CENTER);
        activeTourneysLabel = new JLabel("0", SwingConstants.CENTER);
        upcomingTourneysLabel = new JLabel("0", SwingConstants.CENTER);
        completedTourneysLabel = new JLabel("0", SwingConstants.CENTER);

        row1.add(createKpiCard("🏆 Total Tournaments", totalTourneysLabel, ThemeConfig.COLOR_ACCENT_CYAN));
        row1.add(createKpiCard("⚡ Active Tournaments", activeTourneysLabel, ThemeConfig.COLOR_SUCCESS));
        row1.add(createKpiCard("⏳ Upcoming Tournaments", upcomingTourneysLabel, ThemeConfig.COLOR_WARNING));
        row1.add(createKpiCard("🏁 Completed Tournaments", completedTourneysLabel, new Color(168, 85, 247)));
        scrollRoot.add(row1);
        scrollRoot.add(Box.createVerticalStrut(14));

        // 3. Row 2 KPI Metrics: Teams, Players, Matches, Registrations
        JPanel row2 = new JPanel(new GridLayout(1, 4, 14, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        totalTeamsLabel = new JLabel("0", SwingConstants.CENTER);
        totalPlayersLabel = new JLabel("0", SwingConstants.CENTER);
        totalMatchesLabel = new JLabel("0", SwingConstants.CENTER);
        pendingRegsLabel = new JLabel("0", SwingConstants.CENTER);

        row2.add(createKpiCard("🛡️ Total Teams", totalTeamsLabel, ThemeConfig.COLOR_ACCENT_BLUE));
        row2.add(createKpiCard("👤 Registered Players", totalPlayersLabel, new Color(59, 130, 246)));
        row2.add(createKpiCard("⚔️ Total Matches (Comp/Rem)", totalMatchesLabel, ThemeConfig.COLOR_ACCENT_PURPLE));
        row2.add(createKpiCard("📋 Pending Registrations", pendingRegsLabel, ThemeConfig.COLOR_DANGER));
        scrollRoot.add(row2);
        scrollRoot.add(Box.createVerticalStrut(18));

        // 4. Graphical Charts Section (JFreeChart)
        chartsContainer = UIComponents.createCardPanel();
        chartsContainer.setLayout(new BorderLayout(0, 10));
        chartsContainer.setPreferredSize(new Dimension(1000, 360));
        chartsContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JLabel chartsTitle = new JLabel("📊 PERFORMANCE ANALYTICS & JFREECHART VISUALIZATIONS");
        chartsTitle.setFont(ThemeConfig.FONT_SUBTITLE);
        chartsTitle.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        chartsContainer.add(chartsTitle, BorderLayout.NORTH);

        scrollRoot.add(chartsContainer);
        scrollRoot.add(Box.createVerticalStrut(18));

        // 5. Bottom Split: Recent Activity & Upcoming Matches
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.setPreferredSize(new Dimension(1000, 280));
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // Left Card: Recent Activity Feed
        JPanel activityCard = UIComponents.createCardPanel();
        activityCard.setLayout(new BorderLayout(0, 10));
        JLabel actTitle = new JLabel("🔔 Recent Tournament & Match Activity");
        actTitle.setFont(ThemeConfig.FONT_BOLD);
        actTitle.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        activityCard.add(actTitle, BorderLayout.NORTH);

        activityListModel = new DefaultListModel<>();
        JList<String> activityList = new JList<>(activityListModel);
        activityList.setBackground(ThemeConfig.COLOR_CARD_BG);
        activityList.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        activityList.setFont(ThemeConfig.FONT_REGULAR);
        activityList.setSelectionBackground(ThemeConfig.COLOR_BG_DARK);
        activityList.setSelectionForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        JScrollPane actScroll = new JScrollPane(activityList);
        actScroll.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        activityCard.add(actScroll, BorderLayout.CENTER);
        bottomRow.add(activityCard);

        // Right Card: Upcoming Scheduled Matches
        JPanel matchCard = UIComponents.createCardPanel();
        matchCard.setLayout(new BorderLayout(0, 10));
        JLabel matchTitle = new JLabel("⚔️ Upcoming Scheduled Fixtures");
        matchTitle.setFont(ThemeConfig.FONT_BOLD);
        matchTitle.setForeground(ThemeConfig.COLOR_SUCCESS);
        matchCard.add(matchTitle, BorderLayout.NORTH);

        String[] cols = {"Tournament", "Round", "Matchup", "Time", "Status"};
        upcomingMatchesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable upcomingTable = new JTable(upcomingMatchesModel);
        upcomingTable.setShowGrid(true);
        upcomingTable.setFillsViewportHeight(true);
        JScrollPane matchScroll = new JScrollPane(upcomingTable);
        matchScroll.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        matchCard.add(matchScroll, BorderLayout.CENTER);
        bottomRow.add(matchCard);

        scrollRoot.add(bottomRow);

        JScrollPane mainScrollPane = new JScrollPane(scrollRoot);
        mainScrollPane.setBorder(null);
        mainScrollPane.setOpaque(false);
        mainScrollPane.getViewport().setOpaque(false);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeConfig.FONT_SMALL);
        titleLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadDynamicData() {
        if (refreshBtn != null) {
            refreshBtn.setEnabled(false);
            refreshBtn.setText("🔄 Refreshing...");
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> metrics;
            private Map<String, Integer> tourneysByStatus;
            private List<StatsDAO.GamePopularityStat> gamesPopularity;
            private List<StatsDAO.TeamPointsStat> topTeams;
            private List<StatsDAO.PlayerKillsStat> topPlayers;
            private List<String> activities;
            private List<Match> upcoming;

            @Override
            protected Void doInBackground() {
                metrics = statsDAO.getOverallMetrics();
                tourneysByStatus = statsDAO.getTournamentsByStatus();
                gamesPopularity = statsDAO.getGamePopularityStats();
                topTeams = statsDAO.getTopTeamsStats(5);
                topPlayers = statsDAO.getTopPlayersStats(5);
                activities = statsDAO.getRecentActivityLogs(15);
                upcoming = matchDAO.findUpcoming();
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (metrics != null) {
                        totalTourneysLabel.setText(String.valueOf(metrics.getOrDefault("totalTournaments", 0)));
                        activeTourneysLabel.setText(String.valueOf(metrics.getOrDefault("activeTournaments", 0)));
                        upcomingTourneysLabel.setText(String.valueOf(metrics.getOrDefault("upcomingTournaments", 0)));
                        completedTourneysLabel.setText(String.valueOf(metrics.getOrDefault("completedTournaments", 0)));

                        totalTeamsLabel.setText(String.valueOf(metrics.getOrDefault("totalTeams", 0)));
                        totalPlayersLabel.setText(String.valueOf(metrics.getOrDefault("totalPlayers", 0)));

                        int comp = metrics.getOrDefault("matchesCompleted", 0);
                        int rem = metrics.getOrDefault("matchesRemaining", 0);
                        totalMatchesLabel.setText(comp + " / " + rem);
                        totalMatchesLabel.setToolTipText("Completed Matches: " + comp + " | Remaining Matches: " + rem + " | Total Matches: " + (comp + rem));

                        pendingRegsLabel.setText(String.valueOf(metrics.getOrDefault("pendingRegistrations", 0)));
                    }

                    // Render dynamic JFreeChart Visualizations
                    renderCharts(tourneysByStatus, gamesPopularity, topTeams, topPlayers);

                    // Populate Activity Feed
                    activityListModel.clear();
                    if (activities != null && !activities.isEmpty()) {
                        for (String act : activities) {
                            activityListModel.addElement("• " + act);
                        }
                    } else {
                        activityListModel.addElement("No recent activity recorded.");
                    }

                    // Populate Upcoming Matches
                    upcomingMatchesModel.setRowCount(0);
                    if (upcoming != null) {
                        for (Match m : upcoming) {
                            String timeStr = "-";
                            if (m.getScheduledTime() != null) {
                                String full = m.getScheduledTime().toString();
                                timeStr = full.substring(0, Math.min(16, full.length()));
                            }
                            upcomingMatchesModel.addRow(new Object[]{
                                    m.getTournamentTitle(),
                                    m.getRoundName(),
                                    m.getTeam1Name() + " vs " + m.getTeam2Name(),
                                    timeStr,
                                    m.getStatus()
                            });
                        }
                    }
                } finally {
                    if (refreshBtn != null) {
                        refreshBtn.setEnabled(true);
                        refreshBtn.setText("🔄 Refresh Live Analytics");
                    }
                }
            }
        };
        worker.execute();
    }

    private void renderCharts(Map<String, Integer> tourneysByStatus,
                              List<StatsDAO.GamePopularityStat> gamesPop,
                              List<StatsDAO.TeamPointsStat> topTeams,
                              List<StatsDAO.PlayerKillsStat> topPlayers) {
        chartsContainer.removeAll();

        JLabel chartsTitle = new JLabel("📊 PERFORMANCE ANALYTICS & JFREECHART VISUALIZATIONS");
        chartsTitle.setFont(ThemeConfig.FONT_SUBTITLE);
        chartsTitle.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        chartsContainer.add(chartsTitle, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeConfig.FONT_BOLD);
        tabbedPane.setBackground(ThemeConfig.COLOR_CARD_BG);

        // Chart 1: Tournament Status Pie (Dynamic status counts & percentages)
        tabbedPane.addTab("🏆 Tournaments by Status", ChartUtil.createPieChart("Tournament Status Distribution", tourneysByStatus));

        // Chart 2: Game Popularity Grouped Bar Chart (Tourneys, Teams, Players sorted by tourney count desc)
        tabbedPane.addTab("🎮 Game Popularity", ChartUtil.createGamePopularityChart("Game Popularity & Participation", gamesPop));

        // Chart 3: Top Teams Leaderboard (Points, Wins, Matches Played sorted by points desc)
        tabbedPane.addTab("🛡️ Top Teams (Points)", ChartUtil.createTopTeamsChart("Top Performing Esports Teams", topTeams));

        // Chart 4: Top Players Leaderboard (Kills, Team Name sorted by kills desc with empty state fallback)
        tabbedPane.addTab("🔥 Top Players (Kills)", ChartUtil.createTopPlayersChart("Top Fraggers / Eliminators", topPlayers));

        chartsContainer.add(tabbedPane, BorderLayout.CENTER);
        chartsContainer.revalidate();
        chartsContainer.repaint();
    }
}
