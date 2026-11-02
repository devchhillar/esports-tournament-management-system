package com.esports.view.manager;

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
 * Team Manager Portal.
 * Enables team managers to manage rosters, register for tournaments, view schedules, and inspect team statistics.
 */
public class TeamManagerDashboard extends JFrame {

    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final RegistrationDAO registrationDAO = new RegistrationDAOImpl();
    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final GameDAO gameDAO = new GameDAOImpl();

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel activeTitleLabel;

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            TeamManagerDashboard dashboard = new TeamManagerDashboard();
            dashboard.setVisible(true);
        });
    }

    public TeamManagerDashboard() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("eSports Tournament Management System - Team Manager Command Hub");
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

        JPanel topHeader = createTopHeader();
        mainArea.add(topHeader, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(16, 20, 20, 20));

        contentPanel.add(createDashboardOverviewPanel(), "OVERVIEW");
        contentPanel.add(createMyTeamsPanel(), "MY_TEAM");
        contentPanel.add(createRosterPanel(), "ROSTER");
        contentPanel.add(createTournamentRegistrationPanel(), "REGISTRATION");
        contentPanel.add(createSchedulePanel(), "SCHEDULE");
        contentPanel.add(createResultsPanel(), "RESULTS");
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

        JLabel brandSub = new JLabel("TEAM MANAGER PORTAL");
        brandSub.setFont(ThemeConfig.FONT_SMALL);
        brandSub.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);
        sidebar.add(brandPanel);

        sidebar.add(createNavButton("📊 Manager Dashboard", "OVERVIEW", "Manager Overview & Summary"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🛡️ My Team Profile", "MY_TEAM", "Manage Team Profile & Info"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("👥 Squad & Roster", "ROSTER", "Player Roster & Recruitment"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("🏆 Tournament Entry", "REGISTRATION", "Tournament Registrations"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⚔️ Match Fixtures", "SCHEDULE", "Upcoming Match Schedules"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("📜 Match History", "RESULTS", "Team Match History & Results"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("⭐ Global Rankings", "RANKINGS", "Team Leaderboards"));

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

        activeTitleLabel = new JLabel("Manager Overview & Summary");
        activeTitleLabel.setFont(ThemeConfig.FONT_TITLE);
        activeTitleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        User current = UserSession.getInstance().getCurrentUser();
        String name = current != null ? current.getFullName() : "Team Manager";
        JLabel profileLabel = new JLabel("🛡️ " + name + " (TEAM MANAGER)");
        profileLabel.setFont(ThemeConfig.FONT_BOLD);
        profileLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        header.add(activeTitleLabel, BorderLayout.WEST);
        header.add(profileLabel, BorderLayout.EAST);

        return header;
    }

    // 1. Dashboard Overview
    private JPanel createDashboardOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        User user = UserSession.getInstance().getCurrentUser();
        int userId = user != null ? user.getId() : 2;
        List<Team> myTeams = teamDAO.findByUserId(userId);

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);

        int totalMembers = 0;
        int totalWins = 0;
        int totalMatches = 0;
        for (Team t : myTeams) {
            totalMembers += t.getMemberCount();
            totalWins += t.getMatchesWon();
            totalMatches += t.getMatchesPlayed();
        }

        kpiRow.add(createKpiCard("🛡️ Teams Managed", String.valueOf(myTeams.size()), ThemeConfig.COLOR_ACCENT_CYAN));
        kpiRow.add(createKpiCard("👥 Squad Members", String.valueOf(totalMembers), ThemeConfig.COLOR_SUCCESS));
        kpiRow.add(createKpiCard("🏆 Matches Won", totalWins + " / " + totalMatches, ThemeConfig.COLOR_WARNING));
        kpiRow.add(createKpiCard("⭐ Win Rate", (totalMatches > 0 ? String.format("%.1f%%", (double) totalWins / totalMatches * 100) : "0.0%"), ThemeConfig.COLOR_ACCENT_PURPLE));

        panel.add(kpiRow, BorderLayout.NORTH);

        // Center card with Team summary table
        JPanel centerCard = UIComponents.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 10));
        JLabel title = new JLabel("My Active eSports Teams");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        centerCard.add(title, BorderLayout.NORTH);

        String[] cols = {"Team ID", "Team Name", "Tag", "Game", "Members", "Played", "Wins", "Losses", "Points", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Team t : myTeams) {
            model.addRow(new Object[]{
                    t.getId(), t.getTeamName(), "[" + t.getTeamTag() + "]", t.getGame(),
                    t.getMemberCount(), t.getMatchesPlayed(), t.getMatchesWon(), t.getMatchesLost(),
                    t.getTotalPoints(), t.getStatus()
            });
        }
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        centerCard.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(centerCard, BorderLayout.CENTER);
        return panel;
    }

    // 2. My Teams & Profile Management
    private JPanel createMyTeamsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Team Profile & Details");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        JButton createBtn = UIComponents.createPrimaryButton("➕ Create New Team");
        JButton editBtn = UIComponents.createAccentButton("✏️ Edit Team Profile");

        btns.add(createBtn);
        btns.add(editBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(btns, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Team Name", "Tag", "Game Category", "Leader / Captain", "Roster Size", "Points", "Status", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        User user = UserSession.getInstance().getCurrentUser();
        int userId = user != null ? user.getId() : 2;
        Runnable refreshTeams = () -> {
            model.setRowCount(0);
            List<Team> list = teamDAO.findByUserId(userId);
            for (Team t : list) {
                model.addRow(new Object[]{
                        t.getId(), t.getTeamName(), "[" + t.getTeamTag() + "]", t.getGame(),
                        t.getLeaderName() != null ? t.getLeaderName() : "-",
                        t.getMemberCount(), t.getTotalPoints(), t.getStatus(), t.getDescription()
                });
            }
        };
        refreshTeams.run();

        createBtn.addActionListener(e -> {
            showCreateTeamDialog(userId, refreshTeams);
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int teamId = (Integer) model.getValueAt(row, 0);
                showEditTeamDialog(teamId, refreshTeams);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a team to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // 3. Squad Roster Management
    private JPanel createRosterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        User user = UserSession.getInstance().getCurrentUser();
        int userId = user != null ? user.getId() : 2;
        List<Team> myTeams = teamDAO.findByUserId(userId);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel selLabel = new JLabel("Select Managed Team:");
        selLabel.setFont(ThemeConfig.FONT_BOLD);
        JComboBox<Team> teamCombo = new JComboBox<>();
        for (Team t : myTeams) teamCombo.addItem(t);

        left.add(selLabel);
        left.add(teamCombo);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        JButton recruitBtn = UIComponents.createPrimaryButton("➕ Recruit Player");
        JButton removeBtn = UIComponents.createDangerButton("❌ Remove Member");
        JButton captainBtn = UIComponents.createAccentButton("⭐ Set Captain");

        btns.add(recruitBtn);
        btns.add(captainBtn);
        btns.add(removeBtn);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(btns, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Player ID", "Gamer Tag", "Full Name", "Primary Game", "Role", "Country", "Ranking Pts", "Kills", "MVPs"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        Runnable refreshRoster = () -> {
            model.setRowCount(0);
            Team selTeam = (Team) teamCombo.getSelectedItem();
            if (selTeam != null) {
                List<Player> members = playerDAO.findByTeamId(selTeam.getId());
                for (Player p : members) {
                    model.addRow(new Object[]{
                            p.getId(), p.getGamerTag(), p.getFullName(), p.getPrimaryGame(),
                            p.getInGameRole(), p.getCountry(), p.getRankingPoints(), p.getKills(), p.getMvpCount()
                    });
                }
            }
        };
        refreshRoster.run();
        teamCombo.addActionListener(e -> refreshRoster.run());

        recruitBtn.addActionListener(e -> {
            Team selTeam = (Team) teamCombo.getSelectedItem();
            if (selTeam == null) return;

            List<Player> available = playerDAO.findAvailablePlayers(selTeam.getGame());
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available free agent players found for " + selTeam.getGame() + ".", "No Players Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JComboBox<Player> availBox = new JComboBox<>();
            for (Player p : available) availBox.addItem(p);

            int opt = JOptionPane.showConfirmDialog(this, availBox, "Select Player to Recruit into " + selTeam.getTeamName(), JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                Player p = (Player) availBox.getSelectedItem();
                if (p != null) {
                    if (teamDAO.addMember(selTeam.getId(), p.getId(), "MEMBER")) {
                        JOptionPane.showMessageDialog(this, "Player " + p.getGamerTag() + " successfully joined the team!", "Recruited", JOptionPane.INFORMATION_MESSAGE);
                        refreshRoster.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Could not add player: Team roster has reached maximum size or game is incompatible.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        captainBtn.addActionListener(e -> {
            Team selTeam = (Team) teamCombo.getSelectedItem();
            int r = table.getSelectedRow();
            if (selTeam != null && r >= 0) {
                int pId = (Integer) model.getValueAt(r, 0);
                if (teamDAO.assignCaptain(selTeam.getId(), pId)) {
                    JOptionPane.showMessageDialog(this, "Captain assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshRoster.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a player to assign as captain.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        removeBtn.addActionListener(e -> {
            Team selTeam = (Team) teamCombo.getSelectedItem();
            int r = table.getSelectedRow();
            if (selTeam != null && r >= 0) {
                int pId = (Integer) model.getValueAt(r, 0);
                if (teamDAO.removeMember(selTeam.getId(), pId)) {
                    JOptionPane.showMessageDialog(this, "Player removed from roster.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshRoster.run();
                }
            }
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // 4. Tournament Registration
    private JPanel createTournamentRegistrationPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 14));
        panel.setOpaque(false);

        User user = UserSession.getInstance().getCurrentUser();
        int userId = user != null ? user.getId() : 2;
        List<Team> myTeams = teamDAO.findByUserId(userId);

        // Top Card: Open Tournaments
        JPanel openTourneyCard = UIComponents.createCardPanel();
        openTourneyCard.setLayout(new BorderLayout(0, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title1 = new JLabel("🏆 Open Tournaments Available for Registration");
        title1.setFont(ThemeConfig.FONT_SUBTITLE);
        title1.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        JButton regBtn = UIComponents.createPrimaryButton("⚡ Register My Team");
        topBar.add(title1, BorderLayout.WEST);
        topBar.add(regBtn, BorderLayout.EAST);
        openTourneyCard.add(topBar, BorderLayout.NORTH);

        String[] cols1 = {"ID", "Tournament Title", "Game", "Format", "Prize Pool", "Entry Fee", "Max Teams", "Status", "Start Date"};
        DefaultTableModel model1 = new DefaultTableModel(cols1, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table1 = new JTable(model1);
        table1.setShowGrid(true);
        table1.setFillsViewportHeight(true);

        List<Tournament> openTourneys = tournamentDAO.findByStatus("REGISTRATION_OPEN");
        for (Tournament t : openTourneys) {
            model1.addRow(new Object[]{
                    t.getId(), t.getTitle(), t.getGame(), t.getFormat(),
                    "₹ " + t.getPrizePool(), "₹ " + t.getEntryFee(), t.getMaxTeams(),
                    t.getStatus(), t.getStartDate() != null ? t.getStartDate().toString().substring(0, 16) : "-"
            });
        }
        openTourneyCard.add(new JScrollPane(table1), BorderLayout.CENTER);
        panel.add(openTourneyCard);

        // Bottom Card: My Registrations Status
        JPanel myRegsCard = UIComponents.createCardPanel();
        myRegsCard.setLayout(new BorderLayout(0, 10));
        JLabel title2 = new JLabel("📋 My Team Tournament Registrations & Status");
        title2.setFont(ThemeConfig.FONT_SUBTITLE);
        title2.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        myRegsCard.add(title2, BorderLayout.NORTH);

        String[] cols2 = {"Registration ID", "Tournament Title", "Game", "Team Name", "Registered Date", "Approval Status"};
        DefaultTableModel model2 = new DefaultTableModel(cols2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table2 = new JTable(model2);
        table2.setShowGrid(true);
        table2.setFillsViewportHeight(true);

        Runnable refreshMyRegs = () -> {
            model2.setRowCount(0);
            for (Team t : myTeams) {
                List<Registration> regs = registrationDAO.findByTeam(t.getId());
                for (Registration r : regs) {
                    model2.addRow(new Object[]{
                            r.getId(), r.getTournamentTitle(), r.getGameName(),
                            r.getTeamName(), r.getRegistrationDate() != null ? r.getRegistrationDate().toString().substring(0, 16) : "-",
                            r.getStatus()
                    });
                }
            }
        };
        refreshMyRegs.run();

        regBtn.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an open tournament from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int tourneyId = (Integer) model1.getValueAt(row, 0);
            String tourneyGame = (String) model1.getValueAt(row, 2);

            // Filter user teams matching game
            JComboBox<Team> teamSelect = new JComboBox<>();
            for (Team tm : myTeams) {
                if (tm.getGame().equalsIgnoreCase(tourneyGame)) {
                    teamSelect.addItem(tm);
                }
            }

            if (teamSelect.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "You do not manage any teams eligible for " + tourneyGame + ".", "Ineligible Team", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int opt = JOptionPane.showConfirmDialog(this, teamSelect, "Select Team to Register", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                Team selTeam = (Team) teamSelect.getSelectedItem();
                if (selTeam != null) {
                    if (registrationDAO.isAlreadyRegistered(tourneyId, selTeam.getId())) {
                        JOptionPane.showMessageDialog(this, "Team is already registered for this tournament.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Tournament tourney = tournamentDAO.findById(tourneyId);
                    String txnId = null;
                    if (tourney != null && tourney.getEntryFee() != null && tourney.getEntryFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{
                                "UPI (GPay / PhonePe / Paytm)",
                                "Credit / Debit Card",
                                "Net Banking",
                                "Gaming Wallet"
                        });
                        JPanel payPanel = new JPanel(new GridLayout(3, 1, 0, 8));
                        payPanel.add(new JLabel("Tournament Entry Fee: ₹ " + tourney.getEntryFee()));
                        payPanel.add(new JLabel("Select Payment Method:"));
                        payPanel.add(paymentMethodBox);

                        int payOpt = JOptionPane.showConfirmDialog(this, payPanel, "Tournament Entry Fee Payment - " + tourney.getTitle(), JOptionPane.OK_CANCEL_OPTION);
                        if (payOpt != JOptionPane.OK_OPTION) {
                            return; // Manager cancelled payment
                        }

                        String chosenMethod = "UPI";
                        int selIdx = paymentMethodBox.getSelectedIndex();
                        if (selIdx == 1) chosenMethod = "CREDIT_CARD";
                        else if (selIdx == 2) chosenMethod = "NET_BANKING";
                        else if (selIdx == 3) chosenMethod = "WALLET";

                        txnId = "TXN_" + System.currentTimeMillis();
                        Payment p = new Payment();
                        p.setTournamentId(tourneyId);
                        p.setTeamId(selTeam.getId());
                        p.setUserId(userId);
                        p.setAmount(tourney.getEntryFee());
                        p.setPaymentType("ENTRY_FEE");
                        p.setPaymentMethod(chosenMethod);
                        p.setTransactionId(txnId);
                        p.setStatus("COMPLETED");
                        p.setNotes("Online Entry Fee Payment for " + selTeam.getTeamName() + " (" + tourney.getTitle() + ")");
                        new PaymentDAOImpl().recordPayment(p);
                    }

                    if (registrationDAO.registerTeam(tourneyId, selTeam.getId(), userId)) {
                        String msg = "Registration submitted successfully! Status is PENDING admin approval.";
                        if (txnId != null) {
                            msg += "\nPayment confirmed! Transaction ID: " + txnId;
                        }
                        JOptionPane.showMessageDialog(this, msg, "Registration Submitted", JOptionPane.INFORMATION_MESSAGE);
                        refreshMyRegs.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to submit registration.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        myRegsCard.add(new JScrollPane(table2), BorderLayout.CENTER);
        panel.add(myRegsCard);

        return panel;
    }

    // 5. Match Schedule
    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("⚔️ Upcoming Match Schedules & Fixtures");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Match ID", "Tournament", "Round / Bracket", "Matchup", "Scheduled Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        User user = UserSession.getInstance().getCurrentUser();
        int userId = user != null ? user.getId() : 2;
        List<Team> myTeams = teamDAO.findByUserId(userId);
        for (Team tm : myTeams) {
            List<Match> matches = matchDAO.findByTeam(tm.getId());
            for (Match m : matches) {
                if (!m.getStatus().equalsIgnoreCase("COMPLETED")) {
                    model.addRow(new Object[]{
                            m.getId(), m.getTournamentTitle(), m.getRoundName(),
                            m.getTeam1Name() + " vs " + m.getTeam2Name(),
                            m.getScheduledTime() != null ? m.getScheduledTime().toString().substring(0, 16) : "-",
                            m.getStatus()
                    });
                }
            }
        }

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // 6. Results
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("📜 Completed Matches & Results");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Match ID", "Tournament", "Round", "Team 1", "Score", "Team 2", "Winner", "MVP Player", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        User user = UserSession.getInstance().getCurrentUser();
        int userId = user != null ? user.getId() : 2;
        List<Team> myTeams = teamDAO.findByUserId(userId);
        for (Team tm : myTeams) {
            List<Match> matches = matchDAO.findByTeam(tm.getId());
            for (Match m : matches) {
                if (m.getStatus().equalsIgnoreCase("COMPLETED")) {
                    model.addRow(new Object[]{
                            m.getId(), m.getTournamentTitle(), m.getRoundName(),
                            m.getTeam1Name(), m.getTeam1Score() + " - " + m.getTeam2Score(),
                            m.getTeam2Name(), m.getWinnerTeamName(),
                            m.getMvpPlayerName() != null ? m.getMvpPlayerName() : "-",
                            m.getNotes()
                    });
                }
            }
        }

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // 7. Rankings
    private JPanel createRankingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("⭐ Global Team Rankings & Leaderboard");
        title.setFont(ThemeConfig.FONT_SUBTITLE);
        title.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Rank", "Team Name", "Tag", "Game", "Played", "Won", "Lost", "Total Points", "Win Rate %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        List<Team> all = teamDAO.findAll();
        int rank = 1;
        for (Team t : all) {
            String rankStr = (rank == 1) ? "🥇 1" : (rank == 2) ? "🥈 2" : (rank == 3) ? "🥉 3" : String.valueOf(rank);
            model.addRow(new Object[]{
                    rankStr, t.getTeamName(), "[" + t.getTeamTag() + "]", t.getGame(),
                    t.getMatchesPlayed(), t.getMatchesWon(), t.getMatchesLost(),
                    t.getTotalPoints(), String.format("%.1f%%", t.getWinRate())
            });
            rank++;
        }

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void showCreateTeamDialog(int userId, Runnable onDone) {
        JDialog dialog = new JDialog(this, "Create eSports Team", true);
        dialog.setSize(500, 440);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameField = UIComponents.createTextField(15);
        JTextField tagField = UIComponents.createTextField(6);
        JComboBox<String> gameBox = new JComboBox<>();
        for (Game g : gameDAO.findActive()) gameBox.addItem(g.getName());
        JTextField descField = UIComponents.createTextField(15);

        final String[] uploadedLogoPath = {null};
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoPanel.setOpaque(false);
        JLabel logoStatus = new JLabel("No logo chosen");
        logoStatus.setFont(ThemeConfig.FONT_SMALL);
        logoStatus.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadLogoBtn = UIComponents.createSecondaryButton("📁 Upload Logo");
        uploadLogoBtn.addActionListener(e -> {
            String savedPath = com.esports.util.FileUploadUtil.chooseAndSaveImage(dialog, "logos");
            if (savedPath != null) {
                uploadedLogoPath[0] = savedPath;
                logoStatus.setText("Uploaded: " + new java.io.File(savedPath).getName());
                logoStatus.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        logoPanel.add(uploadLogoBtn);
        logoPanel.add(logoStatus);

        panel.add(new JLabel("Team Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Team Tag:"));
        panel.add(tagField);
        panel.add(new JLabel("Game Category:"));
        panel.add(gameBox);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Team Logo:"));
        panel.add(logoPanel);

        JButton saveBtn = UIComponents.createPrimaryButton("Create Team");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String tag = tagField.getText().trim().toUpperCase();
            if (name.isEmpty() || tag.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Team name and tag are required.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Team t = new Team();
            t.setTeamName(name);
            t.setTeamTag(tag);
            t.setGame((String) gameBox.getSelectedItem());
            t.setLeaderId(userId);
            t.setManagerId(userId);
            t.setDescription(descField.getText().trim());
            t.setLogoPath(uploadedLogoPath[0]);

            if (teamDAO.create(t, userId)) {
                JOptionPane.showMessageDialog(dialog, "Team created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                onDone.run();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create team. Ensure name and tag are unique.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditTeamDialog(int teamId, Runnable onDone) {
        Team t = teamDAO.findById(teamId);
        if (t == null) return;

        JDialog dialog = new JDialog(this, "Edit Team Details - " + t.getTeamName(), true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameField = UIComponents.createTextField(15);
        nameField.setText(t.getTeamName());
        JTextField tagField = UIComponents.createTextField(6);
        tagField.setText(t.getTeamTag());
        JTextField descField = UIComponents.createTextField(15);
        descField.setText(t.getDescription() != null ? t.getDescription() : "");
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "DISBANDED"});
        statusBox.setSelectedItem(t.getStatus());

        final String[] updatedLogoPath = {t.getLogoPath()};
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoPanel.setOpaque(false);
        JLabel logoStatus = new JLabel(t.getLogoPath() != null ? new java.io.File(t.getLogoPath()).getName() : "No logo set");
        logoStatus.setFont(ThemeConfig.FONT_SMALL);
        logoStatus.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadLogoBtn = UIComponents.createSecondaryButton("📁 Change Logo");
        uploadLogoBtn.addActionListener(e -> {
            String savedPath = com.esports.util.FileUploadUtil.chooseAndSaveImage(dialog, "logos");
            if (savedPath != null) {
                updatedLogoPath[0] = savedPath;
                logoStatus.setText("New: " + new java.io.File(savedPath).getName());
                logoStatus.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        logoPanel.add(uploadLogoBtn);
        logoPanel.add(logoStatus);

        panel.add(new JLabel("Team Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Team Tag:"));
        panel.add(tagField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Team Logo:"));
        panel.add(logoPanel);

        JButton saveBtn = UIComponents.createPrimaryButton("Save Profile");
        saveBtn.addActionListener(e -> {
            t.setTeamName(nameField.getText().trim());
            t.setTeamTag(tagField.getText().trim().toUpperCase());
            t.setDescription(descField.getText().trim());
            t.setStatus((String) statusBox.getSelectedItem());
            t.setLogoPath(updatedLogoPath[0]);

            if (teamDAO.update(t)) {
                JOptionPane.showMessageDialog(dialog, "Team updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                onDone.run();
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JPanel createKpiCard(String title, String val, Color accent) {
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeConfig.FONT_SMALL);
        titleLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JLabel valLabel = new JLabel(val);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }
}
