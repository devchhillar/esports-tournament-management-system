package com.esports.view.common;

import com.esports.config.ThemeConfig;
import com.esports.dao.MatchDAO;
import com.esports.dao.MatchDAOImpl;
import com.esports.dao.TournamentDAO;
import com.esports.dao.TournamentDAOImpl;
import com.esports.model.Match;
import com.esports.model.Tournament;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Visual Tournament Bracket Viewer Dialog.
 * Graphically displays tournament elimination trees, rounds, team matchups, live scores, and winners.
 */
public class BracketViewerDialog extends JDialog {

    private final int tournamentId;
    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private JPanel bracketPanel;
    private JLabel subtitleLabel;

    public static void showBracket(Component parent, int tournamentId) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        BracketViewerDialog dialog = new BracketViewerDialog(window, tournamentId);
        dialog.setVisible(true);
    }

    public BracketViewerDialog(Window owner, int tournamentId) {
        super(owner, "🏆 Visual Tournament Bracket & Progression", ModalityType.APPLICATION_MODAL);
        this.tournamentId = tournamentId;
        initializeUI();
        loadBracketData();
    }

    private void initializeUI() {
        setSize(1240, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeConfig.COLOR_BG_DARK);

        // Header Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeConfig.COLOR_BG_SECONDARY);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConfig.COLOR_BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        Tournament t = tournamentDAO.findById(tournamentId);
        String tourneyTitle = (t != null) ? t.getTitle() + " (" + t.getGame() + " - " + t.getFormat() + ")" : "Tournament #" + tournamentId;

        JLabel titleLabel = new JLabel("⚡ " + tourneyTitle);
        titleLabel.setFont(ThemeConfig.FONT_TITLE);
        titleLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);

        subtitleLabel = new JLabel("Interactive Visual Bracket Tree & Automatic Winner Progression");
        subtitleLabel.setFont(ThemeConfig.FONT_SMALL);
        subtitleLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(titleLabel);
        titles.add(subtitleLabel);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);

        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh Bracket");
        refreshBtn.addActionListener(e -> loadBracketData());

        JButton closeBtn = UIComponents.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        headerButtons.add(refreshBtn);
        headerButtons.add(closeBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Bracket Canvas in ScrollPane
        bracketPanel = new JPanel();
        bracketPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 32, 24));
        bracketPanel.setBackground(ThemeConfig.COLOR_BG_DARK);

        JScrollPane scrollPane = new JScrollPane(bracketPanel);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void loadBracketData() {
        bracketPanel.removeAll();
        List<Match> matches = matchDAO.findByTournament(tournamentId);

        if (matches.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel emptyLabel = new JLabel("No matches generated yet. Use 'Generate Fixtures' in the Tournaments panel.");
            emptyLabel.setFont(ThemeConfig.FONT_SUBTITLE);
            emptyLabel.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            bracketPanel.setLayout(new BorderLayout());
            bracketPanel.add(emptyPanel, BorderLayout.CENTER);
            bracketPanel.revalidate();
            bracketPanel.repaint();
            return;
        }

        bracketPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 32, 24));

        // Group matches by round name
        Map<String, List<Match>> roundMap = new LinkedHashMap<>();
        for (Match m : matches) {
            String roundKey = m.getRoundName();
            if (roundKey.contains("Match")) {
                roundKey = roundKey.split(" Match")[0].trim();
            }
            roundMap.computeIfAbsent(roundKey, k -> new ArrayList<>()).add(m);
        }

        for (Map.Entry<String, List<Match>> entry : roundMap.entrySet()) {
            String roundName = entry.getKey();
            List<Match> roundMatches = entry.getValue();

            JPanel column = new JPanel();
            column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
            column.setOpaque(false);
            column.setPreferredSize(new Dimension(280, 700));

            // Column Header
            JLabel roundHeader = new JLabel(roundName.toUpperCase());
            roundHeader.setFont(ThemeConfig.FONT_BOLD);
            roundHeader.setForeground(ThemeConfig.COLOR_ACCENT_PURPLE);
            roundHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
            column.add(roundHeader);
            column.add(Box.createVerticalStrut(14));

            for (Match m : roundMatches) {
                column.add(createMatchCard(m));
                column.add(Box.createVerticalStrut(18));
            }

            column.add(Box.createVerticalGlue());
            bracketPanel.add(column);
        }

        bracketPanel.revalidate();
        bracketPanel.repaint();
    }

    private JPanel createMatchCard(Match m) {
        JPanel card = UIComponents.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));
        card.setMaximumSize(new Dimension(270, 150));
        card.setPreferredSize(new Dimension(270, 150));

        // Match Card Top Bar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel mNum = new JLabel("Match #" + m.getId() + " • " + m.getBracketType());
        mNum.setFont(ThemeConfig.FONT_SMALL);
        mNum.setForeground(ThemeConfig.COLOR_TEXT_MUTED);

        JLabel statusLabel = new JLabel(m.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        if ("COMPLETED".equalsIgnoreCase(m.getStatus())) {
            statusLabel.setForeground(ThemeConfig.COLOR_SUCCESS);
        } else if ("LIVE".equalsIgnoreCase(m.getStatus())) {
            statusLabel.setForeground(ThemeConfig.COLOR_DANGER);
        } else {
            statusLabel.setForeground(ThemeConfig.COLOR_ACCENT_CYAN);
        }

        top.add(mNum, BorderLayout.WEST);
        top.add(statusLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Teams and Scores
        JPanel teamsPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        teamsPanel.setOpaque(false);

        boolean isT1Winner = m.getWinnerTeamId() != null && m.getTeam1Id() != null && m.getWinnerTeamId().equals(m.getTeam1Id());
        boolean isT2Winner = m.getWinnerTeamId() != null && m.getTeam2Id() != null && m.getWinnerTeamId().equals(m.getTeam2Id());

        String t1Display = (m.getTeam1Name() != null && !m.getTeam1Name().isEmpty()) ? m.getTeam1Name() : "TBD (Pending)";
        String t2Display = (m.getTeam2Name() != null && !m.getTeam2Name().isEmpty()) ? m.getTeam2Name() : "TBD (Pending)";

        JPanel t1Row = createTeamRow(t1Display, m.getTeam1Score(), isT1Winner);
        JPanel t2Row = createTeamRow(t2Display, m.getTeam2Score(), isT2Winner);

        teamsPanel.add(t1Row);
        teamsPanel.add(t2Row);
        card.add(teamsPanel, BorderLayout.CENTER);

        // Footer info (Venue / Time)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        String timeStr = (m.getScheduledTime() != null) ? m.getScheduledTime().toString().substring(5, 16) : "-";
        JLabel timeLbl = new JLabel("🕒 " + timeStr + " • " + (m.getVenue() != null ? m.getVenue() : "Main Arena"));
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLbl.setForeground(ThemeConfig.COLOR_TEXT_MUTED);
        footer.add(timeLbl, BorderLayout.WEST);

        card.add(footer, BorderLayout.SOUTH);

        // Glowing border for Live or Completed
        if ("LIVE".equalsIgnoreCase(m.getStatus())) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeConfig.COLOR_DANGER, 2),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        } else if ("COMPLETED".equalsIgnoreCase(m.getStatus())) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeConfig.COLOR_ACCENT_CYAN, 1),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        }

        return card;
    }

    private JPanel createTeamRow(String teamName, int score, boolean isWinner) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel nameLbl = new JLabel(isWinner ? "🏆 " + teamName : teamName);
        nameLbl.setFont(ThemeConfig.FONT_REGULAR);
        nameLbl.setForeground(isWinner ? ThemeConfig.COLOR_SUCCESS : ThemeConfig.COLOR_TEXT_PRIMARY);

        JLabel scoreLbl = new JLabel(String.valueOf(score));
        scoreLbl.setFont(ThemeConfig.FONT_BOLD);
        scoreLbl.setForeground(isWinner ? ThemeConfig.COLOR_SUCCESS : ThemeConfig.COLOR_TEXT_MUTED);

        row.add(nameLbl, BorderLayout.WEST);
        row.add(scoreLbl, BorderLayout.EAST);
        return row;
    }
}
