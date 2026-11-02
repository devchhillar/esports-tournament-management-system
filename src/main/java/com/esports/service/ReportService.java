package com.esports.service;

import com.esports.dao.*;
import com.esports.model.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating, formatting, and exporting professional eSports reports to PDF and Excel/CSV.
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TeamDAO teamDAO = new TeamDAOImpl();
    private final PlayerDAO playerDAO = new PlayerDAOImpl();
    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final TournamentStandingDAO standingDAO = new TournamentStandingDAOImpl();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    // =========================================================================
    // 1. TOURNAMENT SUMMARY REPORT
    // =========================================================================

    public boolean exportTournamentsToPdf(File targetFile) {
        List<Tournament> tournaments = tournamentDAO.findAll();
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36); // Landscape for wide tables

        try {
            PdfWriter.getInstance(document, new FileOutputStream(targetFile));
            document.open();

            // Header Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 229, 255));
            Paragraph title = new Paragraph("⚡ ESPORTS CHAMPIONSHIP TOURNAMENTS REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph meta = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FMT) + " | System: eSports Management Platform", metaFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(18);
            document.add(meta);

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 5.0f, 3.0f, 3.5f, 2.5f, 2.5f, 2.0f, 3.0f});

            // Table Headers
            String[] headers = {"ID", "Tournament Title", "Game", "Format", "Prize Pool", "Entry Fee", "Max", "Status"};
            for (String h : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                headerCell.setBackgroundColor(new Color(19, 27, 42));
                headerCell.setPadding(8);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }

            // Data Rows
            boolean alternate = false;
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Color altColor = new Color(245, 247, 250);

            for (Tournament t : tournaments) {
                Color bg = alternate ? altColor : Color.WHITE;
                addCell(table, String.valueOf(t.getId()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, t.getTitle(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, t.getGame(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, t.getFormat(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, "₹ " + t.getPrizePool(), rowFont, bg, Element.ALIGN_RIGHT);
                addCell(table, "₹ " + t.getEntryFee(), rowFont, bg, Element.ALIGN_RIGHT);
                addCell(table, String.valueOf(t.getMaxTeams()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, t.getStatus(), rowFont, bg, Element.ALIGN_CENTER);
                alternate = !alternate;
            }

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            logger.error("Error exporting tournaments to PDF: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean exportTournamentsToCsv(File targetFile) {
        List<Tournament> tournaments = tournamentDAO.findAll();
        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write("Tournament ID,Title,Game,Format,Prize Pool,Entry Fee,Min Teams,Max Teams,Status,Start Date,End Date,Organizer\n");
            for (Tournament t : tournaments) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%.2f,%.2f,%d,%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        t.getId(),
                        escapeCsv(t.getTitle()),
                        escapeCsv(t.getGame()),
                        t.getFormat(),
                        t.getPrizePool(),
                        t.getEntryFee(),
                        t.getMinTeams(),
                        t.getMaxTeams(),
                        t.getStatus(),
                        t.getStartDate() != null ? t.getStartDate().toString() : "",
                        t.getEndDate() != null ? t.getEndDate().toString() : "",
                        escapeCsv(t.getOrganizer() != null ? t.getOrganizer() : "")
                ));
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting tournaments to CSV: {}", e.getMessage(), e);
            return false;
        }
    }

    // =========================================================================
    // 2. FINANCIAL & PAYMENTS REPORT
    // =========================================================================

    public boolean exportPaymentsToPdf(File targetFile) {
        List<Payment> payments = paymentDAO.findAll();
        BigDecimal totalRevenue = paymentDAO.getTotalRevenue();
        BigDecimal totalPayouts = paymentDAO.getTotalPrizePayouts();
        BigDecimal netBalance = totalRevenue.subtract(totalPayouts);

        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(targetFile));
            document.open();

            // Header Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(157, 78, 221));
            Paragraph title = new Paragraph("💳 FINANCIAL & PRIZE DISTRIBUTION REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph meta = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FMT) + " | Currency: INR (₹)", metaFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(14);
            document.add(meta);

            // Summary Financial KPI Boxes
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(16);

            addKpiCell(summaryTable, "Total Entry Fees Collected", "₹ " + totalRevenue.toPlainString(), new Color(16, 185, 129));
            addKpiCell(summaryTable, "Total Prize Payouts Paid", "₹ " + totalPayouts.toPlainString(), new Color(239, 68, 68));
            addKpiCell(summaryTable, "Net Balance", "₹ " + netBalance.toPlainString(), new Color(0, 229, 255));
            document.add(summaryTable);

            // Payments Ledger Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3.5f, 4.0f, 3.0f, 2.5f, 2.5f, 2.5f, 3.5f});

            String[] headers = {"ID", "Transaction ID", "Tournament", "Team", "Amount", "Type", "Method", "Date"};
            for (String h : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                headerCell.setBackgroundColor(new Color(19, 27, 42));
                headerCell.setPadding(8);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }

            boolean alternate = false;
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Color altColor = new Color(245, 247, 250);

            for (Payment p : payments) {
                Color bg = alternate ? altColor : Color.WHITE;
                addCell(table, String.valueOf(p.getId()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, p.getTransactionId(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, p.getTournamentTitle(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, p.getTeamName() + " [" + p.getTeamTag() + "]", rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, "₹ " + p.getAmount().toPlainString(), rowFont, bg, Element.ALIGN_RIGHT);
                addCell(table, p.getPaymentType(), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, p.getPaymentMethod(), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, p.getPaymentDate() != null ? p.getPaymentDate().toString().substring(0, 16) : "-", rowFont, bg, Element.ALIGN_CENTER);
                alternate = !alternate;
            }

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            logger.error("Error exporting payments to PDF: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean exportPaymentsToCsv(File targetFile) {
        List<Payment> payments = paymentDAO.findAll();
        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write("Payment ID,Transaction ID,Tournament,Team,Team Tag,Amount (INR),Payment Type,Method,Status,Date,Notes\n");
            for (Payment p : payments) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        p.getId(),
                        p.getTransactionId(),
                        escapeCsv(p.getTournamentTitle()),
                        escapeCsv(p.getTeamName()),
                        p.getTeamTag(),
                        p.getAmount(),
                        p.getPaymentType(),
                        p.getPaymentMethod(),
                        p.getStatus(),
                        p.getPaymentDate() != null ? p.getPaymentDate().toString() : "",
                        escapeCsv(p.getNotes() != null ? p.getNotes() : "")
                ));
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting payments to CSV: {}", e.getMessage(), e);
            return false;
        }
    }

    // =========================================================================
    // 3. TOURNAMENT STANDINGS & LEADERBOARD REPORT
    // =========================================================================

    public boolean exportStandingsToPdf(int tournamentId, File targetFile) {
        Tournament t = tournamentDAO.findById(tournamentId);
        String tourneyTitle = t != null ? t.getTitle() : "Tournament #" + tournamentId;
        List<TournamentStanding> standings = standingDAO.getStandingsByTournament(tournamentId);

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(targetFile));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(0, 229, 255));
            Paragraph title = new Paragraph("🏆 OFFICIAL TOURNAMENT STANDINGS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.DARK_GRAY);
            Paragraph sub = new Paragraph(tourneyTitle, subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(14);
            document.add(sub);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4.5f, 2.0f, 1.5f, 1.5f, 1.5f, 1.5f, 2.0f, 2.0f});

            String[] headers = {"Rank", "Squad Name", "Group", "P", "W", "D", "L", "Diff", "Pts"};
            for (String h : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                headerCell.setBackgroundColor(new Color(19, 27, 42));
                headerCell.setPadding(7);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }

            boolean alternate = false;
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Color altColor = new Color(245, 247, 250);

            for (TournamentStanding s : standings) {
                Color bg = alternate ? altColor : Color.WHITE;
                String rankPill = String.valueOf(s.getRankPosition());
                if (s.getRankPosition() == 1) rankPill = "1st [Gold]";
                else if (s.getRankPosition() == 2) rankPill = "2nd [Silver]";
                else if (s.getRankPosition() == 3) rankPill = "3rd [Bronze]";

                addCell(table, rankPill, rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, s.getTeamName() + " [" + s.getTeamTag() + "]", rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, s.getGroupName() != null ? s.getGroupName() : "-", rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(s.getMatchesPlayed()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(s.getWins()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(s.getDraws()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(s.getLosses()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, (s.getScoreDiff() > 0 ? "+" : "") + s.getScoreDiff(), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(s.getPoints()), rowFont, bg, Element.ALIGN_CENTER);
                alternate = !alternate;
            }

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            logger.error("Error exporting standings to PDF: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean exportStandingsToCsv(int tournamentId, File targetFile) {
        List<TournamentStanding> standings = standingDAO.getStandingsByTournament(tournamentId);
        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write("Rank,Team Name,Team Tag,Group,Matches Played,Wins,Draws,Losses,Score Difference,Points\n");
            for (TournamentStanding s : standings) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,%d,%d\n",
                        s.getRankPosition(),
                        escapeCsv(s.getTeamName()),
                        s.getTeamTag(),
                        s.getGroupName() != null ? s.getGroupName() : "",
                        s.getMatchesPlayed(),
                        s.getWins(),
                        s.getDraws(),
                        s.getLosses(),
                        s.getScoreDiff(),
                        s.getPoints()
                ));
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting standings to CSV: {}", e.getMessage(), e);
            return false;
        }
    }

    // =========================================================================
    // 4. PRO PLAYERS PERFORMANCE LEADERBOARD
    // =========================================================================

    public boolean exportPlayersToPdf(File targetFile) {
        List<Player> players = playerDAO.findAll();
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(targetFile));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 229, 255));
            Paragraph title = new Paragraph("🎯 PRO PLAYERS PERFORMANCE LEADERBOARD", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph meta = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FMT) + " | eSports Management System", metaFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(18);
            document.add(meta);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4.0f, 4.0f, 3.5f, 3.0f, 3.0f, 2.5f, 2.5f, 2.5f});

            String[] headers = {"ID", "Gamer Tag", "Full Name", "Current Squad", "Game", "Role", "RP Points", "Kills", "MVPs"};
            for (String h : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                headerCell.setBackgroundColor(new Color(19, 27, 42));
                headerCell.setPadding(8);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }

            boolean alternate = false;
            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Color altColor = new Color(245, 247, 250);

            for (Player p : players) {
                Color bg = alternate ? altColor : Color.WHITE;
                addCell(table, String.valueOf(p.getId()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, p.getGamerTag(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, p.getFullName(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, p.getCurrentTeamName() != null ? p.getCurrentTeamName() : "Free Agent", rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, p.getPrimaryGame(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, p.getInGameRole(), rowFont, bg, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(p.getRankingPoints()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(p.getKills()), rowFont, bg, Element.ALIGN_CENTER);
                addCell(table, String.valueOf(p.getMvpCount()), rowFont, bg, Element.ALIGN_CENTER);
                alternate = !alternate;
            }

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            logger.error("Error exporting players to PDF: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean exportPlayersToCsv(File targetFile) {
        List<Player> players = playerDAO.findAll();
        try (FileWriter writer = new FileWriter(targetFile)) {
            writer.write("Player ID,Gamer Tag,Full Name,Current Squad,Primary Game,Role,Country,Ranking Points,Matches Played,Wins,Losses,Kills,MVPs\n");
            for (Player p : players) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,%d,%d\n",
                        p.getId(),
                        escapeCsv(p.getGamerTag()),
                        escapeCsv(p.getFullName()),
                        escapeCsv(p.getCurrentTeamName() != null ? p.getCurrentTeamName() : "Free Agent"),
                        escapeCsv(p.getPrimaryGame()),
                        escapeCsv(p.getInGameRole()),
                        escapeCsv(p.getCountry()),
                        p.getRankingPoints(),
                        p.getMatchesPlayed(),
                        p.getWins(),
                        p.getLosses(),
                        p.getKills(),
                        p.getMvpCount()
                ));
            }
            return true;
        } catch (IOException e) {
            logger.error("Error exporting players to CSV: {}", e.getMessage(), e);
            return false;
        }
    }

    // --- Helper Methods ---
    private void addCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addKpiCell(PdfPTable table, String title, String value, Color valColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(245, 247, 250));
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pTitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.GRAY));
        pTitle.setAlignment(Element.ALIGN_CENTER);

        Paragraph pVal = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, valColor));
        pVal.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(pTitle);
        cell.addElement(pVal);
        table.addCell(cell);
    }

    private String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}
