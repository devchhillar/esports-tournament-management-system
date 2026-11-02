package com.esports;

import com.esports.dao.GameDAO;
import com.esports.dao.GameDAOImpl;
import com.esports.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game Management Test Suite")
public class GameManagementTest {

    private GameDAO gameDAO;

    @BeforeEach
    void setUp() {
        gameDAO = new GameDAOImpl();
    }

    @Test
    @DisplayName("Should retrieve default esports games")
    void testGetDefaultGames() {
        List<Game> games = gameDAO.findAll();
        assertNotNull(games, "Game list should not be null");
        assertFalse(games.isEmpty(), "Default games should be present in database");

        // Verify key default games exist
        boolean hasBGMI = games.stream().anyMatch(g -> g.getName().equalsIgnoreCase("BGMI"));
        boolean hasValorant = games.stream().anyMatch(g -> g.getName().equalsIgnoreCase("Valorant"));
        boolean hasCS2 = games.stream().anyMatch(g -> g.getName().contains("Counter-Strike 2") || g.getName().contains("CS2") || g.getName().contains("Counter-Strike"));

        assertTrue(hasBGMI, "Default game BGMI should be present");
        assertTrue(hasValorant, "Default game Valorant should be present");
        assertTrue(hasCS2, "Default game CS2 should be present");
    }

    @Test
    @DisplayName("Should find game by ID")
    void testFindGameById() {
        List<Game> games = gameDAO.findAll();
        assertFalse(games.isEmpty());

        Game first = games.get(0);
        Game found = gameDAO.findById(first.getId());
        assertNotNull(found, "Game should be found by its primary key ID");
        assertEquals(first.getName(), found.getName());
        assertTrue(found.getMinTeamSize() > 0, "Min team size should be positive");
        assertTrue(found.getMaxTeamSize() >= found.getMinTeamSize(), "Max team size should be >= min team size");
    }

    @Test
    @DisplayName("Should perform full CRUD on a new custom game")
    void testGameCrud() {
        String uniqueGameName = "Rocket League Arena " + System.currentTimeMillis();

        Game customGame = new Game();
        customGame.setName(uniqueGameName);
        customGame.setCategory("SPORTS / ACTION");
        customGame.setDescription("High-powered hybrid of arcade-style soccer and vehicular mayhem.");
        customGame.setMinTeamSize(3);
        customGame.setMaxTeamSize(4);
        customGame.setStatus("ACTIVE");

        boolean created = gameDAO.create(customGame);
        assertTrue(created, "Game creation should succeed");
        assertTrue(customGame.getId() > 0, "Created game must return valid positive ID");

        // Verify retrieval
        Game retrieved = gameDAO.findById(customGame.getId());
        assertNotNull(retrieved);
        assertEquals(uniqueGameName, retrieved.getName());
        assertEquals(3, retrieved.getMinTeamSize());
        assertEquals(4, retrieved.getMaxTeamSize());

        // Update
        retrieved.setDescription("Updated description for esports competitive mode.");
        retrieved.setMaxTeamSize(5);
        boolean updateResult = gameDAO.update(retrieved);
        assertTrue(updateResult, "Game update should return true");

        Game updated = gameDAO.findById(customGame.getId());
        assertNotNull(updated);
        assertEquals("Updated description for esports competitive mode.", updated.getDescription());
        assertEquals(5, updated.getMaxTeamSize());

        // Delete
        boolean deleteResult = gameDAO.delete(customGame.getId());
        assertTrue(deleteResult, "Game delete should return true");

        Game deleted = gameDAO.findById(customGame.getId());
        assertNull(deleted, "Game should be deleted and return null");
    }
}
