package com.benesse.workoutbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 運動リアクションエンティティ（Lombok版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutReaction {
    private Long reactionId;
    private String workoutId;
    private String userId;
    private String reactionType; // 'like', 'great', 'fire'
    private LocalDateTime createdAt;
    // リレーション（JdbcTemplateで手動設定）
    private Workout workout;
    private User user;
} 