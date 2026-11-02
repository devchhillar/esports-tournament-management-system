-- ============================================================
-- Professional eSports Tournament Management System Schema
-- Database: esports_management
-- Architecture: MySQL 8+ with Strict Constraints & Indexes
-- ============================================================

CREATE DATABASE IF NOT EXISTS `esports_management`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `esports_management`;

-- Drop child tables first
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `tournament_standings`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `login_history`;
DROP TABLE IF EXISTS `match_results`;
DROP TABLE IF EXISTS `matches`;
DROP TABLE IF EXISTS `registrations`;
DROP TABLE IF EXISTS `tournaments`;
DROP TABLE IF EXISTS `team_members`;
DROP TABLE IF EXISTS `teams`;
DROP TABLE IF EXISTS `players`;
DROP TABLE IF EXISTS `games`;
DROP TABLE IF EXISTS `users`;

-- ------------------------------------------------------------
-- 1. Table: users
-- ------------------------------------------------------------
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `role` ENUM('ADMIN', 'TEAM_MANAGER', 'PLAYER', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `profile_picture` VARCHAR(255) DEFAULT NULL,
    `status` ENUM('ACTIVE', 'SUSPENDED', 'PENDING') DEFAULT 'ACTIVE',
    `last_login` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_users_role` (`role`),
    INDEX `idx_users_status` (`status`)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. Table: login_history
-- ------------------------------------------------------------
CREATE TABLE `login_history` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `login_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `ip_address` VARCHAR(50) DEFAULT '127.0.0.1',
    `status` VARCHAR(50) DEFAULT 'SUCCESS',
    CONSTRAINT `fk_login_user` FOREIGN KEY (`user_id`)
        REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. Table: games (Dedicated Game Management Module)
-- ------------------------------------------------------------
CREATE TABLE `games` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `category` VARCHAR(50) NOT NULL,
    `description` TEXT,
    `logo_path` VARCHAR(255) DEFAULT NULL,
    `min_team_size` INT NOT NULL DEFAULT 1,
    `max_team_size` INT NOT NULL DEFAULT 5,
    `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. Table: teams
-- ------------------------------------------------------------
CREATE TABLE `teams` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `team_name` VARCHAR(100) NOT NULL UNIQUE,
    `team_tag` VARCHAR(10) NOT NULL UNIQUE,
    `leader_id` INT NOT NULL,
    `manager_id` INT DEFAULT NULL,
    `game` VARCHAR(100) NOT NULL,
    `logo_path` VARCHAR(255) DEFAULT NULL,
    `description` TEXT,
    `status` ENUM('ACTIVE', 'INACTIVE', 'DISBANDED') DEFAULT 'ACTIVE',
    `ranking_points` INT DEFAULT 1000,
    `matches_played` INT DEFAULT 0,
    `matches_won` INT DEFAULT 0,
    `matches_lost` INT DEFAULT 0,
    `total_points` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_teams_leader` FOREIGN KEY (`leader_id`) 
        REFERENCES `users`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_teams_manager` FOREIGN KEY (`manager_id`) 
        REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. Table: players (Player Profile & Career Statistics)
-- ------------------------------------------------------------
CREATE TABLE `players` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL UNIQUE,
    `gamer_tag` VARCHAR(50) NOT NULL UNIQUE,
    `bio` TEXT,
    `profile_picture` VARCHAR(255) DEFAULT NULL,
    `primary_game` VARCHAR(100) DEFAULT 'Valorant',
    `in_game_role` VARCHAR(50) DEFAULT 'Assault / Fragger',
    `current_team_id` INT DEFAULT NULL,
    `dob` DATE DEFAULT NULL,
    `country` VARCHAR(50) DEFAULT 'India',
    `ranking_points` INT DEFAULT 1000,
    `matches_played` INT DEFAULT 0,
    `wins` INT DEFAULT 0,
    `losses` INT DEFAULT 0,
    `kills` INT DEFAULT 0,
    `mvp_count` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_players_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_players_team` FOREIGN KEY (`current_team_id`) 
        REFERENCES `teams`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. Table: team_members
-- ------------------------------------------------------------
CREATE TABLE `team_members` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `team_id` INT NOT NULL,
    `player_id` INT NOT NULL,
    `role` ENUM('CAPTAIN', 'MEMBER', 'SUBSTITUTE') DEFAULT 'MEMBER',
    `joined_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_members_team` FOREIGN KEY (`team_id`) 
        REFERENCES `teams`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_members_player` FOREIGN KEY (`player_id`) 
        REFERENCES `players`(`id`) ON DELETE CASCADE,
    CONSTRAINT `uq_team_player` UNIQUE (`team_id`, `player_id`)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. Table: tournaments
-- ------------------------------------------------------------
CREATE TABLE `tournaments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `game` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `banner_path` VARCHAR(255) DEFAULT NULL,
    `logo_path` VARCHAR(255) DEFAULT NULL,
    `format` ENUM('SINGLE_ELIMINATION', 'DOUBLE_ELIMINATION', 'ROUND_ROBIN', 'GROUP_STAGE_KNOCKOUT') DEFAULT 'SINGLE_ELIMINATION',
    `min_teams` INT NOT NULL DEFAULT 2,
    `max_teams` INT NOT NULL DEFAULT 16,
    `reg_open_date` DATETIME DEFAULT NULL,
    `reg_close_date` DATETIME DEFAULT NULL,
    `start_date` DATETIME NOT NULL,
    `end_date` DATETIME NOT NULL,
    `entry_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `prize_pool` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `first_prize` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `second_prize` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `third_prize` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `organizer` VARCHAR(150) DEFAULT 'eSports Arena Global',
    `rules` TEXT,
    `status` ENUM('DRAFT', 'UPCOMING', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'UPCOMING',
    `created_by` INT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_tournaments_creator` FOREIGN KEY (`created_by`) 
        REFERENCES `users`(`id`)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 8. Table: registrations
-- ------------------------------------------------------------
CREATE TABLE `registrations` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tournament_id` INT NOT NULL,
    `team_id` INT NOT NULL,
    `registered_by` INT NOT NULL,
    `status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_reg_tournament` FOREIGN KEY (`tournament_id`) 
        REFERENCES `tournaments`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reg_team` FOREIGN KEY (`team_id`) 
        REFERENCES `teams`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reg_user` FOREIGN KEY (`registered_by`) 
        REFERENCES `users`(`id`),
    CONSTRAINT `uq_tourney_team` UNIQUE (`tournament_id`, `team_id`)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 9. Table: matches
-- ------------------------------------------------------------
CREATE TABLE `matches` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tournament_id` INT NOT NULL,
    `round_name` VARCHAR(50) NOT NULL,
    `match_number` INT DEFAULT 1,
    `bracket_type` ENUM('WINNERS', 'LOSERS', 'GRAND_FINAL', 'GROUP', 'KNOCKOUT') DEFAULT 'KNOCKOUT',
    `group_name` VARCHAR(20) DEFAULT NULL,
    `team1_id` INT DEFAULT NULL,
    `team2_id` INT DEFAULT NULL,
    `team1_score` INT DEFAULT 0,
    `team2_score` INT DEFAULT 0,
    `winner_team_id` INT DEFAULT NULL,
    `scheduled_time` DATETIME NOT NULL,
    `venue` VARCHAR(150) DEFAULT 'Online Server / Arena 1',
    `referee` VARCHAR(100) DEFAULT 'Official Esports Referee',
    `next_match_id` INT DEFAULT NULL,
    `next_match_slot` INT DEFAULT NULL,
    `status` ENUM('SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_matches_tournament` FOREIGN KEY (`tournament_id`) 
        REFERENCES `tournaments`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_matches_team1` FOREIGN KEY (`team1_id`) 
        REFERENCES `teams`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_matches_team2` FOREIGN KEY (`team2_id`) 
        REFERENCES `teams`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_matches_winner` FOREIGN KEY (`winner_team_id`) 
        REFERENCES `teams`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 10. Table: match_results
-- ------------------------------------------------------------
CREATE TABLE `match_results` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `match_id` INT NOT NULL UNIQUE,
    `winner_team_id` INT NOT NULL,
    `team1_score` INT NOT NULL DEFAULT 0,
    `team2_score` INT NOT NULL DEFAULT 0,
    `mvp_player_id` INT DEFAULT NULL,
    `notes` TEXT,
    `recorded_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_results_match` FOREIGN KEY (`match_id`) 
        REFERENCES `matches`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_results_winner` FOREIGN KEY (`winner_team_id`) 
        REFERENCES `teams`(`id`),
    CONSTRAINT `fk_results_mvp` FOREIGN KEY (`mvp_player_id`) 
        REFERENCES `players`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 11. Table: tournament_standings
-- ------------------------------------------------------------
CREATE TABLE `tournament_standings` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tournament_id` INT NOT NULL,
    `team_id` INT NOT NULL,
    `group_name` VARCHAR(20) DEFAULT NULL,
    `matches_played` INT DEFAULT 0,
    `wins` INT DEFAULT 0,
    `losses` INT DEFAULT 0,
    `draws` INT DEFAULT 0,
    `points` INT DEFAULT 0,
    `score_diff` INT DEFAULT 0,
    `rank_position` INT DEFAULT 0,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_standings_tourney` FOREIGN KEY (`tournament_id`) 
        REFERENCES `tournaments`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_standings_team` FOREIGN KEY (`team_id`) 
        REFERENCES `teams`(`id`) ON DELETE CASCADE,
    CONSTRAINT `uq_tourney_team_standing` UNIQUE (`tournament_id`, `team_id`)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 12. Table: notifications
-- ------------------------------------------------------------
CREATE TABLE `notifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `message` TEXT NOT NULL,
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) 
        REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 13. Table: payments (Payment & Prize Distribution Management)
-- ------------------------------------------------------------
CREATE TABLE `payments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tournament_id` INT NOT NULL,
    `team_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `payment_type` ENUM('ENTRY_FEE', 'PRIZE_PAYOUT', 'REFUND') NOT NULL DEFAULT 'ENTRY_FEE',
    `payment_method` ENUM('UPI', 'CREDIT_CARD', 'NET_BANKING', 'WALLET', 'OFFLINE') NOT NULL DEFAULT 'UPI',
    `transaction_id` VARCHAR(100) NOT NULL UNIQUE,
    `status` ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'COMPLETED',
    `payment_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `notes` TEXT,
    CONSTRAINT `fk_payment_tournament` FOREIGN KEY (`tournament_id`)
        REFERENCES `tournaments`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_payment_team` FOREIGN KEY (`team_id`)
        REFERENCES `teams`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`)
        REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_payment_status` (`status`),
    INDEX `idx_payment_type` (`payment_type`)
) ENGINE=InnoDB;

-- ============================================================
-- Default Demonstration Seed Data
-- Passwords:
-- Admin:        admin       / Admin@123
-- Team Manager: manager1    / Manager@123
-- Players:      phoenix, shadow, mortal, scout / Player@123
-- Viewer:       viewer1     / Viewer@123
-- ============================================================

-- Users
INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `role`, `full_name`, `phone`, `status`, `last_login`) VALUES
(1, 'admin', 'admin@esports.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'ADMIN', 'System Administrator', '+91 9876543210', 'ACTIVE', NOW()),
(2, 'manager1', 'manager@soultitans.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'TEAM_MANAGER', 'Vikram Rathore', '+91 9876543220', 'ACTIVE', NOW()),
(3, 'phoenix', 'phoenix@gamer.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'PLAYER', 'Rahul Sharma', '+91 9876543211', 'ACTIVE', NOW()),
(4, 'shadow', 'shadow@gamer.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'PLAYER', 'Aman Verma', '+91 9876543212', 'ACTIVE', NOW()),
(5, 'mortal', 'mortal@gamer.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'PLAYER', 'Naman Mathur', '+91 9876543214', 'ACTIVE', NOW()),
(6, 'scout', 'scout@gamer.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'PLAYER', 'Tanmay Singh', '+91 9876543215', 'ACTIVE', NOW()),
(7, 'viewer1', 'viewer@esports.com', '$2a$12$fln37rcyCEqU9pxalOzfFeHQPG3dLKN6lk4fitgaU1Gss.EREfwIi', 'VIEWER', 'Rohan Gupta', '+91 9876543213', 'ACTIVE', NOW());

-- Login History
INSERT INTO `login_history` (`user_id`, `ip_address`, `status`) VALUES
(1, '127.0.0.1', 'SUCCESS'),
(2, '127.0.0.1', 'SUCCESS'),
(3, '127.0.0.1', 'SUCCESS');

-- 8 Default Games
INSERT INTO `games` (`id`, `name`, `category`, `description`, `min_team_size`, `max_team_size`, `status`) VALUES
(1, 'BGMI', 'Battle Royale', 'Battlegrounds Mobile India squad competitive battle royale.', 4, 5, 'ACTIVE'),
(2, 'Valorant', 'Tactical FPS', '5v5 character-based tactical shooter by Riot Games.', 5, 6, 'ACTIVE'),
(3, 'Free Fire', 'Battle Royale', 'Fast-paced mobile survival shooter game.', 4, 5, 'ACTIVE'),
(4, 'Counter-Strike 2', 'Tactical FPS', 'Valve premier 5v5 competitive tactical FPS shooter.', 5, 6, 'ACTIVE'),
(5, 'Call of Duty', 'FPS / Battle Royale', 'Modern warfare tactical and warzone competitive action.', 4, 5, 'ACTIVE'),
(6, 'Dota 2', 'MOBA', 'Legendary 5v5 strategy MOBA played on competitive maps.', 5, 6, 'ACTIVE'),
(7, 'League of Legends', 'MOBA', '5v5 team strategy game with champions and lanes.', 5, 6, 'ACTIVE'),
(8, 'Custom Games', 'Various', 'Custom community competitive formats and scrim matches.', 1, 10, 'ACTIVE');

-- Teams
INSERT INTO `teams` (`id`, `team_name`, `team_tag`, `leader_id`, `manager_id`, `game`, `description`, `ranking_points`, `matches_played`, `matches_won`, `matches_lost`, `total_points`) VALUES
(1, 'Soul Titans', 'SOUL', 3, 2, 'Valorant', 'Premier Indian competitive Valorant lineup.', 1500, 10, 8, 2, 24),
(2, 'GodLike Warriors', 'GL', 4, 2, 'Counter-Strike 2', 'Aggressive CS2 tactical entry fraggers.', 1420, 10, 7, 3, 21),
(3, 'Global Esports', 'GE', 5, NULL, 'Valorant', 'International VCT Pacific franchise team.', 1600, 12, 10, 2, 30),
(4, 'Team Secret', 'TS', 6, NULL, 'BGMI', 'Elite tier-1 BGMI battle royale squad.', 1350, 8, 5, 3, 15);

-- Players
INSERT INTO `players` (`id`, `user_id`, `gamer_tag`, `bio`, `primary_game`, `in_game_role`, `current_team_id`, `ranking_points`, `matches_played`, `wins`, `losses`, `kills`, `mvp_count`, `country`) VALUES
(1, 3, 'PHOENIX_OP', 'Competitive Valorant Fragger and Jett main', 'Valorant', 'Duelist / Entry', 1, 1550, 15, 12, 3, 312, 6, 'India'),
(2, 4, 'SHADOW_SNIPER', 'AWP specialist and CS2 Entry Fragger', 'Counter-Strike 2', 'Sniper / AWP', 2, 1420, 14, 10, 4, 280, 4, 'India'),
(3, 5, 'MORTAL_GOD', 'Legendary IGL and controller strategist', 'Valorant', 'IGL / Controller', 3, 1680, 20, 16, 4, 385, 9, 'India'),
(4, 6, 'SCOUT_TX', 'High speed assault rifle specialist', 'BGMI', 'Assault / Fragger', 4, 1390, 12, 8, 4, 245, 3, 'India');

-- Team Members
INSERT INTO `team_members` (`team_id`, `player_id`, `role`) VALUES
(1, 1, 'CAPTAIN'),
(2, 2, 'CAPTAIN'),
(3, 3, 'CAPTAIN'),
(4, 4, 'CAPTAIN');

-- Tournaments
INSERT INTO `tournaments` (`id`, `title`, `game`, `description`, `format`, `min_teams`, `max_teams`, `reg_open_date`, `reg_close_date`, `start_date`, `end_date`, `entry_fee`, `prize_pool`, `first_prize`, `second_prize`, `third_prize`, `organizer`, `status`, `created_by`) VALUES
(1, 'Valorant Champions Invitational 2026', 'Valorant', 'Premier 5v5 tactical tournament with India top esports rosters.', 'SINGLE_ELIMINATION', 4, 16, NOW() - INTERVAL 5 DAY, NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 7 DAY, 500.00, 100000.00, 60000.00, 30000.00, 10000.00, 'Riot Esports India', 'REGISTRATION_OPEN', 1),
(2, 'BGMI Battle Royale Masters', 'BGMI', 'Squad battle royale tournament with intense showdown on Erangel.', 'ROUND_ROBIN', 4, 20, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 5 DAY, 300.00, 50000.00, 30000.00, 15000.00, 5000.00, 'Krafton India', 'REGISTRATION_CLOSED', 1),
(3, 'CS2 Winter Strike Championship', 'Counter-Strike 2', 'High adrenaline CS2 tournament, Best of 3 competitive bracket.', 'DOUBLE_ELIMINATION', 4, 8, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 3 DAY, NOW() + INTERVAL 2 DAY, 400.00, 75000.00, 45000.00, 20000.00, 10000.00, 'Valve India Pro', 'ONGOING', 1),
(4, 'Valorant Challengers League', 'Valorant', 'Group Stage and Knockout tournament for challenger tier.', 'GROUP_STAGE_KNOCKOUT', 4, 8, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 15 DAY, NOW() + INTERVAL 20 DAY, NOW() + INTERVAL 28 DAY, 250.00, 40000.00, 25000.00, 10000.00, 5000.00, 'eSports Arena', 'UPCOMING', 1);

-- Registrations
INSERT INTO `registrations` (`tournament_id`, `team_id`, `registered_by`, `status`) VALUES
(1, 1, 2, 'APPROVED'),
(1, 3, 5, 'APPROVED'),
(2, 4, 6, 'APPROVED'),
(3, 1, 2, 'APPROVED'),
(3, 2, 4, 'APPROVED');

-- Matches
INSERT INTO `matches` (`id`, `tournament_id`, `round_name`, `match_number`, `bracket_type`, `team1_id`, `team2_id`, `team1_score`, `team2_score`, `winner_team_id`, `scheduled_time`, `status`) VALUES
(1, 3, 'Upper Bracket Round 1', 1, 'WINNERS', 1, 2, 2, 1, 1, NOW() - INTERVAL 2 DAY, 'COMPLETED'),
(2, 3, 'Upper Bracket Finals', 2, 'WINNERS', 1, 3, 1, 2, 3, NOW() - INTERVAL 1 DAY, 'COMPLETED'),
(3, 3, 'Lower Bracket Finals', 3, 'LOSERS', 2, 1, 0, 0, NULL, NOW() + INTERVAL 4 HOUR, 'SCHEDULED'),
(4, 1, 'Quarterfinals Match 1', 1, 'KNOCKOUT', 1, 3, 0, 0, NULL, NOW() + INTERVAL 2 DAY, 'SCHEDULED');

-- Match Results
INSERT INTO `match_results` (`match_id`, `winner_team_id`, `team1_score`, `team2_score`, `mvp_player_id`, `notes`) VALUES
(1, 1, 2, 1, 1, 'Soul Titans won 13-11 on Bind, 13-9 on Ascent.'),
(2, 3, 1, 2, 3, 'Global Esports clinched decisive overtime 15-13 on Haven.');

-- Standings
INSERT INTO `tournament_standings` (`tournament_id`, `team_id`, `matches_played`, `wins`, `losses`, `draws`, `points`, `score_diff`, `rank_position`) VALUES
(3, 3, 1, 1, 0, 0, 3, 1, 1),
(3, 1, 2, 1, 1, 0, 3, 0, 2),
(3, 2, 1, 0, 1, 0, 0, -1, 3);

-- Notifications
INSERT INTO `notifications` (`user_id`, `title`, `message`) VALUES
(1, 'System Initialized', 'eSports Tournament Management System is fully operational.'),
(2, 'Registration Approved', 'Soul Titans registration for Valorant Champions Invitational 2026 has been approved!'),
(3, 'Match Scheduled', 'Your match against Global Esports is scheduled for upcoming tournament round.');

-- Payments
INSERT INTO `payments` (`id`, `tournament_id`, `team_id`, `user_id`, `amount`, `payment_type`, `payment_method`, `transaction_id`, `status`, `notes`) VALUES
(1, 1, 1, 2, 500.00, 'ENTRY_FEE', 'UPI', 'TXN_VAL_1001', 'COMPLETED', 'Entry fee payment for Soul Titans in Valorant Champions 2026'),
(2, 1, 3, 5, 500.00, 'ENTRY_FEE', 'CREDIT_CARD', 'TXN_VAL_1002', 'COMPLETED', 'Entry fee payment for Global Esports'),
(3, 2, 4, 6, 300.00, 'ENTRY_FEE', 'NET_BANKING', 'TXN_BGMI_2001', 'COMPLETED', 'Entry fee payment for Team Secret in BGMI Masters'),
(4, 3, 1, 2, 400.00, 'ENTRY_FEE', 'WALLET', 'TXN_CS2_3001', 'COMPLETED', 'Entry fee for Soul Titans in CS2 Winter Strike'),
(5, 3, 2, 4, 400.00, 'ENTRY_FEE', 'UPI', 'TXN_CS2_3002', 'COMPLETED', 'Entry fee for GodLike Warriors in CS2 Winter Strike');
