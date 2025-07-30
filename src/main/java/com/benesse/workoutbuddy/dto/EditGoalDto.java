package com.benesse.workoutbuddy.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditedGoalDto {
    private Long goalId;
    private String userId;
    private String goalDuration;
    private Integer weeklyFrequency;
    private String exerciseType;
    private Integer sessionTimeMinutes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 