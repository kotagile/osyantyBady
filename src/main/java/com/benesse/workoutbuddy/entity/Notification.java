package com.benesse.workoutbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知エンティティ（Lombok版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private String notificationId;
    private String fromUserId;
    private String toUserId;
    private String notificationType; // 'workout_completed', 'buddy_request', 'reaction'
    private String title;
    private String message;
    private String relatedData; // JSON形式でデータ保存
    private Boolean isRead;
    private LocalDateTime createdAt;
    // リレーション（JdbcTemplateで手動設定）
    private User fromUser;
    private User toUser;
} 