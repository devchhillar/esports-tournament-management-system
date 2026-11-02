package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAOImpl implements GameDAO {

    private static final Logger logger = LoggerFactory.getLogger(GameDAOImpl.class);

    @Override
    public List<Game> findAll() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all games: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Game> findActive() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE status = 'ACTIVE' ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching active games: {}", e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Game findById(int id) {
        String sql = "SELECT * FROM games WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding game by ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Game findByName(String name) {
        String sql = "SELECT * FROM games WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding game by name {}: {}", name, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean create(Game game) {
        String sql = "INSERT INTO games (name, category, description, logo_path, min_team_size, max_team_size, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getCategory());
            ps.setString(3, game.getDescription());
            ps.setString(4, game.getLogoPath());
            ps.setInt(5, game.getMinTeamSize());
            ps.setInt(6, game.getMaxTeamSize());
            ps.setString(7, game.getStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        game.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating game {}: {}", game.getName(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean update(Game game) {
        String sql = "UPDATE games SET name = ?, category = ?, description = ?, logo_path = ?, min_team_size = ?, max_team_size = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getCategory());
            ps.setString(3, game.getDescription());
            ps.setString(4, game.getLogoPath());
            ps.setInt(5, game.getMinTeamSize());
            ps.setInt(6, game.getMaxTeamSize());
            ps.setString(7, game.getStatus());
            ps.setInt(8, game.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating game {}: {}", game.getId(), e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM games WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting game {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    private Game mapResultSet(ResultSet rs) throws SQLException {
        return new Game(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("logo_path"),
                rs.getInt("min_team_size"),
                rs.getInt("max_team_size"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }
}
