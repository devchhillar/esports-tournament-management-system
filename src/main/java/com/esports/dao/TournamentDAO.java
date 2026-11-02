package com.esports.dao;

import com.esports.model.GameCategory;
import com.esports.model.Tournament;
import java.util.List;

public interface TournamentDAO {
    boolean create(Tournament tournament);
    boolean update(Tournament tournament);
    boolean delete(int id);
    boolean updateStatus(int id, String newStatus);
    Tournament findById(int id);
    List<Tournament> findAll();
    List<Tournament> findByGame(String game);
    List<Tournament> findByGame(GameCategory game);
    List<Tournament> findByStatus(String status);
    List<Tournament> search(String keyword, String game, String status);
    int countTotal();
    int countActive();
    int countByStatus(String status);
}
