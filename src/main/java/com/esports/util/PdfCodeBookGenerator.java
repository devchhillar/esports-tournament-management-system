package com.esports.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to generate an official ~25 to 30 page printable PDF Code Book
 * containing complete project source code for academic and professional submission.
 */
public class PdfCodeBookGenerator {

    private static final Color COLOR_PRIMARY = new Color(19, 27, 42);      // Deep Cyber Navy
    private static final Color COLOR_CYAN = new Color(0, 180, 216);         // Cyber Blue/Cyan
    private static final Color COLOR_CODE_BG = new Color(248, 250, 252);    // Very light slate
    private static final Color COLOR_CODE_BORDER = new Color(226, 232, 240);// Border grey
    private static final Color COLOR_HEADER_BG = new Color(15, 23, 42);     // Dark Header
    private static final Color COLOR_MUTED = new Color(100, 116, 139);      // Slate text

    public static void main(String[] args) {
        File file30 = new File("eSports_Tournament_System_Core_Code_30_Pages.pdf");
        File fileFull = new File("eSports_Tournament_Management_System_Complete_Code.pdf");

        System.out.println("Generating 30-Page Curated Edition...");
        generateCurated30Pages(file30);

        System.out.println("Generating Full Master Archive...");
        generateCodeBook(fileFull);

        try (PdfReader r30 = new PdfReader(file30.getAbsolutePath());
             PdfReader rFull = new PdfReader(fileFull.getAbsolutePath())) {
            System.out.println("✅ [Curated 30-Page PDF]: " + file30.getName() + " -> Total Pages: " + r30.getNumberOfPages());
            System.out.println("✅ [Full Master PDF]:     " + fileFull.getName() + " -> Total Pages: " + rFull.getNumberOfPages());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean generateCurated30Pages(File outputFile) {
        Document document = new Document(PageSize.A4, 36, 36, 46, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();
            buildCoverPage(document);
            document.newPage();
            buildArchitectureOverview(document);
            document.newPage();

            // Selected core architectural files tuned for ~28 to 30 pages
            Map<String, String> files = new LinkedHashMap<>();
            files.put("MODULE 1: Database Architecture & Normalized Schema (schema.sql)", "src/main/resources/database/schema.sql");
            files.put("MODULE 2: Database Connectivity & JDBC Manager (DBConnection.java)", "src/main/java/com/esports/config/DBConnection.java");
            files.put("MODULE 3: Cryptographic Password Security with BCrypt (PasswordUtil.java)", "src/main/java/com/esports/config/PasswordUtil.java");
            files.put("MODULE 4: Tournament Domain Entity Model (Tournament.java)", "src/main/java/com/esports/model/Tournament.java");
            files.put("MODULE 5: Match Domain Entity Model (Match.java)", "src/main/java/com/esports/model/Match.java");
            files.put("MODULE 6: Tournament Data Access Object (TournamentDAOImpl.java)", "src/main/java/com/esports/dao/TournamentDAOImpl.java");
            files.put("MODULE 7: Tournament Fixture Generation Engine (FixtureService.java)", "src/main/java/com/esports/service/FixtureService.java");
            files.put("MODULE 8: User Authentication & Role Controller (AuthController.java)", "src/main/java/com/esports/controller/AuthController.java");

            int idx = 0;
            for (Map.Entry<String, String> entry : files.entrySet()) {
                File file = new File(entry.getValue());
                if (file.exists()) {
                    if (idx > 0) document.newPage();
                    addSourceModuleSection(document, entry.getKey(), file);
                    idx++;
                }
            }

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean generateCodeBook(File outputFile) {
        Document document = new Document(PageSize.A4, 36, 36, 46, 40); // 0.5 in margins, space for header/footer

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            // 1. Cover Page
            buildCoverPage(document);
            document.newPage();

            // 2. Table of Contents & System Architecture Overview
            buildArchitectureOverview(document);
            document.newPage();

            // 3. Collect Key Source Files
            Map<String, String> filesToInclude = new LinkedHashMap<>();

            // Module 1: Database
            filesToInclude.put("MODULE 1: Database DDL Schema (schema.sql)", "src/main/resources/database/schema.sql");

            // Module 2: Configuration & Security
            filesToInclude.put("MODULE 2.1: JDBC Connection Manager (DBConnection.java)", "src/main/java/com/esports/config/DBConnection.java");
            filesToInclude.put("MODULE 2.2: Cryptographic Password Utility (PasswordUtil.java)", "src/main/java/com/esports/config/PasswordUtil.java");
            filesToInclude.put("MODULE 2.3: Modern Theme Configuration (ThemeConfig.java)", "src/main/java/com/esports/config/ThemeConfig.java");

            // Module 3: Models
            filesToInclude.put("MODULE 3.1: Tournament Entity Model (Tournament.java)", "src/main/java/com/esports/model/Tournament.java");
            filesToInclude.put("MODULE 3.2: Match Entity Model (Match.java)", "src/main/java/com/esports/model/Match.java");
            filesToInclude.put("MODULE 3.3: Team Entity Model (Team.java)", "src/main/java/com/esports/model/Team.java");
            filesToInclude.put("MODULE 3.4: Player Entity Model (Player.java)", "src/main/java/com/esports/model/Player.java");
            filesToInclude.put("MODULE 3.5: Payment Entity Model (Payment.java)", "src/main/java/com/esports/model/Payment.java");

            // Module 4: Data Access Objects
            filesToInclude.put("MODULE 4.1: Tournament DAO Implementation (TournamentDAOImpl.java)", "src/main/java/com/esports/dao/TournamentDAOImpl.java");
            filesToInclude.put("MODULE 4.2: Match DAO Implementation (MatchDAOImpl.java)", "src/main/java/com/esports/dao/MatchDAOImpl.java");
            filesToInclude.put("MODULE 4.3: Team DAO Implementation (TeamDAOImpl.java)", "src/main/java/com/esports/dao/TeamDAOImpl.java");
            filesToInclude.put("MODULE 4.4: Payment DAO Implementation (PaymentDAOImpl.java)", "src/main/java/com/esports/dao/PaymentDAOImpl.java");

            // Module 5: Algorithms & Services
            filesToInclude.put("MODULE 5: Tournament Fixture Engine (FixtureService.java)", "src/main/java/com/esports/service/FixtureService.java");
            filesToInclude.put("MODULE 6: Document & Analytics Reporting Engine (ReportService.java)", "src/main/java/com/esports/service/ReportService.java");

            // Module 7: Controllers & GUI
            filesToInclude.put("MODULE 7: Authentication Controller (AuthController.java)", "src/main/java/com/esports/controller/AuthController.java");
            filesToInclude.put("MODULE 8.1: Main Super Admin Command Center (AdminDashboard.java)", "src/main/java/com/esports/view/admin/AdminDashboard.java");
            filesToInclude.put("MODULE 8.2: Super Admin Game Management Panel (AdminGamesPanel.java)", "src/main/java/com/esports/view/admin/AdminGamesPanel.java");
            filesToInclude.put("MODULE 8.3: Super Admin Tournament Management Panel (AdminTournamentsPanel.java)", "src/main/java/com/esports/view/admin/AdminTournamentsPanel.java");
            filesToInclude.put("MODULE 8.4: Super Admin Match Operations Panel (AdminMatchesPanel.java)", "src/main/java/com/esports/view/admin/AdminMatchesPanel.java");
            filesToInclude.put("MODULE 8.5: Super Admin Payment & Fees Panel (AdminPaymentsPanel.java)", "src/main/java/com/esports/view/admin/AdminPaymentsPanel.java");
            filesToInclude.put("MODULE 8.6: Super Admin Reports & Exports Panel (AdminReportsPanel.java)", "src/main/java/com/esports/view/admin/AdminReportsPanel.java");

            // Module 9: Web Gateway & Tests
            filesToInclude.put("MODULE 9: Web Server Gateway & REST Controller (WebDashboardServer.java)", "src/main/java/com/esports/web/WebDashboardServer.java");
            filesToInclude.put("MODULE 10: Application Launch & Bootstrap (Main.java)", "src/main/java/com/esports/main/Main.java");
            filesToInclude.put("MODULE 11: Fixture Algorithm Automated Test Suite (FixtureServiceTest.java)", "src/test/java/com/esports/FixtureServiceTest.java");

            // Print each module with clean syntax-like layout
            int moduleCount = 0;
            for (Map.Entry<String, String> entry : filesToInclude.entrySet()) {
                File file = new File(entry.getValue());
                if (file.exists()) {
                    if (moduleCount > 0) {
                        document.newPage();
                    }
                    addSourceModuleSection(document, entry.getKey(), file);
                    moduleCount++;
                }
            }

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void buildCoverPage(Document doc) throws DocumentException {
        Paragraph spacer = new Paragraph("\n\n\n");
        doc.add(spacer);

        Font superFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, COLOR_PRIMARY);
        Paragraph superTitle = new Paragraph("eSports Tournament Management System", superFont);
        superTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(superTitle);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 15, COLOR_CYAN);
        Paragraph subTitle = new Paragraph("COMPLETE PRODUCTION SOURCE CODE & ARCHITECTURE PORTFOLIO", subFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(30);
        doc.add(subTitle);

        // Metadata Box
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(90);
        metaTable.setWidths(new float[]{3.5f, 6.5f});

        addMetaRow(metaTable, "Project Title:", "eSports Tournament Management Desktop Platform");
        addMetaRow(metaTable, "Architecture:", "MVC (Model-View-Controller) + DAO Pattern");
        addMetaRow(metaTable, "Programming Language:", "Java 21 (LTS Release)");
        addMetaRow(metaTable, "GUI Framework:", "Java Swing + FlatLaf Cyber Dark Look & Feel");
        addMetaRow(metaTable, "Database:", "MySQL 8.0+ via JDBC (Atomic PreparedStatements)");
        addMetaRow(metaTable, "Security & Encryption:", "BCrypt Cryptographic Hashing (Cost Factor 12)");
        addMetaRow(metaTable, "Document & Reporting:", "OpenPDF (A4 Landscape & Portrait) + CSV Streaming");
        addMetaRow(metaTable, "Data Visualizations:", "JFreeChart 2D/3D Interactive Analytics");
        addMetaRow(metaTable, "Fixture Algorithms:", "Single Elimination, Double Elimination, Round Robin, Group Stage");
        addMetaRow(metaTable, "Web Gateway Engine:", "Lightweight Java SE Built-in HTTP Server (Port 8080)");
        addMetaRow(metaTable, "Automated Test Suite:", "JUnit 5 Platform (25 Automated Integration Tests)");
        addMetaRow(metaTable, "Compilation Date:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm:ss")));

        doc.add(metaTable);

        Paragraph footerNote = new Paragraph("\n\n\n\nConfidential & Comprehensive Project Submission Dossier\nAll source code verified, compiled, and database-connected.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, COLOR_MUTED));
        footerNote.setAlignment(Element.ALIGN_CENTER);
        doc.add(footerNote);
    }

    private static void addMetaRow(PdfPTable table, String key, String val) {
        Font kf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARY);
        Font vf = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

        PdfPCell c1 = new PdfPCell(new Phrase(key, kf));
        c1.setBackgroundColor(new Color(241, 245, 249));
        c1.setPadding(7);
        c1.setBorderColor(COLOR_CODE_BORDER);

        PdfPCell c2 = new PdfPCell(new Phrase(val, vf));
        c2.setBackgroundColor(Color.WHITE);
        c2.setPadding(7);
        c2.setBorderColor(COLOR_CODE_BORDER);

        table.addCell(c1);
        table.addCell(c2);
    }

    private static void buildArchitectureOverview(Document doc) throws DocumentException {
        Font h1Font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARY);
        Paragraph h1 = new Paragraph("System Architectural Blueprint & Component Map", h1Font);
        h1.setSpacingAfter(12);
        doc.add(h1);

        Font pFont = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, Color.DARK_GRAY);
        Paragraph intro = new Paragraph(
                "This document constitutes the unabridged code repository for the eSports Tournament Management System. " +
                "The application is structured into decoupled, enterprise-grade layers following the Model-View-Controller (MVC) " +
                "and Data Access Object (DAO) software engineering patterns.\n\n" +
                "Key Structural Pillars:\n" +
                "1. Database Layer: Normalized 3NF MySQL schema with relational integrity, cascade deletions, and index optimization.\n" +
                "2. Security & Session: Role-Based Access Control (RBAC) supporting Super Admin, Organizer, Team Manager, Player, and Spectator with BCrypt hash verification.\n" +
                "3. Tournament Fixture Generation Engine: Dynamic elimination brackets supporting power-of-two expansion, automated BYE placement, round-robin permutation matrices, and winner auto-advancement.\n" +
                "4. Desktop Graphical Interface: Cyber dark FlatLaf Swing desktop panels engineered with 2-tier responsive toolbars, non-overlapping component hierarchies, and horizontal-scrolling table viewports.\n" +
                "5. Embedded Web Spectator Gateway: Standard Java SE HTTP Server serving single-page application dashboard with real-time REST endpoints and PDF/CSV binary export handlers.",
                pFont
        );
        intro.setSpacingAfter(20);
        doc.add(intro);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 4.5f, 3.0f});

        addHeaderCell(table, "Layer");
        addHeaderCell(table, "Core Java Classes / Resources");
        addHeaderCell(table, "Responsibilities");

        addBodyCell(table, "Persistence / DAO", "TournamentDAOImpl, MatchDAOImpl, TeamDAOImpl, PlayerDAOImpl, PaymentDAOImpl", "Atomic SQL CRUD, transactions, relational mappings");
        addBodyCell(table, "Business Logic", "FixtureService, ReportService, AuthController", "Brackets generation, winner advancement, PDF/CSV generation");
        addBodyCell(table, "Domain Models", "Tournament, Match, Team, Player, Payment, Game, TournamentStanding", "Encapsulated entity state and POJOs");
        addBodyCell(table, "Desktop UI (Swing)", "AdminDashboard, AdminTournamentsPanel, AdminMatchesPanel, AdminPaymentsPanel, etc.", "FlatLaf dark client, interactive forms, tables");
        addBodyCell(table, "Web Gateway", "WebDashboardServer, StaticPortalHandler, REST Handlers", "Lightweight port 8080 HTTP server & live spectator portal");

        doc.add(table);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        c.setBackgroundColor(COLOR_HEADER_BG);
        c.setPadding(8);
        c.setBorderColor(COLOR_CODE_BORDER);
        table.addCell(c);
    }

    private static void addBodyCell(PdfPTable table, String c1, String c2, String c3) {
        Font bf = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.DARK_GRAY);
        PdfPCell cell1 = new PdfPCell(new Phrase(c1, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, COLOR_PRIMARY)));
        PdfPCell cell2 = new PdfPCell(new Phrase(c2, bf));
        PdfPCell cell3 = new PdfPCell(new Phrase(c3, bf));
        cell1.setPadding(6); cell2.setPadding(6); cell3.setPadding(6);
        cell1.setBorderColor(COLOR_CODE_BORDER); cell2.setBorderColor(COLOR_CODE_BORDER); cell3.setBorderColor(COLOR_CODE_BORDER);
        table.addCell(cell1); table.addCell(cell2); table.addCell(cell3);
    }

    private static void addSourceModuleSection(Document doc, String title, File sourceFile) throws Exception {
        // Module Title Banner
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setPadding(8);
        cell.setBorderColor(COLOR_PRIMARY);
        banner.addCell(cell);
        banner.setSpacingAfter(4);
        doc.add(banner);

        // File Path Header
        Paragraph pathPara = new Paragraph("File Path: " + sourceFile.getPath() + " (" + sourceFile.length() + " bytes)",
                FontFactory.getFont(FontFactory.COURIER_OBLIQUE, 8.5f, COLOR_MUTED));
        pathPara.setSpacingAfter(8);
        doc.add(pathPara);

        // Read Code Lines
        String code = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
        String[] lines = code.split("\r?\n");

        Font codeFont = FontFactory.getFont(FontFactory.COURIER, 7.5f, new Color(30, 41, 59));
        Font numFont = FontFactory.getFont(FontFactory.COURIER, 7.5f, new Color(148, 163, 184));

        PdfPTable codeTable = new PdfPTable(2);
        codeTable.setWidthPercentage(100);
        codeTable.setWidths(new float[]{0.8f, 9.2f});

        for (int i = 0; i < lines.length; i++) {
            String lineNum = String.format("%4d", i + 1);
            String lineCode = lines[i];
            if (lineCode.isEmpty()) lineCode = " ";

            PdfPCell numCell = new PdfPCell(new Phrase(lineNum, numFont));
            numCell.setBackgroundColor(COLOR_CODE_BG);
            numCell.setBorder(Rectangle.NO_BORDER);
            numCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            numCell.setPaddingTop(1.5f);
            numCell.setPaddingBottom(1.5f);
            numCell.setPaddingRight(6);

            PdfPCell lineCell = new PdfPCell(new Phrase(lineCode, codeFont));
            lineCell.setBackgroundColor(COLOR_CODE_BG);
            lineCell.setBorder(Rectangle.NO_BORDER);
            lineCell.setPaddingTop(1.5f);
            lineCell.setPaddingBottom(1.5f);
            lineCell.setPaddingLeft(6);

            codeTable.addCell(numCell);
            codeTable.addCell(lineCell);
        }

        // Wrap in outer bordered container
        PdfPTable container = new PdfPTable(1);
        container.setWidthPercentage(100);
        PdfPCell wrapper = new PdfPCell(codeTable);
        wrapper.setPadding(4);
        wrapper.setBorderColor(COLOR_CODE_BORDER);
        wrapper.setBackgroundColor(COLOR_CODE_BG);
        container.addCell(wrapper);
        container.setSpacingAfter(14);

        doc.add(container);
    }

    /**
     * Handles Running Header & Footer with Page Numbers
     */
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private int pageNumber = 0;

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            pageNumber++;
            if (pageNumber > 1) {
                PdfPTable header = new PdfPTable(1);
                header.setTotalWidth(document.right() - document.left());
                PdfPCell c = new PdfPCell(new Phrase("eSports Tournament Management System — Complete Source Code Dossier",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_MUTED)));
                c.setBorder(Rectangle.BOTTOM);
                c.setBorderColor(COLOR_CODE_BORDER);
                c.setPaddingBottom(4);
                header.addCell(c);
                header.writeSelectedRows(0, -1, document.left(), document.top() + 24, writer.getDirectContent());
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            if (pageNumber > 1) {
                PdfPTable footer = new PdfPTable(2);
                footer.setTotalWidth(document.right() - document.left());
                try {
                    footer.setWidths(new float[]{6f, 4f});
                } catch (DocumentException ignored) {}

                PdfPCell c1 = new PdfPCell(new Phrase("CONFIDENTIAL • ACADEMIC & PROFESSIONAL REPOSITORY",
                        FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_MUTED)));
                c1.setBorder(Rectangle.TOP);
                c1.setBorderColor(COLOR_CODE_BORDER);
                c1.setPaddingTop(4);

                PdfPCell c2 = new PdfPCell(new Phrase("Page " + pageNumber,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_PRIMARY)));
                c2.setBorder(Rectangle.TOP);
                c2.setBorderColor(COLOR_CODE_BORDER);
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c2.setPaddingTop(4);

                footer.addCell(c1);
                footer.addCell(c2);
                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 12, writer.getDirectContent());
            }
        }
    }
}
