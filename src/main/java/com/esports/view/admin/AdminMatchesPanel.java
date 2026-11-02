package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.*;
import com.esports.model.*;
import com.esports.view.common.BracketViewerDialog;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Admin Panel for Scheduling Matches, Conflict Detection, Score Entry, and Bracket Visualization.
 */
public class AdminMatchesPanel extends JPanel {

    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();

    private DefaultTableModel tableModel;
    private JTable matchTable;
    private JComboBox<String> tournamentFilter;
    private JComboBox<String> statusFilter;
    private JComboBox<String> timeFilter;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    public AdminMatchesPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadMatches();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Top Controls Bar - Responsive 2-Tier Layout to Prevent Overlapping
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);

        // Row 1: Filters Bar
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterRow.setOpaque(false);

        tournamentFilter = new JComboBox<>();
        tournamentFilter.addItem("All Tournaments");
        for (Tournament t : tournamentDAO.findAll()) {
            tournamentFilter.addItem(t.getId() + " - " + t.getTitle());
        }
        tournamentFilter.addActionListener(e -> applyFilter());

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "SCHEDULED", "LIVE", "COMPLETED", "CANCELLED"});
        statusFilter.addActionListener(e -> applyFilter());

        timeFilter = new JComboBox<>(new String[]{"All Dates", "Today's Schedule", "This Week's Schedule"});
        timeFilter.addActionListener(e -> applyFilter());

        JLabel tourneyLbl = new JLabel("Tournament:");
        tourneyLbl.setFont(ThemeConfig.FONT_BOLD);
        tourneyLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(ThemeConfig.FONT_BOLD);
        statusLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel timeLbl = new JLabel("Timeline:");
        timeLbl.setFont(ThemeConfig.FONT_BOLD);
        timeLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        filterRow.add(tourneyLbl);
        filterRow.add(tournamentFilter);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(statusLbl);
        filterRow.add(statusFilter);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(timeLbl);
        filterRow.add(timeFilter);

        // Row 2: Match Operations Buttons Bar
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        buttonRow.setOpaque(false);

        JButton scheduleBtn = UIComponents.createPrimaryButton("➕ Schedule");
        JButton rescheduleBtn = UIComponents.createSecondaryButton("🕒 Reschedule");
        JButton cancelMatchBtn = UIComponents.createDangerButton("🚫 Cancel Match");
        JButton liveBtn = UIComponents.createStyledButton("🔴 Go Live", new Color(220, 38, 38), Color.WHITE, new Color(239, 68, 68));
        JButton scoreBtn = UIComponents.createAccentButton("🏆 Score & Winner");
        JButton bracketBtn = UIComponents.createStyledButton("📊 Visual Bracket", new Color(123, 44, 191), Color.WHITE, new Color(157, 78, 221));
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        scheduleBtn.addActionListener(e -> showScheduleDialog());
        rescheduleBtn.addActionListener(e -> showRescheduleDialog());
        cancelMatchBtn.addActionListener(e -> cancelSelectedMatch());
        liveBtn.addActionListener(e -> setLiveStatus());
        scoreBtn.addActionListener(e -> showScoreDialog());
        bracketBtn.addActionListener(e -> openBracketViewer());
        refreshBtn.addActionListener(e -> loadMatches());

        buttonRow.add(scheduleBtn);
        buttonRow.add(rescheduleBtn);
        buttonRow.add(cancelMatchBtn);
        buttonRow.add(liveBtn);
        buttonRow.add(scoreBtn);
        buttonRow.add(bracketBtn);
        buttonRow.add(refreshBtn);

        topBar.add(filterRow);
        topBar.add(Box.createVerticalStrut(4));
        topBar.add(buttonRow);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Table
        String[] cols = {"ID", "Tournament", "Round / Bracket", "Team 1", "Team 2", "Score", "Winner", "Scheduled (IST)", "Venue / Room", "Referee", "Status", "MVP"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        matchTable = new JTable(tableModel);
        matchTable.setShowGrid(true);
        matchTable.setFillsViewportHeight(true);
        matchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matchTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        matchTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        matchTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        matchTable.getColumnModel().getColumn(0).setMinWidth(40);
        matchTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        matchTable.getColumnModel().getColumn(1).setMinWidth(130);
        matchTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        matchTable.getColumnModel().getColumn(2).setMinWidth(110);
        matchTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        matchTable.getColumnModel().getColumn(3).setMinWidth(120);
        matchTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        matchTable.getColumnModel().getColumn(4).setMinWidth(120);
        matchTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        matchTable.getColumnModel().getColumn(5).setMinWidth(70);
        matchTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        matchTable.getColumnModel().getColumn(6).setMinWidth(120);
        matchTable.getColumnModel().getColumn(7).setPreferredWidth(140);
        matchTable.getColumnModel().getColumn(7).setMinWidth(110);
        matchTable.getColumnModel().getColumn(8).setPreferredWidth(140);
        matchTable.getColumnModel().getColumn(8).setMinWidth(110);
        matchTable.getColumnModel().getColumn(9).setPreferredWidth(130);
        matchTable.getColumnModel().getColumn(9).setMinWidth(100);
        matchTable.getColumnModel().getColumn(10).setPreferredWidth(100);
        matchTable.getColumnModel().getColumn(10).setMinWidth(85);
        matchTable.getColumnModel().getColumn(11).setPreferredWidth(120);
        matchTable.getColumnModel().getColumn(11).setMinWidth(95);

        JScrollPane scrollPane = new JScrollPane(matchTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainCard.add(scrollPane, BorderLayout.CENTER);

        // Footer note
        JLabel tzLabel = new JLabel("🕒 All competitive match schedules are synchronized to Indian Standard Time (IST - Asia/Kolkata). Overlapping schedules (< 2 hrs) are automatically blocked.");
        tzLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tzLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
        tzLabel.setBorder(new EmptyBorder(6, 4, 0, 0));
        mainCard.add(tzLabel, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);
    }

    private void applyFilter() {
        String tourneySel = (String) tournamentFilter.getSelectedItem();
        String statusSel = (String) statusFilter.getSelectedItem();
        String timeSel = (String) timeFilter.getSelectedItem();

        Integer filterTourneyId = null;
        if (tourneySel != null && !tourneySel.equals("All Tournaments")) {
            try {
                filterTourneyId = Integer.parseInt(tourneySel.split(" - ")[0].trim());
            } catch (Exception ignored) {}
        }

        tableModel.setRowCount(0);
        List<Match> matches = (filterTourneyId != null) ? matchDAO.findByTournament(filterTourneyId) : matchDAO.findAll();
        LocalDateTime now = LocalDateTime.now(IST_ZONE);

        for (Match m : matches) {
            boolean statusMatch = statusSel == null || statusSel.equals("All Statuses") || m.getStatus().equalsIgnoreCase(statusSel);
            boolean timeMatch = true;

            if (m.getScheduledTime() != null && timeSel != null) {
                LocalDateTime mTime = m.getScheduledTime().toLocalDateTime();
                if ("Today's Schedule".equals(timeSel)) {
                    timeMatch = mTime.toLocalDate().isEqual(now.toLocalDate());
                } else if ("This Week's Schedule".equals(timeSel)) {
                    timeMatch = !mTime.isBefore(now.minusDays(1)) && mTime.isBefore(now.plusDays(7));
                }
            }

            if (statusMatch && timeMatch) {
                String scoreStr = m.getStatus().equalsIgnoreCase("COMPLETED") ? (m.getTeam1Score() + " - " + m.getTeam2Score()) : "VS";
                tableModel.addRow(new Object[]{
                        m.getId(),
                        m.getTournamentTitle(),
                        m.getRoundName(),
                        m.getTeam1Name(),
                        m.getTeam2Name(),
                        scoreStr,
                        m.getWinnerTeamName() != null ? m.getWinnerTeamName() : "-",
                        m.getScheduledTime() != null ? m.getScheduledTime().toString().substring(0, 16) : "-",
                        m.getVenue() != null ? m.getVenue() : "Main Arena",
                        m.getReferee() != null ? m.getReferee() : "Official Referee",
                        m.getStatus(),
                        m.getMvpPlayerName() != null ? m.getMvpPlayerName() : "-"
                });
            }
        }
    }

    private void loadMatches() {
        applyFilter();
    }

    private void openBracketViewer() {
        String tourneySel = (String) tournamentFilter.getSelectedItem();
        Integer tourneyId = null;
        if (tourneySel != null && !tourneySel.equals("All Tournaments")) {
            try {
                tourneyId = Integer.parseInt(tourneySel.split(" - ")[0].trim());
            } catch (Exception ignored) {}
        }

        if (tourneyId == null) {
            int row = matchTable.getSelectedRow();
            if (row >= 0) {
                int matchId = (Integer) tableModel.getValueAt(row, 0);
                Match m = matchDAO.findById(matchId);
                if (m != null) tourneyId = m.getTournamentId();
            }
        }

        if (tourneyId == null) {
            List<Tournament> all = tournamentDAO.findAll();
            if (!all.isEmpty()) tourneyId = all.get(0).getId();
        }

        if (tourneyId != null) {
            BracketViewerDialog.showBracket(this, tourneyId);
        } else {
            JOptionPane.showMessageDialog(this, "No tournament found to view bracket.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setLiveStatus() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a match to set LIVE.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int matchId = (Integer) tableModel.getValueAt(row, 0);
        Match m = matchDAO.findById(matchId);
        if (m != null) {
            m.setStatus("LIVE");
            matchDAO.update(m);
            JOptionPane.showMessageDialog(this, "Match #" + matchId + " is now marked LIVE on the Spectator Hub!", "Live Broadcast Started", JOptionPane.INFORMATION_MESSAGE);
            loadMatches();
        }
    }

    private void cancelSelectedMatch() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a match to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int matchId = (Integer) tableModel.getValueAt(row, 0);
        String reason = JOptionPane.showInputDialog(this, "Please enter the cancellation reason:", "Cancel Match #" + matchId, JOptionPane.WARNING_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            if (matchDAO.cancelMatch(matchId, reason.trim())) {
                JOptionPane.showMessageDialog(this, "Match #" + matchId + " has been CANCELLED.", "Match Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadMatches();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel match.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Schedule Competitive Match (IST)", true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 12));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JComboBox<Tournament> tourneyBox = new JComboBox<>();
        for (Tournament t : tournamentDAO.findAll()) {
            tourneyBox.addItem(t);
        }

        JTextField roundField = UIComponents.createTextField(15);
        roundField.setText("Quarterfinals Match 1");

        JComboBox<Team> team1Box = new JComboBox<>();
        JComboBox<Team> team2Box = new JComboBox<>();
        for (Team tm : teamDAO.findAll()) {
            team1Box.addItem(tm);
            team2Box.addItem(tm);
        }
        if (team2Box.getItemCount() > 1) {
            team2Box.setSelectedIndex(1);
        }

        JComboBox<String> bracketBox = new JComboBox<>(new String[]{"KNOCKOUT", "WINNERS", "LOSERS", "GRAND_FINAL", "GROUP"});

        JTextField timeField = UIComponents.createTextField(15);
        timeField.setText(LocalDateTime.now(IST_ZONE).plusDays(1).withHour(18).withMinute(0).format(DATE_TIME_FORMAT));

        JTextField venueField = UIComponents.createTextField(15);
        venueField.setText("Server Lobby #1 (Mumbai/Asia)");

        JTextField refereeField = UIComponents.createTextField(15);
        refereeField.setText("Official League Referee");

        panel.add(new JLabel("Tournament:"));
        panel.add(tourneyBox);
        panel.add(new JLabel("Round / Stage:"));
        panel.add(roundField);
        panel.add(new JLabel("Team 1:"));
        panel.add(team1Box);
        panel.add(new JLabel("Team 2:"));
        panel.add(team2Box);
        panel.add(new JLabel("Bracket Format:"));
        panel.add(bracketBox);
        panel.add(new JLabel("Date & Time (YYYY-MM-DD HH:mm IST):"));
        panel.add(timeField);
        panel.add(new JLabel("Match Venue / Online Room:"));
        panel.add(venueField);
        panel.add(new JLabel("Assigned Referee / Caster:"));
        panel.add(refereeField);

        JButton saveBtn = UIComponents.createPrimaryButton("Confirm & Schedule");
        saveBtn.addActionListener(e -> {
            Tournament selTourney = (Tournament) tourneyBox.getSelectedItem();
            Team t1 = (Team) team1Box.getSelectedItem();
            Team t2 = (Team) team2Box.getSelectedItem();

            if (selTourney == null || t1 == null || t2 == null) {
                JOptionPane.showMessageDialog(dialog, "Please select tournament and two teams.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (t1.getId() == t2.getId()) {
                JOptionPane.showMessageDialog(dialog, "Team 1 and Team 2 must be different.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDateTime scheduledLdt;
            try {
                scheduledLdt = LocalDateTime.parse(timeField.getText().trim(), DATE_TIME_FORMAT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date/time format. Please use YYYY-MM-DD HH:mm", "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Timestamp scheduledTs = Timestamp.valueOf(scheduledLdt);

            // Scheduling Conflict Detection
            if (matchDAO.hasSchedulingConflict(t1.getId(), scheduledTs, null)) {
                JOptionPane.showMessageDialog(dialog,
                        "⚠️ SCHEDULING CONFLICT DETECTED!\n\n" +
                        "Team '" + t1.getTeamName() + "' is already scheduled for an active match within 2 hours of this time.\n" +
                        "Please select a non-overlapping time slot to avoid team fatigue and forfeit.",
                        "Team Scheduling Conflict", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (matchDAO.hasSchedulingConflict(t2.getId(), scheduledTs, null)) {
                JOptionPane.showMessageDialog(dialog,
                        "⚠️ SCHEDULING CONFLICT DETECTED!\n\n" +
                        "Team '" + t2.getTeamName() + "' is already scheduled for an active match within 2 hours of this time.\n" +
                        "Please select a non-overlapping time slot.",
                        "Team Scheduling Conflict", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Match m = new Match();
            m.setTournamentId(selTourney.getId());
            m.setRoundName(roundField.getText().trim());
            m.setTeam1Id(t1.getId());
            m.setTeam2Id(t2.getId());
            m.setBracketType((String) bracketBox.getSelectedItem());
            m.setScheduledTime(scheduledTs);
            m.setVenue(venueField.getText().trim());
            m.setReferee(refereeField.getText().trim());
            m.setStatus("SCHEDULED");

            if (matchDAO.create(m)) {
                JOptionPane.showMessageDialog(dialog, "Match scheduled successfully with no conflicts!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadMatches();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to schedule match.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancelBtn);
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);
        root.add(panel, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showRescheduleDialog() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a match to reschedule.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int matchId = (Integer) tableModel.getValueAt(row, 0);
        Match m = matchDAO.findById(matchId);
        if (m == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reschedule Match #" + m.getId() + " - " + m.getTeam1Name() + " vs " + m.getTeam2Name(), true);
        dialog.setSize(520, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JTextField timeField = UIComponents.createTextField(15);
        if (m.getScheduledTime() != null) {
            timeField.setText(m.getScheduledTime().toLocalDateTime().format(DATE_TIME_FORMAT));
        } else {
            timeField.setText(LocalDateTime.now(IST_ZONE).plusDays(1).format(DATE_TIME_FORMAT));
        }

        JTextField venueField = UIComponents.createTextField(15);
        venueField.setText(m.getVenue() != null ? m.getVenue() : "Main Esports Arena");

        JTextField refereeField = UIComponents.createTextField(15);
        refereeField.setText(m.getReferee() != null ? m.getReferee() : "Official Referee");

        panel.add(new JLabel("New Scheduled Time (IST):"));
        panel.add(timeField);
        panel.add(new JLabel("Venue / Online Room:"));
        panel.add(venueField);
        panel.add(new JLabel("Referee / Caster:"));
        panel.add(refereeField);

        JButton saveBtn = UIComponents.createPrimaryButton("Save Reschedule");
        saveBtn.addActionListener(e -> {
            LocalDateTime ldt;
            try {
                ldt = LocalDateTime.parse(timeField.getText().trim(), DATE_TIME_FORMAT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD HH:mm", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Timestamp ts = Timestamp.valueOf(ldt);

            // Conflict detection
            if (m.getTeam1Id() != null && matchDAO.hasSchedulingConflict(m.getTeam1Id(), ts, m.getId())) {
                JOptionPane.showMessageDialog(dialog, "⚠️ Scheduling conflict: Team '" + m.getTeam1Name() + "' is already playing within 2 hours.", "Conflict Detected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (m.getTeam2Id() != null && matchDAO.hasSchedulingConflict(m.getTeam2Id(), ts, m.getId())) {
                JOptionPane.showMessageDialog(dialog, "⚠️ Scheduling conflict: Team '" + m.getTeam2Name() + "' is already playing within 2 hours.", "Conflict Detected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (matchDAO.rescheduleMatch(m.getId(), ts, venueField.getText().trim(), refereeField.getText().trim())) {
                JOptionPane.showMessageDialog(dialog, "Match rescheduled successfully with no conflicts!", "Rescheduled", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadMatches();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to reschedule match.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancelBtn);
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);
        root.add(panel, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void showScoreDialog() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a match to record score and winner.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int matchId = (Integer) tableModel.getValueAt(row, 0);
        Match m = matchDAO.findById(matchId);
        if (m == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Score: " + m.getTeam1Name() + " vs " + m.getTeam2Name(), true);
        dialog.setSize(540, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JSpinner score1Spinner = new JSpinner(new SpinnerNumberModel(m.getTeam1Score(), 0, 100, 1));
        JSpinner score2Spinner = new JSpinner(new SpinnerNumberModel(m.getTeam2Score(), 0, 100, 1));

        JComboBox<TeamChoice> winnerCombo = new JComboBox<>();
        winnerCombo.addItem(new TeamChoice(m.getTeam1Id(), m.getTeam1Name()));
        winnerCombo.addItem(new TeamChoice(m.getTeam2Id(), m.getTeam2Name()));

        JComboBox<PlayerChoice> mvpCombo = new JComboBox<>();
        mvpCombo.addItem(new PlayerChoice(null, "-- None / Undecided --"));
        if (m.getTeam1Id() != null) {
            for (Player p : playerDAO.findByTeamId(m.getTeam1Id())) {
                mvpCombo.addItem(new PlayerChoice(p.getId(), p.getGamerTag() + " (" + m.getTeam1Name() + ")"));
            }
        }
        if (m.getTeam2Id() != null) {
            for (Player p : playerDAO.findByTeamId(m.getTeam2Id())) {
                mvpCombo.addItem(new PlayerChoice(p.getId(), p.getGamerTag() + " (" + m.getTeam2Name() + ")"));
            }
        }

        JTextField notesField = UIComponents.createTextField(15);
        notesField.setText("Decisive competitive showdown.");

        panel.add(new JLabel(m.getTeam1Name() + " Score:"));
        panel.add(score1Spinner);
        panel.add(new JLabel(m.getTeam2Name() + " Score:"));
        panel.add(score2Spinner);
        panel.add(new JLabel("Declared Winner:"));
        panel.add(winnerCombo);
        panel.add(new JLabel("MVP Player:"));
        panel.add(mvpCombo);
        panel.add(new JLabel("Match Notes:"));
        panel.add(notesField);

        JButton saveBtn = UIComponents.createPrimaryButton("Declare Winner & Save");
        saveBtn.addActionListener(e -> {
            int s1 = (Integer) score1Spinner.getValue();
            int s2 = (Integer) score2Spinner.getValue();
            TeamChoice winnerChoice = (TeamChoice) winnerCombo.getSelectedItem();
            PlayerChoice mvpChoice = (PlayerChoice) mvpCombo.getSelectedItem();

            if (winnerChoice == null) {
                JOptionPane.showMessageDialog(dialog, "Please declare a match winner.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Confirm match score submission:\n" +
                    m.getTeam1Name() + ": " + s1 + "\n" +
                    m.getTeam2Name() + ": " + s2 + "\n" +
                    "Winner: " + winnerChoice.name + "\n\n" +
                    "This will automatically update team standings and advance the winner in the tournament bracket.",
                    "Confirm Result Submission", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            boolean success = matchDAO.recordMatchScore(
                    m.getId(),
                    s1,
                    s2,
                    winnerChoice.id,
                    mvpChoice != null ? mvpChoice.id : null,
                    notesField.getText().trim()
            );

            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "Match score recorded successfully!\nWinner declared: " + winnerChoice.name +
                        "\nWinner has been automatically advanced to the next bracket round if scheduled.",
                        "Match Recorded & Winner Advanced", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadMatches();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to record match score.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(cancelBtn);
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);
        root.add(panel, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void deleteSelectedMatch() {
        int row = matchTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a match to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int matchId = (Integer) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Match fixture #" + matchId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (matchDAO.delete(matchId)) {
                JOptionPane.showMessageDialog(this, "Match deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMatches();
            }
        }
    }

    private static class TeamChoice {
        final Integer id;
        final String name;
        TeamChoice(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class PlayerChoice {
        final Integer id;
        final String name;
        PlayerChoice(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
