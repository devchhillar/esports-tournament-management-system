package com.esports.dao;

import com.esports.model.TournamentStanding;
import java.util.List;

public interface TournamentStandingDAO {
    List<TournamentStanding> getStandingsByTournament(int tournamentId);
    List<TournamentStanding> getStandingsByGroup(int tournamentId, String groupName);
    boolean initializeStandings(int tournamentId, List<Integer> teamIds, String groupName);
    void recalculateRanks(int tournamentId);
}
