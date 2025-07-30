package com.benesse.workoutbuddy.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.benesse.workoutbuddy.dto.EditedGoalDto;
import com.benesse.workoutbuddy.entity.UserGoal;
import com.benesse.workoutbuddy.repository.UserGoalRepository;

/**
 * 目標設定サービス
 */
@Service
public class GoalService {
	
	@Autowired
	private UserGoalRepository userGoalRepository;
	
	/**
	 * 目標DB挿入用サービス
	 * @param userId
	 * @param goalDto
	 */
	public void setGoal(String userId, EditedGoalDto goalDto) {
		
		//    	レポジトリで該当ユーザーIDの目標を論理削除（削除後にフォームの目標をDB挿入）
		userGoalRepository.changeActiveStatus(userId);
		
		//    	フォームで受け取った新しい目標をエンティティ型に変換
		UserGoal userGoal = new UserGoal();
		
		userGoal.setGoalId(goalDto.getGoalId());
		
		//本当はセッションからユーザーIDを取得する
		userGoal.setUserId(userId);
		
		userGoal.setGoalDuration(goalDto.getGoalDuration());
		userGoal.setWeeklyFrequency(goalDto.getWeeklyFrequency());
		userGoal.setExerciseType(goalDto.getExerciseType());
		userGoal.setSessionTimeMinutes(goalDto.getSessionTimeMinutes());
		userGoal.setCreatedAt(LocalDateTime.now());
		userGoal.setUpdatedAt(LocalDateTime.now());
		userGoal.setIsActive(true);
		userGoalRepository.save(userGoal);
		
	};
	
	public EditedGoalDto getLatestGoal(String userId) {
		UserGoal latestGoalEntity = userGoalRepository.getLatestGoal(userId);
		
		EditedGoalDto latestGoalDto = new EditedGoalDto();
		
		latestGoalDto.setGoalId(latestGoalEntity.getGoalId());
		latestGoalDto.setUserId(latestGoalEntity.getUserId());
		latestGoalDto.setGoalDuration(latestGoalEntity.getGoalDuration());
		latestGoalDto.setWeeklyFrequency(latestGoalEntity.getWeeklyFrequency());
		latestGoalDto.setExerciseType(latestGoalEntity.getExerciseType());
		latestGoalDto.setSessionTimeMinutes(latestGoalEntity.getSessionTimeMinutes());
		latestGoalDto.setIsActive(latestGoalEntity.getIsActive());
		latestGoalDto.setCreatedAt(latestGoalEntity.getCreatedAt());
		latestGoalDto.setUpdatedAt(latestGoalEntity.getUpdatedAt());
		
		return latestGoalDto;
	}
}