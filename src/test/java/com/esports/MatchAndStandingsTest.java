package com.esports;

import com.esports.dao.*;
import com.esports.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Match, Standings, and Dashboard Statistics Test Suite")
public class MatchAndStandingsTest {

    private MatchDAO matchDAO;
    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private TournamentDAO tournamentDAO;
    private TournamentStandingDAO standingDAO;
    private StatsDAO statsDAO;

    private Tournament testTournament;
    private Team teamAlpha;
    private Team teamBeta;
    private Player mvpPlayer;

    @BeforeEach
    void setUp() {
        matchDAO = new MatchDAOImpl();
        teamDAO = new TeamDAOImpl();
        playerDAO = new PlayerDAOImpl();
        tournamentDAO = new TournamentDAOImpl();
        standingDAO = new TournamentStandingDAOImpl();
        statsDAO = new StatsDAOImpl();

        // 1. Create temporary Tournament
        testTournament = new Tournament();
        testTournament.setTitle("Championship Finals " + System.currentTimeMillis());
        testTournament.setGame("Valorant");
        testTournament.setFormat("SINGLE_ELIMINATION");
        testTournament.setMaxTeams(8);
        testTournament.setMinTeams(2);
        testTournament.setStartDate(java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        testTournament.setEndDate(java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(4)));
        testTournament.setStatus("ONGOING");
        testTournament.setOrganizer("ESports Official League");
        tournamentDAO.create(testTournament);

        // 2. Fetch seed teams (or create two distinct teams)
        List<Team> teams = teamDAO.findAll();
        assertTrue(teams.size() >= 2, "At least two teams must exist in database");
        teamAlpha = teams.get(0);
        teamBeta = teams.get(1);

        // 3. Fetch a seed player for MVP
        List<Player> players = playerDAO.findAll();
        assertFalse(players.isEmpty(), "At least one player must exist in database");
        mvpPlayer = players.get(0);
    }

    @AfterEach
    void tearDown() {
        if (testTournament != null && testTournament.getId() > 0) {
            matchDAO.deleteByTournament(testTournament.getId());
            tournamentDAO.delete(testTournament.getId());
        }
    }

    @Test
    @DisplayName("Should create match fixture, update scores, declare winner and auto-update standings")
    void testMatchScoringAndStandingsProgression() {
        int initialMvpCount = mvpPlayer.getMvpCount();
        int initialAlphaMatches = teamAlpha.getMatchesPlayed();
        int initialAlphaWins = teamAlpha.getMatchesWon();

        // 1. Create a match fixture
        Match match = new Match();
        match.setTournamentId(testTournament.getId());
        match.setRoundName("Grand Final");
        match.setMatchNumber(1);
        match.setBracketType("GRAND_FINAL");
        match.setTeam1Id(teamAlpha.getId());
        match.setTeam2Id(teamBeta.getId());
        match.setScheduledTime(Timestamp.valueOf(LocalDateTime.now().plusHours(2)));
        match.setStatus("SCHEDULED");

        boolean created = matchDAO.create(match);
        assertTrue(created, "Match fixture creation should succeed");
        assertTrue(match.getId() > 0, "Generated match ID should be positive");

        // 2. Initialize tournament standings
        standingDAO.initializeStandings(testTournament.getId(), List.of(teamAlpha.getId(), teamBeta.getId()), null);

        // 3. Record score and declare winner: Team Alpha wins 13 - 9 against Team Beta with mvpPlayer
        int team1Score = 13;
        int team2Score = 9;
        boolean scoreRecorded = matchDAO.recordMatchScore(
                match.getId(),
                team1Score,
                team2Score,
                teamAlpha.getId(),
                mvpPlayer.getId(),
                "Overtime thriller victory by Team Alpha"
        );
        assertTrue(scoreRecorded, "Match score recording transaction should succeed");

        // 4. Verify match status in DB
        Match updatedMatch = matchDAO.findById(match.getId());
        assertNotNull(updatedMatch);
        assertEquals("COMPLETED", updatedMatch.getStatus());
        assertEquals(13, updatedMatch.getTeam1Score());
        assertEquals(9, updatedMatch.getTeam2Score());
        assertEquals(teamAlpha.getId(), updatedMatch.getWinnerTeamId());

        // 5. Verify Team stats updated
        Team reloadedAlpha = teamDAO.findById(teamAlpha.getId());
        assertNotNull(reloadedAlpha);
        assertEquals(initialAlphaMatches + 1, reloadedAlpha.getMatchesPlayed(), "Alpha matches played must increment by 1");
        assertEquals(initialAlphaWins + 1, reloadedAlpha.getMatchesWon(), "Alpha matches won must increment by 1");

        // 6. Verify MVP player stats updated
        Player reloadedPlayer = playerDAO.findById(mvpPlayer.getId());
        assertNotNull(reloadedPlayer);
        assertEquals(initialMvpCount + 1, reloadedPlayer.getMvpCount(), "MVP player count must increment by 1");

        // 7. Verify Tournament Standings updated
        List<TournamentStanding> standings = standingDAO.getStandingsByTournament(testTournament.getId());
        assertNotNull(standings);
        assertFalse(standings.isEmpty(), "Standings must be populated for tournament");

        TournamentStanding leader = standings.get(0);
        assertEquals(teamAlpha.getId(), leader.getTeamId(), "Winner (Team Alpha) should be ranked 1st in standings");
        assertEquals(1, leader.getWins());
        assertEquals(3, leader.getPoints(), "Winner should receive 3 tournament points");
        assertEquals(4, leader.getScoreDiff(), "Score difference should be 13 - 9 = 4");
    }

    @Test
    @DisplayName("Should dynamically retrieve system-wide metrics and chart datasets from MySQL")
    void testDashboardStatisticsAndCharts() {
        Map<String, Integer> metrics = statsDAO.getOverallMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalTournaments"));
        assertTrue(metrics.containsKey("totalTeams"));
        assertTrue(metrics.containsKey("totalPlayers"));
        assertTrue(metrics.containsKey("totalMatches"));
        assertTrue(metrics.get("totalTournaments") > 0, "Total tournaments count should be positive");
        assertTrue(metrics.get("totalTeams") > 0, "Total teams count should be positive");

        // Tournaments by status (for pie chart)
        Map<String, Integer> tourneyByStatus = statsDAO.getTournamentsByStatus();
        assertNotNull(tourneyByStatus);
        assertFalse(tourneyByStatus.isEmpty(), "Tournaments by status should contain categories");

        // Teams by game (for bar chart)
        Map<String, Integer> teamsByGame = statsDAO.getTeamsByGame();
        assertNotNull(teamsByGame);
        assertFalse(teamsByGame.isEmpty(), "Teams by game should contain entries");

        // Top teams by points
        Map<String, Integer> topTeams = statsDAO.getTopTeamsByPoints(5);
        assertNotNull(topTeams);

        // Recent activity logs
        List<String> activities = statsDAO.getRecentActivityLogs(5);
        assertNotNull(activities);
    }

    @Test
    @DisplayName("Should detect scheduling conflicts within +/- 120 minutes")
    void testSchedulingConflictDetection() {
        Team conflictTeam = new Team();
        conflictTeam.setTeamName("Conflict QA " + System.currentTimeMillis());
        conflictTeam.setTeamTag("C" + (System.currentTimeMillis() % 1000));
        conflictTeam.setGame("Valorant");
        conflictTeam.setLeaderId(1);
        conflictTeam.setStatus("ACTIVE");
        teamDAO.create(conflictTeam, 1);

        try {
            LocalDateTime baseTime = LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).withSecond(0).withNano(0);

            // Create match for conflictTeam at baseTime
            Match match1 = new Match();
            match1.setTournamentId(testTournament.getId());
            match1.setRoundName("Round 1");
            match1.setMatchNumber(10);
            match1.setBracketType("KNOCKOUT");
            match1.setTeam1Id(conflictTeam.getId());
            match1.setTeam2Id(teamBeta.getId());
            match1.setScheduledTime(Timestamp.valueOf(baseTime));
            match1.setStatus("SCHEDULED");
            assertTrue(matchDAO.create(match1));

            // 1. Conflict should be TRUE for 30 minutes later (overlap < 120 mins)
            boolean conflict30Min = matchDAO.hasSchedulingConflict(conflictTeam.getId(), Timestamp.valueOf(baseTime.plusMinutes(30)), null);
            assertTrue(conflict30Min, "30 minutes later should trigger scheduling conflict");

            // 2. Conflict should be TRUE for 90 minutes earlier (overlap < 120 mins)
            boolean conflict90MinEarlier = matchDAO.hasSchedulingConflict(conflictTeam.getId(), Timestamp.valueOf(baseTime.minusMinutes(90)), null);
            assertTrue(conflict90MinEarlier, "90 minutes earlier should trigger scheduling conflict");

            // 3. Conflict should be FALSE when excluding match1 itself (for reschedule dialog check)
            boolean conflictSelf = matchDAO.hasSchedulingConflict(conflictTeam.getId(), Timestamp.valueOf(baseTime.plusMinutes(30)), match1.getId());
            assertFalse(conflictSelf, "Excluding current match ID should not report conflict against itself");

            // 4. Conflict should be FALSE for 3 hours later (> 120 mins)
            boolean conflict3Hours = matchDAO.hasSchedulingConflict(conflictTeam.getId(), Timestamp.valueOf(baseTime.plusMinutes(180)), null);
            assertFalse(conflict3Hours, "3 hours later should not trigger scheduling conflict");
        } finally {
            matchDAO.deleteByTournament(testTournament.getId());
            teamDAO.delete(conflictTeam.getId());
        }
    }

    @Test
    @DisplayName("Should reschedule match with venue/referee and cancel match with reason")
    void testRescheduleAndCancelMatch() {
        LocalDateTime initialTime = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0);
        LocalDateTime newTime = LocalDateTime.now().plusDays(3).withHour(18).withMinute(30);

        Match match = new Match();
        match.setTournamentId(testTournament.getId());
        match.setRoundName("Semifinal");
        match.setMatchNumber(20);
        match.setBracketType("KNOCKOUT");
        match.setTeam1Id(teamAlpha.getId());
        match.setTeam2Id(teamBeta.getId());
        match.setScheduledTime(Timestamp.valueOf(initialTime));
        match.setVenue("LAN Stage A");
        match.setReferee("Referee Alpha");
        match.setStatus("SCHEDULED");
        assertTrue(matchDAO.create(match));

        // 1. Reschedule match
        boolean rescheduled = matchDAO.rescheduleMatch(match.getId(), Timestamp.valueOf(newTime), "Grand Arena Stage 1", "Chief Referee Kumar");
        assertTrue(rescheduled, "Rescheduling should succeed");

        Match reloaded = matchDAO.findById(match.getId());
        assertNotNull(reloaded);
        assertEquals("Grand Arena Stage 1", reloaded.getVenue());
        assertEquals("Chief Referee Kumar", reloaded.getReferee());

        // 2. Cancel match
        boolean cancelled = matchDAO.cancelMatch(match.getId(), "Technical Server Failure");
        assertTrue(cancelled, "Cancellation should succeed");

        Match cancelledMatch = matchDAO.findById(match.getId());
        assertNotNull(cancelledMatch);
        assertEquals("CANCELLED", cancelledMatch.getStatus());
    }

    @Test
    @DisplayName("Should automatically advance winning team to next linked bracket slot in atomic transaction")
    void testAutomaticWinnerAdvancementInBracketTree() {
        // 1. Create Grand Final match with NULL teams (awaiting semifinal winners)
        Match grandFinal = new Match();
        grandFinal.setTournamentId(testTournament.getId());
        grandFinal.setRoundName("Grand Final");
        grandFinal.setMatchNumber(100);
        grandFinal.setBracketType("GRAND_FINAL");
        grandFinal.setTeam1Id(null);
        grandFinal.setTeam2Id(null);
        grandFinal.setScheduledTime(Timestamp.valueOf(LocalDateTime.now().plusDays(4)));
        grandFinal.setStatus("SCHEDULED");
        assertTrue(matchDAO.create(grandFinal));
        assertTrue(grandFinal.getId() > 0);

        // 2. Create Semifinal 1 match with teamAlpha and teamBeta, linked to grandFinal slot 1
        Match semi1 = new Match();
        semi1.setTournamentId(testTournament.getId());
        semi1.setRoundName("Semifinal 1");
        semi1.setMatchNumber(101);
        semi1.setBracketType("KNOCKOUT");
        semi1.setTeam1Id(teamAlpha.getId());
        semi1.setTeam2Id(teamBeta.getId());
        semi1.setScheduledTime(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
        semi1.setStatus("SCHEDULED");
        semi1.setNextMatchId(grandFinal.getId());
        semi1.setNextMatchSlot(1);
        assertTrue(matchDAO.create(semi1));

        // Verify grandFinal currently has null team1Id
        Match preCheckGf = matchDAO.findById(grandFinal.getId());
        assertNotNull(preCheckGf);
        assertNull(preCheckGf.getTeam1Id(), "Grand Final team 1 slot should initially be null");

        // 3. Record score in Semifinal 1: Team Alpha wins 2 - 0
        boolean scoreSuccess = matchDAO.recordMatchScore(
                semi1.getId(),
                2,
                0,
                teamAlpha.getId(),
                mvpPlayer.getId(),
                "Alpha qualifies for Grand Final"
        );
        assertTrue(scoreSuccess, "Recording score must succeed and trigger advancement");

        // 4. Verify Grand Final team1Id has been automatically updated to teamAlpha in MySQL
        Match postCheckGf = matchDAO.findById(grandFinal.getId());
        assertNotNull(postCheckGf);
        assertEquals(teamAlpha.getId(), postCheckGf.getTeam1Id(), "Grand Final team 1 must be automatically filled with Team Alpha");
    }
}
