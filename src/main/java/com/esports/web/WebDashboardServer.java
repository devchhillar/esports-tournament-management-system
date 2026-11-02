package com.esports.web;

import com.esports.dao.*;
import com.esports.model.*;
import com.esports.service.ReportService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Built-in Lightweight Web Gateway & Tournament Portal.
 * Powered by standard Java SE HttpServer (Zero external dependencies).
 * Serves Cyber Dark Web Dashboard and REST APIs on port 8080.
 * Specially optimized with responsive CSS Flexbox/Grid for Super Admin Pages.
 */
public class WebDashboardServer {

    private static final Logger logger = LoggerFactory.getLogger(WebDashboardServer.class);
    private static HttpServer server;
    private static int activePort = 8080;

    private static final GameDAO gameDAO = new GameDAOImpl();
    private static final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private static final MatchDAO matchDAO = new MatchDAOImpl();
    private static final TeamDAO teamDAO = new TeamDAOImpl();
    private static final PlayerDAO playerDAO = new PlayerDAOImpl();
    private static final TournamentStandingDAO standingDAO = new TournamentStandingDAOImpl();
    private static final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private static final StatsDAO statsDAO = new StatsDAOImpl();
    private static final ReportService reportService = new ReportService();

    public static synchronized void startServer(int port) {
        if (server != null) return;
        try {
            activePort = port;
            server = HttpServer.create(new InetSocketAddress(port), 0);

            // 1. Root Portal Page
            server.createContext("/", new StaticPortalHandler());

            // 2. REST API Endpoints
            server.createContext("/api/stats", new StatsApiHandler());
            server.createContext("/api/games", new GamesApiHandler());
            server.createContext("/api/tournaments", new TournamentsApiHandler());
            server.createContext("/api/matches", new MatchesApiHandler());
            server.createContext("/api/teams", new TeamsApiHandler());
            server.createContext("/api/players", new PlayersApiHandler());
            server.createContext("/api/standings", new StandingsApiHandler());
            server.createContext("/api/payments", new PaymentsApiHandler());
            server.createContext("/api/reports/export", new ReportsExportHandler());

            // 3. Static Uploads Handler (Logos & Avatars)
            server.createContext("/uploads", new UploadsHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            logger.info("⚡ Web Dashboard Portal active at http://localhost:{}", port);
            System.out.println("🌐 [Web Portal]: Access online in browser at http://localhost:" + port);
        } catch (IOException e) {
            logger.error("Could not start Web Dashboard Server on port {}: {}", port, e.getMessage());
        }
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static int getActivePort() {
        return activePort;
    }

    public static void main(String[] args) {
        startServer(8080);
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }

    // --- Helper to send JSON or HTML response ---
    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // --- API Handlers ---
    static class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, Integer> metrics = statsDAO.getOverallMetrics();
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Integer> e : metrics.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(escapeJson(e.getKey())).append("\":").append(e.getValue());
            }
            sb.append("}");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class GamesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Game> list = gameDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Game g = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(g.getId()).append(",")
                        .append("\"name\":\"").append(escapeJson(g.getName())).append("\",")
                        .append("\"category\":\"").append(escapeJson(g.getCategory())).append("\",")
                        .append("\"minTeamSize\":").append(g.getMinTeamSize()).append(",")
                        .append("\"maxTeamSize\":").append(g.getMaxTeamSize()).append(",")
                        .append("\"status\":\"").append(escapeJson(g.getStatus())).append("\",")
                        .append("\"description\":\"").append(escapeJson(g.getDescription() != null ? g.getDescription() : "")).append("\"")
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class TournamentsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Tournament> list = tournamentDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Tournament t = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(t.getId()).append(",")
                        .append("\"title\":\"").append(escapeJson(t.getTitle())).append("\",")
                        .append("\"game\":\"").append(escapeJson(t.getGame())).append("\",")
                        .append("\"format\":\"").append(escapeJson(t.getFormat())).append("\",")
                        .append("\"prizePool\":").append(t.getPrizePool()).append(",")
                        .append("\"entryFee\":").append(t.getEntryFee()).append(",")
                        .append("\"maxTeams\":").append(t.getMaxTeams()).append(",")
                        .append("\"status\":\"").append(escapeJson(t.getStatus())).append("\",")
                        .append("\"startDate\":\"").append(t.getStartDate() != null ? t.getStartDate().toString() : "").append("\",")
                        .append("\"endDate\":\"").append(t.getEndDate() != null ? t.getEndDate().toString() : "").append("\",")
                        .append("\"organizer\":\"").append(escapeJson(t.getOrganizer() != null ? t.getOrganizer() : "")).append("\"")
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class MatchesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Match> list = matchDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Match m = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(m.getId()).append(",")
                        .append("\"tournamentId\":").append(m.getTournamentId()).append(",")
                        .append("\"tournamentTitle\":\"").append(escapeJson(m.getTournamentTitle())).append("\",")
                        .append("\"roundName\":\"").append(escapeJson(m.getRoundName())).append("\",")
                        .append("\"bracketType\":\"").append(escapeJson(m.getBracketType())).append("\",")
                        .append("\"team1Id\":").append(m.getTeam1Id() != null ? m.getTeam1Id() : "null").append(",")
                        .append("\"team2Id\":").append(m.getTeam2Id() != null ? m.getTeam2Id() : "null").append(",")
                        .append("\"team1Name\":\"").append(escapeJson(m.getTeam1Name() != null ? m.getTeam1Name() : "TBD")).append("\",")
                        .append("\"team2Name\":\"").append(escapeJson(m.getTeam2Name() != null ? m.getTeam2Name() : "TBD")).append("\",")
                        .append("\"team1Score\":").append(m.getTeam1Score()).append(",")
                        .append("\"team2Score\":").append(m.getTeam2Score()).append(",")
                        .append("\"winnerTeamId\":").append(m.getWinnerTeamId() != null ? m.getWinnerTeamId() : "null").append(",")
                        .append("\"winnerName\":\"").append(escapeJson(m.getWinnerTeamName() != null ? m.getWinnerTeamName() : "")).append("\",")
                        .append("\"venue\":\"").append(escapeJson(m.getVenue() != null ? m.getVenue() : "Main Arena")).append("\",")
                        .append("\"referee\":\"").append(escapeJson(m.getReferee() != null ? m.getReferee() : "Official Referee")).append("\",")
                        .append("\"scheduledTime\":\"").append(m.getScheduledTime() != null ? m.getScheduledTime().toString() : "").append("\",")
                        .append("\"status\":\"").append(escapeJson(m.getStatus())).append("\",")
                        .append("\"nextMatchId\":").append(m.getNextMatchId() != null ? m.getNextMatchId() : "null").append(",")
                        .append("\"nextMatchSlot\":").append(m.getNextMatchSlot() != null ? m.getNextMatchSlot() : "null")
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class TeamsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Team> list = teamDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Team t = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(t.getId()).append(",")
                        .append("\"teamName\":\"").append(escapeJson(t.getTeamName())).append("\",")
                        .append("\"teamTag\":\"").append(escapeJson(t.getTeamTag())).append("\",")
                        .append("\"game\":\"").append(escapeJson(t.getGame())).append("\",")
                        .append("\"leaderName\":\"").append(escapeJson(t.getLeaderName() != null ? t.getLeaderName() : "-")).append("\",")
                        .append("\"matchesPlayed\":").append(t.getMatchesPlayed()).append(",")
                        .append("\"matchesWon\":").append(t.getMatchesWon()).append(",")
                        .append("\"matchesLost\":").append(t.getMatchesLost()).append(",")
                        .append("\"rankingPoints\":").append(t.getRankingPoints()).append(",")
                        .append("\"totalPoints\":").append(t.getTotalPoints()).append(",")
                        .append("\"status\":\"").append(escapeJson(t.getStatus())).append("\",")
                        .append("\"logoPath\":\"").append(escapeJson(t.getLogoPath() != null ? t.getLogoPath() : "")).append("\"")
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class PlayersApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Player> list = playerDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Player p = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(p.getId()).append(",")
                        .append("\"gamerTag\":\"").append(escapeJson(p.getGamerTag())).append("\",")
                        .append("\"fullName\":\"").append(escapeJson(p.getFullName())).append("\",")
                        .append("\"currentTeamName\":\"").append(escapeJson(p.getCurrentTeamName() != null ? p.getCurrentTeamName() : "Free Agent")).append("\",")
                        .append("\"primaryGame\":\"").append(escapeJson(p.getPrimaryGame())).append("\",")
                        .append("\"role\":\"").append(escapeJson(p.getInGameRole())).append("\",")
                        .append("\"country\":\"").append(escapeJson(p.getCountry())).append("\",")
                        .append("\"rankingPoints\":").append(p.getRankingPoints()).append(",")
                        .append("\"matchesPlayed\":").append(p.getMatchesPlayed()).append(",")
                        .append("\"wins\":").append(p.getWins()).append(",")
                        .append("\"kills\":").append(p.getKills()).append(",")
                        .append("\"mvps\":").append(p.getMvpCount()).append(",")
                        .append("\"avatarUrl\":\"").append(escapeJson(p.getProfilePicture() != null ? p.getProfilePicture() : "")).append("\"")
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class StandingsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int tourneyId = 0;
            if (query != null && query.contains("tournamentId=")) {
                try {
                    tourneyId = Integer.parseInt(query.split("tournamentId=")[1].split("&")[0]);
                } catch (Exception ignored) {}
            }

            List<TournamentStanding> list;
            if (tourneyId > 0) {
                list = standingDAO.getStandingsByTournament(tourneyId);
            } else {
                List<Tournament> tourneys = tournamentDAO.findAll();
                if (!tourneys.isEmpty()) {
                    list = standingDAO.getStandingsByTournament(tourneys.get(0).getId());
                } else {
                    list = List.of();
                }
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                TournamentStanding s = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"rank\":").append(s.getRankPosition()).append(",")
                        .append("\"teamName\":\"").append(escapeJson(s.getTeamName())).append("\",")
                        .append("\"teamTag\":\"").append(escapeJson(s.getTeamTag())).append("\",")
                        .append("\"groupName\":\"").append(escapeJson(s.getGroupName() != null ? s.getGroupName() : "-")).append("\",")
                        .append("\"played\":").append(s.getMatchesPlayed()).append(",")
                        .append("\"wins\":").append(s.getWins()).append(",")
                        .append("\"draws\":").append(s.getDraws()).append(",")
                        .append("\"losses\":").append(s.getLosses()).append(",")
                        .append("\"points\":").append(s.getPoints()).append(",")
                        .append("\"scoreDiff\":").append(s.getScoreDiff())
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class PaymentsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Payment> list = paymentDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Payment p = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                        .append("\"id\":").append(p.getId()).append(",")
                        .append("\"transactionId\":\"").append(escapeJson(p.getTransactionId())).append("\",")
                        .append("\"tournamentTitle\":\"").append(escapeJson(p.getTournamentTitle())).append("\",")
                        .append("\"teamName\":\"").append(escapeJson(p.getTeamName())).append("\",")
                        .append("\"teamTag\":\"").append(escapeJson(p.getTeamTag())).append("\",")
                        .append("\"userName\":\"").append(escapeJson(p.getUserName())).append("\",")
                        .append("\"amount\":").append(p.getAmount()).append(",")
                        .append("\"paymentType\":\"").append(escapeJson(p.getPaymentType())).append("\",")
                        .append("\"paymentMethod\":\"").append(escapeJson(p.getPaymentMethod())).append("\",")
                        .append("\"status\":\"").append(escapeJson(p.getStatus())).append("\",")
                        .append("\"paymentDate\":\"").append(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "").append("\",")
                        .append("\"notes\":\"").append(escapeJson(p.getNotes() != null ? p.getNotes() : "")).append("\"")
                        .append("}");
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json; charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    static class ReportsExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String type = "tournaments";
            String format = "pdf";
            int tourneyId = 1;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if ("type".equalsIgnoreCase(pair[0])) type = pair[1];
                        if ("format".equalsIgnoreCase(pair[0])) format = pair[1];
                        if ("tourneyId".equalsIgnoreCase(pair[0])) {
                            try { tourneyId = Integer.parseInt(pair[1]); } catch (Exception ignored) {}
                        }
                    }
                }
            }

            File tempFile = File.createTempFile("esports_report_", "." + format.toLowerCase());
            boolean success = false;

            if ("pdf".equalsIgnoreCase(format)) {
                switch (type.toLowerCase()) {
                    case "tournaments" -> success = reportService.exportTournamentsToPdf(tempFile);
                    case "payments" -> success = reportService.exportPaymentsToPdf(tempFile);
                    case "standings" -> success = reportService.exportStandingsToPdf(tourneyId, tempFile);
                    case "players" -> success = reportService.exportPlayersToPdf(tempFile);
                }
            } else {
                switch (type.toLowerCase()) {
                    case "tournaments" -> success = reportService.exportTournamentsToCsv(tempFile);
                    case "payments" -> success = reportService.exportPaymentsToCsv(tempFile);
                    case "standings" -> success = reportService.exportStandingsToCsv(tourneyId, tempFile);
                    case "players" -> success = reportService.exportPlayersToCsv(tempFile);
                }
            }

            if (success && tempFile.exists()) {
                byte[] data = Files.readAllBytes(tempFile.toPath());
                tempFile.delete();
                String mime = "pdf".equalsIgnoreCase(format) ? "application/pdf" : "text/csv; charset=UTF-8";
                String filename = "esports_" + type + "_report." + format.toLowerCase();
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                sendResponse(exchange, 200, mime, data);
            } else {
                if (tempFile.exists()) tempFile.delete();
                byte[] err = "Report generation failed".getBytes(StandardCharsets.UTF_8);
                sendResponse(exchange, 500, "text/plain", err);
            }
        }
    }

    static class UploadsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/")) path = path.substring(1);
            File file = new File(path);
            if (file.exists() && !file.isDirectory()) {
                String mime = "application/octet-stream";
                if (path.endsWith(".png")) mime = "image/png";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime = "image/jpeg";
                else if (path.endsWith(".webp")) mime = "image/webp";

                byte[] bytes = Files.readAllBytes(file.toPath());
                sendResponse(exchange, 200, mime, bytes);
            } else {
                byte[] notFound = "File Not Found".getBytes(StandardCharsets.UTF_8);
                sendResponse(exchange, 404, "text/plain", notFound);
            }
        }
    }

    // --- Single-Page Application Cyber Dark Portal ---
    static class StaticPortalHandler implements HttpHandler {
        private static byte[] cachedHtml;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/")) {
                byte[] notFound = "Not Found".getBytes(StandardCharsets.UTF_8);
                sendResponse(exchange, 404, "text/plain", notFound);
                return;
            }

            byte[] htmlBytes = loadPortalBytes();
            sendResponse(exchange, 200, "text/html; charset=UTF-8", htmlBytes);
        }

        private static synchronized byte[] loadPortalBytes() {
            if (cachedHtml != null) {
                return cachedHtml;
            }

            try (InputStream is = WebDashboardServer.class.getResourceAsStream("/web/portal.html")) {
                if (is != null) {
                    cachedHtml = is.readAllBytes();
                    return cachedHtml;
                }
            } catch (Exception e) {
                logger.warn("Could not read /web/portal.html from classpath: {}", e.getMessage());
            }

            // Fallback to disk during development
            File diskFile = new File("src/main/resources/web/portal.html");
            if (diskFile.exists()) {
                try {
                    cachedHtml = Files.readAllBytes(diskFile.toPath());
                    return cachedHtml;
                } catch (IOException ignored) {}
            }

            cachedHtml = "<html><body><h1>eSports Portal Ready</h1></body></html>".getBytes(StandardCharsets.UTF_8);
            return cachedHtml;
        }
    }
}
