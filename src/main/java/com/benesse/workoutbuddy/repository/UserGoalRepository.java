package com.benesse.workoutbuddy.repository;

import com.benesse.workoutbuddy.entity.UserGoal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ユーザー目標リポジトリ（JdbcTemplate版）
 */
@Repository
public class UserGoalRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<UserGoal> userGoalRowMapper = new RowMapper<UserGoal>() {
        @Override
        public UserGoal mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserGoal goal = new UserGoal();
            goal.setGoalId(rs.getLong("goal_id"));
            goal.setUserId(rs.getString("user_id"));
            goal.setGoalDuration(rs.getString("goal_duration"));
            goal.setWeeklyFrequency(rs.getInt("weekly_frequency"));
            goal.setExerciseType(rs.getString("exercise_type"));
            goal.setSessionTimeMinutes(rs.getInt("session_time_minutes"));
            goal.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            goal.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
            goal.setIsActive(rs.getBoolean("is_active"));
            return goal;
        }
    };
    
    /**
     * ユーザーIDでアクティブな目標を取得
     */
    public Optional<UserGoal> findActiveGoalByUserId(String userId) {
        String sql = "SELECT * FROM user_goals WHERE user_id = ? AND is_active = 1";
        List<UserGoal> goals = jdbcTemplate.query(sql, userGoalRowMapper, userId);
        return goals.isEmpty() ? Optional.empty() : Optional.of(goals.get(0));
    }
    
    /**
     * ユーザーIDでアクティブな目標一覧を取得
     */
    public List<UserGoal> findActiveGoalsByUserId(String userId) {
        String sql = "SELECT * FROM user_goals WHERE user_id = ? AND is_active = 1";
        return jdbcTemplate.query(sql, userGoalRowMapper, userId);
    }
    
    /**
     * ユーザーIDで目標一覧を取得（作成日時順）
     */
    public List<UserGoal> findByUserIdOrderByCreatedAtDesc(String userId) {
        String sql = "SELECT * FROM user_goals WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userGoalRowMapper, userId);
    }
    
    /**
     * ユーザーIDで目標が存在するかチェック
     */
    public boolean existsByUserIdAndIsActiveTrue(String userId) {
        String sql = "SELECT COUNT(*) FROM user_goals WHERE user_id = ? AND is_active = 1";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count > 0;
    }
    
    /**
     * 目標を保存
     */
    public UserGoal save(UserGoal goal) {
        if (goal.getGoalId() == null) {
            // 新規作成
            String sql = "INSERT INTO user_goals (user_id, goal_duration, weekly_frequency, exercise_type, session_time_minutes, created_at, updated_at, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                goal.getUserId(),
                goal.getGoalDuration(),
                goal.getWeeklyFrequency(),
                goal.getExerciseType(),
                goal.getSessionTimeMinutes(),
                goal.getCreatedAt(),
                goal.getUpdatedAt(),
                goal.getIsActive()
            );
            
            // 生成されたIDを取得
            String idSql = "SELECT SCOPE_IDENTITY()";
            Long id = jdbcTemplate.queryForObject(idSql, Long.class);
            goal.setGoalId(id);
        } else {
            // 更新
            String sql = "UPDATE user_goals SET goal_duration = ?, weekly_frequency = ?, exercise_type = ?, session_time_minutes = ?, updated_at = ?, is_active = ? WHERE goal_id = ?";
            jdbcTemplate.update(sql,
                goal.getGoalDuration(),
                goal.getWeeklyFrequency(),
                goal.getExerciseType(),
                goal.getSessionTimeMinutes(),
                goal.getUpdatedAt(),
                goal.getIsActive(),
                goal.getGoalId()
            );
        }
        return goal;
    }
} 