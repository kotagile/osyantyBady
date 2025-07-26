package com.benesse.workoutbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ（JdbcTemplate版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private String userId;
    private String userName;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    // リレーション（JdbcTemplateで手動設定）
    private UserGoal userGoal;
} 