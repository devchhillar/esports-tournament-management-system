package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.*;
import com.esports.model.*;
import com.esports.service.ReportService;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Admin Panel for Generating, Previewing, and Exporting Tournament Reports to PDF and Excel/CSV.
 */
public class AdminReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final TournamentStandingDAO standingDAO = new TournamentStandingDAOImpl();

    private JComboBox<String> reportTypeCombo;
    private JComboBox<TournamentChoice> tournamentCombo;
    private DefaultTableModel previewModel;
    private JTable previewTable;
    private JLabel previewTitleLabel;
    private JLabel statusLabel;

    public AdminReportsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadReportPreview();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Controls Top Bar - Responsive 2-Tier Layout to Prevent Overlapping
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);

        // Row 1: Report Selection Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterRow.setOpaque(false);

        reportTypeCombo = new JComboBox<>(new String[]{
                "🏆 Championship Tournaments Summary",
                "💳 Financial & Prize Distribution Report",
                "📊 Official Tournament Standings",
                "🎯 Pro Players Performance Report"
        });
        reportTypeCombo.addActionListener(e -> {
            boolean isStandings = reportTypeCombo.getSelectedIndex() == 2;
            tournamentCombo.setEnabled(isStandings);
            loadReportPreview();
        });

        tournamentCombo = new JComboBox<>();
        for (Tournament t : tournamentDAO.findAll()) {
            tournamentCombo.addItem(new TournamentChoice(t.getId(), t.getTitle()));
        }
        tournamentCombo.setEnabled(false);
        tournamentCombo.addActionListener(e -> {
            if (reportTypeCombo.getSelectedIndex() == 2) {
                loadReportPreview();
            }
        });

        JLabel reportLbl = new JLabel("Report Type:");
        reportLbl.setFont(ThemeConfig.FONT_BOLD);
        reportLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel tourneyLbl = new JLabel("Tournament:");
        tourneyLbl.setFont(ThemeConfig.FONT_BOLD);
        tourneyLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        filterRow.add(reportLbl);
        filterRow.add(reportTypeCombo);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(tourneyLbl);
        filterRow.add(tournamentCombo);

        // Row 2: Export Actions
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actionRow.setOpaque(false);

        JButton exportPdfBtn = UIComponents.createStyledButton("📄 Export to PDF", new Color(239, 68, 68), Color.WHITE, new Color(248, 113, 113));
        JButton exportCsvBtn = UIComponents.createStyledButton("📊 Export to Excel / CSV", new Color(16, 185, 129), Color.WHITE, new Color(52, 211, 153));
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh Preview");

        exportPdfBtn.addActionListener(e -> exportToPdf());
        exportCsvBtn.addActionListener(e -> exportToCsv());
        refreshBtn.addActionListener(e -> loadReportPreview());

        actionRow.add(exportPdfBtn);
        actionRow.add(exportCsvBtn);
        actionRow.add(refreshBtn);

        topBar.add(filterRow);
        topBar.add(Box.createVerticalStrut(4));
        topBar.add(actionRow);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Preview Header & Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setOpaque(false);

        previewTitleLabel = new JLabel("Report Data Preview");
        previewTitleLabel.setFont(ThemeConfig.FONT_SUBTITLE);
        previewTitleLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        tableContainer.add(previewTitleLabel, BorderLayout.NORTH);

        previewModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        previewTable = new JTable(previewModel);
        previewTable.setShowGrid(true);
        previewTable.setFillsViewportHeight(true);
        previewTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom status
        statusLabel = new JLabel("Ready to generate and export reports.", SwingConstants.LEFT);
        statusLabel.setFont(ThemeConfig.FONT_SMALL);
        statusLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
        tableContainer.add(statusLabel, BorderLayout.SOUTH);

        mainCard.add(tableContainer, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);
    }

    private void loadReportPreview() {
        int idx = reportTypeCombo.getSelectedIndex();
        switch (idx) {
            case 0 -> loadTournamentsPreview();
            case 1 -> loadPaymentsPreview();
            case 2 -> loadStandingsPreview();
            case 3 -> loadPlayersPreview();
        }
        adjustColumnWidths();
    }

    private void adjustColumnWidths() {
        for (int i = 0; i < previewTable.getColumnCount(); i++) {
            previewTable.getColumnModel().getColumn(i).setMinWidth(90);
            previewTable.getColumnModel().getColumn(i).setPreferredWidth(140);
        }
        if (previewTable.getColumnCount() > 0) {
            previewTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            previewTable.getColumnModel().getColumn(0).setMinWidth(40);
        }
        if (previewTable.getColumnCount() > 1) {
            previewTable.getColumnModel().getColumn(1).setPreferredWidth(180);
            previewTable.getColumnModel().getColumn(1).setMinWidth(120);
        }
    }

    private void loadTournamentsPreview() {
        previewTitleLabel.setText("Preview: Championship Tournaments Summary");
        String[] cols = {"ID", "Tournament Title", "Game", "Format", "Prize Pool", "Entry Fee", "Max Teams", "Status", "Start Date"};
        previewModel.setDataVector(new Object[][]{}, cols);

        List<Tournament> list = tournamentDAO.findAll();
        for (Tournament t : list) {
            previewModel.addRow(new Object[]{
                    t.getId(), t.getTitle(), t.getGame(), t.getFormat(),
                    "₹ " + t.getPrizePool(), "₹ " + t.getEntryFee(), t.getMaxTeams(),
                    t.getStatus(), t.getStartDate() != null ? t.getStartDate().toString().substring(0, 16) : "-"
            });
        }
        statusLabel.setText("Showing " + list.size() + " tournament records.");
    }

    private void loadPaymentsPreview() {
        previewTitleLabel.setText("Preview: Financial & Prize Distribution Report");
        String[] cols = {"ID", "Transaction ID", "Tournament Title", "Team", "Amount", "Type", "Method", "Status", "Date"};
        previewModel.setDataVector(new Object[][]{}, cols);

        List<Payment> list = paymentDAO.findAll();
        for (Payment p : list) {
            previewModel.addRow(new Object[]{
                    p.getId(), p.getTransactionId(), p.getTournamentTitle(),
                    p.getTeamName() + " [" + p.getTeamTag() + "]",
                    "₹ " + p.getAmount().toPlainString(), p.getPaymentType(),
                    p.getPaymentMethod(), p.getStatus(),
                    p.getPaymentDate() != null ? p.getPaymentDate().toString().substring(0, 16) : "-"
            });
        }
        statusLabel.setText("Showing " + list.size() + " payment and payout records.");
    }

    private void loadStandingsPreview() {
        TournamentChoice tc = (TournamentChoice) tournamentCombo.getSelectedItem();
        int tourneyId = tc != null ? tc.id : 1;
        previewTitleLabel.setText("Preview: Official Standings - " + (tc != null ? tc.name : "Tournament"));

        String[] cols = {"Rank", "Squad Name", "Group", "Played", "Wins", "Draws", "Losses", "Round Diff", "Points"};
        previewModel.setDataVector(new Object[][]{}, cols);

        List<TournamentStanding> list = standingDAO.getStandingsByTournament(tourneyId);
        for (TournamentStanding s : list) {
            previewModel.addRow(new Object[]{
                    s.getRankPosition(), s.getTeamName() + " [" + s.getTeamTag() + "]",
                    s.getGroupName() != null ? s.getGroupName() : "-",
                    s.getMatchesPlayed(), s.getWins(), s.getDraws(), s.getLosses(),
                    (s.getScoreDiff() > 0 ? "+" : "") + s.getScoreDiff(), s.getPoints()
            });
        }
        statusLabel.setText("Showing standings for " + list.size() + " teams.");
    }

    private void loadPlayersPreview() {
        previewTitleLabel.setText("Preview: Pro Players Performance Leaderboard");
        String[] cols = {"ID", "Gamer Tag", "Full Name", "Current Squad", "Game Discipline", "In-Game Role", "RP Points", "Kills", "MVPs"};
        previewModel.setDataVector(new Object[][]{}, cols);

        List<Player> list = playerDAO.findAll();
        for (Player p : list) {
            previewModel.addRow(new Object[]{
                    p.getId(), p.getGamerTag(), p.getFullName(),
                    p.getCurrentTeamName() != null ? p.getCurrentTeamName() : "Free Agent",
                    p.getPrimaryGame(), p.getInGameRole(), p.getRankingPoints(),
                    p.getKills(), p.getMvpCount()
            });
        }
        statusLabel.setText("Showing " + list.size() + " competitor profiles.");
    }

    private void exportToPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Report to PDF Document");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("esports_report_" + System.currentTimeMillis() + ".pdf"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }

            int idx = reportTypeCombo.getSelectedIndex();
            boolean success = false;
            switch (idx) {
                case 0 -> success = reportService.exportTournamentsToPdf(file);
                case 1 -> success = reportService.exportPaymentsToPdf(file);
                case 2 -> {
                    TournamentChoice tc = (TournamentChoice) tournamentCombo.getSelectedItem();
                    int tourneyId = tc != null ? tc.id : 1;
                    success = reportService.exportStandingsToPdf(tourneyId, file);
                }
                case 3 -> success = reportService.exportPlayersToPdf(file);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Report exported to PDF successfully!\nSaved to: " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("PDF Exported: " + file.getName());
                statusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export PDF report.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Report to Excel / CSV Spreadsheet");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Spreadsheets (*.csv)", "csv"));
        chooser.setSelectedFile(new File("esports_report_" + System.currentTimeMillis() + ".csv"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            int idx = reportTypeCombo.getSelectedIndex();
            boolean success = false;
            switch (idx) {
                case 0 -> success = reportService.exportTournamentsToCsv(file);
                case 1 -> success = reportService.exportPaymentsToCsv(file);
                case 2 -> {
                    TournamentChoice tc = (TournamentChoice) tournamentCombo.getSelectedItem();
                    int tourneyId = tc != null ? tc.id : 1;
                    success = reportService.exportStandingsToCsv(tourneyId, file);
                }
                case 3 -> success = reportService.exportPlayersToCsv(file);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Report exported to Excel/CSV successfully!\nSaved to: " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("CSV Exported: " + file.getName());
                statusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export CSV report.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private record TournamentChoice(int id, String name) {
        @Override public String toString() { return name; }
    }
}
