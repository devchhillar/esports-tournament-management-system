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
import java.math.BigDecimal;
import java.util.List;

/**
 * Admin Panel for Managing Tournament Entry Fees, Payments, and Prize Payouts.
 */
public class AdminPaymentsPanel extends JPanel {

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    private DefaultTableModel tableModel;
    private JTable paymentTable;
    private JComboBox<TournamentChoice> tourneyFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> statusFilterCombo;

    private JLabel totalRevenueLabel;
    private JLabel totalPayoutsLabel;
    private JLabel netBalanceLabel;
    private JLabel totalTxnCountLabel;

    public AdminPaymentsPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadPayments();
    }

    private void initializeUI() {
        // 1. Top KPI Summary Cards Row
        JPanel topKpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        topKpiRow.setOpaque(false);

        totalRevenueLabel = new JLabel("₹ 0.00", SwingConstants.CENTER);
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalRevenueLabel.setForeground(ThemeConfig.COLOR_SUCCESS);

        totalPayoutsLabel = new JLabel("₹ 0.00", SwingConstants.CENTER);
        totalPayoutsLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalPayoutsLabel.setForeground(ThemeConfig.COLOR_DANGER);

        netBalanceLabel = new JLabel("₹ 0.00", SwingConstants.CENTER);
        netBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        netBalanceLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        totalTxnCountLabel = new JLabel("0", SwingConstants.CENTER);
        totalTxnCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalTxnCountLabel.setForeground(new Color(245, 158, 11));

        topKpiRow.add(createKpiCard("💰 Total Entry Fees Collected", totalRevenueLabel));
        topKpiRow.add(createKpiCard("🏆 Total Prize Money Paid", totalPayoutsLabel));
        topKpiRow.add(createKpiCard("⚖️ Net Tournament Balance", netBalanceLabel));
        topKpiRow.add(createKpiCard("📜 Total Transactions", totalTxnCountLabel));

        add(topKpiRow, BorderLayout.NORTH);

        // 2. Main Card with Controls and Table
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // Controls Bar - Responsive 2-Tier Layout to Prevent Overlapping
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);

        // Row 1: Filters Bar
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterRow.setOpaque(false);

        tourneyFilterCombo = new JComboBox<>();
        tourneyFilterCombo.addItem(new TournamentChoice(0, "All Tournaments"));
        for (Tournament t : tournamentDAO.findAll()) {
            tourneyFilterCombo.addItem(new TournamentChoice(t.getId(), t.getTitle()));
        }
        tourneyFilterCombo.addActionListener(e -> applyFilters());

        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "ENTRY_FEE", "PRIZE_PAYOUT", "REFUND"});
        typeFilterCombo.addActionListener(e -> applyFilters());

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "COMPLETED", "PENDING", "FAILED", "REFUNDED"});
        statusFilterCombo.addActionListener(e -> applyFilters());

        JLabel tourneyLbl = new JLabel("Tournament:");
        tourneyLbl.setFont(ThemeConfig.FONT_BOLD);
        tourneyLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel typeLbl = new JLabel("Type:");
        typeLbl.setFont(ThemeConfig.FONT_BOLD);
        typeLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setFont(ThemeConfig.FONT_BOLD);
        statusLbl.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        filterRow.add(tourneyLbl);
        filterRow.add(tourneyFilterCombo);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(typeLbl);
        filterRow.add(typeFilterCombo);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(statusLbl);
        filterRow.add(statusFilterCombo);

        // Row 2: Financial Action Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        buttonRow.setOpaque(false);

        JButton recordBtn = UIComponents.createPrimaryButton("➕ Record Fee Payment");
        JButton payoutBtn = UIComponents.createStyledButton("🏆 Distribute Prize Payout", new Color(157, 78, 221), Color.WHITE, new Color(192, 132, 252));
        JButton updateStatusBtn = UIComponents.createAccentButton("🔄 Update Status");
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        recordBtn.addActionListener(e -> showRecordPaymentDialog());
        payoutBtn.addActionListener(e -> showPrizePayoutDialog());
        updateStatusBtn.addActionListener(e -> showUpdateStatusDialog());
        refreshBtn.addActionListener(e -> loadPayments());

        buttonRow.add(recordBtn);
        buttonRow.add(payoutBtn);
        buttonRow.add(updateStatusBtn);
        buttonRow.add(refreshBtn);

        topBar.add(filterRow);
        topBar.add(Box.createVerticalStrut(4));
        topBar.add(buttonRow);
        mainCard.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Transaction ID", "Tournament Title", "Team / Recipient", "User Account", "Amount (INR)", "Payment Type", "Method", "Status", "Date", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        paymentTable = new JTable(tableModel);
        paymentTable.setShowGrid(true);
        paymentTable.setFillsViewportHeight(true);
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        paymentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        paymentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        paymentTable.getColumnModel().getColumn(0).setMinWidth(40);
        paymentTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        paymentTable.getColumnModel().getColumn(1).setMinWidth(110);
        paymentTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        paymentTable.getColumnModel().getColumn(2).setMinWidth(140);
        paymentTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        paymentTable.getColumnModel().getColumn(3).setMinWidth(120);
        paymentTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        paymentTable.getColumnModel().getColumn(4).setMinWidth(100);
        paymentTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        paymentTable.getColumnModel().getColumn(5).setMinWidth(90);
        paymentTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        paymentTable.getColumnModel().getColumn(6).setMinWidth(90);
        paymentTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(7).setMinWidth(80);
        paymentTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        paymentTable.getColumnModel().getColumn(8).setMinWidth(80);
        paymentTable.getColumnModel().getColumn(9).setPreferredWidth(130);
        paymentTable.getColumnModel().getColumn(9).setMinWidth(110);
        paymentTable.getColumnModel().getColumn(10).setPreferredWidth(180);
        paymentTable.getColumnModel().getColumn(10).setMinWidth(120);

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel) {
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeConfig.FONT_SMALL);
        titleLbl.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadPayments() {
        // Update KPI Totals
        BigDecimal rev = paymentDAO.getTotalRevenue();
        BigDecimal payouts = paymentDAO.getTotalPrizePayouts();
        BigDecimal net = rev.subtract(payouts);

        totalRevenueLabel.setText("₹ " + rev.toPlainString());
        totalPayoutsLabel.setText("₹ " + payouts.toPlainString());
        netBalanceLabel.setText("₹ " + net.toPlainString());

        applyFilters();
    }

    private void applyFilters() {
        TournamentChoice tc = (TournamentChoice) tourneyFilterCombo.getSelectedItem();
        int tourneyId = tc != null ? tc.id : 0;
        String typeFilter = (String) typeFilterCombo.getSelectedItem();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();

        List<Payment> list;
        if (tourneyId > 0) {
            list = paymentDAO.findByTournament(tourneyId);
        } else {
            list = paymentDAO.findAll();
        }

        tableModel.setRowCount(0);
        int matchedCount = 0;
        for (Payment p : list) {
            if (typeFilter != null && !typeFilter.equals("All Types") && !typeFilter.equalsIgnoreCase(p.getPaymentType())) {
                continue;
            }
            if (statusFilter != null && !statusFilter.equals("All Statuses") && !statusFilter.equalsIgnoreCase(p.getStatus())) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getTransactionId(),
                    p.getTournamentTitle(),
                    p.getTeamName() + " [" + p.getTeamTag() + "]",
                    p.getUserName(),
                    "₹ " + p.getAmount().toPlainString(),
                    p.getPaymentType(),
                    p.getPaymentMethod(),
                    p.getStatus(),
                    p.getPaymentDate() != null ? p.getPaymentDate().toString().substring(0, 16) : "-",
                    p.getNotes() != null ? p.getNotes() : ""
            });
            matchedCount++;
        }
        totalTxnCountLabel.setText(String.valueOf(matchedCount));
    }

    private void showRecordPaymentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Tournament Entry Fee Payment", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JComboBox<Tournament> tourneyBox = new JComboBox<>();
        for (Tournament t : tournamentDAO.findAll()) tourneyBox.addItem(t);

        JComboBox<Team> teamBox = new JComboBox<>();
        for (Team tm : teamDAO.findAll()) teamBox.addItem(tm);

        JComboBox<User> userBox = new JComboBox<>();
        for (User u : userDAO.findAll()) userBox.addItem(u);

        JTextField amountField = UIComponents.createTextField(10);
        if (tourneyBox.getItemCount() > 0) {
            amountField.setText(String.valueOf(tourneyBox.getItemAt(0).getEntryFee()));
        }
        tourneyBox.addActionListener(e -> {
            Tournament sel = (Tournament) tourneyBox.getSelectedItem();
            if (sel != null) amountField.setText(String.valueOf(sel.getEntryFee()));
        });

        JComboBox<String> methodBox = new JComboBox<>(new String[]{"UPI", "CREDIT_CARD", "NET_BANKING", "WALLET", "OFFLINE"});
        JTextField notesField = UIComponents.createTextField(15);
        notesField.setText("Entry fee manual verification");

        panel.add(new JLabel("Tournament:"));
        panel.add(tourneyBox);
        panel.add(new JLabel("Team:"));
        panel.add(teamBox);
        panel.add(new JLabel("Paid By User:"));
        panel.add(userBox);
        panel.add(new JLabel("Amount (INR):"));
        panel.add(amountField);
        panel.add(new JLabel("Payment Method:"));
        panel.add(methodBox);
        panel.add(new JLabel("Notes:"));
        panel.add(notesField);

        JButton saveBtn = UIComponents.createPrimaryButton("Record Payment");
        saveBtn.addActionListener(e -> {
            Tournament selT = (Tournament) tourneyBox.getSelectedItem();
            Team selTm = (Team) teamBox.getSelectedItem();
            User selU = (User) userBox.getSelectedItem();

            if (selT == null || selTm == null || selU == null) {
                JOptionPane.showMessageDialog(dialog, "Please select tournament, team, and user.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                BigDecimal amt = new BigDecimal(amountField.getText().trim());
                String txnId = "TXN_" + System.currentTimeMillis();

                Payment p = new Payment();
                p.setTournamentId(selT.getId());
                p.setTeamId(selTm.getId());
                p.setUserId(selU.getId());
                p.setAmount(amt);
                p.setPaymentType("ENTRY_FEE");
                p.setPaymentMethod((String) methodBox.getSelectedItem());
                p.setTransactionId(txnId);
                p.setStatus("COMPLETED");
                p.setNotes(notesField.getText().trim());

                if (paymentDAO.recordPayment(p)) {
                    JOptionPane.showMessageDialog(dialog, "Entry fee payment recorded successfully!\nTransaction ID: " + txnId, "Payment Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadPayments();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to record payment in database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showPrizePayoutDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Distribute Tournament Prize Money", true);
        dialog.setSize(500, 480);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 14));
        panel.setBackground(ThemeConfig.COLOR_BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JComboBox<Tournament> tourneyBox = new JComboBox<>();
        for (Tournament t : tournamentDAO.findAll()) tourneyBox.addItem(t);

        JComboBox<Team> teamBox = new JComboBox<>();
        for (Team tm : teamDAO.findAll()) teamBox.addItem(tm);

        JTextField amountField = UIComponents.createTextField(10);
        JComboBox<String> placeBox = new JComboBox<>(new String[]{"1st Place (Champion)", "2nd Place (Runner-Up)", "3rd Place (2nd Runner-Up)", "Special MVP Award"});

        JComboBox<String> methodBox = new JComboBox<>(new String[]{"UPI", "NET_BANKING", "WALLET", "OFFLINE"});
        JTextField notesField = UIComponents.createTextField(15);
        notesField.setText("Championship prize money disbursement");

        panel.add(new JLabel("Tournament:"));
        panel.add(tourneyBox);
        panel.add(new JLabel("Winning Team:"));
        panel.add(teamBox);
        panel.add(new JLabel("Prize Category:"));
        panel.add(placeBox);
        panel.add(new JLabel("Prize Amount (INR):"));
        panel.add(amountField);
        panel.add(new JLabel("Disbursement Method:"));
        panel.add(methodBox);
        panel.add(new JLabel("Payout Notes:"));
        panel.add(notesField);

        JButton saveBtn = UIComponents.createStyledButton("Disburse Prize", new Color(157, 78, 221), Color.WHITE, new Color(192, 132, 252));
        saveBtn.addActionListener(e -> {
            Tournament selT = (Tournament) tourneyBox.getSelectedItem();
            Team selTm = (Team) teamBox.getSelectedItem();
            if (selT == null || selTm == null) return;

            try {
                BigDecimal amt = new BigDecimal(amountField.getText().trim());
                String txnId = "PRIZE_" + System.currentTimeMillis();

                Payment p = new Payment();
                p.setTournamentId(selT.getId());
                p.setTeamId(selTm.getId());
                p.setUserId(selTm.getLeaderId() > 0 ? selTm.getLeaderId() : 1);
                p.setAmount(amt);
                p.setPaymentType("PRIZE_PAYOUT");
                p.setPaymentMethod((String) methodBox.getSelectedItem());
                p.setTransactionId(txnId);
                p.setStatus("COMPLETED");
                p.setNotes(placeBox.getSelectedItem() + " - " + notesField.getText().trim());

                if (paymentDAO.recordPayment(p)) {
                    JOptionPane.showMessageDialog(dialog, "Prize money payout disbursed successfully!\nTransaction ID: " + txnId, "Payout Recorded", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadPayments();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to record prize payout.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric prize amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = UIComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showUpdateStatusDialog() {
        int row = paymentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment transaction from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int paymentId = (Integer) tableModel.getValueAt(row, 0);
        String txnId = (String) tableModel.getValueAt(row, 1);
        String curStatus = (String) tableModel.getValueAt(row, 8);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"COMPLETED", "PENDING", "FAILED", "REFUNDED"});
        statusCombo.setSelectedItem(curStatus);

        int opt = JOptionPane.showConfirmDialog(this, statusCombo, "Update Status for " + txnId, JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (paymentDAO.updateStatus(paymentId, newStatus)) {
                JOptionPane.showMessageDialog(this, "Payment status updated successfully!", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadPayments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update payment status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private record TournamentChoice(int id, String name) {
        @Override public String toString() { return name; }
    }
}
