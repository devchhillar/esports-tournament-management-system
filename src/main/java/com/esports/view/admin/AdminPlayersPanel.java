package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.GameDAO;
import com.esports.dao.GameDAOImpl;
import com.esports.dao.PlayerDAO;
import com.esports.dao.PlayerDAOImpl;
import com.esports.model.Game;
import com.esports.model.Player;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Panel for Managing Players, Ranks, and Competitive Profiles.
 */
public class AdminPlayersPanel extends JPanel {

    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final GameDAO gameDAO = new GameDAOImpl();

    private DefaultTableModel tableModel;
    private JTable playerTable;
    private JComboBox<String> gameFilterCombo;
    private JTextField searchField;

    public AdminPlayersPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadPlayers();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Top Controls Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        searchField = UIComponents.createTextField(10);
        searchField.addActionListener(e -> applyFilters());

        JButton searchBtn = UIComponents.createSecondaryButton("🔍");
        searchBtn.addActionListener(e -> applyFilters());

        gameFilterCombo = new JComboBox<>();
        gameFilterCombo.addItem("All Games");
        for (Game g : gameDAO.findAll()) {
            gameFilterCombo.addItem(g.getName());
        }
        gameFilterCombo.addActionListener(e -> applyFilters());

        leftFilters.add(new JLabel("Search:"));
        leftFilters.add(searchField);
        leftFilters.add(searchBtn);
        leftFilters.add(new JLabel("Game:"));
        leftFilters.add(gameFilterCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton editBtn = UIComponents.createAccentButton("✏️ Edit Profile");
        JButton pointsBtn = UIComponents.createStyledButton("⭐ Adjust Points", new Color(245, 158, 11), Color.WHITE, new Color(251, 191, 36));
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        editBtn.addActionListener(e -> showEditPlayerDialog());
        pointsBtn.addActionListener(e -> adjustPoints());
        refreshBtn.addActionListener(e -> loadPlayers());

        buttonPanel.add(editBtn);
        buttonPanel.add(pointsBtn);
        buttonPanel.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(buttonPanel, BorderLayout.EAST);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Table
        String[] cols = {"Rank", "ID", "Gamer Tag", "Full Name", "Current Squad", "Primary Game", "Role", "Country", "Points", "Matches", "W / L", "Kills", "MVPs", "Win %"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        playerTable = new JTable(tableModel);
        playerTable.setShowGrid(true);
        playerTable.setFillsViewportHeight(true);
        playerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        playerTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        playerTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        playerTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        playerTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        playerTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        playerTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        playerTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        playerTable.getColumnModel().getColumn(8).setPreferredWidth(70);
        playerTable.getColumnModel().getColumn(9).setPreferredWidth(60);
        playerTable.getColumnModel().getColumn(10).setPreferredWidth(60);
        playerTable.getColumnModel().getColumn(11).setPreferredWidth(60);
        playerTable.getColumnModel().getColumn(12).setPreferredWidth(60);
        playerTable.getColumnModel().getColumn(13).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(playerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private void applyFilters() {
        String kw = searchField.getText().trim().toLowerCase();
        String game = (String) gameFilterCombo.getSelectedItem();

        tableModel.setRowCount(0);
        List<Player> list = (game != null && !game.equals("All Games")) ? playerDAO.getLeaderboard(game, 100) : playerDAO.findAll();
        int rank = 1;
        for (Player p : list) {
            boolean matchKw = kw.isEmpty() || p.getGamerTag().toLowerCase().contains(kw) ||
                    (p.getFullName() != null && p.getFullName().toLowerCase().contains(kw)) ||
                    (p.getCurrentTeamName() != null && p.getCurrentTeamName().toLowerCase().contains(kw));

            if (matchKw) {
                String rankStr = (rank == 1) ? "🥇 1" : (rank == 2) ? "🥈 2" : (rank == 3) ? "🥉 3" : String.valueOf(rank);
                tableModel.addRow(new Object[]{
                        rankStr,
                        p.getId(),
                        p.getGamerTag(),
                        p.getFullName() != null ? p.getFullName() : "-",
                        p.getCurrentTeamName() != null ? p.getCurrentTeamName() : "Free Agent",
                        p.getPrimaryGame(),
                        p.getInGameRole(),
                        p.getCountry(),
                        p.getRankingPoints(),
                        p.getMatchesPlayed(),
                        p.getWins() + " / " + p.getLosses(),
                        p.getKills(),
                        p.getMvpCount(),
                        String.format("%.1f%%", p.getWinRate())
                });
                rank++;
            }
        }
    }

    private void loadPlayers() {
        applyFilters();
    }

    private void showEditPlayerDialog() {
        int row = playerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a player to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int playerId = (Integer) tableModel.getValueAt(row, 1);
        Player p = playerDAO.findById(playerId);
        if (p == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Player Profile - " + p.getGamerTag(), true);
        dialog.setSize(500, 490);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField tagField = UIComponents.createTextField(15);
        tagField.setText(p.getGamerTag());

        JComboBox<String> gameBox = new JComboBox<>();
        for (Game g : gameDAO.findAll()) {
            gameBox.addItem(g.getName());
        }
        gameBox.setSelectedItem(p.getPrimaryGame());

        JTextField roleField = UIComponents.createTextField(15);
        roleField.setText(p.getInGameRole());

        JTextField countryField = UIComponents.createTextField(15);
        countryField.setText(p.getCountry());

        JTextField bioField = UIComponents.createTextField(15);
        bioField.setText(p.getBio() != null ? p.getBio() : "");

        final String[] avatarPath = {p.getProfilePicture()};
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        avatarPanel.setOpaque(false);
        JLabel avatarStatus = new JLabel(p.getProfilePicture() != null ? new java.io.File(p.getProfilePicture()).getName() : "No avatar set");
        avatarStatus.setFont(ThemeConfig.FONT_SMALL);
        avatarStatus.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadAvatarBtn = UIComponents.createSecondaryButton("📁 Browse Avatar");
        uploadAvatarBtn.addActionListener(e -> {
            String saved = com.esports.util.FileUploadUtil.chooseAndSaveImage(dialog, "avatars");
            if (saved != null) {
                avatarPath[0] = saved;
                avatarStatus.setText("Uploaded: " + new java.io.File(saved).getName());
                avatarStatus.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        avatarPanel.add(uploadAvatarBtn);
        avatarPanel.add(avatarStatus);

        panel.add(new JLabel("Gamer Tag:"));
        panel.add(tagField);
        panel.add(new JLabel("Primary Game:"));
        panel.add(gameBox);
        panel.add(new JLabel("In-Game Role:"));
        panel.add(roleField);
        panel.add(new JLabel("Country:"));
        panel.add(countryField);
        panel.add(new JLabel("Biography:"));
        panel.add(bioField);
        panel.add(new JLabel("Player Avatar:"));
        panel.add(avatarPanel);

        JButton saveBtn = UIComponents.createPrimaryButton("Update Profile");
        saveBtn.addActionListener(e -> {
            p.setGamerTag(tagField.getText().trim());
            p.setPrimaryGame((String) gameBox.getSelectedItem());
            p.setInGameRole(roleField.getText().trim());
            p.setCountry(countryField.getText().trim());
            p.setBio(bioField.getText().trim());
            p.setProfilePicture(avatarPath[0]);

            if (playerDAO.updateProfile(p)) {
                JOptionPane.showMessageDialog(dialog, "Player profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadPlayers();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update player profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void adjustPoints() {
        int row = playerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a player to adjust ranking points.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int playerId = (Integer) tableModel.getValueAt(row, 1);
        String tag = (String) tableModel.getValueAt(row, 2);

        String input = JOptionPane.showInputDialog(this, "Enter ranking points adjustment delta for " + tag + " (e.g. 50 or -25):", "Adjust Points", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int delta = Integer.parseInt(input.trim());
                playerDAO.updateRankingAndStats(playerId, delta, delta > 0);
                JOptionPane.showMessageDialog(this, "Ranking points updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPlayers();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid integer delta value.", "Invalid Number", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
