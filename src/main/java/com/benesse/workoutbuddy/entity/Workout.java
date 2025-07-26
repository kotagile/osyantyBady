package com.benesse.workoutbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 運動記録エンティティ（JdbcTemplate版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workout {
    private String workoutId;
    private String userId;
    private LocalDate workoutDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private String exerciseType;
    private String comment;
    private String status; // 'completed', 'paused', 'cancelled', 'in_progress'
    private LocalDateTime createdAt;
    // リレーション（JdbcTemplateで手動設定）
    private User user;
    
    /**
     * 運動が進行中かどうかを判定
     */
    public boolean isInProgress() {
        return "in_progress".equals(status);
    }
    
    /**
     * 運動が完了しているかどうかを判定
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    /**
     * 運動時間を分単位で取得
     */
    public Integer getDurationMinutes() {
        return durationSeconds != null ? durationSeconds / 60 : 0;
    }
} 