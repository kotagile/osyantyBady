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
 * 運動記録リポジトリクラス
 * 
 * <p>運動記録のデータベースアクセスを担当します。
 * JdbcTemplateを使用してSQLクエリを実行し、運動記録のCRUD操作を提供します。</p>
 * 
 * <p>主な機能：</p>
 * <ul>
 *   <li>運動記録の作成・更新・取得</li>
 *   <li>ユーザー別の運動記録検索</li>
 *   <li>期間別の運動記録検索</li>
 *   <li>運動日数のカウント</li>
 *   <li>進行中運動の検索</li>
 * </ul>
 * 
 * @author nagahama
 * @version 1.0
 * @since 2024-01-01
 */
@Repository
public class WorkoutRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * WorkoutエンティティのRowMapper
     * 
     * <p>データベースの結果セットをWorkoutエンティティにマッピングします。</p>
     */
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
     * ユーザーIDで運動記録を取得（運動日降順）
     * 
     * <p>指定されたユーザーの全ての運動記録を運動日の降順で取得します。</p>
     * 
     * @param userId ユーザーID
     * @return 運動記録のリスト（運動日降順）
     */
    public List<Workout> findByUserIdOrderByWorkoutDateDesc(String userId) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? ORDER BY workout_date DESC";
        return jdbcTemplate.query(sql, workoutRowMapper, userId);
    }
    
    /**
     * ユーザーIDと日付範囲で運動記録を取得
     * 
     * <p>指定された期間内の運動記録を取得します。</p>
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 期間内の運動記録リスト
     */
    public List<Workout> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND workout_date BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, workoutRowMapper, userId, startDate, endDate);
    }
    
    /**
     * ユーザーIDと日付で運動記録を取得
     * 
     * <p>指定された日付の運動記録を取得します。</p>
     * 
     * @param userId ユーザーID
     * @param workoutDate 運動日
     * @return 指定日の運動記録リスト
     */
    public List<Workout> findByUserIdAndWorkoutDate(String userId, LocalDate workoutDate) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND workout_date = ?";
        return jdbcTemplate.query(sql, workoutRowMapper, userId, workoutDate);
    }
    
    /**
     * ユーザーIDと日付で運動記録数をカウント
     * 
     * <p>指定された日付の運動記録数を取得します。</p>
     * 
     * @param userId ユーザーID
     * @param date 対象日
     * @return 運動記録数
     */
    public int countByUserIdAndDate(String userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM workouts WHERE user_id = ? AND workout_date = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, date);
    }
    
    /**
     * 進行中の運動記録を取得
     * 
     * <p>指定されたユーザーの進行中（in_progress）の運動記録を取得します。
     * 通常、1ユーザーにつき1つの進行中運動のみ存在します。</p>
     * 
     * @param userId ユーザーID
     * @param status 運動ステータス（通常は"in_progress"）
     * @return 進行中の運動記録（存在しない場合は空）
     */
    public Optional<Workout> findByUserIdAndStatus(String userId, String status) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND status = ?";
        List<Workout> workouts = jdbcTemplate.query(sql, workoutRowMapper, userId, status);
        return workouts.isEmpty() ? Optional.empty() : Optional.of(workouts.get(0));
    }
    
    /**
     * 完了済みの運動記録を取得（運動日降順）
     * 
     * <p>指定されたユーザーの完了済み運動記録を運動日の降順で取得します。</p>
     * 
     * @param userId ユーザーID
     * @param status 運動ステータス（通常は"completed"）
     * @return 完了済み運動記録のリスト（運動日降順）
     */
    public List<Workout> findByUserIdAndStatusOrderByWorkoutDateDesc(String userId, String status) {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND status = ? ORDER BY workout_date DESC";
        return jdbcTemplate.query(sql, workoutRowMapper, userId, status);
    }
    
    /**
     * 期間内の運動日数をカウント（重複排除）
     * 
     * <p>指定された期間内の運動日数を重複を除いてカウントします。
     * 週間進捗の計算に使用されます。</p>
     * 
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 運動日数（重複排除済み）
     */
    public int countDistinctWorkoutDays(String userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(DISTINCT workout_date) FROM workouts WHERE user_id = ? AND workout_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, startDate, endDate);
    }
    
    /**
     * 運動記録を保存
     * 
     * <p>運動記録をデータベースに保存します。
     * 新規作成の場合はINSERT、既存レコードの場合はUPDATEを実行します。</p>
     * 
     * @param workout 保存する運動記録
     * @return 保存された運動記録
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
     * 
     * <p>指定された運動IDの運動記録を取得します。</p>
     * 
     * @param workoutId 運動ID
     * @return 運動記録（存在しない場合は空）
     */
    public Optional<Workout> findById(String workoutId) {
        String sql = "SELECT * FROM workouts WHERE workout_id = ?";
        List<Workout> workouts = jdbcTemplate.query(sql, workoutRowMapper, workoutId);
        return workouts.isEmpty() ? Optional.empty() : Optional.of(workouts.get(0));
    }
} 