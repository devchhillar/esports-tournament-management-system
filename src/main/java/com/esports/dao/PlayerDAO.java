package com.esports.dao;

import com.esports.model.GameCategory;
import com.esports.model.Player;
import java.util.List;

public interface PlayerDAO {
    Player findByUserId(int userId);
    Player findById(int id);
    List<Player> findAll();
    List<Player> getLeaderboard(String game, int limit);
    List<Player> getLeaderboard(GameCategory game, int limit);
    List<Player> findByTeamId(int teamId);
    List<Player> findAvailablePlayers(String game);
    List<Player> findTopKillers(int limit);
    List<Player> findTopMvps(int limit);
    boolean createPlayer(Player player);
    boolean updateProfile(Player player);
    boolean updateRankingAndStats(int playerId, int pointsDelta, boolean won);
    boolean updateStats(int playerId, int matchesDelta, int winsDelta, int lossesDelta, int killsDelta, int mvpDelta, int pointsDelta);
    int countTotal();
}
