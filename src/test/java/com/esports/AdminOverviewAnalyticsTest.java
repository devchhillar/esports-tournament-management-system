package com.esports;

import com.esports.dao.StatsDAO;
import com.esports.dao.StatsDAOImpl;
import com.esports.dao.TournamentDAO;
import com.esports.dao.TournamentDAOImpl;
import com.esports.model.Tournament;
import com.esports.view.admin.AdminOverviewPanel;
import com.esports.view.common.ChartUtil;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminOverviewAnalyticsTest {

    private final StatsDAO statsDAO = new StatsDAOImpl();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();

    @Test
    @Order(1)
    public void testTournamentStatusDistributionChart() {
        System.out.println("\n=== 1. TEST TOURNAMENT STATUS DISTRIBUTION (PIE CHART) ===");
        Map<String, Integer> statusCounts = statsDAO.getTournamentsByStatus();
        assertNotNull(statusCounts, "Status map must not be null");
        System.out.println("Current status counts from DB: " + statusCounts);

        // Verify that only valid positive counts are included
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            assertNotNull(entry.getKey(), "Status key must not be null");
            assertFalse(entry.getKey().trim().isEmpty(), "Status key must not be empty");
            assertTrue(entry.getValue() > 0, "Status count must be positive");
        }

        // Test chart creation
        ChartPanel chartPanel = ChartUtil.createPieChart("Tournament Status Distribution", statusCounts);
        assertNotNull(chartPanel);
        JFreeChart chart = chartPanel.getChart();
        assertNotNull(chart);
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        assertNotNull(plot);
        assertNotNull(plot.getLabelGenerator(), "Pie section label generator must be configured");

        // Verify dataset values match DB exactly
        @SuppressWarnings("rawtypes")
        DefaultPieDataset dataset = (DefaultPieDataset) plot.getDataset();
        for (String status : statusCounts.keySet()) {
            assertEquals(statusCounts.get(status), dataset.getValue(status).intValue());
        }

        // Test empty state handling
        ChartPanel emptyPanel = ChartUtil.createPieChart("Empty Distribution", Collections.emptyMap());
        PiePlot<?> emptyPlot = (PiePlot<?>) emptyPanel.getChart().getPlot();
        assertEquals(0, emptyPlot.getDataset().getItemCount());
        assertNotNull(emptyPlot.getNoDataMessage());
        System.out.println("Empty state message verified: " + emptyPlot.getNoDataMessage());
    }

    @Test
    @Order(2)
    public void testGamePopularityVisualization() {
        System.out.println("\n=== 2. TEST GAME POPULARITY CHART ===");
        List<StatsDAO.GamePopularityStat> games = statsDAO.getGamePopularityStats();
        assertNotNull(games, "Game popularity stats must not be null");
        System.out.println("Fetched " + games.size() + " games with activity:");
        for (StatsDAO.GamePopularityStat g : games) {
            System.out.println("  " + g.gameName() + " -> Tournaments: " + g.tournamentCount() + ", Teams: " + g.teamCount() + ", Players: " + g.playerCount());
        }

        // Verify sorting by tournament count descending
        for (int i = 0; i < games.size() - 1; i++) {
            assertTrue(games.get(i).tournamentCount() >= games.get(i + 1).tournamentCount(),
                    "Games must be sorted by tournament count descending: " + games.get(i) + " vs " + games.get(i + 1));
        }

        ChartPanel chartPanel = ChartUtil.createGamePopularityChart("Game Popularity & Participation", games);
        assertNotNull(chartPanel);
        CategoryPlot plot = chartPanel.getChart().getCategoryPlot();
        assertEquals(3, plot.getDataset().getRowCount(), "Should have 3 series (Tournaments, Teams, Players)");
        assertEquals("Tournaments", plot.getDataset().getRowKey(0));
        assertEquals("Teams", plot.getDataset().getRowKey(1));
        assertEquals("Players", plot.getDataset().getRowKey(2));
    }

    @Test
    @Order(3)
    public void testTopTeamsPointsVisualization() {
        System.out.println("\n=== 3. TEST TOP TEAMS (POINTS) CHART ===");
        List<StatsDAO.TeamPointsStat> teams = statsDAO.getTopTeamsStats(5);
        assertNotNull(teams, "Top teams list must not be null");
        System.out.println("Fetched " + teams.size() + " top teams:");
        for (StatsDAO.TeamPointsStat t : teams) {
            System.out.println("  " + t.teamName() + " -> Total Points: " + t.totalPoints() + ", Won: " + t.matchesWon() + ", Played: " + t.matchesPlayed());
        }

        // Verify sorting by total points descending
        for (int i = 0; i < teams.size() - 1; i++) {
            assertTrue(teams.get(i).totalPoints() >= teams.get(i + 1).totalPoints(),
                    "Teams must be sorted by total points descending: " + teams.get(i) + " vs " + teams.get(i + 1));
        }

        ChartPanel chartPanel = ChartUtil.createTopTeamsChart("Top Performing Esports Teams", teams);
        assertNotNull(chartPanel);
        CategoryPlot plot = chartPanel.getChart().getCategoryPlot();
        assertEquals(3, plot.getDataset().getRowCount(), "Should have 3 series (Total Points, Wins, Matches Played)");
        assertEquals("Total Points", plot.getDataset().getRowKey(0));
        assertEquals("Wins", plot.getDataset().getRowKey(1));
        assertEquals("Matches Played", plot.getDataset().getRowKey(2));
    }

    @Test
    @Order(4)
    public void testTopPlayersKillsVisualization() {
        System.out.println("\n=== 4. TEST TOP PLAYERS (KILLS) CHART ===");
        List<StatsDAO.PlayerKillsStat> players = statsDAO.getTopPlayersStats(5);
        assertNotNull(players, "Top players list must not be null");
        System.out.println("Fetched " + players.size() + " top players with recorded kills:");
        for (StatsDAO.PlayerKillsStat p : players) {
            System.out.println("  " + p.gamerTag() + " (" + p.teamName() + ") -> Kills: " + p.kills() + ", MVPs: " + p.mvpCount());
            assertTrue(p.kills() > 0, "Top fraggers chart must only include players with kills > 0");
        }

        // Verify sorting by kills descending
        for (int i = 0; i < players.size() - 1; i++) {
            assertTrue(players.get(i).kills() >= players.get(i + 1).kills(),
                    "Players must be sorted by total kills descending");
        }

        ChartPanel chartPanel = ChartUtil.createTopPlayersChart("Top Fraggers / Eliminators", players);
        assertNotNull(chartPanel);
        CategoryPlot plot = chartPanel.getChart().getCategoryPlot();
        if (!players.isEmpty()) {
            assertEquals(1, plot.getDataset().getRowCount(), "Should have 1 series (Total Kills)");
            assertEquals("Total Kills", plot.getDataset().getRowKey(0));
        }

        // Verify empty state when no kills recorded
        ChartPanel emptyPanel = ChartUtil.createTopPlayersChart("Empty Fraggers", Collections.emptyList());
        CategoryPlot emptyPlot = emptyPanel.getChart().getCategoryPlot();
        assertEquals(0, emptyPlot.getDataset().getColumnCount());
        assertNotNull(emptyPlot.getNoDataMessage());
        System.out.println("Empty state message verified: " + emptyPlot.getNoDataMessage());
    }

    @Test
    @Order(5)
    public void testDashboardKpiMetricsSynchronization() {
        System.out.println("\n=== 5. TEST DASHBOARD STATISTICS SYNCHRONIZATION ===");
        Map<String, Integer> metrics = statsDAO.getOverallMetrics();
        assertNotNull(metrics);
        System.out.println("KPI Metrics from DB:");
        metrics.forEach((k, v) -> System.out.println("  " + k + " = " + v));

        assertTrue(metrics.containsKey("totalTournaments"));
        assertTrue(metrics.containsKey("activeTournaments"));
        assertTrue(metrics.containsKey("upcomingTournaments"));
        assertTrue(metrics.containsKey("completedTournaments"));
        assertTrue(metrics.containsKey("totalTeams"));
        assertTrue(metrics.containsKey("totalPlayers"));
        assertTrue(metrics.containsKey("totalMatches"));
        assertTrue(metrics.containsKey("matchesCompleted"));
        assertTrue(metrics.containsKey("matchesRemaining"));
        assertTrue(metrics.containsKey("pendingRegistrations"));

        int total = metrics.get("totalTournaments");
        assertTrue(total >= 0);
        int upcoming = metrics.get("upcomingTournaments");
        assertTrue(upcoming >= 0);
    }

    @Test
    @Order(6)
    public void testLiveTournamentWorkflowUpdatesAnalytics() {
        System.out.println("\n=== 6. TEST REAL-TIME TOURNAMENT LIFECYCLE REFLECTION IN ANALYTICS ===");
        Map<String, Integer> initialMetrics = statsDAO.getOverallMetrics();
        int initialTotalTourneys = initialMetrics.getOrDefault("totalTournaments", 0);
        int initialUpcoming = initialMetrics.getOrDefault("upcomingTournaments", 0);
        int initialActive = initialMetrics.getOrDefault("activeTournaments", 0);

        Map<String, Integer> initialStatus = statsDAO.getTournamentsByStatus();
        int initialUpcomingStatus = initialStatus.getOrDefault("UPCOMING", 0);
        int initialOngoingStatus = initialStatus.getOrDefault("ONGOING", 0);

        // Step 1: Create a new tournament with status UPCOMING
        Tournament t = new Tournament();
        t.setTitle("Analytics Auto-Verification Cup " + System.currentTimeMillis());
        t.setGame("Valorant");
        t.setFormat("SINGLE_ELIMINATION");
        t.setMaxTeams(16);
        t.setMinTeams(4);
        t.setPrizePool(new BigDecimal("50000.00"));
        t.setEntryFee(new BigDecimal("500.00"));
        t.setStatus("UPCOMING");
        t.setOrganizer("ESports Arena Test Org");
        t.setStartDate(new Timestamp(System.currentTimeMillis() + 86400000L));
        t.setEndDate(new Timestamp(System.currentTimeMillis() + 172800000L));
        t.setCreatedBy(1);

        boolean created = tournamentDAO.create(t);
        assertTrue(created, "Tournament creation must succeed");
        assertTrue(t.getId() > 0, "Created tournament must have a valid generated ID");
        int testTourneyId = t.getId();
        System.out.println("Created test tournament ID: " + testTourneyId);

        try {
            // Step 2: Verify Total Tournaments and Upcoming Tournaments increased
            Map<String, Integer> metricsAfterCreate = statsDAO.getOverallMetrics();
            assertEquals(initialTotalTourneys + 1, metricsAfterCreate.get("totalTournaments").intValue(),
                    "Total tournaments KPI must increment by 1");
            assertEquals(initialUpcoming + 1, metricsAfterCreate.get("upcomingTournaments").intValue(),
                    "Upcoming tournaments KPI must increment by 1");

            Map<String, Integer> statusAfterCreate = statsDAO.getTournamentsByStatus();
            assertEquals(initialUpcomingStatus + 1, statusAfterCreate.get("UPCOMING").intValue(),
                    "UPCOMING status count in pie chart data must increment by 1");

            // Step 3: Change Status to ONGOING
            boolean statusUpdated = tournamentDAO.updateStatus(testTourneyId, "ONGOING");
            assertTrue(statusUpdated, "Status update must succeed");

            Map<String, Integer> metricsAfterStatusUpdate = statsDAO.getOverallMetrics();
            assertEquals(initialUpcoming, metricsAfterStatusUpdate.get("upcomingTournaments").intValue(),
                    "Upcoming tournaments KPI must revert");
            assertEquals(initialActive + 1, metricsAfterStatusUpdate.get("activeTournaments").intValue(),
                    "Active tournaments KPI must increment by 1 when ONGOING");

            Map<String, Integer> statusAfterUpdate = statsDAO.getTournamentsByStatus();
            assertEquals(initialUpcomingStatus, statusAfterUpdate.getOrDefault("UPCOMING", 0).intValue());
            assertEquals(initialOngoingStatus + 1, statusAfterUpdate.get("ONGOING").intValue(),
                    "ONGOING status count in pie chart data must increment by 1");
            System.out.println("Pie chart dynamically reflects ONGOING status change!");

        } finally {
            // Step 4: Delete the test tournament and verify clean restoration
            boolean deleted = tournamentDAO.delete(testTourneyId);
            assertTrue(deleted, "Tournament deletion must succeed");
            System.out.println("Deleted test tournament ID: " + testTourneyId);

            Map<String, Integer> restoredMetrics = statsDAO.getOverallMetrics();
            assertEquals(initialTotalTourneys, restoredMetrics.get("totalTournaments").intValue(),
                    "Total tournaments KPI must return to initial state");
            assertEquals(initialUpcoming, restoredMetrics.get("upcomingTournaments").intValue(),
                    "Upcoming tournaments KPI must return to initial state");
            assertEquals(initialActive, restoredMetrics.get("activeTournaments").intValue(),
                    "Active tournaments KPI must return to initial state");

            Map<String, Integer> restoredStatus = statsDAO.getTournamentsByStatus();
            assertEquals(initialUpcomingStatus, restoredStatus.getOrDefault("UPCOMING", 0).intValue(),
                    "UPCOMING status count must restore to initial state");
            assertEquals(initialOngoingStatus, restoredStatus.getOrDefault("ONGOING", 0).intValue(),
                    "ONGOING status count must restore to initial state");
            System.out.println("Analytics and charts successfully restored to exact initial counts.");
        }
    }

    @Test
    @Order(7)
    public void testAdminOverviewPanelInitialization() throws Exception {
        System.out.println("\n=== 7. TEST ADMIN OVERVIEW PANEL SWING INITIALIZATION ===");
        SwingUtilities.invokeAndWait(() -> {
            AdminOverviewPanel panel = new AdminOverviewPanel();
            assertNotNull(panel);
            assertEquals(1, panel.getComponentCount());
        });
        System.out.println("AdminOverviewPanel initialized cleanly on EDT without errors.");
    }
}
