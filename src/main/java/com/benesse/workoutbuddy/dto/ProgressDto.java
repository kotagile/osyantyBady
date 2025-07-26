package com.benesse.workoutbuddy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDto {
    private String userId;
    private String userName;
    private Integer totalWorkouts;
    private Integer completedWorkouts;
    private Integer totalMinutes;
    private Integer goalFrequency;
    private Integer goalSessionTime;

    public ProgressDto(Integer currentCount, Integer targetCount, Integer progressPercentage, String encouragementMessage) {
        this.totalWorkouts = currentCount;
        this.goalFrequency = targetCount;
        this.completedWorkouts = progressPercentage;
        this.userName = encouragementMessage;
    }

    public ProgressDto(Integer currentCount, Integer targetCount, Integer progressPercentage, String encouragementMessage, String userName, String exerciseType) {
        this.totalWorkouts = currentCount;
        this.goalFrequency = targetCount;
        this.completedWorkouts = progressPercentage;
        this.userName = userName;
        // encouragementMessage, exerciseTypeは用途に応じて適切に割り当ててください
    }
} 