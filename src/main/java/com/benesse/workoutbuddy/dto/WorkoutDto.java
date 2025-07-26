package com.benesse.workoutbuddy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDto {
    private String workoutId;
    private String userId;
    private String workoutDate;
    private String startTime;
    private String endTime;
    private Integer durationSeconds;
    private String exerciseType;
    private String comment;
    private String status;
    private String createdAt;
} 