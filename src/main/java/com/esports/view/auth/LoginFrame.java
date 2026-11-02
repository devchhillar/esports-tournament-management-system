package com.esports.view.auth;

import com.esports.config.DBConnection;
import com.esports.config.ThemeConfig;
import com.esports.config.UserSession;
import com.esports.controller.AuthController;
import com.esports.model.Role;
import com.esports.model.User;
import com.esports.view.common.UIComponents;
import com.esports.view.manager.TeamManagerDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Gaming Login View with FlatLaf styling and database authentication.
 */
public class LoginFrame extends JFrame {

    private final AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        this.authController = new AuthController();
        initializeUI();
        checkDatabaseStatus();
    }

    private void initializeUI() {
        setTitle("eSports Tournament Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Background Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(ThemeConfig.COLOR_BG_DARK);

        // Center Card
        JPanel cardPanel = UIComponents.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setPreferredSize(new Dimension(380, 520));

        // Gaming Logo / Title Header
        JLabel logoIcon = new JLabel("⚡ ESPORTS ARENA ⚡", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoIcon.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Tournament Management Platform", SwingConstants.CENTER);
        subtitleLabel.setFont(ThemeConfig.FONT_SMALL);
        subtitleLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username Field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeConfig.FONT_BOLD);
        userLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = UIComponents.createTextField(20);
        usernameField.setMaximumSize(new Dimension(320, 38));

        // Password Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeConfig.FONT_BOLD);
        passLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = UIComponents.createPasswordField(20);
        passwordField.setMaximumSize(new Dimension(320, 38));

        // Login Button
        JButton loginButton = UIComponents.createPrimaryButton("LOGIN NOW");
        loginButton.setMaximumSize(new Dimension(320, 42));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        // Quick Guest / Viewer Mode
        JButton viewerButton = UIComponents.createSecondaryButton("Browse as Viewer / Guest");
        viewerButton.setMaximumSize(new Dimension(320, 38));
        viewerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewerButton.addActionListener(e -> launchViewerMode());

        // Browser Portal Button
        JButton browserButton = UIComponents.createStyledButton("🌐 Open Web Portal in Browser", new Color(0, 229, 255), Color.BLACK, new Color(56, 189, 248));
        browserButton.setMaximumSize(new Dimension(320, 38));
        browserButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        browserButton.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080"));
            } catch (Exception ex) {
                try {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", "http://localhost:8080"});
                } catch (Exception ignored) {}
            }
        });

        // Register Link Button
        JButton registerButton = UIComponents.createAccentButton("Create New Account");
        registerButton.setMaximumSize(new Dimension(320, 38));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        // Database connection indicator
        statusLabel = new JLabel("Checking MySQL connection...", SwingConstants.CENTER);
        statusLabel.setFont(ThemeConfig.FONT_SMALL);
        statusLabel.setForeground(ThemeConfig.COLOR_WARNING);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assembly
        cardPanel.add(logoIcon);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(24));

        cardPanel.add(createFieldWrapper(userLabel, usernameField));
        cardPanel.add(Box.createVerticalStrut(14));

        cardPanel.add(createFieldWrapper(passLabel, passwordField));
        cardPanel.add(Box.createVerticalStrut(24));

        cardPanel.add(loginButton);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(viewerButton);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(browserButton);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(registerButton);

        cardPanel.add(Box.createVerticalGlue());
        cardPanel.add(statusLabel);
        cardPanel.add(Box.createVerticalStrut(8));

        mainPanel.add(cardPanel);
        setContentPane(mainPanel);

        // Enter key activates login
        getRootPane().setDefaultButton(loginButton);
    }

    private JPanel createFieldWrapper(JLabel label, JComponent input) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(320, 64));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(input);
        return wrapper;
    }

    private void checkDatabaseStatus() {
        SwingWorker<DBConnection.ConnectionStatus, Void> worker = new SwingWorker<>() {
            @Override
            protected DBConnection.ConnectionStatus doInBackground() {
                return DBConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    DBConnection.ConnectionStatus status = get();
                    if (status.isSuccess()) {
                        statusLabel.setText("● MySQL Connected");
                        statusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
                    } else {
                        statusLabel.setText("⚠ MySQL Offline (Check db.properties)");
                        statusLabel.setForeground(ThemeConfig.COLOR_DANGER);
                    }
                } catch (Exception ignored) {
                    statusLabel.setText("⚠ Database Status Unknown");
                }
            }
        };
        worker.execute();
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthController.AuthResult result = authController.login(username, password);
        if (result.isSuccess()) {
            User user = result.getUser();
            JOptionPane.showMessageDialog(this, result.getMessage(), "Welcome", JOptionPane.INFORMATION_MESSAGE);
            navigateDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void launchViewerMode() {
        // Create a temporary guest viewer user
        User guest = new User("guest_viewer", "guest@esports.com", "", Role.VIEWER, "Guest Spectator", "");
        UserSession.getInstance().login(guest);
        navigateDashboard(guest);
    }

    private void navigateDashboard(User user) {
        dispose();
        // Dynamic routing according to user role
        switch (user.getRole()) {
            case ADMIN:
                com.esports.view.admin.AdminDashboard.launch();
                break;
            case TEAM_MANAGER:
                TeamManagerDashboard.launch();
                break;
            case PLAYER:
                com.esports.view.player.PlayerDashboard.launch();
                break;
            case VIEWER:
            default:
                com.esports.view.viewer.ViewerDashboard.launch();
                break;
        }
    }
}
