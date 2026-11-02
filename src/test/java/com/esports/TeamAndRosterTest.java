package com.esports;

import com.esports.dao.*;
import com.esports.model.Player;
import com.esports.model.Role;
import com.esports.model.Team;
import com.esports.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Team Management and Roster System Test Suite")
public class TeamAndRosterTest {

    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        teamDAO = new TeamDAOImpl();
        playerDAO = new PlayerDAOImpl();
        userDAO = new UserDAOImpl();
    }

    @Test
    @DisplayName("Should retrieve existing seed teams")
    void testFindTeams() {
        List<Team> allTeams = teamDAO.findAll();
        assertNotNull(allTeams);
        assertFalse(allTeams.isEmpty(), "Seed teams should be present in database");

        Team firstTeam = allTeams.get(0);
        assertTrue(firstTeam.getId() > 0);
        assertNotNull(firstTeam.getTeamName());
        assertNotNull(firstTeam.getGame());

        Team byId = teamDAO.findById(firstTeam.getId());
        assertNotNull(byId);
        assertEquals(firstTeam.getTeamName(), byId.getTeamName());
    }

    @Test
    @DisplayName("Should verify max roster limits and player game compatibility")
    void testRosterLimitsAndEligibility() {
        // Find existing team (e.g., Soul or GodLike for BGMI)
        List<Team> bgmiTeams = teamDAO.findByGame("BGMI");
        assertFalse(bgmiTeams.isEmpty(), "At least one BGMI team should exist");
        Team bgmiTeam = bgmiTeams.get(0);

        int maxRosterSize = teamDAO.getMaxRosterSize(bgmiTeam.getId());
        assertEquals(5, maxRosterSize, "BGMI default max team size should be 5 (4 active + 1 sub)");

        int currentMembers = teamDAO.getMemberCount(bgmiTeam.getId());
        assertTrue(currentMembers >= 0 && currentMembers <= maxRosterSize);

        // Find or create a test player with Valorant as primary game
        User valorantUser = new User();
        String valUsername = "val_player_" + System.currentTimeMillis();
        valorantUser.setUsername(valUsername);
        valorantUser.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
        valorantUser.setEmail(valUsername + "@test.com");
        valorantUser.setFullName("Valorant Player QA");
        valorantUser.setRole(Role.PLAYER);
        userDAO.createUser(valorantUser);

        Player valPlayer = new Player();
        valPlayer.setUserId(valorantUser.getId());
        valPlayer.setGamerTag("Val_" + (System.currentTimeMillis() % 100000));
        valPlayer.setPrimaryGame("Valorant");
        valPlayer.setInGameRole("DUELIST");
        playerDAO.createPlayer(valPlayer);

        // Player with primary game "Valorant" should NOT be eligible to join "BGMI" team
        boolean isEligible = teamDAO.isPlayerEligibleForTeam(valPlayer.getId(), bgmiTeam.getId());
        assertFalse(isEligible, "Valorant player must NOT be eligible to join a BGMI team due to game mismatch");

        // Attempting to add member directly must fail
        boolean added = teamDAO.addMember(bgmiTeam.getId(), valPlayer.getId(), "ENTRY");
        assertFalse(added, "addMember must return false for incompatible game");
    }

    @Test
    @DisplayName("Should auto-enroll creator as captain, add teammate, and remove team member correctly")
    void testRosterLifecycle() {
        // Create leader user + player compatible with BGMI
        User leaderUser = new User();
        String leaderUname = "bgmi_leader_" + System.currentTimeMillis();
        leaderUser.setUsername(leaderUname);
        leaderUser.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
        leaderUser.setEmail(leaderUname + "@test.com");
        leaderUser.setFullName("BGMI QA Leader");
        leaderUser.setRole(Role.PLAYER);
        userDAO.createUser(leaderUser);

        Player leaderPlayer = new Player();
        leaderPlayer.setUserId(leaderUser.getId());
        leaderPlayer.setGamerTag("Cap_" + (System.currentTimeMillis() % 100000));
        leaderPlayer.setPrimaryGame("BGMI");
        leaderPlayer.setInGameRole("IGL");
        playerDAO.createPlayer(leaderPlayer);

        // Create a new test team for BGMI - leader player should be automatically enrolled as Captain
        Team newTeam = new Team();
        newTeam.setTeamName("QA Esports " + System.currentTimeMillis());
        newTeam.setTeamTag("Q" + (System.currentTimeMillis() % 100000));
        newTeam.setGame("BGMI");
        newTeam.setLeaderId(leaderUser.getId());
        newTeam.setStatus("ACTIVE");

        boolean teamCreated = teamDAO.create(newTeam, leaderUser.getId());
        assertTrue(teamCreated, "Team creation should succeed");
        assertTrue(newTeam.getId() > 0);

        // Verify leader was automatically enrolled as Captain
        assertEquals(1, teamDAO.getMemberCount(newTeam.getId()), "Leader should be auto-enrolled as captain");

        // Create a teammate player
        User teammateUser = new User();
        String mateUname = "bgmi_mate_" + System.currentTimeMillis();
        teammateUser.setUsername(mateUname);
        teammateUser.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
        teammateUser.setEmail(mateUname + "@test.com");
        teammateUser.setFullName("BGMI Teammate");
        teammateUser.setRole(Role.PLAYER);
        userDAO.createUser(teammateUser);

        Player teammatePlayer = new Player();
        teammatePlayer.setUserId(teammateUser.getId());
        teammatePlayer.setGamerTag("Mate_" + (System.currentTimeMillis() % 100000));
        teammatePlayer.setPrimaryGame("BGMI");
        teammatePlayer.setInGameRole("ASSAULTER");
        playerDAO.createPlayer(teammatePlayer);

        // Add teammate to team
        boolean added = teamDAO.addMember(newTeam.getId(), teammatePlayer.getId(), "MEMBER");
        assertTrue(added, "Teammate with matching game should be added successfully");
        assertEquals(2, teamDAO.getMemberCount(newTeam.getId()), "Roster should now have 2 members");

        // Assign teammate as new captain
        boolean captainAssigned = teamDAO.assignCaptain(newTeam.getId(), teammatePlayer.getId());
        assertTrue(captainAssigned, "Captain assignment should succeed");

        // Remove teammate
        boolean removed = teamDAO.removeMember(newTeam.getId(), teammatePlayer.getId());
        assertTrue(removed, "Teammate removal should succeed");
        assertEquals(1, teamDAO.getMemberCount(newTeam.getId()), "Roster count should be 1 after removal");

        // Clean up test team
        teamDAO.delete(newTeam.getId());
    }
}
