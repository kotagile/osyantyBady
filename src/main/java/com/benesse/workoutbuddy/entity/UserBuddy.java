package com.benesse.workoutbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ユーザーバディ関係エンティティ（Lombok版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBuddy {
    private Long buddyId;
    private String requesterId;
    private String requestedId;
    private String status; // 'pending', 'accepted', 'rejected'
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    // リレーション（JdbcTemplateで手動設定）
    private User requester;
    private User requested;
} 