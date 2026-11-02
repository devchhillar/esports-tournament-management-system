package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.dao.GameDAO;
import com.esports.dao.GameDAOImpl;
import com.esports.dao.RegistrationDAO;
import com.esports.dao.RegistrationDAOImpl;
import com.esports.dao.TournamentDAO;
import com.esports.dao.TournamentDAOImpl;
import com.esports.model.Game;
import com.esports.model.Team;
import com.esports.model.Tournament;
import com.esports.service.FixtureService;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin Panel for Tournament Management & Automated Fixture Generation.
 */
public class AdminTournamentsPanel extends JPanel {

    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final GameDAO gameDAO = new GameDAOImpl();
    private final RegistrationDAO registrationDAO = new RegistrationDAOImpl();
    private final FixtureService fixtureService = new FixtureService();

    private DefaultTableModel tableModel;
    private JTable tournamentTable;
    private JComboBox<String> gameFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;
    private JLabel statusLabel;

    public AdminTournamentsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadTournaments();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Top Controls Bar - Responsive 2-Tier Layout to Prevent Overlapping
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);

        // Row 1: Search and Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(ThemeConfig.FONT_BOLD);
        searchLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        searchField = UIComponents.createTextField(14);
        searchField.setToolTipText("Search by Tournament Title, Organizer, Game, or Tournament ID");
        searchField.addActionListener(e -> applyFilter());

        JButton searchBtn = UIComponents.createSecondaryButton("🔍 Search");
        searchBtn.addActionListener(e -> applyFilter());

        JButton resetBtn = UIComponents.createSecondaryButton("🔄 Reset");
        resetBtn.setToolTipText("Clear all filters and show all tournaments");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            if (gameFilterCombo.getItemCount() > 0) gameFilterCombo.setSelectedIndex(0);
            if (statusFilterCombo.getItemCount() > 0) statusFilterCombo.setSelectedIndex(0);
            applyFilter();
        });

        JLabel gameLbl = new JLabel("Game:");
        gameLbl.setFont(ThemeConfig.FONT_BOLD);
        gameLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        gameFilterCombo = new JComboBox<>();
        gameFilterCombo.addItem("All Games");
        List<Game> allGames = gameDAO.findAll();
        for (Game g : allGames) {
            gameFilterCombo.addItem(g.getName());
        }
        gameFilterCombo.addActionListener(e -> applyFilter());

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(ThemeConfig.FONT_BOLD);
        statusLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        statusFilterCombo = new JComboBox<>(new String[]{
                "All Statuses", "DRAFT", "UPCOMING", "REGISTRATION_OPEN",
                "REGISTRATION_CLOSED", "ONGOING", "COMPLETED", "CANCELLED"
        });
        statusFilterCombo.addActionListener(e -> applyFilter());

        filterRow.add(searchLbl);
        filterRow.add(searchField);
        filterRow.add(searchBtn);
        filterRow.add(resetBtn);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(gameLbl);
        filterRow.add(gameFilterCombo);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(statusLbl);
        filterRow.add(statusFilterCombo);

        // Row 2: Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actionRow.setOpaque(false);

        JButton addBtn = UIComponents.createPrimaryButton("➕ New Tournament");
        addBtn.addActionListener(e -> showCreateDialog());

        JButton editBtn = UIComponents.createAccentButton("✏️ Edit");
        editBtn.addActionListener(e -> showEditDialog());

        JButton fixtureBtn = UIComponents.createStyledButton("⚡ Generate Fixtures", new Color(0, 180, 136), Color.WHITE, new Color(0, 220, 160));
        fixtureBtn.addActionListener(e -> generateFixturesForSelected());

        JButton bracketBtn = UIComponents.createStyledButton("📊 View Bracket", new Color(123, 44, 191), Color.WHITE, new Color(157, 78, 221));
        bracketBtn.addActionListener(e -> showBracketForSelected());

        JButton statusBtn = UIComponents.createSecondaryButton("🔄 Set Status");
        statusBtn.addActionListener(e -> showChangeStatusDialog());

        JButton deleteBtn = UIComponents.createDangerButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteSelected());

        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(fixtureBtn);
        actionRow.add(bracketBtn);
        actionRow.add(statusBtn);
        actionRow.add(deleteBtn);

        topBar.add(filterRow);
        topBar.add(Box.createVerticalStrut(4));
        topBar.add(actionRow);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Table Data Binding
        String[] cols = {"ID", "Title", "Game", "Format", "Prize Pool", "Entry Fee", "Max Teams", "Status", "Start Date", "Organizer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 6) return Integer.class;
                return String.class;
            }
        };

        tournamentTable = new JTable(tableModel);
        tournamentTable.setShowGrid(true);
        tournamentTable.setFillsViewportHeight(true);
        tournamentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tournamentTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tournamentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tournamentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        tournamentTable.getColumnModel().getColumn(0).setMinWidth(40);
        tournamentTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        tournamentTable.getColumnModel().getColumn(1).setMinWidth(160);
        tournamentTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        tournamentTable.getColumnModel().getColumn(2).setMinWidth(110);
        tournamentTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        tournamentTable.getColumnModel().getColumn(3).setMinWidth(120);
        tournamentTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        tournamentTable.getColumnModel().getColumn(4).setMinWidth(90);
        tournamentTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        tournamentTable.getColumnModel().getColumn(5).setMinWidth(80);
        tournamentTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        tournamentTable.getColumnModel().getColumn(6).setMinWidth(70);
        tournamentTable.getColumnModel().getColumn(7).setPreferredWidth(140);
        tournamentTable.getColumnModel().getColumn(7).setMinWidth(110);
        tournamentTable.getColumnModel().getColumn(8).setPreferredWidth(130);
        tournamentTable.getColumnModel().getColumn(8).setMinWidth(110);
        tournamentTable.getColumnModel().getColumn(9).setPreferredWidth(150);
        tournamentTable.getColumnModel().getColumn(9).setMinWidth(110);

        JScrollPane scrollPane = new JScrollPane(tournamentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainCard.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Status Bar (Loading, Counter & Empty State)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 4, 4, 4));

        statusLabel = new JLabel("⏳ Initializing tournament database records...");
        statusLabel.setFont(ThemeConfig.FONT_REGULAR);
        statusLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        bottomBar.add(statusLabel, BorderLayout.WEST);
        mainCard.add(bottomBar, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);
    }

    public void applyFilter() {
        String kw = (searchField != null) ? searchField.getText().trim() : "";
        String game = (gameFilterCombo != null) ? (String) gameFilterCombo.getSelectedItem() : "All Games";
        String status = (statusFilterCombo != null) ? (String) statusFilterCombo.getSelectedItem() : "All Statuses";

        // Normalizing filter parameters
        String gameParam = (game == null || game.equalsIgnoreCase("All Games") || game.equalsIgnoreCase("ALL")) ? null : game;
        String statusParam = (status == null || status.equalsIgnoreCase("All Statuses") || status.equalsIgnoreCase("ALL")) ? null : status;

        if (statusLabel != null) {
            statusLabel.setText("⏳ Loading tournaments from database...");
            statusLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        }

        SwingWorker<List<Tournament>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Tournament> doInBackground() {
                return tournamentDAO.search(kw, gameParam, statusParam);
            }

            @Override
            protected void done() {
                try {
                    List<Tournament> list = get();
                    tableModel.setRowCount(0);

                    if (list == null || list.isEmpty()) {
                        if (statusLabel != null) {
                            if (kw.isEmpty() && gameParam == null && statusParam == null) {
                                statusLabel.setText("ℹ️ No tournaments in database. Click '➕ New Tournament' to create one.");
                            } else {
                                statusLabel.setText("ℹ️ No tournaments match your search/filter criteria. Click '🔄 Reset' to show all.");
                            }
                            statusLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
                        }
                    } else {
                        for (Tournament t : list) {
                            String startDateStr = "-";
                            if (t.getStartDate() != null) {
                                String s = t.getStartDate().toString();
                                startDateStr = s.length() >= 16 ? s.substring(0, 16) : s;
                            }
                            tableModel.addRow(new Object[]{
                                    t.getId(),
                                    t.getTitle() != null ? t.getTitle() : "-",
                                    t.getGame() != null ? t.getGame() : "-",
                                    t.getFormat() != null ? t.getFormat() : "-",
                                    "₹ " + (t.getPrizePool() != null ? t.getPrizePool().toPlainString() : "0.00"),
                                    "₹ " + (t.getEntryFee() != null ? t.getEntryFee().toPlainString() : "0.00"),
                                    t.getMaxTeams(),
                                    t.getStatus() != null ? t.getStatus() : "-",
                                    startDateStr,
                                    t.getOrganizer() != null ? t.getOrganizer() : "-"
                            });
                        }
                        if (statusLabel != null) {
                            statusLabel.setText("✅ Displaying " + list.size() + " tournament(s) [Total in DB: " + tournamentDAO.countTotal() + "]");
                            statusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
                        }
                    }
                } catch (Exception ex) {
                    tableModel.setRowCount(0);
                    if (statusLabel != null) {
                        statusLabel.setText("❌ Error loading tournaments: " + ex.getMessage());
                        statusLabel.setForeground(ThemeConfig.COLOR_DANGER);
                    }
                    JOptionPane.showMessageDialog(AdminTournamentsPanel.this,
                            "Database communication error while loading tournaments:\n" + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void loadTournaments() {
        applyFilter();
    }

    private void showCreateDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New Tournament", true);
        dialog.setSize(620, 680);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(11, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JTextField titleField = UIComponents.createTextField(20);
        JComboBox<String> gameCombo = new JComboBox<>();
        List<Game> activeGames = gameDAO.findActive();
        if (activeGames.isEmpty()) {
            activeGames = gameDAO.findAll();
        }
        for (Game g : activeGames) {
            gameCombo.addItem(g.getName());
        }
        if (gameCombo.getItemCount() == 0) {
            gameCombo.addItem("Valorant");
            gameCombo.addItem("BGMI");
            gameCombo.addItem("Counter-Strike 2");
            gameCombo.addItem("Dota 2");
        }

        JComboBox<String> formatCombo = new JComboBox<>(new String[]{
                "SINGLE_ELIMINATION", "DOUBLE_ELIMINATION", "ROUND_ROBIN", "GROUP_STAGE_KNOCKOUT"
        });
        JTextField prizeField = UIComponents.createTextField(10);
        prizeField.setText("50000");
        JTextField entryField = UIComponents.createTextField(10);
        entryField.setText("500");

        JTextField firstPrizeField = UIComponents.createTextField(10);
        firstPrizeField.setText("30000");
        JTextField secondPrizeField = UIComponents.createTextField(10);
        secondPrizeField.setText("15000");
        JTextField thirdPrizeField = UIComponents.createTextField(10);
        thirdPrizeField.setText("5000");

        JSpinner minTeamsSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 32, 1));
        JSpinner maxTeamsSpinner = new JSpinner(new SpinnerNumberModel(16, 2, 64, 1));

        JTextField organizerField = UIComponents.createTextField(20);
        organizerField.setText("eSports Arena Global");

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"REGISTRATION_OPEN", "UPCOMING", "DRAFT"});

        panel.add(new JLabel("Tournament Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Game Title:"));
        panel.add(gameCombo);
        panel.add(new JLabel("Format:"));
        panel.add(formatCombo);
        panel.add(new JLabel("Min / Max Teams:"));
        JPanel teamPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        teamPanel.setOpaque(false);
        teamPanel.add(minTeamsSpinner);
        teamPanel.add(maxTeamsSpinner);
        panel.add(teamPanel);

        panel.add(new JLabel("Total Prize Pool (₹):"));
        panel.add(prizeField);
        panel.add(new JLabel("Entry Fee (₹):"));
        panel.add(entryField);

        panel.add(new JLabel("1st / 2nd / 3rd Prize (₹):"));
        JPanel prizeSplit = new JPanel(new GridLayout(1, 3, 4, 0));
        prizeSplit.setOpaque(false);
        prizeSplit.add(firstPrizeField);
        prizeSplit.add(secondPrizeField);
        prizeSplit.add(thirdPrizeField);
        panel.add(prizeSplit);

        panel.add(new JLabel("Organizer Name:"));
        panel.add(organizerField);
        panel.add(new JLabel("Initial Status:"));
        panel.add(statusCombo);

        JButton saveBtn = UIComponents.createPrimaryButton("Create Tournament");
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tournament title is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                BigDecimal prize = new BigDecimal(prizeField.getText().trim());
                BigDecimal entry = new BigDecimal(entryField.getText().trim());
                BigDecimal p1 = new BigDecimal(firstPrizeField.getText().trim());
                BigDecimal p2 = new BigDecimal(secondPrizeField.getText().trim());
                BigDecimal p3 = new BigDecimal(thirdPrizeField.getText().trim());

                if (prize.compareTo(BigDecimal.ZERO) < 0 || entry.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(dialog, "Prize pool and entry fee cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int minTeams = (Integer) minTeamsSpinner.getValue();
                int maxTeams = (Integer) maxTeamsSpinner.getValue();
                if (minTeams > maxTeams) {
                    JOptionPane.showMessageDialog(dialog, "Minimum teams cannot exceed maximum teams.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Tournament tourney = new Tournament();
                tourney.setTitle(title);
                tourney.setGame((String) gameCombo.getSelectedItem());
                tourney.setFormat((String) formatCombo.getSelectedItem());
                tourney.setMinTeams(minTeams);
                tourney.setMaxTeams(maxTeams);
                tourney.setPrizePool(prize);
                tourney.setEntryFee(entry);
                tourney.setFirstPrize(p1);
                tourney.setSecondPrize(p2);
                tourney.setThirdPrize(p3);
                tourney.setOrganizer(organizerField.getText().trim().isEmpty() ? "eSports Arena Global" : organizerField.getText().trim());
                tourney.setStatus((String) statusCombo.getSelectedItem());
                tourney.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
                tourney.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
                tourney.setCreatedBy(UserSession.getInstance().isLoggedIn() ? UserSession.getInstance().getCurrentUser().getId() : 1);

                if (tournamentDAO.create(tourney)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Tournament '" + title + "' created successfully with ID #" + tourney.getId() + "!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();

                    // Reset search & filters so the new tournament is immediately visible in the table
                    if (searchField != null) searchField.setText("");
                    if (gameFilterCombo != null && gameFilterCombo.getItemCount() > 0) gameFilterCombo.setSelectedIndex(0);
                    if (statusFilterCombo != null && statusFilterCombo.getItemCount() > 0) statusFilterCombo.setSelectedIndex(0);
                    loadTournaments();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create tournament in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric prize and fee values.", "Number Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int row = tournamentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tournament to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tournamentTable.convertRowIndexToModel(row);
        int tourneyId = (Integer) tableModel.getValueAt(modelRow, 0);
        Tournament t = tournamentDAO.findById(tourneyId);
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Could not find tournament details for ID #" + tourneyId, "Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Tournament - #" + t.getId() + " " + t.getTitle(), true);
        dialog.setSize(600, 620);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 12));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JTextField titleField = UIComponents.createTextField(20);
        titleField.setText(t.getTitle());

        JComboBox<String> gameCombo = new JComboBox<>();
        for (Game g : gameDAO.findAll()) {
            gameCombo.addItem(g.getName());
        }
        if (gameCombo.getItemCount() == 0 && t.getGame() != null) {
            gameCombo.addItem(t.getGame());
        }
        if (t.getGame() != null) {
            gameCombo.setSelectedItem(t.getGame());
        }

        JComboBox<String> formatCombo = new JComboBox<>(new String[]{
                "SINGLE_ELIMINATION", "DOUBLE_ELIMINATION", "ROUND_ROBIN", "GROUP_STAGE_KNOCKOUT"
        });
        formatCombo.setSelectedItem(t.getFormat());

        JTextField prizeField = UIComponents.createTextField(10);
        prizeField.setText(t.getPrizePool() != null ? t.getPrizePool().toPlainString() : "0");

        JTextField entryField = UIComponents.createTextField(10);
        entryField.setText(t.getEntryFee() != null ? t.getEntryFee().toPlainString() : "0");

        JSpinner maxTeamsSpinner = new JSpinner(new SpinnerNumberModel(t.getMaxTeams() > 0 ? t.getMaxTeams() : 16, 2, 64, 1));

        JTextField organizerField = UIComponents.createTextField(20);
        organizerField.setText(t.getOrganizer() != null ? t.getOrganizer() : "eSports Arena Global");

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{
                "DRAFT", "UPCOMING", "REGISTRATION_OPEN", "REGISTRATION_CLOSED", "ONGOING", "COMPLETED", "CANCELLED"
        });
        statusCombo.setSelectedItem(t.getStatus());

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Game:"));
        panel.add(gameCombo);
        panel.add(new JLabel("Format:"));
        panel.add(formatCombo);
        panel.add(new JLabel("Prize Pool (₹):"));
        panel.add(prizeField);
        panel.add(new JLabel("Entry Fee (₹):"));
        panel.add(entryField);
        panel.add(new JLabel("Max Teams:"));
        panel.add(maxTeamsSpinner);
        panel.add(new JLabel("Organizer:"));
        panel.add(organizerField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        JButton saveBtn = UIComponents.createPrimaryButton("Update Tournament");
        saveBtn.addActionListener(e -> {
            try {
                String newTitle = titleField.getText().trim();
                if (newTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Tournament title cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BigDecimal prize = new BigDecimal(prizeField.getText().trim());
                BigDecimal entry = new BigDecimal(entryField.getText().trim());
                if (prize.compareTo(BigDecimal.ZERO) < 0 || entry.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(dialog, "Prize pool and entry fee cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                t.setTitle(newTitle);
                t.setGame((String) gameCombo.getSelectedItem());
                t.setFormat((String) formatCombo.getSelectedItem());
                t.setPrizePool(prize);
                t.setEntryFee(entry);
                t.setMaxTeams((Integer) maxTeamsSpinner.getValue());
                t.setOrganizer(organizerField.getText().trim());
                t.setStatus((String) statusCombo.getSelectedItem());

                if (tournamentDAO.update(t)) {
                    JOptionPane.showMessageDialog(dialog, "Tournament #" + t.getId() + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadTournaments();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update tournament in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric prize and fee values.", "Number Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void generateFixturesForSelected() {
        int row = tournamentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tournament to generate fixtures for.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tournamentTable.convertRowIndexToModel(row);
        int tourneyId = (Integer) tableModel.getValueAt(modelRow, 0);
        Tournament t = tournamentDAO.findById(tourneyId);
        if (t == null) return;

        List<Team> approvedTeams = registrationDAO.getApprovedTeams(tourneyId);
        if (approvedTeams.size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Tournament '" + t.getTitle() + "' has only " + approvedTeams.size() + " approved team(s).\n" +
                    "At least 2 approved teams are required to generate brackets and schedules.",
                    "Insufficient Teams", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Generate fixtures for '" + t.getTitle() + "'?\n" +
                "Format: " + t.getFormat() + "\n" +
                "Participating Approved Teams: " + approvedTeams.size() + "\n\n" +
                "Note: Any previously generated uncompleted fixtures for this tournament will be replaced.",
                "Confirm Fixture Generation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            FixtureService.GenerationResult result = fixtureService.generateFixtures(
                    tourneyId,
                    t.getFormat(),
                    approvedTeams,
                    LocalDateTime.now().plusDays(1).withHour(16).withMinute(0)
            );

            if (result.isSuccess()) {
                tournamentDAO.updateStatus(tourneyId, "ONGOING");
                JOptionPane.showMessageDialog(this,
                        result.getMessage() + "\nTournament status has been set to ONGOING.\n" +
                        "You can view and record match scores in the Matches Panel.",
                        "Fixtures Generated Successfully", JOptionPane.INFORMATION_MESSAGE);
                loadTournaments();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Generation Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showBracketForSelected() {
        int row = tournamentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tournament to view its bracket.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tournamentTable.convertRowIndexToModel(row);
        int tourneyId = (Integer) tableModel.getValueAt(modelRow, 0);
        com.esports.view.common.BracketViewerDialog.showBracket(this, tourneyId);
    }

    private void showChangeStatusDialog() {
        int row = tournamentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tournament.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tournamentTable.convertRowIndexToModel(row);
        int tourneyId = (Integer) tableModel.getValueAt(modelRow, 0);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 7);

        String[] statuses = {"DRAFT", "UPCOMING", "REGISTRATION_OPEN", "REGISTRATION_CLOSED", "ONGOING", "COMPLETED", "CANCELLED"};
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select new status for tournament #" + tourneyId + ":",
                "Update Tournament Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                currentStatus);

        if (selected != null && !selected.equals(currentStatus)) {
            if (tournamentDAO.updateStatus(tourneyId, selected)) {
                JOptionPane.showMessageDialog(this, "Tournament status updated to " + selected, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadTournaments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update tournament status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        int row = tournamentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a tournament to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tournamentTable.convertRowIndexToModel(row);
        int tourneyId = (Integer) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete tournament '" + title + "' (ID: #" + tourneyId + ")?\n" +
                "All associated registrations, fixtures, and standings will also be permanently deleted.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (tournamentDAO.delete(tourneyId)) {
                JOptionPane.showMessageDialog(this, "Tournament #" + tourneyId + " successfully deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadTournaments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete tournament #" + tourneyId + " from database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
