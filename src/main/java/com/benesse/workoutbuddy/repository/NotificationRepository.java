package com.benesse.workoutbuddy.repository;

import com.benesse.workoutbuddy.entity.Notification;
import com.benesse.workoutbuddy.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知リポジトリ（JdbcTemplate版）
 */
@Repository
public class NotificationRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<Notification> notificationRowMapper = new RowMapper<Notification>() {
        @Override
        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
            Notification notification = new Notification();
            notification.setNotificationId(rs.getString("notification_id"));
            notification.setFromUserId(rs.getString("from_user_id"));
            notification.setToUserId(rs.getString("to_user_id"));
            notification.setNotificationType(rs.getString("notification_type"));
            notification.setTitle(rs.getString("title"));
            notification.setMessage(rs.getString("message"));
            notification.setRelatedData(rs.getString("related_data"));
            notification.setIsRead(rs.getBoolean("is_read"));
            notification.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            
            // 送信者の情報を設定
            if (rs.getString("from_user_id") != null) {
                User fromUser = new User();
                fromUser.setUserId(rs.getString("from_user_id"));
                fromUser.setUserName(rs.getString("from_user_name"));
                notification.setFromUser(fromUser);
            }
            
            // 受信者の情報を設定
            User toUser = new User();
            toUser.setUserId(rs.getString("to_user_id"));
            toUser.setUserName(rs.getString("to_user_name"));
            notification.setToUser(toUser);
            
            return notification;
        }
    };
    
    /**
     * 受信者IDで通知一覧を取得（作成日時順）
     */
    public List<Notification> findByToUserIdOrderByCreatedAtDesc(String toUserId) {
        String sql = "SELECT n.*, u1.user_name as from_user_name, u2.user_name as to_user_name " +
                    "FROM notifications n " +
                    "LEFT JOIN users u1 ON n.from_user_id = u1.user_id " +
                    "LEFT JOIN users u2 ON n.to_user_id = u2.user_id " +
                    "WHERE n.to_user_id = ? " +
                    "ORDER BY n.created_at DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, toUserId);
    }
    
    /**
     * 受信者IDで未読通知一覧を取得
     */
    public List<Notification> findByToUserIdAndIsReadFalseOrderByCreatedAtDesc(String toUserId) {
        String sql = "SELECT n.*, u1.user_name as from_user_name, u2.user_name as to_user_name " +
                    "FROM notifications n " +
                    "LEFT JOIN users u1 ON n.from_user_id = u1.user_id " +
                    "LEFT JOIN users u2 ON n.to_user_id = u2.user_id " +
                    "WHERE n.to_user_id = ? AND n.is_read = 0 " +
                    "ORDER BY n.created_at DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, toUserId);
    }
    
    /**
     * 受信者IDで未読通知数をカウント
     */
    public int countUnreadNotifications(String userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE to_user_id = ? AND is_read = 0";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }
    
    /**
     * 通知種別で通知一覧を取得
     */
    public List<Notification> findByToUserIdAndNotificationTypeOrderByCreatedAtDesc(String toUserId, String notificationType) {
        String sql = "SELECT n.*, u1.user_name as from_user_name, u2.user_name as to_user_name " +
                    "FROM notifications n " +
                    "LEFT JOIN users u1 ON n.from_user_id = u1.user_id " +
                    "LEFT JOIN users u2 ON n.to_user_id = u2.user_id " +
                    "WHERE n.to_user_id = ? AND n.notification_type = ? " +
                    "ORDER BY n.created_at DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, toUserId, notificationType);
    }
    
    /**
     * 受信者IDで通知を既読にする
     */
    public void markAllAsRead(String userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE to_user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
    
    /**
     * 通知を保存
     */
    public Notification save(Notification notification) {
        if (notification.getNotificationId() == null) {
            // 新規作成
            String sql = "INSERT INTO notifications (notification_id, from_user_id, to_user_id, notification_type, title, message, related_data, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                notification.getNotificationId(),
                notification.getFromUserId(),
                notification.getToUserId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedData(),
                notification.getIsRead(),
                notification.getCreatedAt()
            );
        } else {
            // 更新
            String sql = "UPDATE notifications SET is_read = ? WHERE notification_id = ?";
            jdbcTemplate.update(sql,
                notification.getIsRead(),
                notification.getNotificationId()
            );
        }
        return notification;
    }
    
    /**
     * 通知を削除
     */
    public void delete(Notification notification) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        jdbcTemplate.update(sql, notification.getNotificationId());
    }
    
    /**
     * IDで通知を取得
     */
    public Notification findById(String notificationId) {
        String sql = "SELECT n.*, u1.user_name as from_user_name, u2.user_name as to_user_name " +
                    "FROM notifications n " +
                    "LEFT JOIN users u1 ON n.from_user_id = u1.user_id " +
                    "LEFT JOIN users u2 ON n.to_user_id = u2.user_id " +
                    "WHERE n.notification_id = ?";
        List<Notification> notifications = jdbcTemplate.query(sql, notificationRowMapper, notificationId);
        return notifications.isEmpty() ? null : notifications.get(0);
    }
} 