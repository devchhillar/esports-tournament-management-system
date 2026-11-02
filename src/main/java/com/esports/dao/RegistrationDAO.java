package com.esports.dao;

import com.esports.model.Registration;
import com.esports.model.Team;
import java.util.List;

public interface RegistrationDAO {
    boolean registerTeam(int tournamentId, int teamId, int userId);
    boolean updateStatus(int registrationId, String status);
    List<Registration> findAll();
    List<Registration> findByTournament(int tournamentId);
    List<Registration> findByTeam(int teamId);
    List<Team> getApprovedTeams(int tournamentId);
    boolean isAlreadyRegistered(int tournamentId, int teamId);
    int countPending();
}
