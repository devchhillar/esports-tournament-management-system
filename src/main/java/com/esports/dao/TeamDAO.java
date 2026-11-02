package com.esports.dao;

import com.esports.model.GameCategory;
import com.esports.model.Team;
import java.util.List;

public interface TeamDAO {
    boolean create(Team team, int creatorUserId);
    boolean update(Team team);
    boolean delete(int id);
    Team findById(int id);
    List<Team> findByLeaderId(int leaderId);
    List<Team> findByManagerId(int managerId);
    List<Team> findByUserId(int userId);
    List<Team> findAll();
    List<Team> findByGame(String game);
    List<Team> findByGame(GameCategory game);
    
    // Roster Membership
    boolean addMember(int teamId, int playerId, String role);
    boolean removeMember(int teamId, int playerId);
    boolean assignCaptain(int teamId, int playerId);
    boolean assignManager(int teamId, int managerUserId);
    int getMemberCount(int teamId);
    int getMaxRosterSize(int teamId);
    boolean isPlayerEligibleForTeam(int playerId, int teamId);
    
    int countTotal();
}
