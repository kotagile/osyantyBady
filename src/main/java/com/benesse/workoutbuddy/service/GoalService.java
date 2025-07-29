package com.benesse.workoutbuddy.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.benesse.workoutbuddy.dto.GoalDto;
import com.benesse.workoutbuddy.entity.UserGoal;
import com.benesse.workoutbuddy.repository.UserGoalRepository;

/**
 * 目標設定サービス
 */
@Service
public class GoalService {
    
    @Autowired
    private UserGoalRepository userGoalRepository;
    
    public void setGoal(String userId, GoalDto goalDto) {
    	
    	
//    	レポジトリで該当ユーザーIDの最新目標を論理削除
    	userGoalRepository.changeActiveStatus(userId);
    	
//    	フォームで受け取った新しい目標のエンティティ生成
    	UserGoal userGoal = new UserGoal();
    	userGoal.setGoalId(goalDto.getGoalId());
    	userGoal.setUserId(goalDto.getUserId());
    	userGoal.setGoalDuration(goalDto.getGoalDuration());
    	userGoal.setWeeklyFrequency(goalDto.getWeeklyFrequency());
    	userGoal.setExerciseType(goalDto.getExerciseType());
    	userGoal.setSessionTimeMinutes(goalDto.getSessionTimeMinutes());
    	userGoal.setCreatedAt(LocalDateTime.parse(goalDto.getCreatedAt()));
    	userGoal.setUpdatedAt(LocalDateTime.parse(goalDto.getUpdatedAt()));    	
    	userGoal.setIsActive(true);
    	userGoalRepository.save(userGoal);
    	
    	
    	
    	
    };
    
} 