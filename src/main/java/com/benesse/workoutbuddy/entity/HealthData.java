package com.benesse.workoutbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康データエンティティ（Lombok版）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthData {
    private Long healthId;
    private String userId;
    private String dataType; // 'weight', 'body_fat', 'sleep'
    private BigDecimal dataValue;
    private String dataText; // 睡眠時間等のテキストデータ用
    private LocalDate recordedDate;
    private LocalDateTime createdAt;
    // リレーション（JdbcTemplateで手動設定）
    private User user;
} 