package com.esports.dao;

import com.esports.model.Game;
import java.util.List;

public interface GameDAO {
    List<Game> findAll();
    List<Game> findActive();
    Game findById(int id);
    Game findByName(String name);
    boolean create(Game game);
    boolean update(Game game);
    boolean delete(int id);
}
