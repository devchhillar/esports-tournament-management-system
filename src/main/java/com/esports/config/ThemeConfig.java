package com.esports.config;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Modern Gaming UI Theme Manager using FlatLaf.
 * Customizes palettes with dark navy backgrounds, neon cyan/purple accents,
 * smooth rounded borders, and professional typography.
 */
public class ThemeConfig {

    // Gaming Color Palette
    public static final Color COLOR_BG_DARK = new Color(11, 15, 25);         // Deepest Background (#0b0f19)
    public static final Color COLOR_BG_SECONDARY = new Color(17, 24, 39);    // Secondary Background (#111827)
    public static final Color COLOR_CARD_BG = new Color(30, 41, 59);         // Card & Panel Surface (#1e293b)
    public static final Color COLOR_CARD_HOVER = new Color(42, 57, 82);      // Card Hover (#2a3952)
    
    public static final Color COLOR_ACCENT_CYAN = new Color(0, 229, 255);     // Neon Cyan (#00e5ff)
    public static final Color COLOR_ACCENT_PURPLE = new Color(168, 85, 247); // Neon Purple (#a855f7)
    public static final Color COLOR_ACCENT_BLUE = new Color(59, 130, 246);   // Gaming Blue (#3b82f6)

    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);       // Emerald Green (#10b981)
    public static final Color COLOR_WARNING = new Color(245, 158, 11);       // Amber Gold (#f59e0b)
    public static final Color COLOR_DANGER = new Color(239, 68, 68);         // Crimson Red (#ef4444)

    public static final Color COLOR_TEXT_PRIMARY = new Color(255, 255, 255); // Pure White (#ffffff)
    public static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);   // Slate Gray (#94a3b8)
    public static final Color COLOR_BORDER = new Color(51, 65, 85);          // Subtle Border (#334155)

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Initializes FlatLaf Dark theme and overrides properties for an eSports look.
     */
    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

            // Global UI overrides for a modern cyber gaming look
            UIManager.put("Component.arc", 12);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);

            UIManager.put("Panel.background", COLOR_BG_DARK);
            UIManager.put("TableHeader.background", COLOR_BG_SECONDARY);
            UIManager.put("TableHeader.foreground", COLOR_ACCENT_CYAN);
            UIManager.put("TableHeader.font", FONT_BOLD);

            UIManager.put("Table.background", COLOR_CARD_BG);
            UIManager.put("Table.foreground", COLOR_TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", COLOR_ACCENT_BLUE);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", COLOR_BORDER);
            UIManager.put("Table.rowHeight", 34);

            UIManager.put("TextField.background", COLOR_BG_SECONDARY);
            UIManager.put("TextField.foreground", COLOR_TEXT_PRIMARY);
            UIManager.put("TextField.caretColor", COLOR_ACCENT_CYAN);
            UIManager.put("PasswordField.background", COLOR_BG_SECONDARY);
            UIManager.put("PasswordField.foreground", COLOR_TEXT_PRIMARY);
            UIManager.put("PasswordField.caretColor", COLOR_ACCENT_CYAN);

            UIManager.put("ComboBox.background", COLOR_BG_SECONDARY);
            UIManager.put("ComboBox.foreground", COLOR_TEXT_PRIMARY);

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf theme: " + ex.getMessage());
        }
    }
}
