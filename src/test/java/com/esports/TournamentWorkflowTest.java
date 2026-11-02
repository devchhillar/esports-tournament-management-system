package com.esports;

import com.esports.dao.TournamentDAO;
import com.esports.dao.TournamentDAOImpl;
import com.esports.model.Tournament;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comprehensive 10-Step Tournament Workflow & Regression Test Suite")
public class TournamentWorkflowTest {

    private TournamentDAO tournamentDAO;
    private Tournament testTournament;

    @BeforeEach
    void setUp() {
        tournamentDAO = new TournamentDAOImpl();
    }

    @AfterEach
    void tearDown() {
        if (testTournament != null && testTournament.getId() > 0) {
            tournamentDAO.delete(testTournament.getId());
        }
    }

    @Test
    @DisplayName("Step 1 to 10: Mandatory Full Tournament Lifecycle Verification")
    public void testCompleteTournamentWorkflow() {
        // Step 1: Initial state - Ensure default search returns all existing tournaments
        List<Tournament> initialList = tournamentDAO.search("", "All Games", "All Statuses");
        assertNotNull(initialList, "Default search list must not be null");
        int initialCount = initialList.size();
        System.out.println("Step 1: Initial tournaments in DB with default filters: " + initialCount);

        // Step 2 & 3: New Tournament Creation & Save
        String uniqueTitle = "Apex Championship Series " + System.currentTimeMillis();
        testTournament = new Tournament();
        testTournament.setTitle(uniqueTitle);
        testTournament.setGame("Valorant");
        testTournament.setFormat("DOUBLE_ELIMINATION");
        testTournament.setMinTeams(4);
        testTournament.setMaxTeams(16);
        testTournament.setPrizePool(new BigDecimal("75000.00"));
        testTournament.setEntryFee(new BigDecimal("750.00"));
        testTournament.setFirstPrize(new BigDecimal("45000.00"));
        testTournament.setSecondPrize(new BigDecimal("20000.00"));
        testTournament.setThirdPrize(new BigDecimal("10000.00"));
        testTournament.setOrganizer("Apex Global League");
        testTournament.setStatus("REGISTRATION_OPEN");
        testTournament.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(2)));
        testTournament.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
        testTournament.setCreatedBy(1);

        // Save
        boolean created = tournamentDAO.create(testTournament);
        assertTrue(created, "Tournament creation must return true");
        assertTrue(testTournament.getId() > 0, "Created tournament must have a generated positive ID");
        int createdId = testTournament.getId();
        System.out.println("Step 3: Created tournament with ID: #" + createdId);

        // Step 4 & 5: Verify it appears immediately in the table with default filters ("All Games", "All Statuses")
        List<Tournament> afterCreation = tournamentDAO.search("", "All Games", "All Statuses");
        assertEquals(initialCount + 1, afterCreation.size(), "Table row count must increase by 1 after creation");
        boolean foundInDefault = afterCreation.stream().anyMatch(t -> t.getId() == createdId);
        assertTrue(foundInDefault, "Newly created tournament MUST appear in the table with default 'All Games' & 'All Statuses'");

        // Step 6: Verify persistence across independent queries
        Tournament fetched = tournamentDAO.findById(createdId);
        assertNotNull(fetched, "Tournament must persist in MySQL database");
        assertEquals(uniqueTitle, fetched.getTitle());
        assertEquals("Valorant", fetched.getGame());
        assertEquals("DOUBLE_ELIMINATION", fetched.getFormat());
        assertEquals(0, new BigDecimal("75000.00").compareTo(fetched.getPrizePool()));
        assertEquals(0, new BigDecimal("750.00").compareTo(fetched.getEntryFee()));
        assertEquals("REGISTRATION_OPEN", fetched.getStatus());
        assertEquals("Apex Global League", fetched.getOrganizer());
        System.out.println("Step 6: Verified persistent storage in MySQL database.");

        // Step 7: Search by Title and Search by ID
        List<Tournament> searchByTitle = tournamentDAO.search(uniqueTitle, "All Games", "All Statuses");
        assertEquals(1, searchByTitle.size(), "Search by exact title should return the tournament");
        assertEquals(createdId, searchByTitle.get(0).getId());

        List<Tournament> searchById = tournamentDAO.search(String.valueOf(createdId), "All Games", "All Statuses");
        assertEquals(1, searchById.size(), "Search by ID should return the tournament");
        assertEquals(createdId, searchById.get(0).getId());
        System.out.println("Step 7: Search by Title & ID successful.");

        // Step 8: Apply Game and Status filters
        List<Tournament> filterGame = tournamentDAO.search("", "Valorant", "All Statuses");
        assertTrue(filterGame.stream().anyMatch(t -> t.getId() == createdId), "Tournament should match 'Valorant' game filter");

        List<Tournament> filterWrongGame = tournamentDAO.search("", "BGMI", "All Statuses");
        assertFalse(filterWrongGame.stream().anyMatch(t -> t.getId() == createdId), "Tournament must NOT appear under 'BGMI'");

        List<Tournament> filterStatus = tournamentDAO.search("", "All Games", "REGISTRATION_OPEN");
        assertTrue(filterStatus.stream().anyMatch(t -> t.getId() == createdId), "Tournament should match 'REGISTRATION_OPEN' status");

        List<Tournament> filterBoth = tournamentDAO.search("", "Valorant", "REGISTRATION_OPEN");
        assertTrue(filterBoth.stream().anyMatch(t -> t.getId() == createdId), "Tournament should match combined Game + Status filter");
        System.out.println("Step 8: Filter tests by Game and Status successful.");

        // Step 9: Edit the tournament and verify updated information
        fetched.setTitle("Apex Masters Series Season 2 - Updated");
        fetched.setGame("Counter-Strike 2");
        fetched.setFormat("GROUP_STAGE_KNOCKOUT");
        fetched.setPrizePool(new BigDecimal("120000.00"));
        fetched.setEntryFee(new BigDecimal("1200.00"));
        fetched.setMaxTeams(32);
        fetched.setStatus("ONGOING");
        fetched.setOrganizer("Valve & Apex Arena");

        boolean updated = tournamentDAO.update(fetched);
        assertTrue(updated, "Update tournament must return true");

        Tournament reFetched = tournamentDAO.findById(createdId);
        assertNotNull(reFetched);
        assertEquals("Apex Masters Series Season 2 - Updated", reFetched.getTitle());
        assertEquals("Counter-Strike 2", reFetched.getGame());
        assertEquals("GROUP_STAGE_KNOCKOUT", reFetched.getFormat());
        assertEquals(0, new BigDecimal("120000.00").compareTo(reFetched.getPrizePool()));
        assertEquals(32, reFetched.getMaxTeams());
        assertEquals("ONGOING", reFetched.getStatus());
        assertEquals("Valve & Apex Arena", reFetched.getOrganizer());
        System.out.println("Step 9: Tournament update verified successfully.");

        // Step 10: Delete the test tournament and verify its removal
        boolean deleted = tournamentDAO.delete(createdId);
        assertTrue(deleted, "Delete tournament must return true");

        Tournament afterDelete = tournamentDAO.findById(createdId);
        assertNull(afterDelete, "Tournament must not exist in database after deletion");

        List<Tournament> finalList = tournamentDAO.search("", "All Games", "All Statuses");
        assertEquals(initialCount, finalList.size(), "Row count must return to initialCount after deletion");
        assertFalse(finalList.stream().anyMatch(t -> t.getId() == createdId), "Deleted tournament must not appear in table");
        System.out.println("Step 10: Deletion and table cleanup verified successfully!");

        // Clear pointer so tearDown doesn't attempt redundant delete
        testTournament = null;
    }
}
