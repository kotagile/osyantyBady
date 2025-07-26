package com.benesse.workoutbuddy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalDto {
    private Long goalId;
    private String userId;
    private String goalDuration;
    private Integer weeklyFrequency;
    private String exerciseType;
    private Integer sessionTimeMinutes;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
} 