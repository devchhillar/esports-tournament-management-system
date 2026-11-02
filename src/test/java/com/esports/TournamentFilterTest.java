package com.esports;

import com.esports.dao.TournamentDAO;
import com.esports.dao.TournamentDAOImpl;
import com.esports.model.Tournament;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TournamentFilterTest {

    @Test
    public void testSearchWithDefaultFilters() {
        TournamentDAO dao = new TournamentDAOImpl();
        
        Tournament t = new Tournament();
        t.setTitle("Default Filter Test Cup " + System.currentTimeMillis());
        t.setGame("Valorant");
        t.setFormat("SINGLE_ELIMINATION");
        t.setMaxTeams(16);
        t.setMinTeams(2);
        t.setStartDate(Timestamp.valueOf(LocalDateTime.now().plusDays(2)));
        t.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
        t.setStatus("UPCOMING");
        t.setOrganizer("Test Org");
        assertTrue(dao.create(t));
        
        try {
            // Test 1: What AdminTournamentsPanel passes: "All Games" and "All Statuses"
            List<Tournament> currentDefault = dao.search("", "All Games", "All Statuses");
            System.out.println("Result with ('', 'All Games', 'All Statuses'): " + currentDefault.size());
            assertFalse(currentDefault.isEmpty(), "Tournaments should be found with 'All Games' and 'All Statuses'");
            
            // Test 2: What findAll() returns
            List<Tournament> all = dao.findAll();
            System.out.println("Result with findAll(): " + all.size());
            assertEquals(all.size(), currentDefault.size(), "Default search should return the exact same count as findAll()");
            
            // Test 3: What search with ALL returns
            List<Tournament> searchAll = dao.search("", "ALL", "ALL");
            System.out.println("Result with ('', 'ALL', 'ALL'): " + searchAll.size());
            assertEquals(all.size(), searchAll.size());

            // Test 4: Search by ID
            List<Tournament> searchById = dao.search(String.valueOf(t.getId()), "All Games", "All Statuses");
            assertEquals(1, searchById.size(), "Should find tournament by ID");
            assertEquals(t.getId(), searchById.get(0).getId());

            // Test 5: Search by Game filter
            List<Tournament> searchByGame = dao.search("", "Valorant", "All Statuses");
            assertFalse(searchByGame.isEmpty(), "Should find tournaments for Valorant");
            for (Tournament item : searchByGame) {
                assertEquals("Valorant", item.getGame());
            }

            // Test 6: Search by Status filter
            List<Tournament> searchByStatus = dao.search("", "All Games", "UPCOMING");
            assertFalse(searchByStatus.isEmpty(), "Should find tournaments for UPCOMING status");
            for (Tournament item : searchByStatus) {
                assertEquals("UPCOMING", item.getStatus());
            }
        } finally {
            dao.delete(t.getId());
        }
    }
}
