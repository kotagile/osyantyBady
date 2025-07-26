package com.benesse.workoutbuddy.service;

import com.benesse.workoutbuddy.dto.GoalDto;
import com.benesse.workoutbuddy.entity.UserGoal;
import com.benesse.workoutbuddy.repository.UserGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 目標設定サービス
 */
@Service
@Transactional
public class GoalService {
    
    @Autowired
    private UserGoalRepository userGoalRepository;
    
    /**
     * 目標設定
     */
    public void setGoal(String userId, GoalDto goalDto) {
        // 既存のアクティブな目標を非アクティブにする
        Optional<UserGoal> existingGoal = userGoalRepository.findActiveGoalByUserId(userId);
        if (existingGoal.isPresent()) {
            UserGoal oldGoal = existingGoal.get();
            oldGoal.setIsActive(false);
            oldGoal.setUpdatedAt(LocalDateTime.now());
            userGoalRepository.save(oldGoal);
        }
        
        // 新しい目標を作成
        UserGoal newGoal = new UserGoal();
        newGoal.setUserId(userId);
        newGoal.setGoalDuration(goalDto.getGoalDuration());
        newGoal.setWeeklyFrequency(goalDto.getWeeklyFrequency());
        newGoal.setExerciseType(goalDto.getExerciseType());
        newGoal.setSessionTimeMinutes(goalDto.getSessionTimeMinutes());
        newGoal.setCreatedAt(LocalDateTime.now());
        newGoal.setUpdatedAt(LocalDateTime.now());
        newGoal.setIsActive(true);
        
        userGoalRepository.save(newGoal);
    }
    
    /**
     * 現在の目標を取得
     */
    @Transactional(readOnly = true)
    public UserGoal getCurrentGoal(String userId) {
        Optional<UserGoal> goal = userGoalRepository.findActiveGoalByUserId(userId);
        return goal.orElse(null);
    }
    
    /**
     * 目標履歴を取得
     */
    @Transactional(readOnly = true)
    public List<UserGoal> getGoalHistory(String userId) {
        return userGoalRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 目標が設定されているかチェック
     */
    @Transactional(readOnly = true)
    public boolean hasActiveGoal(String userId) {
        return userGoalRepository.existsByUserIdAndIsActiveTrue(userId);
    }
    
    /**
     * 目標を削除（非アクティブ化）
     */
    public void deleteGoal(String userId) {
        Optional<UserGoal> activeGoal = userGoalRepository.findActiveGoalByUserId(userId);
        if (activeGoal.isPresent()) {
            UserGoal goal = activeGoal.get();
            goal.setIsActive(false);
            goal.setUpdatedAt(LocalDateTime.now());
            userGoalRepository.save(goal);
        }
    }

    // 既存の目標をDTOで返す（なければ空のDTO）
    public GoalDto getGoalDtoForUser(String userId) {
        try {
            UserGoal existingGoal = getCurrentGoal(userId);
            if (existingGoal != null) {
                GoalDto goalDto = new GoalDto();
                goalDto.setGoalDuration(existingGoal.getGoalDuration());
                goalDto.setWeeklyFrequency(existingGoal.getWeeklyFrequency());
                goalDto.setExerciseType(existingGoal.getExerciseType());
                goalDto.setSessionTimeMinutes(existingGoal.getSessionTimeMinutes());
                return goalDto;
            }
        } catch (RuntimeException e) {
            // 無視して空DTO返却
        }
        return new GoalDto();
    }

    // 目標履歴を安全に返す（例外時は空リスト）
    public java.util.List<UserGoal> getGoalHistorySafe(String userId) {
        try {
            return getGoalHistory(userId);
        } catch (RuntimeException e) {
            return java.util.List.of();
        }
    }
} 