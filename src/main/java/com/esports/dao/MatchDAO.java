package com.esports.dao;

import com.esports.model.Match;
import com.esports.model.MatchResult;
import java.util.List;

public interface MatchDAO {
    boolean create(Match match);
    boolean update(Match match);
    boolean delete(int id);
    Match findById(int id);
    List<Match> findAll();
    List<Match> findByTournament(int tournamentId);
    List<Match> findByTeam(int teamId);
    List<Match> findUpcoming();
    List<Match> findLive();
    List<Match> findCompleted();
    List<Match> findByDateRange(java.sql.Timestamp startTime, java.sql.Timestamp endTime);
    
    // Scheduling and operations
    boolean rescheduleMatch(int matchId, java.sql.Timestamp newTime, String newVenue, String newReferee);
    boolean cancelMatch(int matchId, String cancellationReason);
    boolean hasSchedulingConflict(int teamId, java.sql.Timestamp targetTime, Integer excludeMatchId);
    boolean advanceWinner(int completedMatchId, int winnerTeamId);

    boolean recordResult(MatchResult result);
    boolean recordMatchScore(int matchId, int team1Score, int team2Score, int winnerTeamId, Integer mvpPlayerId, String notes);
    boolean deleteByTournament(int tournamentId);
    int countTotal();
    int countCompleted();
    int countRemaining();
    int countLive();
}
