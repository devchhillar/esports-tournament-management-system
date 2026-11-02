package com.esports.main;

import com.esports.config.DBConnection;
import com.esports.config.ThemeConfig;
import com.esports.view.auth.LoginFrame;

import javax.swing.*;

/**
 * Main Entry Point for the eSports Tournament Management System.
 * Initializes FlatLaf gaming UI look and feel and displays the Login Screen.
 */
public class Main {

    public static void main(String[] args) {
        if (args != null && args.length > 0 && ("--web".equalsIgnoreCase(args[0]) || "-w".equalsIgnoreCase(args[0]))) {
            com.esports.web.WebLauncher.main(args);
            return;
        }

        // 1. Enable Hardware Acceleration for smooth Swing rendering
        System.setProperty("sun.java2d.opengl", "true");

        // 2. Apply Custom Modern Cyber Dark Theme via FlatLaf
        ThemeConfig.applyTheme();

        // 3. Start Built-in Web Server for Browser Portal at http://localhost:8080
        new Thread(() -> {
            com.esports.web.WebDashboardServer.startServer(8080);
        }).start();

        // 4. Check Database Connection asynchronously
        new Thread(() -> {
            DBConnection.ConnectionStatus status = DBConnection.testConnection();
            if (status.isSuccess()) {
                System.out.println("✅ [Database]: " + status.getMessage());
            } else {
                System.err.println("⚠️ [Database Warning]: " + status.getMessage());
                System.err.println("👉 Please ensure MySQL 8 is running and db.properties credentials match your local MySQL configuration.");
            }
        }).start();

        // 5. Launch Application on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
