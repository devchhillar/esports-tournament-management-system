package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.RegistrationDAO;
import com.esports.dao.RegistrationDAOImpl;
import com.esports.model.Registration;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Panel for Reviewing, Approving, and Rejecting Tournament Registrations.
 */
public class AdminRegistrationsPanel extends JPanel {

    private final RegistrationDAO registrationDAO = new RegistrationDAOImpl();
    private DefaultTableModel tableModel;
    private JTable registrationTable;

    public AdminRegistrationsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadRegistrations();
    }

    private void initializeUI() {
        // Top Action Bar
        JPanel topBar = UIComponents.createCardPanel();
        topBar.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Tournament Registration Requests");
        titleLabel.setFont(ThemeConfig.FONT_SUBTITLE);
        titleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton approveBtn = UIComponents.createStyledButton("✅ Approve Selected", ThemeConfig.COLOR_SUCCESS, Color.WHITE, new Color(52, 211, 153));
        JButton rejectBtn = UIComponents.createDangerButton("❌ Reject Selected");
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        approveBtn.addActionListener(e -> updateSelectedStatus("APPROVED"));
        rejectBtn.addActionListener(e -> updateSelectedStatus("REJECTED"));
        refreshBtn.addActionListener(e -> loadRegistrations());

        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(refreshBtn);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(buttonPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UIComponents.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"Reg ID", "Tournament Title", "Game", "Team Name", "Tag", "Registered By", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        registrationTable = new JTable(tableModel);
        registrationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(registrationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadRegistrations() {
        SwingWorker<List<Registration>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Registration> doInBackground() {
                return registrationDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Registration> list = get();
                    tableModel.setRowCount(0);
                    for (Registration r : list) {
                        tableModel.addRow(new Object[]{
                                r.getId(),
                                r.getTournamentTitle(),
                                r.getGameName(),
                                r.getTeamName(),
                                "[" + r.getTeamTag() + "]",
                                r.getRegisteredByName(),
                                r.getStatus(),
                                r.getRegistrationDate() != null ? r.getRegistrationDate().toString().substring(0, 16) : ""
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void updateSelectedStatus(String newStatus) {
        int row = registrationTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a registration entry from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int regId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        if (newStatus.equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Registration is already marked as " + newStatus, "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean ok = registrationDAO.updateStatus(regId, newStatus);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Registration #" + regId + " updated to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            loadRegistrations();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update registration status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
