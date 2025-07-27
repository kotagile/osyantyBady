package com.benesse.workoutbuddy.repository;

import com.benesse.workoutbuddy.entity.Workout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 運動記録リポジトリ（JdbcTemplate版）
 */
@Repository
public class WorkoutRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<Workout> workoutRowMapper = new RowMapper<Workout>() {
        @Override
        public Workout mapRow(ResultSet rs, int rowNum) throws SQLException {
            Workout workout = new Workout();
            workout.setWorkoutId(rs.getString("workout_id"));
            workout.setUserId(rs.getString("user_id"));
            workout.setWorkoutDate(rs.getObject("workout_date", LocalDate.class));
            workout.setStartTime(rs.getObject("start_time", LocalDateTime.class));
            workout.setEndTime(rs.getObject("end_time", LocalDateTime.class));
            workout.setDurationSeconds(rs.getObject("duration_seconds", Integer.class));
            workout.setExerciseType(rs.getString("exercise_type"));
            workout.setComment(rs.getString("comment"));
            workout.setStatus(rs.getString("status"));
            workout.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            return workout;
        }
    };
    
    /**
     * ユーザーIDで運動記録を取得
     */
    public List<Workout> findByUserIdOrderByWorkoutDateDesc(String userId) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? ORDER BY workout_date DESC";
        return jdbcTemplate.query(sql, workoutRowMapper, userId);
    }
    
    /**
     * ユーザーIDと日付範囲で運動記録を取得
     */
    public List<Workout> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND workout_date BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, workoutRowMapper, userId, startDate, endDate);
    }
    
    /**
     * ユーザーIDと日付で運動記録を取得
     */
    public List<Workout> findByUserIdAndWorkoutDate(String userId, LocalDate workoutDate) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND workout_date = ?";
        return jdbcTemplate.query(sql, workoutRowMapper, userId, workoutDate);
    }
    
    /**
     * ユーザーIDと日付で運動記録数をカウント
     */
    public int countByUserIdAndDate(String userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM workouts WHERE user_id = ? AND workout_date = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, date);
    }
    
    /**
     * 進行中の運動記録を取得
     */
    public Optional<Workout> findByUserIdAndStatus(String userId, String status) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND status = ?";
        List<Workout> workouts = jdbcTemplate.query(sql, workoutRowMapper, userId, status);
        return workouts.isEmpty() ? Optional.empty() : Optional.of(workouts.get(0));
    }
    
    /**
     * 完了済みの運動記録を取得
     */
    public List<Workout> findByUserIdAndStatusOrderByWorkoutDateDesc(String userId, String status) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND status = ? ORDER BY workout_date DESC";
        return jdbcTemplate.query(sql, workoutRowMapper, userId, status);
    }
    
    /**
     * 期間内の運動日数をカウント（重複排除）
     */
    public int countDistinctWorkoutDays(String userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(DISTINCT workout_date) FROM workouts WHERE user_id = ? AND workout_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, startDate, endDate);
    }
    
    /**
     * 運動記録を保存
     */
    public Workout save(Workout workout) {
        // 既存の運動記録をチェック
        Optional<Workout> existingWorkout = findById(workout.getWorkoutId());
        
        if (existingWorkout.isEmpty()) {
            // 新規作成
            String sql = "INSERT INTO workouts (workout_id, user_id, workout_date, start_time, end_time, duration_seconds, exercise_type, comment, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                workout.getWorkoutId(),
                workout.getUserId(),
                workout.getWorkoutDate(),
                workout.getStartTime(),
                workout.getEndTime(),
                workout.getDurationSeconds(),
                workout.getExerciseType(),
                workout.getComment(),
                workout.getStatus(),
                workout.getCreatedAt()
            );
        } else {
            // 更新
            String sql = "UPDATE workouts SET end_time = ?, duration_seconds = ?, comment = ?, status = ? WHERE workout_id = ?";
            jdbcTemplate.update(sql,
                workout.getEndTime(),
                workout.getDurationSeconds(),
                workout.getComment(),
                workout.getStatus(),
                workout.getWorkoutId()
            );
        }
        return workout;
    }
    
    /**
     * 運動記録をIDで取得
     */
    public Optional<Workout> findById(String workoutId) {
        String sql = "SELECT * FROM workouts WHERE workout_id = ?";
        List<Workout> workouts = jdbcTemplate.query(sql, workoutRowMapper, workoutId);
        return workouts.isEmpty() ? Optional.empty() : Optional.of(workouts.get(0));
    }
} 