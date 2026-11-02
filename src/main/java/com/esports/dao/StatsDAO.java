package com.esports.dao;

import java.util.Map;
import java.util.List;

public interface StatsDAO {

    record GamePopularityStat(String gameName, int tournamentCount, int teamCount, int playerCount) {}
    record TeamPointsStat(String teamName, int totalPoints, int matchesPlayed, int matchesWon, int matchesLost) {}
    record PlayerKillsStat(String gamerTag, String teamName, int kills, int mvpCount) {}

    Map<String, Integer> getOverallMetrics();
    Map<String, Integer> getTournamentsByStatus();
    Map<String, Integer> getTournamentsByGame();
    Map<String, Integer> getTeamsByGame();
    Map<String, Integer> getTopTeamsByPoints(int limit);
    Map<String, Integer> getTopPlayersByKills(int limit);
    Map<String, Integer> getTopPlayersByMvp(int limit);
    List<String> getRecentActivityLogs(int limit);

    List<GamePopularityStat> getGamePopularityStats();
    List<TeamPointsStat> getTopTeamsStats(int limit);
    List<PlayerKillsStat> getTopPlayersStats(int limit);
}
