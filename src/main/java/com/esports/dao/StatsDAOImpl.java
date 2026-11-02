package com.esports.dao;

import com.esports.config.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsDAOImpl implements StatsDAO {

    private static final Logger logger = LoggerFactory.getLogger(StatsDAOImpl.class);

    @Override
    public Map<String, Integer> getOverallMetrics() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            map.put("totalTournaments", getCount(conn, "SELECT COUNT(*) FROM tournaments"));
            map.put("activeTournaments", getCount(conn, "SELECT COUNT(*) FROM tournaments WHERE status IN ('ONGOING', 'REGISTRATION_OPEN')"));
            map.put("upcomingTournaments", getCount(conn, "SELECT COUNT(*) FROM tournaments WHERE status = 'UPCOMING'"));
            map.put("completedTournaments", getCount(conn, "SELECT COUNT(*) FROM tournaments WHERE status = 'COMPLETED'"));
            
            map.put("totalTeams", getCount(conn, "SELECT COUNT(*) FROM teams"));
            map.put("totalPlayers", getCount(conn, "SELECT COUNT(*) FROM players"));
            
            map.put("totalMatches", getCount(conn, "SELECT COUNT(*) FROM matches"));
            map.put("matchesCompleted", getCount(conn, "SELECT COUNT(*) FROM matches WHERE status = 'COMPLETED'"));
            map.put("matchesRemaining", getCount(conn, "SELECT COUNT(*) FROM matches WHERE status IN ('SCHEDULED', 'LIVE')"));
            map.put("pendingRegistrations", getCount(conn, "SELECT COUNT(*) FROM registrations WHERE status = 'PENDING'"));
        } catch (SQLException e) {
            logger.error("Error fetching overall metrics: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public Map<String, Integer> getTournamentsByStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM tournaments WHERE status IS NOT NULL AND TRIM(status) != '' GROUP BY status ORDER BY count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Error getting tournaments by status: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public Map<String, Integer> getTournamentsByGame() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT game, COUNT(*) AS count FROM tournaments GROUP BY game ORDER BY count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("game"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Error getting tournaments by game: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public Map<String, Integer> getTeamsByGame() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT game, COUNT(*) AS count FROM teams GROUP BY game ORDER BY count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("game"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.error("Error getting teams by game: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public Map<String, Integer> getTopTeamsByPoints(int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT team_name, total_points FROM teams ORDER BY total_points DESC, ranking_points DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("team_name"), rs.getInt("total_points"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting top teams: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public Map<String, Integer> getTopPlayersByKills(int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT gamer_tag, kills FROM players ORDER BY kills DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("gamer_tag"), rs.getInt("kills"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting top players by kills: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public Map<String, Integer> getTopPlayersByMvp(int limit) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT gamer_tag, mvp_count FROM players ORDER BY mvp_count DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("gamer_tag"), rs.getInt("mvp_count"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting top players by MVP: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public List<GamePopularityStat> getGamePopularityStats() {
        List<GamePopularityStat> list = new ArrayList<>();
        String sql = "SELECT all_games.game AS game_name, " +
                     "COUNT(DISTINCT t.id) AS tourney_count, " +
                     "COUNT(DISTINCT tm.id) AS team_count, " +
                     "COUNT(DISTINCT p.id) AS player_count " +
                     "FROM (" +
                     "    SELECT DISTINCT game FROM tournaments WHERE game IS NOT NULL AND TRIM(game) != '' " +
                     "    UNION " +
                     "    SELECT name AS game FROM games WHERE name IS NOT NULL AND TRIM(name) != '' " +
                     ") all_games " +
                     "LEFT JOIN tournaments t ON LOWER(all_games.game) = LOWER(t.game) " +
                     "LEFT JOIN teams tm ON LOWER(all_games.game) = LOWER(tm.game) " +
                     "LEFT JOIN players p ON LOWER(all_games.game) = LOWER(p.primary_game) " +
                     "GROUP BY all_games.game " +
                     "HAVING tourney_count > 0 OR team_count > 0 OR player_count > 0 " +
                     "ORDER BY tourney_count DESC, team_count DESC, player_count DESC, all_games.game ASC " +
                     "LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new GamePopularityStat(
                        rs.getString("game_name"),
                        rs.getInt("tourney_count"),
                        rs.getInt("team_count"),
                        rs.getInt("player_count")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error getting game popularity stats: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TeamPointsStat> getTopTeamsStats(int limit) {
        List<TeamPointsStat> list = new ArrayList<>();
        String sql = "SELECT team_name, total_points, matches_played, matches_won, matches_lost " +
                     "FROM teams ORDER BY total_points DESC, ranking_points DESC, matches_won DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TeamPointsStat(
                            rs.getString("team_name"),
                            rs.getInt("total_points"),
                            rs.getInt("matches_played"),
                            rs.getInt("matches_won"),
                            rs.getInt("matches_lost")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting top teams stats: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<PlayerKillsStat> getTopPlayersStats(int limit) {
        List<PlayerKillsStat> list = new ArrayList<>();
        String sql = "SELECT p.gamer_tag, COALESCE(t.team_name, 'Free Agent') AS team_name, p.kills, p.mvp_count " +
                     "FROM players p " +
                     "LEFT JOIN teams t ON p.current_team_id = t.id " +
                     "WHERE p.kills > 0 " +
                     "ORDER BY p.kills DESC, p.mvp_count DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PlayerKillsStat(
                            rs.getString("gamer_tag"),
                            rs.getString("team_name"),
                            rs.getInt("kills"),
                            rs.getInt("mvp_count")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting top players stats: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<String> getRecentActivityLogs(int limit) {
        List<String> list = new ArrayList<>();
        // Fetch recent match results, recent tournaments created, recent registrations
        String sql = "SELECT CONCAT('Match Completed: ', m.round_name, ' - Winner: ', w.team_name, ' (', m.team1_score, '-', m.team2_score, ')') AS log_entry, " +
                     "m.created_at AS ts FROM matches m " +
                     "JOIN teams w ON m.winner_team_id = w.id WHERE m.status = 'COMPLETED' " +
                     "UNION ALL " +
                     "SELECT CONCAT('Tournament Created: ', title, ' [', game, ' - ', format, ']') AS log_entry, created_at AS ts FROM tournaments " +
                     "UNION ALL " +
                     "SELECT CONCAT('Team Registered: ', tm.team_name, ' registered for ', t.title, ' (', r.status, ')') AS log_entry, r.registration_date AS ts " +
                     "FROM registrations r JOIN teams tm ON r.team_id = tm.id JOIN tournaments t ON r.tournament_id = t.id " +
                     "ORDER BY ts DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("log_entry"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting recent activity logs: {}", e.getMessage(), e);
        }
        return list;
    }

    private int getCount(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignored) {}
        return 0;
    }
}
