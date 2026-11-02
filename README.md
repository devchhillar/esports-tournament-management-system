# 🎮 Professional eSports Tournament Management System

A production-grade, database-connected desktop software system built in **Java** (JDK 17/21 LTS compatible), **Java Swing** with **FlatLaf Cyber Dark theme**, **MySQL 8** via raw JDBC `PreparedStatement`s, **BCrypt** password security, **JFreeChart** dynamic visualizations, and **OpenPDF** reporting engine following the **MVC + DAO Architecture**.

---

## 🚀 Key Modules & System Architecture

### 1. 🛡️ Super Admin Command Center
- **Executive Command Dashboard**: Real-time KPIs for Total, Active, Upcoming, and Completed Tournaments, Teams, Players, Matches, and Financial Revenue.
- **Dynamic JFreeCharts**:
  - Tournament Status Breakdown (Donut/Pie Chart).
  - Game Popularity & Team Participation (Bar Chart).
  - Leaderboard Standings & Win/Loss Ratios.
- **Official Game Management**: Full CRUD for 8 default games (BGMI, Valorant, Free Fire, CS2, Call of Duty, Dota 2, League of Legends, Custom Games) with team size bounds.
- **Tournament Manager**: Multi-criteria search, date-window checks, prize pool allocations (1st, 2nd, 3rd splits), registration approval/rejection, and visual bracket inspection.
- **Automated Fixture System**:
  - **Single Elimination**: Automated knockout bracket tree with power-of-two expansion, preliminary BYE allocations, and pre-linked advancement slots.
  - **Double Elimination**: Upper bracket (Winners), Lower bracket (Losers), and Grand Championship Final.
  - **Round Robin**: Berger Circle rotation algorithm avoiding duplicate fixtures with Odd Team BYE rounds.
  - **Group Stage + Knockout**: Multi-group round-robin tables with automated playoff qualification.
- **Interactive Visual Elimination Bracket Viewer**: Visual tournament bracket tree (`BracketViewerDialog`) rendering match cards, live scores, status badges, and winner progression.
- **Match Scheduling with Conflict Detection**:
  - Scheduled in **`Asia/Kolkata`** timezone.
  - Venues/arenas and referee/caster assignments.
  - Rescheduling and cancellation with reason capture.
  - **120-minute conflict detection** preventing teams from having overlapping matches within +/- 2 hours.
- **Live Match & Score Control**: Atomic MySQL transactions recording match scores, declaring winners, awarding MVP trophies, and automatically advancing winners to the next linked bracket slot.
- **Standings & Rankings**: Automated points calculation (Win: +3, Draw: +1, Loss: 0), round/goal differential tracking, and podium medals (🥇, 🥈, 🥉).
- **Payment & Financial Management**: Track tournament entry fees, prize money disbursements, transaction IDs, payment methods (UPI, Card, Net Banking, Wallet), and net revenue balances.
- **Reports & PDF / Excel (CSV) Export**: One-click generation and file export of Tournament Summaries, Financial Ledgers, Official Standings, and Player Performance Leaderboards.
- **Admin Profile & Security Audit**: Profile editor, avatar picture uploads, BCrypt password changes, and login activity history.

### 2. ⚔️ Team Manager & Organizer Portal
- **Team Hub**: Manage team profile, description, and upload official team logos (`uploads/logos/`).
- **Roster Management & Rules**:
  - Maximum team size validation (e.g. 5 for BGMI, 6 for Valorant including substitutes).
  - Game compatibility checks (prevents recruiting players with mismatched primary games).
  - Captain assignment and roster removal.
- **Tournament Registration & Online Payment**:
  - Browse open tournaments and submit registrations.
  - Integrated entry fee checkout (UPI, Card, Net Banking, Wallet) with automatic transaction ID generation.
  - Real-time approval tracking (`PENDING`, `APPROVED`, `REJECTED`).
- **Match Schedules & Results**: Live view of team fixtures, venues, referees, and opponents.

### 3. 🎯 Pro Player Portal
- **Career Card**: Real-time stats (Matches Played, Wins, Losses, Win Rate %, Kills, MVP Awards, Ranking Points).
- **Profile Customization**: Update in-game name (IGN / Gamer Tag), bio, tactical role (Fragger, IGL, Support, Sniper), country, and avatar upload (`uploads/avatars/`).
- **Squad Affiliation**: View current team, captain, and teammates.
- **Global Leaderboards**: Privacy-protected ranking view with individual performance metrics.

### 4. 👁️ Public Spectator & Browser Portal
- **Desktop Guest Mode**: Direct guest access with zero login friction to view brackets, schedules, and standings.
- **Built-in Web Portal**: Embedded zero-dependency HTTP server (`http://localhost:8080`) serving a responsive Cyber Dark web dashboard in Chrome, Edge, Firefox, or Brave.
- **REST APIs**: Public endpoints for `/api/tournaments`, `/api/matches`, `/api/teams`, `/api/players`, `/api/standings`, and `/api/stats`.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Language** | Java (JDK 17 / Java 21 LTS compatible) |
| **GUI Framework** | Java Swing + FlatLaf Cyber Dark Theme (`#0D1117`, cyan `#00E5FF`, purple `#9D4EDD`) |
| **Database** | MySQL 8.0 (`esports_management`) |
| **Persistence** | JDBC with `PreparedStatement` & Atomic Transactions |
| **Architecture** | Strict MVC + DAO Design Pattern |
| **Security** | BCrypt Work Factor 12 (`jbcrypt`) |
| **Data Visualization** | JFreeChart 1.5.4 |
| **PDF Reporting** | OpenPDF 1.3.39 (LibrePDF) |
| **Build Tool** | Apache Maven 3 |
| **Logging** | SLF4J 2.0 + Logback Classic 1.5 |
| **Testing** | JUnit Jupiter 5.10.2 (**25/25 Integration Tests Passing**) |

---

## 🗄️ Database Schema (`esports_management`)

The database contains 13 relational tables with foreign keys, triggers, and indices:
1. `users`: Credentials, BCrypt hash, user roles (`ADMIN`, `TEAM_MANAGER`, `PLAYER`, `VIEWER`), profile pictures, status, last login.
2. `login_history`: User audit trail with IP address, timestamps, and authentication outcome.
3. `games`: Official eSports titles, team size bounds (min/max), categories, and status.
4. `teams`: Teams, leader ID, manager ID, game, logo path, ranking points, career records.
5. `players`: Gamer tags, tactical roles, primary games, avatars, career kills, MVPs, ranking points.
6. `team_members`: Roster memberships with roles (`CAPTAIN`, `MEMBER`, `SUBSTITUTE`).
7. `tournaments`: Tournament info, formats, dates, entry fees, prize pools, statuses.
8. `registrations`: Team tournament signups with approval states (`PENDING`, `APPROVED`, `REJECTED`).
9. `matches`: Fixtures with rounds, bracket types, team matchups, live scores, scheduled times, venues, referees, `next_match_id`, and `next_match_slot`.
10. `match_results`: Verified match outcomes, MVP player references, and match notes.
11. `tournament_standings`: Dynamic leaderboard with wins, losses, draws, points, score differences, and rank.
12. `notifications`: In-app direct messaging and system broadcasts.
13. `payments`: Entry fee collections, prize disbursements, payment methods, transaction IDs, and statuses.

---

## 🔑 Pre-Seeded Demonstration Accounts

All pre-configured accounts have password: **`admin123`**

| Role | Username | Password | Notes |
|---|---|---|---|
| **Super Admin** | `admin` | `admin123` | Full control over tournaments, games, teams, fixtures, matches, payments, and reports |
| **Team Manager / Organizer** | `manager1` | `admin123` | Manager of Soul Titans, recruits free agents, pays entry fees |
| **Player (Valorant)** | `phoenix` | `admin123` | Pro player on Soul Titans, avatar customization, stats |
| **Player (CS2)** | `shadow` | `admin123` | Pro player on GodLike Warriors |
| **Player (BGMI)** | `mortal` | `admin123` | Pro player on Team Secret |
| **Player (Free Fire)**| `scout` | `admin123` | Free agent player profile |
| **Spectator / Viewer**| `viewer1` | `admin123` | Registered spectator account |
| **Guest Spectator** | *(None)* | *(None)* | Click **"Browse as Viewer / Guest"** on the login screen |

---

## 💻 How to Build and Run

### Prerequisites
- JDK 17 or JDK 21 installed (`java -version`).
- MySQL 8 running with credentials matching `src/main/resources/db.properties`.
- Apache Maven 3.8+ (or use IDE built-in Maven).

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Execute Automated Integration Test Suite
```bash
mvn test
```
*Executes all 25 automated tests across Authentication, Database, Fixtures, Match Scheduling, Conflicts, Automatic Winner Advancement, Payments, and PDF/CSV Export.*

### 3. Package Executable Fat JAR
```bash
mvn package -DskipTests
```
Generates the self-contained executable JAR with all dependencies:
`target/esports-tournament-system-1.0.0-jar-with-dependencies.jar` (~12 MB)

### 4. Launch Desktop Application (Java Swing)
```bash
java -jar target/esports-tournament-system-1.0.0-jar-with-dependencies.jar
```

### 5. Launch in Web Browser (Chrome, Edge, Firefox, Brave)
```bash
# Launch via JAR with browser portal flag
java -jar target/esports-tournament-system-1.0.0-jar-with-dependencies.jar --web

# Or run the Web Server directly
java -cp target/esports-tournament-system-1.0.0-jar-with-dependencies.jar com.esports.web.WebDashboardServer
```
Then open your browser to: **`http://localhost:8080`**
