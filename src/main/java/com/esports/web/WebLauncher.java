package com.esports.web;

import com.esports.config.DBConnection;

import java.awt.Desktop;
import java.net.URI;

/**
 * Standalone Web Launcher for running the eSports Tournament Management System in any web browser.
 */
public class WebLauncher {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  eSports Tournament Management System Web Portal ");
        System.out.println("==================================================");

        // 1. Verify database connectivity
        DBConnection.ConnectionStatus status = DBConnection.testConnection();
        if (status.isSuccess()) {
            System.out.println("✅ [Database]: " + status.getMessage());
        } else {
            System.err.println("⚠️ [Database Warning]: " + status.getMessage());
        }

        // 2. Start Web Dashboard Server on port 8080
        int port = 8080;
        WebDashboardServer.startServer(port);

        String url = "http://localhost:" + port;
        System.out.println("🚀 [Browser]: Launching default browser to " + url + " ...");

        // 3. Open browser automatically
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Windows fallback
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            }
        } catch (Exception e) {
            System.out.println("👉 Please open your browser and navigate to: " + url);
        }

        System.out.println("🟢 Server is live and serving requests. Press Ctrl+C in terminal to stop.");
    }
}
