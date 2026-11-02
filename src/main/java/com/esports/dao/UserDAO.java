package com.esports.dao;

import com.esports.model.LoginHistory;
import com.esports.model.Role;
import com.esports.model.User;
import java.util.List;

/**
 * Data Access Object Interface for User and Profile operations.
 */
public interface UserDAO {
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(int id);
    boolean createUser(User user);
    boolean updateProfile(User user);
    boolean updatePassword(int userId, String newHashedPassword);
    boolean updateProfilePicture(int userId, String picturePath);
    boolean updateLastLogin(int userId);
    boolean isUsernameTaken(String username);
    boolean isEmailTaken(String email);
    List<User> findAll();
    List<User> findByRole(Role role);
    
    // Login History
    void recordLoginHistory(int userId, String ipAddress, String status);
    List<LoginHistory> getLoginHistory(int userId);
}
