package com.esports.dao;

import com.esports.model.Notification;
import java.util.List;

public interface NotificationDAO {
    boolean sendNotification(int userId, String title, String message);
    boolean broadcast(String title, String message);
    List<Notification> getByUser(int userId);
    boolean markAsRead(int notificationId);
    int countUnread(int userId);
}
