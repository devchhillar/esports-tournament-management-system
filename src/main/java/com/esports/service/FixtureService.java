package com.esports.service;

import com.esports.dao.MatchDAO;
import com.esports.dao.MatchDAOImpl;
import com.esports.dao.TournamentStandingDAO;
import com.esports.dao.TournamentStandingDAOImpl;
import com.esports.model.Match;
import com.esports.model.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service implementing automated fixture generation algorithms:
 * 1. Single Elimination (Knockout Bracket with Byes and auto-progression)
 * 2. Double Elimination (Winners Bracket, Losers Bracket, Grand Final)
 * 3. Round Robin (Circle Algorithm / Berger System with Odd Team Byes)
 * 4. Group Stage + Knockout (Group division, group fixtures, standings initialization, playoff slots)
 */
public class FixtureService {

    private static final Logger logger = LoggerFactory.getLogger(FixtureService.class);
    private final MatchDAO matchDAO;
    private final TournamentStandingDAO standingDAO;

    public FixtureService() {
        this.matchDAO = new MatchDAOImpl();
        this.standingDAO = new TournamentStandingDAOImpl();
    }

    public FixtureService(MatchDAO matchDAO, TournamentStandingDAO standingDAO) {
        this.matchDAO = matchDAO;
        this.standingDAO = standingDAO;
    }

    /**
     * Master generation router based on tournament format.
     */
    public GenerationResult generateFixtures(int tournamentId, String format, List<Team> teams, LocalDateTime startDateTime) {
        if (teams == null || teams.size() < 2) {
            return new GenerationResult(false, "At least 2 approved teams are required to generate fixtures.", 0);
        }

        // Clean any existing fixtures for this tournament first
        matchDAO.deleteByTournament(tournamentId);

        String fmt = format != null ? format.toUpperCase() : "SINGLE_ELIMINATION";
        switch (fmt) {
            case "DOUBLE_ELIMINATION":
                return generateDoubleElimination(tournamentId, teams, startDateTime);
            case "ROUND_ROBIN":
                return generateRoundRobin(tournamentId, teams, startDateTime);
            case "GROUP_STAGE_KNOCKOUT":
                return generateGroupStageKnockout(tournamentId, teams, startDateTime);
            case "SINGLE_ELIMINATION":
            default:
                return generateSingleElimination(tournamentId, teams, startDateTime);
        }
    }

    /**
     * A. SINGLE ELIMINATION BRACKET GENERATOR
     * Generates a complete knockout bracket tree with next_match_id links,
     * power-of-two expansion, BYE allocation, and automatic winner progression.
     */
    public GenerationResult generateSingleElimination(int tournamentId, List<Team> teams, LocalDateTime startDateTime) {
        List<Team> roster = new ArrayList<>(teams);
        Collections.shuffle(roster); // Random seed draw

        int teamCount = roster.size();
        if (teamCount < 2) {
            return new GenerationResult(false, "At least 2 teams are required.", 0);
        }

        int powerOfTwo = 2;
        int depth = 1;
        while (powerOfTwo < teamCount) {
            powerOfTwo *= 2;
            depth++;
        }

        int totalMatchesCreated = 0;
        int matchNumber = 1;
        LocalDateTime matchTime = startDateTime != null ? startDateTime : LocalDateTime.now().plusDays(1);

        // Special case: Exactly 2 teams -> Grand Final only
        if (teamCount == 2) {
            Match gf = new Match();
            gf.setTournamentId(tournamentId);
            gf.setRoundName("Grand Final");
            gf.setMatchNumber(matchNumber);
            gf.setBracketType("GRAND_FINAL");
            gf.setTeam1Id(roster.get(0).getId());
            gf.setTeam2Id(roster.get(1).getId());
            gf.setScheduledTime(Timestamp.valueOf(matchTime));
            gf.setVenue("Main Esports Arena");
            gf.setReferee("Senior Head Referee");
            gf.setStatus("SCHEDULED");
            matchDAO.create(gf);

            standingDAO.initializeStandings(tournamentId, List.of(roster.get(0).getId(), roster.get(1).getId()), null);
            return new GenerationResult(true, "Single Elimination Championship Final generated.", 1);
        }

        // Pre-create the tournament tree levels from Final down to Round 1
        // Level 0 = Grand Final (1 match)
        // Level 1 = Semifinals (2 matches)
        // Level 2 = Quarterfinals (4 matches), etc.
        List<List<Match>> levels = new ArrayList<>();
        int currentLevelSize = 1;
        for (int l = 0; l < depth; l++) {
            List<Match> levelMatches = new ArrayList<>();
            String roundTitle;
            if (currentLevelSize == 1) roundTitle = "Grand Final";
            else if (currentLevelSize == 2) roundTitle = "Semifinals";
            else if (currentLevelSize == 4) roundTitle = "Quarterfinals";
            else if (currentLevelSize == 8) roundTitle = "Round of 16";
            else roundTitle = "Round of " + (currentLevelSize * 2);

            for (int m = 0; m < currentLevelSize; m++) {
                Match match = new Match();
                match.setTournamentId(tournamentId);
                match.setRoundName(currentLevelSize == 1 ? "Grand Final" : (roundTitle + " Match " + (m + 1)));
                match.setMatchNumber(matchNumber++);
                match.setBracketType(currentLevelSize == 1 ? "GRAND_FINAL" : "KNOCKOUT");
                match.setScheduledTime(Timestamp.valueOf(matchTime.plusDays(depth - l)));
                match.setVenue("Esports Arena - Stage " + ((m % 4) + 1));
                match.setReferee("Referee " + ((m % 3) + 1));
                match.setStatus("SCHEDULED");
                levelMatches.add(match);
            }
            levels.add(0, levelMatches); // prepend so level 0 is leaf level, last is Final
            currentLevelSize *= 2;
        }

        // Link parent nodes: for each level (except Final), link match to parent in next level
        // level 0 is highest level (leaves), level levels.size() - 1 is Grand Final
        for (int l = 0; l < levels.size() - 1; l++) {
            List<Match> currentMatches = levels.get(l);
            List<Match> nextMatches = levels.get(l + 1);
            for (int i = 0; i < currentMatches.size(); i++) {
                Match child = currentMatches.get(i);
                Match parent = nextMatches.get(i / 2);
                int slot = (i % 2 == 0) ? 1 : 2;
                child.setNextMatchSlot(slot);
                // Parent match ID will be set once parent is inserted or persisted
            }
        }

        // Persist tree in reverse (from Final down to leaves) so parent IDs exist!
        for (int l = levels.size() - 1; l >= 0; l--) {
            List<Match> matches = levels.get(l);
            for (int i = 0; i < matches.size(); i++) {
                Match m = matches.get(i);
                if (l < levels.size() - 1) {
                    Match parent = levels.get(l + 1).get(i / 2);
                    m.setNextMatchId(parent.getId());
                }
            }
        }

        // Distribute teams across the leaves (level 0)
        List<Match> leafMatches = levels.get(0);
        int rosterIdx = 0;
        int numByes = powerOfTwo - teamCount;

        List<Match> activeMatchesToSave = new ArrayList<>();

        for (int i = 0; i < leafMatches.size(); i++) {
            Match leaf = leafMatches.get(i);
            if (rosterIdx < roster.size()) {
                Team t1 = roster.get(rosterIdx++);
                leaf.setTeam1Id(t1.getId());
            }

            if (numByes > 0 && rosterIdx >= roster.size()) {
                // This leaf gets a BYE for team 2 -> team 1 advances directly to parent
                numByes--;
            } else if (rosterIdx < roster.size()) {
                Team t2 = roster.get(rosterIdx++);
                leaf.setTeam2Id(t2.getId());
            }
        }

        // Now save all matches from Final down to leaves in DB
        for (int l = levels.size() - 1; l >= 0; l--) {
            List<Match> levelMatches = levels.get(l);
            for (Match m : levelMatches) {
                // If this is a leaf that only got 1 team (BYE), advance team directly to parent
                if (l == 0 && m.getTeam1Id() != null && m.getTeam2Id() == null) {
                    // Solo team advances with BYE
                    if (m.getNextMatchId() != null && m.getNextMatchSlot() != null) {
                        for (Match p : levels.get(1)) {
                            if (p.getId() == m.getNextMatchId()) {
                                if (m.getNextMatchSlot() == 1) p.setTeam1Id(m.getTeam1Id());
                                else p.setTeam2Id(m.getTeam1Id());
                                break;
                            }
                        }
                    }
                    continue; // don't create unnecessary solo bye match
                }

                matchDAO.create(m);
                totalMatchesCreated++;
            }
            // Update child matches with the generated parent match IDs
            if (l > 0) {
                List<Match> childMatches = levels.get(l - 1);
                for (int i = 0; i < childMatches.size(); i++) {
                    Match child = childMatches.get(i);
                    Match parent = levelMatches.get(i / 2);
                    child.setNextMatchId(parent.getId());
                }
            }
        }

        // Initialize standings
        List<Integer> ids = roster.stream().map(Team::getId).toList();
        standingDAO.initializeStandings(tournamentId, ids, null);

        return new GenerationResult(true, "Single Elimination Bracket generated with " + totalMatchesCreated + " matches and automatic progression.", totalMatchesCreated);
    }

    /**
     * B. DOUBLE ELIMINATION BRACKET GENERATOR
     * Generates Winners Bracket, Losers Bracket, and Grand Finals.
     */
    public GenerationResult generateDoubleElimination(int tournamentId, List<Team> teams, LocalDateTime startDateTime) {
        List<Team> roster = new ArrayList<>(teams);
        Collections.shuffle(roster);

        int totalMatches = 0;
        int matchNum = 1;
        LocalDateTime matchTime = startDateTime != null ? startDateTime : LocalDateTime.now().plusDays(1);

        // 1. Upper Bracket (Winners Bracket)
        for (int i = 0; i < roster.size(); i += 2) {
            if (i + 1 < roster.size()) {
                Match m = new Match();
                m.setTournamentId(tournamentId);
                m.setRoundName("Winners Bracket Round 1 - Match " + (i / 2 + 1));
                m.setMatchNumber(matchNum++);
                m.setBracketType("WINNERS");
                m.setTeam1Id(roster.get(i).getId());
                m.setTeam2Id(roster.get(i + 1).getId());
                m.setScheduledTime(Timestamp.valueOf(matchTime));
                m.setStatus("SCHEDULED");

                matchDAO.create(m);
                totalMatches++;
                matchTime = matchTime.plusHours(3);
            }
        }

        // 2. Upper Bracket Finals slot (if at least 4 teams)
        if (roster.size() >= 4) {
            Match wbFinal = new Match();
            wbFinal.setTournamentId(tournamentId);
            wbFinal.setRoundName("Winners Bracket Final");
            wbFinal.setMatchNumber(matchNum++);
            wbFinal.setBracketType("WINNERS");
            wbFinal.setTeam1Id(roster.get(0).getId());
            wbFinal.setTeam2Id(roster.get(2).getId());
            wbFinal.setScheduledTime(Timestamp.valueOf(matchTime.plusDays(1)));
            wbFinal.setStatus("SCHEDULED");
            matchDAO.create(wbFinal);
            totalMatches++;

            // 3. Losers Bracket Final slot
            Match lbFinal = new Match();
            lbFinal.setTournamentId(tournamentId);
            lbFinal.setRoundName("Losers Bracket Final");
            lbFinal.setMatchNumber(matchNum++);
            lbFinal.setBracketType("LOSERS");
            lbFinal.setTeam1Id(roster.get(1).getId());
            lbFinal.setTeam2Id(roster.get(3).getId());
            lbFinal.setScheduledTime(Timestamp.valueOf(matchTime.plusDays(2)));
            lbFinal.setStatus("SCHEDULED");
            matchDAO.create(lbFinal);
            totalMatches++;
        }

        // 4. Grand Final Match
        Match grandFinal = new Match();
        grandFinal.setTournamentId(tournamentId);
        grandFinal.setRoundName("Grand Championship Final");
        grandFinal.setMatchNumber(matchNum++);
        grandFinal.setBracketType("GRAND_FINAL");
        grandFinal.setTeam1Id(roster.get(0).getId());
        grandFinal.setTeam2Id(roster.get(1).getId());
        grandFinal.setScheduledTime(Timestamp.valueOf(matchTime.plusDays(3)));
        grandFinal.setStatus("SCHEDULED");
        matchDAO.create(grandFinal);
        totalMatches++;

        // Initialize standings
        List<Integer> ids = roster.stream().map(Team::getId).toList();
        standingDAO.initializeStandings(tournamentId, ids, null);

        return new GenerationResult(true, "Double Elimination Bracket generated (" + totalMatches + " matches: Upper, Lower & Grand Finals).", totalMatches);
    }

    /**
     * C. ROUND ROBIN GENERATOR (Circle Algorithm / Berger System)
     * Every team plays every other team once.
     */
    public GenerationResult generateRoundRobin(int tournamentId, List<Team> teams, LocalDateTime startDateTime) {
        List<Team> roster = new ArrayList<>(teams);
        boolean hasDummy = false;

        // If odd number of teams, add dummy Bye team
        if (roster.size() % 2 != 0) {
            Team byeTeam = new Team();
            byeTeam.setId(-1);
            byeTeam.setTeamName("BYE");
            byeTeam.setTeamTag("BYE");
            roster.add(byeTeam);
            hasDummy = true;
        }

        int n = roster.size();
        int totalRounds = n - 1;
        int matchesPerRound = n / 2;
        int matchNum = 1;
        int totalMatchesCreated = 0;
        LocalDateTime matchTime = startDateTime != null ? startDateTime : LocalDateTime.now().plusDays(1);

        for (int round = 0; round < totalRounds; round++) {
            String roundTitle = "Round Robin - Round " + (round + 1);

            for (int i = 0; i < matchesPerRound; i++) {
                Team t1 = roster.get(i);
                Team t2 = roster.get(n - 1 - i);

                // Skip Bye matches
                if (t1.getId() == -1 || t2.getId() == -1) {
                    continue;
                }

                Match m = new Match();
                m.setTournamentId(tournamentId);
                m.setRoundName(roundTitle);
                m.setMatchNumber(matchNum++);
                m.setBracketType("GROUP");
                m.setTeam1Id(t1.getId());
                m.setTeam2Id(t2.getId());
                m.setScheduledTime(Timestamp.valueOf(matchTime));
                m.setStatus("SCHEDULED");

                matchDAO.create(m);
                totalMatchesCreated++;
                matchTime = matchTime.plusHours(2);
            }

            // Circle shift: keep element 0 fixed, rotate others clockwise
            Team last = roster.remove(n - 1);
            roster.add(1, last);
            matchTime = matchTime.plusDays(1).withHour(14).withMinute(0);
        }

        // Initialize standings
        List<Integer> realIds = teams.stream().map(Team::getId).toList();
        standingDAO.initializeStandings(tournamentId, realIds, null);

        return new GenerationResult(true, "Round Robin Schedule generated (" + totalRounds + " rounds, " + totalMatchesCreated + " matches).", totalMatchesCreated);
    }

    /**
     * D. GROUP STAGE + KNOCKOUT GENERATOR
     * Divides teams into Group A & Group B, generates group round-robin, and pre-schedules knockout playoffs.
     */
    public GenerationResult generateGroupStageKnockout(int tournamentId, List<Team> teams, LocalDateTime startDateTime) {
        List<Team> roster = new ArrayList<>(teams);
        Collections.shuffle(roster);

        List<Team> groupA = new ArrayList<>();
        List<Team> groupB = new ArrayList<>();

        for (int i = 0; i < roster.size(); i++) {
            if (i % 2 == 0) groupA.add(roster.get(i));
            else groupB.add(roster.get(i));
        }

        int totalMatches = 0;
        int matchNum = 1;
        LocalDateTime matchTime = startDateTime != null ? startDateTime : LocalDateTime.now().plusDays(1);

        // 1. Group A Round Robin
        totalMatches += generateGroupFixtures(tournamentId, groupA, "Group A", matchNum, matchTime);
        matchNum += totalMatches;

        // 2. Group B Round Robin
        int matchesB = generateGroupFixtures(tournamentId, groupB, "Group B", matchNum, matchTime.plusHours(2));
        totalMatches += matchesB;
        matchNum += matchesB;

        // 3. Playoff Knockout Bracket (Semifinals & Finals)
        LocalDateTime playoffTime = matchTime.plusDays(5);
        if (groupA.size() >= 2 && groupB.size() >= 2) {
            // SF 1: Group A #1 vs Group B #2
            Match sf1 = new Match();
            sf1.setTournamentId(tournamentId);
            sf1.setRoundName("Playoff Semifinal 1 (Group A #1 vs Group B #2)");
            sf1.setMatchNumber(matchNum++);
            sf1.setBracketType("KNOCKOUT");
            sf1.setTeam1Id(groupA.get(0).getId());
            sf1.setTeam2Id(groupB.get(1).getId());
            sf1.setScheduledTime(Timestamp.valueOf(playoffTime));
            sf1.setStatus("SCHEDULED");
            matchDAO.create(sf1);
            totalMatches++;

            // SF 2: Group B #1 vs Group A #2
            Match sf2 = new Match();
            sf2.setTournamentId(tournamentId);
            sf2.setRoundName("Playoff Semifinal 2 (Group B #1 vs Group A #2)");
            sf2.setMatchNumber(matchNum++);
            sf2.setBracketType("KNOCKOUT");
            sf2.setTeam1Id(groupB.get(0).getId());
            sf2.setTeam2Id(groupA.get(1).getId());
            sf2.setScheduledTime(Timestamp.valueOf(playoffTime.plusHours(3)));
            sf2.setStatus("SCHEDULED");
            matchDAO.create(sf2);
            totalMatches++;

            // Grand Final
            Match gf = new Match();
            gf.setTournamentId(tournamentId);
            gf.setRoundName("Grand Championship Final");
            gf.setMatchNumber(matchNum++);
            gf.setBracketType("KNOCKOUT");
            gf.setTeam1Id(groupA.get(0).getId());
            gf.setTeam2Id(groupB.get(0).getId());
            gf.setScheduledTime(Timestamp.valueOf(playoffTime.plusDays(1)));
            gf.setStatus("SCHEDULED");
            matchDAO.create(gf);
            totalMatches++;
        }

        // Initialize standings for both groups
        List<Integer> idsA = groupA.stream().map(Team::getId).toList();
        List<Integer> idsB = groupB.stream().map(Team::getId).toList();
        standingDAO.initializeStandings(tournamentId, idsA, "Group A");
        standingDAO.initializeStandings(tournamentId, idsB, "Group B");

        return new GenerationResult(true, "Group Stage + Knockout fixtures generated (" + totalMatches + " matches across Group A, B and Playoffs).", totalMatches);
    }

    private int generateGroupFixtures(int tournamentId, List<Team> groupTeams, String groupName, int startMatchNum, LocalDateTime startTime) {
        int created = 0;
        int mNum = startMatchNum;
        LocalDateTime tTime = startTime;

        for (int i = 0; i < groupTeams.size(); i++) {
            for (int j = i + 1; j < groupTeams.size(); j++) {
                Team t1 = groupTeams.get(i);
                Team t2 = groupTeams.get(j);

                Match m = new Match();
                m.setTournamentId(tournamentId);
                m.setRoundName(groupName + " Match " + (created + 1));
                m.setMatchNumber(mNum++);
                m.setBracketType("GROUP");
                m.setGroupName(groupName);
                m.setTeam1Id(t1.getId());
                m.setTeam2Id(t2.getId());
                m.setScheduledTime(Timestamp.valueOf(tTime));
                m.setStatus("SCHEDULED");

                matchDAO.create(m);
                created++;
                tTime = tTime.plusHours(2);
            }
        }
        return created;
    }

    public static class GenerationResult {
        private final boolean success;
        private final String message;
        private final int matchesCount;

        public GenerationResult(boolean success, String message, int matchesCount) {
            this.success = success;
            this.message = message;
            this.matchesCount = matchesCount;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getMatchesCount() { return matchesCount; }
    }
}
