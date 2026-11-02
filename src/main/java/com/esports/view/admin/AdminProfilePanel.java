package com.esports.view.admin;

import com.esports.config.PasswordUtil;
import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.dao.UserDAO;
import com.esports.dao.UserDAOImpl;
import com.esports.model.LoginHistory;
import com.esports.model.User;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Profile Management Module.
 * Displays profile information, profile editing, secure BCrypt password change, and login history.
 */
public class AdminProfilePanel extends JPanel {

    private final UserDAO userDAO = new UserDAOImpl();

    private JLabel adminIdLabel;
    private JLabel usernameLabel;
    private JLabel roleLabel;
    private JLabel createdAtLabel;
    private JLabel lastLoginLabel;

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private String currentAvatarPath;
    private JLabel avatarStatusLabel;

    private JPasswordField currentPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;

    private DefaultTableModel historyTableModel;

    public AdminProfilePanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        initializeUI();
        loadProfileData();
    }

    private void initializeUI() {
        JPanel scrollRoot = new JPanel();
        scrollRoot.setLayout(new BoxLayout(scrollRoot, BoxLayout.Y_AXIS));
        scrollRoot.setOpaque(false);
        scrollRoot.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Row: Profile Details & Edit Card
        JPanel topRow = new JPanel(new GridLayout(1, 2, 16, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 410));

        // Left Card: Profile Information & Edit Form
        JPanel profileCard = UIComponents.createCardPanel();
        profileCard.setLayout(new BorderLayout(0, 12));

        JLabel pTitle = new JLabel("👤 Admin Account & Profile Information");
        pTitle.setFont(ThemeConfig.FONT_SUBTITLE);
        pTitle.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        profileCard.add(pTitle, BorderLayout.NORTH);

        JPanel pForm = new JPanel(new GridLayout(9, 2, 8, 8));
        pForm.setOpaque(false);

        adminIdLabel = new JLabel("-");
        usernameLabel = new JLabel("-");
        roleLabel = new JLabel("-");
        createdAtLabel = new JLabel("-");
        lastLoginLabel = new JLabel("-");

        adminIdLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        usernameLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        roleLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
        createdAtLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
        lastLoginLabel.setForeground(ThemeConfig.COLOR_WARNING);

        fullNameField = UIComponents.createTextField(15);
        emailField = UIComponents.createTextField(15);
        phoneField = UIComponents.createTextField(15);

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        avatarPanel.setOpaque(false);
        avatarStatusLabel = new JLabel("No picture set");
        avatarStatusLabel.setFont(ThemeConfig.FONT_SMALL);
        avatarStatusLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JButton uploadAvatarBtn = UIComponents.createSecondaryButton("📁 Change Picture");
        uploadAvatarBtn.addActionListener(e -> {
            String saved = com.esports.util.FileUploadUtil.chooseAndSaveImage(this, "avatars");
            if (saved != null) {
                currentAvatarPath = saved;
                avatarStatusLabel.setText("Selected: " + new java.io.File(saved).getName());
                avatarStatusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
            }
        });
        avatarPanel.add(uploadAvatarBtn);
        avatarPanel.add(avatarStatusLabel);

        pForm.add(new JLabel("Administrator ID:"));
        pForm.add(adminIdLabel);
        pForm.add(new JLabel("Username:"));
        pForm.add(usernameLabel);
        pForm.add(new JLabel("Access Role:"));
        pForm.add(roleLabel);
        pForm.add(new JLabel("Full Name:"));
        pForm.add(fullNameField);
        pForm.add(new JLabel("Email Address:"));
        pForm.add(emailField);
        pForm.add(new JLabel("Phone Number:"));
        pForm.add(phoneField);
        pForm.add(new JLabel("Profile Picture:"));
        pForm.add(avatarPanel);
        pForm.add(new JLabel("Account Created:"));
        pForm.add(createdAtLabel);
        pForm.add(new JLabel("Last Login:"));
        pForm.add(lastLoginLabel);

        profileCard.add(pForm, BorderLayout.CENTER);

        JButton saveProfileBtn = UIComponents.createPrimaryButton("💾 Update Profile Details");
        saveProfileBtn.addActionListener(e -> saveProfile());
        profileCard.add(saveProfileBtn, BorderLayout.SOUTH);
        topRow.add(profileCard);

        // Right Card: Change Password Form
        JPanel passCard = UIComponents.createCardPanel();
        passCard.setLayout(new BorderLayout(0, 12));

        JLabel passTitle = new JLabel("🔒 Security & Password Update");
        passTitle.setFont(ThemeConfig.FONT_SUBTITLE);
        passTitle.setForeground(ThemeConfig.COLOR_ACCENT_PURPLE);
        passCard.add(passTitle, BorderLayout.NORTH);

        JPanel passForm = new JPanel(new GridLayout(4, 2, 8, 14));
        passForm.setOpaque(false);

        currentPassField = UIComponents.createPasswordField(15);
        newPassField = UIComponents.createPasswordField(15);
        confirmPassField = UIComponents.createPasswordField(15);

        passForm.add(new JLabel("Current Password:"));
        passForm.add(currentPassField);
        passForm.add(new JLabel("New Password (min 6 chars):"));
        passForm.add(newPassField);
        passForm.add(new JLabel("Confirm New Password:"));
        passForm.add(confirmPassField);

        passCard.add(passForm, BorderLayout.CENTER);

        JButton changePassBtn = UIComponents.createAccentButton("🔑 Change Password");
        changePassBtn.addActionListener(e -> changePassword());
        passCard.add(changePassBtn, BorderLayout.SOUTH);
        topRow.add(passCard);

        scrollRoot.add(topRow);
        scrollRoot.add(Box.createVerticalStrut(18));

        // Bottom Card: Login History
        JPanel historyCard = UIComponents.createCardPanel();
        historyCard.setLayout(new BorderLayout(0, 10));
        historyCard.setPreferredSize(new Dimension(1000, 260));

        JLabel histTitle = new JLabel("📜 Security Login Activity History");
        histTitle.setFont(ThemeConfig.FONT_SUBTITLE);
        histTitle.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        historyCard.add(histTitle, BorderLayout.NORTH);

        String[] cols = {"Session ID", "Username", "Login Timestamp", "IP Address", "Authentication Status"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable historyTable = new JTable(historyTableModel);
        historyTable.setShowGrid(true);
        historyTable.setFillsViewportHeight(true);
        JScrollPane histScroll = new JScrollPane(historyTable);
        histScroll.setBorder(BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER));
        historyCard.add(histScroll, BorderLayout.CENTER);

        scrollRoot.add(historyCard);

        JScrollPane scrollPane = new JScrollPane(scrollRoot);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadProfileData() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) {
            user = userDAO.findById(1); // Default to admin 1 if session is empty
        }
        if (user != null) {
            adminIdLabel.setText("#" + user.getId());
            usernameLabel.setText("@" + user.getUsername());
            roleLabel.setText(user.getRole().getDisplayName());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
            currentAvatarPath = user.getProfilePicture();
            if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
                avatarStatusLabel.setText(new java.io.File(currentAvatarPath).getName());
                avatarStatusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
            } else {
                avatarStatusLabel.setText("No picture set");
                avatarStatusLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
            }
            createdAtLabel.setText(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "-");
            lastLoginLabel.setText(user.getLastLogin() != null ? user.getLastLogin().toString() : "Current Session");

            // Load Login History
            historyTableModel.setRowCount(0);
            List<LoginHistory> history = userDAO.getLoginHistory(user.getId());
            for (LoginHistory lh : history) {
                historyTableModel.addRow(new Object[]{
                        lh.getId(),
                        lh.getUsername(),
                        lh.getLoginTime() != null ? lh.getLoginTime().toString() : "-",
                        lh.getIpAddress(),
                        lh.getStatus()
                });
            }
        }
    }

    private void saveProfile() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) user = userDAO.findById(1);
        if (user == null) return;

        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name and Email cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setProfilePicture(currentAvatarPath);

        if (userDAO.updateProfile(user)) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProfileData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) user = userDAO.findById(1);
        if (user == null) return;

        String curPass = new String(currentPassField.getPassword()).trim();
        String newPass = new String(newPassField.getPassword()).trim();
        String confPass = new String(confirmPassField.getPassword()).trim();

        if (curPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All password fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!PasswordUtil.checkPassword(curPass, user.getPasswordHash())) {
            JOptionPane.showMessageDialog(this, "Incorrect current password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Mismatch", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hashed = PasswordUtil.hashPassword(newPass);
        if (userDAO.updatePassword(user.getId(), hashed)) {
            user.setPasswordHash(hashed);
            JOptionPane.showMessageDialog(this, "Password changed successfully!", "Security Updated", JOptionPane.INFORMATION_MESSAGE);
            currentPassField.setText("");
            newPassField.setText("");
            confirmPassField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update password in database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
