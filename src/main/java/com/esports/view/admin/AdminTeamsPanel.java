package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.dao.*;
import com.esports.model.*;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Panel for Managing Teams, Rosters, Captains, and Managers.
 */
public class AdminTeamsPanel extends JPanel {

    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final GameDAO gameDAO = new GameDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    private DefaultTableModel tableModel;
    private JTable teamTable;
    private JComboBox<String> gameFilterCombo;

    public AdminTeamsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadTeams();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Top Bar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        gameFilterCombo = new JComboBox<>();
        gameFilterCombo.addItem("All Games");
        for (Game g : gameDAO.findAll()) {
            gameFilterCombo.addItem(g.getName());
        }
        gameFilterCombo.addActionListener(e -> loadTeams());

        leftFilters.add(new JLabel("Filter by Game:"));
        leftFilters.add(gameFilterCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton addBtn = UIComponents.createPrimaryButton("➕ Register Team");
        JButton editBtn = UIComponents.createAccentButton("✏️ Edit Team");
        JButton rosterBtn = UIComponents.createStyledButton("👥 Manage Roster", new Color(16, 185, 129), Color.WHITE, new Color(52, 211, 153));
        JButton deleteBtn = UIComponents.createDangerButton("🗑️ Delete");
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        addBtn.addActionListener(e -> showAddTeamDialog());
        editBtn.addActionListener(e -> showEditTeamDialog());
        rosterBtn.addActionListener(e -> showManageRosterDialog());
        deleteBtn.addActionListener(e -> deleteSelectedTeam());
        refreshBtn.addActionListener(e -> loadTeams());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(rosterBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(buttonPanel, BorderLayout.EAST);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Table
        String[] cols = {"ID", "Team Name", "Tag", "Game Title", "Leader / Captain", "Manager", "Members", "Played", "Won", "Lost", "Points", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        teamTable = new JTable(tableModel);
        teamTable.setShowGrid(true);
        teamTable.setFillsViewportHeight(true);
        teamTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        teamTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        teamTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        teamTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        teamTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        teamTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        teamTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        teamTable.getColumnModel().getColumn(6).setPreferredWidth(70);
        teamTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        teamTable.getColumnModel().getColumn(8).setPreferredWidth(50);
        teamTable.getColumnModel().getColumn(9).setPreferredWidth(50);
        teamTable.getColumnModel().getColumn(10).setPreferredWidth(60);
        teamTable.getColumnModel().getColumn(11).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(teamTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private void loadTeams() {
        String filter = (String) gameFilterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        List<Team> teams = (filter != null && !filter.equals("All Games")) ? teamDAO.findByGame(filter) : teamDAO.findAll();
        for (Team t : teams) {
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getTeamName(),
                    "[" + t.getTeamTag() + "]",
                    t.getGame(),
                    t.getLeaderName() != null ? t.getLeaderName() : ("User #" + t.getLeaderId()),
                    t.getManagerName() != null ? t.getManagerName() : "Unassigned",
                    t.getMemberCount(),
                    t.getMatchesPlayed(),
                    t.getMatchesWon(),
                    t.getMatchesLost(),
                    t.getTotalPoints(),
                    t.getStatus()
            });
        }
    }

    private void showAddTeamDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New eSports Team", true);
        dialog.setSize(500, 490);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 12, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameField = UIComponents.createTextField(20);
        JTextField tagField = UIComponents.createTextField(6);
        JComboBox<String> gameCombo = new JComboBox<>();
        for (Game g : gameDAO.findActive()) {
            gameCombo.addItem(g.getName());
        }

        JComboBox<User> leaderCombo = new JComboBox<>();
        for (User u : userDAO.findAll()) {
            leaderCombo.addItem(u);
        }

        JComboBox<UserChoice> managerCombo = new JComboBox<>();
        managerCombo.addItem(new UserChoice(null, "-- None / Self Managed --"));
        for (User u : userDAO.findByRole(Role.TEAM_MANAGER)) {
            managerCombo.addItem(new UserChoice(u.getId(), u.getFullName() + " (@" + u.getUsername() + ")"));
        }

        JTextField descField = UIComponents.createTextField(20);

        final String[] logoPath = {null};
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoPanel.setOpaque(false);
        JLabel logoStatus = new JLabel("No logo set");
        logoStatus.setFont(ThemeConfig.FONT_SMALL);
        logoStatus.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadLogoBtn = UIComponents.createSecondaryButton("📁 Browse Logo");
        uploadLogoBtn.addActionListener(e -> {
            String saved = com.esports.util.FileUploadUtil.chooseAndSaveImage(dialog, "logos");
            if (saved != null) {
                logoPath[0] = saved;
                logoStatus.setText("Uploaded: " + new java.io.File(saved).getName());
                logoStatus.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        logoPanel.add(uploadLogoBtn);
        logoPanel.add(logoStatus);

        panel.add(new JLabel("Team Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Team Tag (e.g. SOUL):"));
        panel.add(tagField);
        panel.add(new JLabel("Game Category:"));
        panel.add(gameCombo);
        panel.add(new JLabel("Team Leader / Captain:"));
        panel.add(leaderCombo);
        panel.add(new JLabel("Team Manager:"));
        panel.add(managerCombo);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Team Logo:"));
        panel.add(logoPanel);

        JButton saveBtn = UIComponents.createPrimaryButton("Create Team");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String tag = tagField.getText().trim().toUpperCase();

            if (name.isEmpty() || tag.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Team name and tag are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User leader = (User) leaderCombo.getSelectedItem();
            UserChoice manager = (UserChoice) managerCombo.getSelectedItem();

            Team team = new Team();
            team.setTeamName(name);
            team.setTeamTag(tag);
            team.setGame((String) gameCombo.getSelectedItem());
            team.setLeaderId(leader != null ? leader.getId() : 1);
            team.setManagerId(manager != null ? manager.id : null);
            team.setDescription(descField.getText().trim());
            team.setLogoPath(logoPath[0]);
            team.setStatus("ACTIVE");

            if (teamDAO.create(team, team.getLeaderId())) {
                JOptionPane.showMessageDialog(dialog, "Team created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadTeams();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create team. Ensure team name and tag are unique.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditTeamDialog() {
        int row = teamTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a team to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int teamId = (Integer) tableModel.getValueAt(row, 0);
        Team t = teamDAO.findById(teamId);
        if (t == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Team - " + t.getTeamName(), true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField nameField = UIComponents.createTextField(15);
        nameField.setText(t.getTeamName());
        JTextField tagField = UIComponents.createTextField(6);
        tagField.setText(t.getTeamTag());
        JTextField descField = UIComponents.createTextField(15);
        descField.setText(t.getDescription() != null ? t.getDescription() : "");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "DISBANDED"});
        statusCombo.setSelectedItem(t.getStatus());

        JComboBox<UserChoice> managerCombo = new JComboBox<>();
        managerCombo.addItem(new UserChoice(null, "-- None / Self Managed --"));
        for (User u : userDAO.findByRole(Role.TEAM_MANAGER)) {
            managerCombo.addItem(new UserChoice(u.getId(), u.getFullName() + " (@" + u.getUsername() + ")"));
        }
        if (t.getManagerId() != null) {
            for (int i = 0; i < managerCombo.getItemCount(); i++) {
                if (managerCombo.getItemAt(i).id != null && managerCombo.getItemAt(i).id.equals(t.getManagerId())) {
                    managerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        final String[] updatedLogoPath = {t.getLogoPath()};
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoPanel.setOpaque(false);
        JLabel logoStatus = new JLabel(t.getLogoPath() != null ? new java.io.File(t.getLogoPath()).getName() : "No logo set");
        logoStatus.setFont(ThemeConfig.FONT_SMALL);
        logoStatus.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadLogoBtn = UIComponents.createSecondaryButton("📁 Browse Logo");
        uploadLogoBtn.addActionListener(e -> {
            String saved = com.esports.util.FileUploadUtil.chooseAndSaveImage(dialog, "logos");
            if (saved != null) {
                updatedLogoPath[0] = saved;
                logoStatus.setText("Uploaded: " + new java.io.File(saved).getName());
                logoStatus.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        logoPanel.add(uploadLogoBtn);
        logoPanel.add(logoStatus);

        panel.add(new JLabel("Team Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Team Tag:"));
        panel.add(tagField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Team Manager:"));
        panel.add(managerCombo);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Team Logo:"));
        panel.add(logoPanel);

        JButton saveBtn = UIComponents.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            t.setTeamName(nameField.getText().trim());
            t.setTeamTag(tagField.getText().trim().toUpperCase());
            t.setStatus((String) statusCombo.getSelectedItem());
            UserChoice mc = (UserChoice) managerCombo.getSelectedItem();
            t.setManagerId(mc != null ? mc.id : null);
            t.setDescription(descField.getText().trim());
            t.setLogoPath(updatedLogoPath[0]);

            if (teamDAO.update(t)) {
                JOptionPane.showMessageDialog(dialog, "Team updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadTeams();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update team.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showManageRosterDialog() {
        int row = teamTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a team to manage roster.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int teamId = (Integer) tableModel.getValueAt(row, 0);
        Team team = teamDAO.findById(teamId);
        if (team == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Roster Management - " + team.getTeamName() + " (" + team.getGame() + ")", true);
        dialog.setSize(640, 500);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(ThemeConfig.COLOR_BG_DARK);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Roster table
        String[] cols = {"Player ID", "Gamer Tag", "Full Name", "Role", "Ranking Pts", "Kills"};
        DefaultTableModel rModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable rTable = new JTable(rModel);
        rTable.setShowGrid(true);
        rTable.setFillsViewportHeight(true);

        Runnable refreshRoster = () -> {
            rModel.setRowCount(0);
            List<Player> members = playerDAO.findByTeamId(teamId);
            for (Player p : members) {
                rModel.addRow(new Object[]{p.getId(), p.getGamerTag(), p.getFullName(), p.getInGameRole(), p.getRankingPoints(), p.getKills()});
            }
        };
        refreshRoster.run();

        content.add(new JScrollPane(rTable), BorderLayout.CENTER);

        // Action controls
        JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actPanel.setOpaque(false);

        JButton addPlayerBtn = UIComponents.createPrimaryButton("➕ Add Player to Squad");
        addPlayerBtn.addActionListener(e -> {
            List<Player> available = playerDAO.findAvailablePlayers(team.getGame());
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No unassigned players available for " + team.getGame() + ".", "No Players", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JComboBox<Player> availCombo = new JComboBox<>();
            for (Player ap : available) availCombo.addItem(ap);

            int res = JOptionPane.showConfirmDialog(dialog, availCombo, "Select Player to Add to Squad", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                Player selPlayer = (Player) availCombo.getSelectedItem();
                if (selPlayer != null) {
                    if (teamDAO.addMember(teamId, selPlayer.getId(), "MEMBER")) {
                        JOptionPane.showMessageDialog(dialog, "Player " + selPlayer.getGamerTag() + " added to " + team.getTeamName(), "Player Added", JOptionPane.INFORMATION_MESSAGE);
                        refreshRoster.run();
                        loadTeams();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to add player. Roster limit might be reached or player is incompatible.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton removePlayerBtn = UIComponents.createDangerButton("❌ Remove Selected");
        removePlayerBtn.addActionListener(e -> {
            int rSel = rTable.getSelectedRow();
            if (rSel >= 0) {
                int pId = (Integer) rModel.getValueAt(rSel, 0);
                if (teamDAO.removeMember(teamId, pId)) {
                    JOptionPane.showMessageDialog(dialog, "Player removed from roster.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshRoster.run();
                    loadTeams();
                }
            }
        });

        actPanel.add(addPlayerBtn);
        actPanel.add(removePlayerBtn);
        content.add(actPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void deleteSelectedTeam() {
        int row = teamTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a team to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int teamId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete team '" + name + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (teamDAO.delete(teamId)) {
                JOptionPane.showMessageDialog(this, "Team deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTeams();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete team: team has active match fixtures or registrations.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class UserChoice {
        final Integer id;
        final String name;
        UserChoice(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
