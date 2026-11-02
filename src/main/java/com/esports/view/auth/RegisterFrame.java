package com.esports.view.auth;

import com.esports.config.ThemeConfig;
import com.esports.controller.AuthController;
import com.esports.model.Role;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Gaming Registration View.
 * Allows users to register as Admin, Player, or Viewer.
 */
public class RegisterFrame extends JFrame {

    private final AuthController authController;
    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<Role> roleComboBox;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterFrame() {
        this.authController = new AuthController();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("eSports Tournament Management System - Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JPanel cardPanel = UIComponents.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setPreferredSize(new Dimension(440, 640));

        // Header
        JLabel logoIcon = new JLabel("JOIN ESPORTS ARENA", SwingConstants.CENTER);
        logoIcon.setFont(ThemeConfig.FONT_TITLE);
        logoIcon.setForeground(ThemeConfig.COLOR_ACCENT_PURPLE);
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Create your official gaming profile", SwingConstants.CENTER);
        subtitleLabel.setFont(ThemeConfig.FONT_SMALL);
        subtitleLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        fullNameField = UIComponents.createTextField(20);
        usernameField = UIComponents.createTextField(20);
        emailField = UIComponents.createTextField(20);
        phoneField = UIComponents.createTextField(20);
        roleComboBox = UIComponents.createComboBox(Role.values());
        roleComboBox.setSelectedItem(Role.PLAYER);
        passwordField = UIComponents.createPasswordField(20);
        confirmPasswordField = UIComponents.createPasswordField(20);

        // Buttons
        JButton registerButton = UIComponents.createAccentButton("COMPLETE REGISTRATION");
        registerButton.setMaximumSize(new Dimension(380, 42));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> performRegistration());

        JButton backButton = UIComponents.createSecondaryButton("Back to Login");
        backButton.setMaximumSize(new Dimension(380, 38));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        // Assemble Form
        cardPanel.add(logoIcon);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(20));

        cardPanel.add(createFieldWrapper("Full Name", fullNameField));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createFieldWrapper("Username", usernameField));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createFieldWrapper("Email Address", emailField));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createFieldWrapper("Phone Number", phoneField));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createFieldWrapper("User Role", roleComboBox));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createFieldWrapper("Password (Min 6 chars)", passwordField));
        cardPanel.add(Box.createVerticalStrut(10));

        cardPanel.add(createFieldWrapper("Confirm Password", confirmPasswordField));
        cardPanel.add(Box.createVerticalStrut(22));

        cardPanel.add(registerButton);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(backButton);
        cardPanel.add(Box.createVerticalGlue());

        mainPanel.add(cardPanel);
        setContentPane(mainPanel);

        getRootPane().setDefaultButton(registerButton);
    }

    private JPanel createFieldWrapper(String labelText, JComponent input) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(380, 56));

        JLabel label = new JLabel(labelText);
        label.setFont(ThemeConfig.FONT_SMALL);
        label.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(380, 32));

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(input);
        return wrapper;
    }

    private void performRegistration() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        Role role = (Role) roleComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        AuthController.AuthResult result = authController.register(
                username, email, password, confirmPassword, role, fullName, phone
        );

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Registration Succeeded", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
