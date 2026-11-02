package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.GameDAO;
import com.esports.dao.GameDAOImpl;
import com.esports.model.Game;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dedicated Game Management Module.
 * Allows tournament administrators to manage official eSports titles, team sizes, and categories.
 */
public class AdminGamesPanel extends JPanel {

    private final GameDAO gameDAO = new GameDAOImpl();
    private JTable gamesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;

    public AdminGamesPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadGames();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 1. Top Controls Bar - Structured 2-Tier Layout to Prevent Overlapping
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);

        // Row 1: Search & Category Filter
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterRow.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(ThemeConfig.FONT_BOLD);
        searchLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        searchField = UIComponents.createTextField(14);
        searchField.addActionListener(e -> filterGames());

        JButton searchBtn = UIComponents.createSecondaryButton("🔍 Search");
        searchBtn.addActionListener(e -> filterGames());

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(ThemeConfig.FONT_BOLD);
        catLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        categoryFilter = new JComboBox<>(new String[]{"All Categories", "Battle Royale", "Tactical FPS", "FPS / Battle Royale", "MOBA", "Various"});
        categoryFilter.setFont(ThemeConfig.FONT_REGULAR);
        categoryFilter.setBackground(ThemeConfig.COLOR_CARD_BG);
        categoryFilter.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        categoryFilter.addActionListener(e -> filterGames());

        filterRow.add(searchLabel);
        filterRow.add(searchField);
        filterRow.add(searchBtn);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(catLabel);
        filterRow.add(categoryFilter);

        // Row 2: Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionRow.setOpaque(false);

        JButton addBtn = UIComponents.createPrimaryButton("➕ Add New Game");
        addBtn.addActionListener(e -> showAddGameDialog());

        JButton editBtn = UIComponents.createAccentButton("✏️ Edit Selected");
        editBtn.addActionListener(e -> showEditGameDialog());

        JButton deleteBtn = UIComponents.createDangerButton("🗑️ Delete");
        deleteBtn.addActionListener(e -> deleteSelectedGame());

        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadGames());

        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(deleteBtn);
        actionRow.add(refreshBtn);

        topBar.add(filterRow);
        topBar.add(Box.createVerticalStrut(4));
        topBar.add(actionRow);
        mainCard.add(topBar, BorderLayout.NORTH);

        // 2. Table
        String[] cols = {"ID", "Game Name", "Category", "Min Size", "Max Size", "Status", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        gamesTable = new JTable(tableModel);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesTable.setShowGrid(true);
        gamesTable.setFillsViewportHeight(true);
        gamesTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        gamesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        gamesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        gamesTable.getColumnModel().getColumn(0).setMinWidth(40);
        gamesTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        gamesTable.getColumnModel().getColumn(1).setMinWidth(120);
        gamesTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        gamesTable.getColumnModel().getColumn(2).setMinWidth(100);
        gamesTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        gamesTable.getColumnModel().getColumn(3).setMinWidth(60);
        gamesTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        gamesTable.getColumnModel().getColumn(4).setMinWidth(60);
        gamesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        gamesTable.getColumnModel().getColumn(5).setMinWidth(70);
        gamesTable.getColumnModel().getColumn(6).setPreferredWidth(320);
        gamesTable.getColumnModel().getColumn(6).setMinWidth(150);

        JScrollPane scrollPane = new JScrollPane(gamesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private void loadGames() {
        tableModel.setRowCount(0);
        List<Game> games = gameDAO.findAll();
        for (Game g : games) {
            tableModel.addRow(new Object[]{
                    g.getId(),
                    g.getName(),
                    g.getCategory(),
                    g.getMinTeamSize(),
                    g.getMaxTeamSize(),
                    g.getStatus(),
                    g.getDescription()
            });
        }
    }

    private void filterGames() {
        String query = searchField.getText().trim().toLowerCase();
        String cat = (String) categoryFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<Game> games = gameDAO.findAll();
        for (Game g : games) {
            boolean matchesQuery = query.isEmpty() || g.getName().toLowerCase().contains(query) ||
                    (g.getDescription() != null && g.getDescription().toLowerCase().contains(query));
            boolean matchesCat = cat == null || cat.equals("All Categories") || g.getCategory().equalsIgnoreCase(cat);

            if (matchesQuery && matchesCat) {
                tableModel.addRow(new Object[]{
                        g.getId(),
                        g.getName(),
                        g.getCategory(),
                        g.getMinTeamSize(),
                        g.getMaxTeamSize(),
                        g.getStatus(),
                        g.getDescription()
                });
            }
        }
    }

    private void showAddGameDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New eSports Title", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JTextField nameField = UIComponents.createTextField(15);
        JComboBox<String> catBox = new JComboBox<>(new String[]{"Battle Royale", "Tactical FPS", "FPS / Battle Royale", "MOBA", "Fighting", "Strategy", "Sports", "Various"});
        JSpinner minSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        JSpinner maxSizeSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 15, 1));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        JTextArea descArea = new JTextArea(3, 15);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(ThemeConfig.COLOR_CARD_BG);
        descArea.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        panel.add(new JLabel("Game Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(catBox);
        panel.add(new JLabel("Min Team Size:"));
        panel.add(minSizeSpinner);
        panel.add(new JLabel("Max Team Size:"));
        panel.add(maxSizeSpinner);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descArea));

        JButton saveBtn = UIComponents.createPrimaryButton("Save Game");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Game name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int minSize = (Integer) minSizeSpinner.getValue();
            int maxSize = (Integer) maxSizeSpinner.getValue();
            if (minSize > maxSize) {
                JOptionPane.showMessageDialog(dialog, "Min team size cannot exceed max team size.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Game g = new Game(name, (String) catBox.getSelectedItem(), descArea.getText().trim(), minSize, maxSize);
            g.setStatus((String) statusBox.getSelectedItem());
            if (gameDAO.create(g)) {
                JOptionPane.showMessageDialog(dialog, "Game added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadGames();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create game. Name might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showEditGameDialog() {
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a game to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int gameId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Game g = gameDAO.findById(gameId);
        if (g == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Game - " + g.getName(), true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JTextField nameField = UIComponents.createTextField(15);
        nameField.setText(g.getName());
        JComboBox<String> catBox = new JComboBox<>(new String[]{"Battle Royale", "Tactical FPS", "FPS / Battle Royale", "MOBA", "Fighting", "Strategy", "Sports", "Various"});
        catBox.setSelectedItem(g.getCategory());
        JSpinner minSizeSpinner = new JSpinner(new SpinnerNumberModel(g.getMinTeamSize(), 1, 10, 1));
        JSpinner maxSizeSpinner = new JSpinner(new SpinnerNumberModel(g.getMaxTeamSize(), 1, 15, 1));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        statusBox.setSelectedItem(g.getStatus());
        JTextArea descArea = new JTextArea(3, 15);
        descArea.setText(g.getDescription());
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(ThemeConfig.COLOR_CARD_BG);
        descArea.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        panel.add(new JLabel("Game Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(catBox);
        panel.add(new JLabel("Min Team Size:"));
        panel.add(minSizeSpinner);
        panel.add(new JLabel("Max Team Size:"));
        panel.add(maxSizeSpinner);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descArea));

        JButton saveBtn = UIComponents.createPrimaryButton("Update Game");
        saveBtn.addActionListener(e -> {
            g.setName(nameField.getText().trim());
            g.setCategory((String) catBox.getSelectedItem());
            g.setMinTeamSize((Integer) minSizeSpinner.getValue());
            g.setMaxTeamSize((Integer) maxSizeSpinner.getValue());
            g.setStatus((String) statusBox.getSelectedItem());
            g.setDescription(descArea.getText().trim());

            if (gameDAO.update(g)) {
                JOptionPane.showMessageDialog(dialog, "Game updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadGames();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update game.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void deleteSelectedGame() {
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a game to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int gameId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete game '" + name + "'?\nWarning: This may affect related tournament entries.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (gameDAO.delete(gameId)) {
                JOptionPane.showMessageDialog(this, "Game deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadGames();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete game: It is currently linked to existing tournaments or teams.", "Foreign Key Protection", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
