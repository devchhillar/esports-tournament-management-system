package com.esports;

import com.esports.dao.MatchDAO;
import com.esports.dao.MatchDAOImpl;
import com.esports.dao.TeamDAO;
import com.esports.dao.TeamDAOImpl;
import com.esports.dao.TournamentDAO;
import com.esports.dao.TournamentDAOImpl;
import com.esports.model.Match;
import com.esports.model.Team;
import com.esports.model.Tournament;
import com.esports.service.FixtureService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fixture Generation System Test Suite")
public class FixtureServiceTest {

    private FixtureService fixtureService;
    private MatchDAO matchDAO;
    private TeamDAO teamDAO;
    private TournamentDAO tournamentDAO;
    private Tournament testTournament;
    private final List<Integer> createdTeamIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        fixtureService = new FixtureService();
        matchDAO = new MatchDAOImpl();
        teamDAO = new TeamDAOImpl();
        tournamentDAO = new TournamentDAOImpl();

        // Create temporary tournament
        testTournament = new Tournament();
        testTournament.setTitle("Fixture Test Cup " + System.currentTimeMillis());
        testTournament.setGame("Valorant");
        testTournament.setFormat("SINGLE_ELIMINATION");
        testTournament.setMaxTeams(16);
        testTournament.setMinTeams(2);
        testTournament.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(2)));
        testTournament.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
        testTournament.setStatus("REGISTRATION_CLOSED");
        testTournament.setOrganizer("ESports QA System");
        tournamentDAO.create(testTournament);
    }

    @AfterEach
    void tearDown() {
        if (testTournament != null && testTournament.getId() > 0) {
            matchDAO.deleteByTournament(testTournament.getId());
            tournamentDAO.delete(testTournament.getId());
        }
        for (int tid : createdTeamIds) {
            teamDAO.delete(tid);
        }
        createdTeamIds.clear();
    }

    private List<Team> getOrCreateTestTeams(int count) {
        List<Team> teams = new ArrayList<>(teamDAO.findAll());
        int needed = count - teams.size();
        for (int i = 0; i < needed; i++) {
            Team t = new Team();
            t.setTeamName("QA Seed Team " + System.currentTimeMillis() + "_" + i);
            t.setTeamTag("Q" + (System.currentTimeMillis() % 1000) + "_" + i);
            t.setGame("Valorant");
            t.setLeaderId(1); // admin user
            t.setStatus("ACTIVE");
            teamDAO.create(t, 1);
            createdTeamIds.add(t.getId());
            teams.add(t);
        }
        return new ArrayList<>(teams.subList(0, count));
    }

    @Test
    @DisplayName("Should generate Single Elimination full bracket tree for 4 teams")
    void testSingleEliminationPowerOfTwo() {
        List<Team> teams = getOrCreateTestTeams(4);
        FixtureService.GenerationResult result = fixtureService.generateSingleElimination(
                testTournament.getId(), teams, LocalDateTime.now().plusDays(2));

        assertTrue(result.isSuccess(), "Single elimination generation should succeed: " + result.getMessage());
        assertEquals(3, result.getMatchesCount(), "4 teams in single elimination should produce 2 semifinals + 1 grand final = 3 matches");

        List<Match> generated = matchDAO.findByTournament(testTournament.getId());
        assertEquals(3, generated.size());
        
        // Find Grand Final and Semifinals
        Match grandFinal = generated.stream().filter(m -> "GRAND_FINAL".equalsIgnoreCase(m.getBracketType())).findFirst().orElse(null);
        assertNotNull(grandFinal, "Bracket tree must contain Grand Final");

        List<Match> semis = generated.stream().filter(m -> "KNOCKOUT".equalsIgnoreCase(m.getBracketType())).toList();
        assertEquals(2, semis.size(), "Should have 2 semifinal matches");
        
        // Both semifinals should link to the Grand Final
        for (Match semi : semis) {
            assertEquals(grandFinal.getId(), semi.getNextMatchId(), "Semifinal next_match_id must point to Grand Final");
            assertTrue(semi.getNextMatchSlot() == 1 || semi.getNextMatchSlot() == 2, "Next match slot must be 1 or 2");
        }
    }

    @Test
    @DisplayName("Should generate Single Elimination bracket tree with BYEs for 5 teams")
    void testSingleEliminationWithByes() {
        List<Team> teams = getOrCreateTestTeams(5);
        FixtureService.GenerationResult result = fixtureService.generateSingleElimination(
                testTournament.getId(), teams, LocalDateTime.now().plusDays(2));

        assertTrue(result.isSuccess());
        // For 5 teams (bracket size 8), 3 quarterfinal matches are played (with 3 byes) + 2 semifinals + 1 grand final = 6 matches
        assertEquals(6, result.getMatchesCount(), "5 teams should generate complete 6-match bracket tree with BYEs");

        List<Match> generated = matchDAO.findByTournament(testTournament.getId());
        assertEquals(6, generated.size());
    }

    @Test
    @DisplayName("Should generate Round Robin schedule with Berger algorithm (4 teams)")
    void testRoundRobinEvenTeams() {
        List<Team> teams = getOrCreateTestTeams(4);
        FixtureService.GenerationResult result = fixtureService.generateFixtures(
                testTournament.getId(), "ROUND_ROBIN", teams, LocalDateTime.now().plusDays(1));

        assertTrue(result.isSuccess(), "Round robin should succeed: " + result.getMessage());
        // For 4 teams, 3 rounds * 2 matches/round = 6 total matches
        assertEquals(6, result.getMatchesCount(), "4 teams in round robin should play 6 total matches");

        List<Match> generated = matchDAO.findByTournament(testTournament.getId());
        assertEquals(6, generated.size());

        for (Match m : generated) {
            assertEquals("GROUP", m.getBracketType());
            assertTrue(m.getTeam1Id() > 0);
            assertTrue(m.getTeam2Id() > 0);
            assertNotEquals(m.getTeam1Id(), m.getTeam2Id(), "A team cannot play itself");
        }
    }

    @Test
    @DisplayName("Should generate Round Robin schedule with BYE rounds for odd teams (3 teams)")
    void testRoundRobinOddTeams() {
        List<Team> teams = getOrCreateTestTeams(3);
        FixtureService.GenerationResult result = fixtureService.generateFixtures(
                testTournament.getId(), "ROUND_ROBIN", teams, LocalDateTime.now().plusDays(1));

        assertTrue(result.isSuccess());
        // For 3 teams, total matches = 3 * 2 / 2 = 3 matches (each team plays 2 matches, 1 BYE per round)
        assertEquals(3, result.getMatchesCount(), "3 teams in round robin should play 3 total matches");

        List<Match> generated = matchDAO.findByTournament(testTournament.getId());
        assertEquals(3, generated.size());
    }

    @Test
    @DisplayName("Should generate Double Elimination tournament with Upper and Lower brackets (4 teams)")
    void testDoubleElimination() {
        List<Team> teams = getOrCreateTestTeams(4);
        FixtureService.GenerationResult result = fixtureService.generateFixtures(
                testTournament.getId(), "DOUBLE_ELIMINATION", teams, LocalDateTime.now().plusDays(1));

        assertTrue(result.isSuccess(), "Double elimination should succeed: " + result.getMessage());
        assertEquals(5, result.getMatchesCount(), "Double elimination for 4 teams requires 5 matches (Winners, Losers, Grand Final)");

        List<Match> generated = matchDAO.findByTournament(testTournament.getId());
        assertTrue(generated.stream().anyMatch(m -> "WINNERS".equalsIgnoreCase(m.getBracketType())), "Should have WINNERS bracket matches");
        assertTrue(generated.stream().anyMatch(m -> "LOSERS".equalsIgnoreCase(m.getBracketType())), "Should have LOSERS bracket matches");
        assertTrue(generated.stream().anyMatch(m -> "GRAND_FINAL".equalsIgnoreCase(m.getBracketType())), "Should have GRAND_FINAL match");
    }

    @Test
    @DisplayName("Should generate Group Stage + Knockout fixtures (8 teams)")
    void testGroupStageKnockout() {
        List<Team> teams = getOrCreateTestTeams(8);
        FixtureService.GenerationResult result = fixtureService.generateFixtures(
                testTournament.getId(), "GROUP_STAGE_KNOCKOUT", teams, LocalDateTime.now().plusDays(1));

        assertTrue(result.isSuccess(), "Group stage + knockout generation should succeed");
        // 8 teams divided into 2 groups of 4 teams.
        // Each group of 4 plays 6 matches -> 12 group matches.
        // Plus 3 playoff matches (Semifinals + Finals) -> 15 matches total.
        assertTrue(result.getMatchesCount() >= 12, "Group stage should generate group matches plus playoff brackets");

        List<Match> generated = matchDAO.findByTournament(testTournament.getId());
        assertTrue(generated.stream().anyMatch(m -> "GROUP".equalsIgnoreCase(m.getBracketType())));
        assertTrue(generated.stream().anyMatch(m -> "KNOCKOUT".equalsIgnoreCase(m.getBracketType()) || "PLAYOFF".equalsIgnoreCase(m.getBracketType())));
    }
}
