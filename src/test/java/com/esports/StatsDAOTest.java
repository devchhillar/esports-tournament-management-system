package com.esports;

import com.esports.config.DBConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatsDAOTest {

    @Test
    public void testAllQueries() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            // 1. Tournaments by Status
            System.out.println("--- 1. TOURNAMENTS BY STATUS ---");
            try (ResultSet rs = st.executeQuery("SELECT status, COUNT(*) AS count FROM tournaments GROUP BY status ORDER BY count DESC")) {
                while (rs.next()) {
                    System.out.println("Status: " + rs.getString("status") + " -> " + rs.getInt("count"));
                }
            }

            // 2. Game Popularity
            System.out.println("\n--- 2. GAME POPULARITY ---");
            String gameSql = "SELECT all_games.game AS game_name, " +
                    "COUNT(DISTINCT t.id) AS tourney_count, " +
                    "COUNT(DISTINCT tm.id) AS team_count, " +
                    "COUNT(DISTINCT p.id) AS player_count " +
                    "FROM (" +
                    "    SELECT DISTINCT game FROM tournaments WHERE game IS NOT NULL " +
                    "    UNION " +
                    "    SELECT name AS game FROM games WHERE name IS NOT NULL " +
                    ") all_games " +
                    "LEFT JOIN tournaments t ON LOWER(all_games.game) = LOWER(t.game) " +
                    "LEFT JOIN teams tm ON LOWER(all_games.game) = LOWER(tm.game) " +
                    "LEFT JOIN players p ON LOWER(all_games.game) = LOWER(p.primary_game) " +
                    "GROUP BY all_games.game " +
                    "ORDER BY tourney_count DESC, team_count DESC, all_games.game ASC";
            try (ResultSet rs = st.executeQuery(gameSql)) {
                while (rs.next()) {
                    System.out.println(rs.getString("game_name") + " -> Tourneys=" + rs.getInt("tourney_count") + ", Teams=" + rs.getInt("team_count") + ", Players=" + rs.getInt("player_count"));
                }
            }

            // 3. Top Teams
            System.out.println("\n--- 3. TOP TEAMS ---");
            String teamSql = "SELECT team_name, total_points, matches_played, matches_won, matches_lost, ranking_points " +
                    "FROM teams ORDER BY total_points DESC, ranking_points DESC LIMIT 5";
            try (ResultSet rs = st.executeQuery(teamSql)) {
                while (rs.next()) {
                    System.out.println("Team: " + rs.getString("team_name") + " | Pts: " + rs.getInt("total_points") + " | Played: " + rs.getInt("matches_played") + " | Won: " + rs.getInt("matches_won"));
                }
            }

            // 4. Top Players (Kills)
            System.out.println("\n--- 4. TOP PLAYERS (KILLS) ---");
            String playerSql = "SELECT p.gamer_tag, COALESCE(t.team_name, 'Free Agent') AS team_name, p.kills " +
                    "FROM players p LEFT JOIN teams t ON p.current_team_id = t.id " +
                    "ORDER BY p.kills DESC LIMIT 5";
            try (ResultSet rs = st.executeQuery(playerSql)) {
                while (rs.next()) {
                    System.out.println("Player: " + rs.getString("gamer_tag") + " (" + rs.getString("team_name") + ") -> Kills: " + rs.getInt("kills"));
                }
            }
        }
    }
}
