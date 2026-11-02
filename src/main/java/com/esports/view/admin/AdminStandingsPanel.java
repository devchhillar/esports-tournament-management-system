package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.*;
import com.esports.model.Tournament;
import com.esports.model.TournamentStanding;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tournament Standings & Rankings Management Panel.
 * Calculates points, wins, losses, score differentials, and rank positions.
 */
public class AdminStandingsPanel extends JPanel {

    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TournamentStandingDAO standingDAO = new TournamentStandingDAOImpl();

    private JComboBox<TournamentItem> tournamentCombo;
    private DefaultTableModel tableModel;
    private JTable standingsTable;
    private JLabel tournamentInfoLabel;

    public AdminStandingsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadTournaments();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Top Controls
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JLabel selectLabel = new JLabel("Select Tournament:");
        selectLabel.setFont(ThemeConfig.FONT_BOLD);
        selectLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        tournamentCombo = new JComboBox<>();
        tournamentCombo.setFont(ThemeConfig.FONT_REGULAR);
        tournamentCombo.setBackground(ThemeConfig.COLOR_CARD_BG);
        tournamentCombo.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        tournamentCombo.addActionListener(e -> loadStandings());

        tournamentInfoLabel = new JLabel("");
        tournamentInfoLabel.setFont(ThemeConfig.FONT_SMALL);
        tournamentInfoLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        leftActions.add(selectLabel);
        leftActions.add(tournamentCombo);
        leftActions.add(tournamentInfoLabel);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton recalculateBtn = UIComponents.createPrimaryButton("⚡ Recalculate Ranks");
        recalculateBtn.addActionListener(e -> recalculateCurrentTournament());

        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadStandings());

        rightActions.add(recalculateBtn);
        rightActions.add(refreshBtn);

        topBar.add(leftActions, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Standings Table
        String[] cols = {"Rank", "Team Name", "Tag", "Stage / Group", "Matches Played", "Wins (W)", "Losses (L)", "Draws (D)", "Score Diff", "Points (PTS)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        standingsTable = new JTable(tableModel);
        standingsTable.setShowGrid(true);
        standingsTable.setFillsViewportHeight(true);
        standingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        standingsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        standingsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        standingsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        standingsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        standingsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        standingsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        standingsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        standingsTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        standingsTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        standingsTable.getColumnModel().getColumn(9).setPreferredWidth(100);

        // Center align numerical columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        standingsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        standingsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        standingsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        standingsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        standingsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        standingsTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        standingsTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(standingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private void loadTournaments() {
        tournamentCombo.removeAllItems();
        List<Tournament> tourneys = tournamentDAO.findAll();
        for (Tournament t : tourneys) {
            tournamentCombo.addItem(new TournamentItem(t.getId(), t.getTitle(), t.getGame(), t.getFormat()));
        }
        if (tournamentCombo.getItemCount() > 0) {
            tournamentCombo.setSelectedIndex(0);
        }
    }

    private void loadStandings() {
        TournamentItem selected = (TournamentItem) tournamentCombo.getSelectedItem();
        if (selected == null) return;

        tournamentInfoLabel.setText("Format: " + selected.format + " | Title: " + selected.game);
        tableModel.setRowCount(0);

        List<TournamentStanding> list = standingDAO.getStandingsByTournament(selected.id);
        int rank = 1;
        for (TournamentStanding s : list) {
            String rankDisplay = (rank == 1) ? "🥇 1" : (rank == 2) ? "🥈 2" : (rank == 3) ? "🥉 3" : String.valueOf(rank);
            tableModel.addRow(new Object[]{
                    rankDisplay,
                    s.getTeamName(),
                    s.getTeamTag(),
                    s.getGroupName() != null ? s.getGroupName() : "Overall Bracket",
                    s.getMatchesPlayed(),
                    s.getWins(),
                    s.getLosses(),
                    s.getDraws(),
                    (s.getScoreDiff() > 0 ? "+" : "") + s.getScoreDiff(),
                    s.getPoints()
            });
            rank++;
        }
    }

    private void recalculateCurrentTournament() {
        TournamentItem selected = (TournamentItem) tournamentCombo.getSelectedItem();
        if (selected == null) return;

        standingDAO.recalculateRanks(selected.id);
        JOptionPane.showMessageDialog(this, "Tournament standings and rankings recalculated successfully!", "Recalculation Complete", JOptionPane.INFORMATION_MESSAGE);
        loadStandings();
    }

    private static class TournamentItem {
        final int id;
        final String title;
        final String game;
        final String format;

        TournamentItem(int id, String title, String game, String format) {
            this.id = id;
            this.title = title;
            this.game = game;
            this.format = format;
        }

        @Override
        public String toString() {
            return "#" + id + " - " + title + " (" + game + ")";
        }
    }
}
