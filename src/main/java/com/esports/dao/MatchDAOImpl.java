package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.Match;
import com.esports.model.MatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDAOImpl implements MatchDAO {

    private static final Logger logger = LoggerFactory.getLogger(MatchDAOImpl.class);

    @Override
    public boolean create(Match match) {
        String sql = "INSERT INTO matches (tournament_id, round_name, match_number, bracket_type, group_name, " +
                     "team1_id, team2_id, team1_score, team2_score, winner_team_id, scheduled_time, venue, referee, " +
                     "next_match_id, next_match_slot, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, match.getTournamentId());
            ps.setString(2, match.getRoundName());
            ps.setInt(3, match.getMatchNumber());
            ps.setString(4, match.getBracketType() != null ? match.getBracketType() : "KNOCKOUT");
            ps.setString(5, match.getGroupName());
            if (match.getTeam1Id() != null && match.getTeam1Id() > 0) {
                ps.setInt(6, match.getTeam1Id());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            if (match.getTeam2Id() != null && match.getTeam2Id() > 0) {
                ps.setInt(7, match.getTeam2Id());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, match.getTeam1Score());
            ps.setInt(9, match.getTeam2Score());
            if (match.getWinnerTeamId() != null && match.getWinnerTeamId() > 0) {
                ps.setInt(10, match.getWinnerTeamId());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setTimestamp(11, match.getScheduledTime());
            ps.setString(12, match.getVenue() != null ? match.getVenue() : "Online Server / Arena 1");
            ps.setString(13, match.getReferee() != null ? match.getReferee() : "Official Esports Referee");
            if (match.getNextMatchId() != null && match.getNextMatchId() > 0) {
                ps.setInt(14, match.getNextMatchId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            if (match.getNextMatchSlot() != null && match.getNextMatchSlot() > 0) {
                ps.setInt(15, match.getNextMatchSlot());
            } else {
                ps.setNull(15, Types.INTEGER);
            }
            ps.setString(16, match.getStatus() != null ? match.getStatus() : "SCHEDULED");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        match.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating match fixture: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean update(Match match) {
        String sql = "UPDATE matches SET round_name = ?, match_number = ?, bracket_type = ?, group_name = ?, " +
                     "team1_id = ?, team2_id = ?, team1_score = ?, team2_score = ?, winner_team_id = ?, " +
                     "scheduled_time = ?, venue = ?, referee = ?, next_match_id = ?, next_match_slot = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, match.getRoundName());
            ps.setInt(2, match.getMatchNumber());
            ps.setString(3, match.getBracketType());
            ps.setString(4, match.getGroupName());
            if (match.getTeam1Id() != null && match.getTeam1Id() > 0) {
                ps.setInt(5, match.getTeam1Id());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (match.getTeam2Id() != null && match.getTeam2Id() > 0) {
                ps.setInt(6, match.getTeam2Id());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, match.getTeam1Score());
            ps.setInt(8, match.getTeam2Score());
            if (match.getWinnerTeamId() != null && match.getWinnerTeamId() > 0) {
                ps.setInt(9, match.getWinnerTeamId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setTimestamp(10, match.getScheduledTime());
            ps.setString(11, match.getVenue());
            ps.setString(12, match.getReferee());
            if (match.getNextMatchId() != null && match.getNextMatchId() > 0) {
                ps.setInt(13, match.getNextMatchId());
            } else {
                ps.setNull(13, Types.INTEGER);
            }
            if (match.getNextMatchSlot() != null && match.getNextMatchSlot() > 0) {
                ps.setInt(14, match.getNextMatchSlot());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setString(15, match.getStatus());
            ps.setInt(16, match.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating match {}: {}", match.getId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM matches WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting match {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteByTournament(int tournamentId) {
        String sql = "DELETE FROM matches WHERE tournament_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Error deleting tournament fixtures {}: {}", tournamentId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Match findById(int id) {
        String sql = baseQuery() + " WHERE m.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding match by id {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Match> findAll() {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " ORDER BY m.scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all matches: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Match> findByTournament(int tournamentId) {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE m.tournament_id = ? ORDER BY m.match_number ASC, m.scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding matches by tournament {}: {}", tournamentId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Match> findByTeam(int teamId) {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE m.team1_id = ? OR m.team2_id = ? ORDER BY m.scheduled_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding matches by team {}: {}", teamId, e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Match> findUpcoming() {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE m.status = 'SCHEDULED' ORDER BY m.scheduled_time ASC LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding upcoming matches: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Match> findLive() {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE m.status = 'LIVE' ORDER BY m.scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding live matches: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Match> findCompleted() {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE m.status = 'COMPLETED' ORDER BY m.scheduled_time DESC LIMIT 15";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding completed matches: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean recordMatchScore(int matchId, int team1Score, int team2Score, int winnerTeamId, Integer mvpPlayerId, String notes) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Fetch match info
            int tourneyId = 0;
            int team1Id = 0;
            int team2Id = 0;
            String fetchSql = "SELECT tournament_id, team1_id, team2_id FROM matches WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, matchId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tourneyId = rs.getInt("tournament_id");
                        team1Id = rs.getInt("team1_id");
                        team2Id = rs.getInt("team2_id");
                    }
                }
            }

            if (tourneyId == 0) {
                conn.rollback();
                return false;
            }

            // 2. Update matches row
            String updateMatchSql = "UPDATE matches SET status = 'COMPLETED', team1_score = ?, team2_score = ?, winner_team_id = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateMatchSql)) {
                ps.setInt(1, team1Score);
                ps.setInt(2, team2Score);
                ps.setInt(3, winnerTeamId);
                ps.setInt(4, matchId);
                ps.executeUpdate();
            }

            // 3. Insert or Replace match_results
            String resultSql = "INSERT INTO match_results (match_id, winner_team_id, team1_score, team2_score, mvp_player_id, notes) " +
                               "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                               "winner_team_id = VALUES(winner_team_id), team1_score = VALUES(team1_score), " +
                               "team2_score = VALUES(team2_score), mvp_player_id = VALUES(mvp_player_id), notes = VALUES(notes)";
            try (PreparedStatement ps = conn.prepareStatement(resultSql)) {
                ps.setInt(1, matchId);
                ps.setInt(2, winnerTeamId);
                ps.setInt(3, team1Score);
                ps.setInt(4, team2Score);
                if (mvpPlayerId != null && mvpPlayerId > 0) {
                    ps.setInt(5, mvpPlayerId);
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setString(6, notes);
                ps.executeUpdate();
            }

            // 4. Update team career stats
            int loserTeamId = (winnerTeamId == team1Id) ? team2Id : team1Id;
            String winTeamSql = "UPDATE teams SET matches_played = matches_played + 1, matches_won = matches_won + 1, " +
                                "total_points = total_points + 3, ranking_points = ranking_points + 25 WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(winTeamSql)) {
                ps.setInt(1, winnerTeamId);
                ps.executeUpdate();
            }
            String loseTeamSql = "UPDATE teams SET matches_played = matches_played + 1, matches_lost = matches_lost + 1, " +
                                 "ranking_points = GREATEST(0, ranking_points - 10) WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(loseTeamSql)) {
                ps.setInt(1, loserTeamId);
                ps.executeUpdate();
            }

            // 5. Update MVP player if specified
            if (mvpPlayerId != null && mvpPlayerId > 0) {
                String mvpSql = "UPDATE players SET mvp_count = mvp_count + 1, ranking_points = ranking_points + 50 WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(mvpSql)) {
                    ps.setInt(1, mvpPlayerId);
                    ps.executeUpdate();
                }
            }

            // 6. Update Tournament Standings
            updateStandingRecord(conn, tourneyId, winnerTeamId, true, (winnerTeamId == team1Id ? team1Score - team2Score : team2Score - team1Score));
            updateStandingRecord(conn, tourneyId, loserTeamId, false, (loserTeamId == team1Id ? team1Score - team2Score : team2Score - team1Score));

            // 7. Automatic Bracket Winner Advancement
            advanceWinnerInternal(conn, matchId, winnerTeamId);

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Error recording match score: {}", e.getMessage(), e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private void advanceWinnerInternal(Connection conn, int matchId, int winnerTeamId) {
        String findNextSql = "SELECT next_match_id, next_match_slot FROM matches WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(findNextSql)) {
            ps.setInt(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nextId = rs.getInt("next_match_id");
                    int nextSlot = rs.getInt("next_match_slot");
                    if (!rs.wasNull() && nextId > 0 && nextSlot > 0) {
                        String col = (nextSlot == 1) ? "team1_id" : "team2_id";
                        String upSql = "UPDATE matches SET " + col + " = ? WHERE id = ?";
                        try (PreparedStatement upPs = conn.prepareStatement(upSql)) {
                            upPs.setInt(1, winnerTeamId);
                            upPs.setInt(2, nextId);
                            upPs.executeUpdate();
                            logger.info("Winner {} advanced from match {} to next match {} (slot {})",
                                    winnerTeamId, matchId, nextId, nextSlot);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.warn("Could not automatically advance winner for match {}: {}", matchId, e.getMessage());
        }
    }

    @Override
    public boolean advanceWinner(int completedMatchId, int winnerTeamId) {
        try (Connection conn = DBConnection.getConnection()) {
            advanceWinnerInternal(conn, completedMatchId, winnerTeamId);
            return true;
        } catch (SQLException e) {
            logger.error("Error advancing winner: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean rescheduleMatch(int matchId, Timestamp newTime, String newVenue, String newReferee) {
        String sql = "UPDATE matches SET scheduled_time = ?, venue = COALESCE(?, venue), referee = COALESCE(?, referee), status = 'SCHEDULED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, newTime);
            ps.setString(2, newVenue);
            ps.setString(3, newReferee);
            ps.setInt(4, matchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error rescheduling match {}: {}", matchId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean cancelMatch(int matchId, String cancellationReason) {
        String sql = "UPDATE matches SET status = 'CANCELLED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error cancelling match {}: {}", matchId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasSchedulingConflict(int teamId, Timestamp targetTime, Integer excludeMatchId) {
        if (teamId <= 0 || targetTime == null) return false;
        String sql = "SELECT COUNT(*) FROM matches WHERE (team1_id = ? OR team2_id = ?) " +
                     "AND status IN ('SCHEDULED', 'LIVE') " +
                     "AND (id != ? OR ? IS NULL) " +
                     "AND ABS(TIMESTAMPDIFF(MINUTE, scheduled_time, ?)) < 120";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            if (excludeMatchId != null && excludeMatchId > 0) {
                ps.setInt(3, excludeMatchId);
                ps.setInt(4, excludeMatchId);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }
            ps.setTimestamp(5, targetTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking scheduling conflict for team {}: {}", teamId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<Match> findByDateRange(Timestamp startTime, Timestamp endTime) {
        List<Match> list = new ArrayList<>();
        String sql = baseQuery() + " WHERE m.scheduled_time BETWEEN ? AND ? ORDER BY m.scheduled_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, startTime);
            ps.setTimestamp(2, endTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding matches by date range: {}", e.getMessage(), e);
        }
        return list;
    }

    private void updateStandingRecord(Connection conn, int tournamentId, int teamId, boolean won, int scoreDiff) throws SQLException {
        String upsertSql = "INSERT INTO tournament_standings (tournament_id, team_id, matches_played, wins, losses, draws, points, score_diff) " +
                           "VALUES (?, ?, 1, ?, ?, 0, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE matches_played = matches_played + 1, " +
                           "wins = wins + VALUES(wins), losses = losses + VALUES(losses), " +
                           "points = points + VALUES(points), score_diff = score_diff + VALUES(score_diff)";
        try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
            ps.setInt(1, tournamentId);
            ps.setInt(2, teamId);
            ps.setInt(3, won ? 1 : 0);
            ps.setInt(4, won ? 0 : 1);
            ps.setInt(5, won ? 3 : 0);
            ps.setInt(6, scoreDiff);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean recordResult(MatchResult result) {
        return recordMatchScore(
                result.getMatchId(),
                result.getTeam1Score(),
                result.getTeam2Score(),
                result.getWinnerTeamId(),
                result.getMvpPlayerId(),
                result.getNotes()
        );
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM matches";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting total matches: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countCompleted() {
        String sql = "SELECT COUNT(*) FROM matches WHERE status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting completed matches: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countRemaining() {
        String sql = "SELECT COUNT(*) FROM matches WHERE status IN ('SCHEDULED', 'LIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting remaining matches: {}", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countLive() {
        String sql = "SELECT COUNT(*) FROM matches WHERE status = 'LIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting live matches: {}", e.getMessage(), e);
        }
        return 0;
    }

    private String baseQuery() {
        return "SELECT m.*, t.title AS tournament_title, t.game AS game_name, " +
               "t1.team_name AS team1_name, t2.team_name AS team2_name, " +
               "w.team_name AS winner_name, mr.mvp_player_id, p.gamer_tag AS mvp_player_name, mr.notes " +
               "FROM matches m " +
               "JOIN tournaments t ON m.tournament_id = t.id " +
               "LEFT JOIN teams t1 ON m.team1_id = t1.id " +
               "LEFT JOIN teams t2 ON m.team2_id = t2.id " +
               "LEFT JOIN teams w ON m.winner_team_id = w.id " +
               "LEFT JOIN match_results mr ON m.id = mr.match_id " +
               "LEFT JOIN players p ON mr.mvp_player_id = p.id";
    }

    private Match mapRow(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setId(rs.getInt("id"));
        m.setTournamentId(rs.getInt("tournament_id"));
        m.setRoundName(rs.getString("round_name"));
        m.setMatchNumber(rs.getInt("match_number"));
        m.setBracketType(rs.getString("bracket_type"));
        m.setGroupName(rs.getString("group_name"));
        int t1 = rs.getInt("team1_id");
        if (!rs.wasNull()) m.setTeam1Id(t1);
        int t2 = rs.getInt("team2_id");
        if (!rs.wasNull()) m.setTeam2Id(t2);
        m.setTeam1Score(rs.getInt("team1_score"));
        m.setTeam2Score(rs.getInt("team2_score"));
        int winnerId = rs.getInt("winner_team_id");
        if (!rs.wasNull()) {
            m.setWinnerTeamId(winnerId);
        }
        m.setScheduledTime(rs.getTimestamp("scheduled_time"));
        m.setStatus(rs.getString("status"));
        m.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            m.setVenue(rs.getString("venue"));
            m.setReferee(rs.getString("referee"));
            int nId = rs.getInt("next_match_id");
            if (!rs.wasNull()) m.setNextMatchId(nId);
            int nSlot = rs.getInt("next_match_slot");
            if (!rs.wasNull()) m.setNextMatchSlot(nSlot);

            m.setTournamentTitle(rs.getString("tournament_title"));
            m.setGameName(rs.getString("game_name"));
            m.setTeam1Name(rs.getString("team1_name"));
            m.setTeam2Name(rs.getString("team2_name"));
            m.setWinnerTeamName(rs.getString("winner_name"));
            int mvpId = rs.getInt("mvp_player_id");
            if (!rs.wasNull()) {
                m.setMvpPlayerId(mvpId);
                m.setMvpPlayerName(rs.getString("mvp_player_name"));
            }
            m.setNotes(rs.getString("notes"));
        } catch (SQLException ignored) {}

        return m;
    }
}
