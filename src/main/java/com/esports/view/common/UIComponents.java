package com.esports.view.common;

import com.esports.config.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reusable Modern Gaming UI Component Factory.
 * Builds sleek gaming buttons, cards, input fields, badges, and banners.
 */
public class UIComponents {

    /**
     * Primary neon action button (Cyan / Blue).
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, ThemeConfig.COLOR_ACCENT_BLUE, Color.WHITE, ThemeConfig.COLOR_ACCENT_CYAN);
    }

    /**
     * Accent neon button (Purple).
     */
    public static JButton createAccentButton(String text) {
        return createStyledButton(text, ThemeConfig.COLOR_ACCENT_PURPLE, Color.WHITE, new Color(200, 120, 255));
    }

    /**
     * Danger button (Red).
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, ThemeConfig.COLOR_DANGER, Color.WHITE, new Color(248, 113, 113));
    }

    /**
     * Outlined / secondary button.
     */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, ThemeConfig.COLOR_CARD_BG, ThemeConfig.COLOR_TEXT_PRIMARY, ThemeConfig.COLOR_CARD_HOVER);
    }

    public static JButton createStyledButton(String text, Color bgColor, Color fgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(ThemeConfig.FONT_BOLD);
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.brighter(), 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                if (bgColor == ThemeConfig.COLOR_CARD_BG) {
                    button.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                if (bgColor == ThemeConfig.COLOR_CARD_BG) {
                    button.setForeground(fgColor);
                }
            }
        });

        return button;
    }

    /**
     * Creates a styled card container panel with rounded edges and dark navy surface.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeConfig.COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ThemeConfig.COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        return panel;
    }

    /**
     * Modern text field with padding and custom styling.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(ThemeConfig.FONT_REGULAR);
        tf.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        tf.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        tf.setCaretColor(ThemeConfig.COLOR_ACCENT_CYAN);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    /**
     * Modern password field with padding and custom styling.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(ThemeConfig.FONT_REGULAR);
        pf.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        pf.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        pf.setCaretColor(ThemeConfig.COLOR_ACCENT_CYAN);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConfig.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return pf;
    }

    /**
     * Modern combo box.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(ThemeConfig.FONT_REGULAR);
        cb.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        cb.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);
        return cb;
    }

    /**
     * Section header with neon accent icon indicator.
     */
    public static JPanel createHeader(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeConfig.FONT_TITLE);
        titleLabel.setForeground(ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(ThemeConfig.FONT_SMALL);
        subtitleLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        return headerPanel;
    }
}
