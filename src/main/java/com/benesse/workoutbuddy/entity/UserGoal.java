package com.benesse.workoutbuddy.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * ユーザー目標エンティティ（JdbcTemplate版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGoal {
    
    private Long goalId;
    private String userId;
    private String goalDuration; // '3months', '6months', '1year'
    private Integer weeklyFrequency;
    private String exerciseType;
    private Integer sessionTimeMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    // リレーション（JdbcTemplateで手動設定）
    private User user;
} 