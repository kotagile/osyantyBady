package com.benesse.workoutbuddy.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benesse.workoutbuddy.dto.ProgressDto;
import com.benesse.workoutbuddy.entity.User;
import com.benesse.workoutbuddy.entity.UserGoal;
import com.benesse.workoutbuddy.entity.Workout;
import com.benesse.workoutbuddy.repository.UserGoalRepository;
import com.benesse.workoutbuddy.repository.UserRepository;
import com.benesse.workoutbuddy.repository.WorkoutRepository;

/**
 * 運動記録サービスクラス
 * 
 * <p>運動の開始、完了、進捗管理、記録取得などのビジネスロジックを提供します。
 * ユーザーの運動習慣の継続をサポートする機能を実装しています。</p>
 * 
 * <p>主な機能：</p>
 * <ul>
 *   <li>運動セッションの開始・完了</li>
 *   <li>週間進捗の計算</li>
 *   <li>運動記録の取得・管理</li>
 *   <li>リアクション機能</li>
 *   <li>バディへの通知</li>
 * </ul>
 * 
 * @author nagahama
 * @version 1.0
 * @since 2024-01-01
 */
@Service
@Transactional
public class WorkoutService {
    
    @Autowired
    private WorkoutRepository workoutRepository;
    
    @Autowired
    private UserGoalRepository userGoalRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 運動を開始
     * 
     * <p>指定された運動種別で新しい運動セッションを開始します。
     * 進行中の運動がある場合は例外をスローします。</p>
     * 
     * @param userId ユーザーID
     * @param exerciseType 運動種別（例：ランニング、筋トレ、ウォーキング）
     * @return 作成された運動記録
     * @throws RuntimeException アクティブな目標が設定されていない場合、または既に進行中の運動がある場合
     */
    public Workout startWorkout(String userId, String exerciseType) {
        // 目標取得（存在チェックのみ）
        userGoalRepository.findActiveGoalByUserId(userId)
            .orElseThrow(() -> new RuntimeException("アクティブな目標が設定されていません"));
        
        // 重複チェック（同日の進行中運動）
        Optional<Workout> ongoingWorkout = workoutRepository.findByUserIdAndStatus(userId, "in_progress");
        
        if (ongoingWorkout.isPresent()) {
            throw new RuntimeException("既に進行中の運動があります");
        }
        
        // 運動開始
        Workout workout = new Workout();
        String workoutId = UUID.randomUUID().toString();
        workout.setWorkoutId(workoutId);
        workout.setUserId(userId);
        workout.setWorkoutDate(LocalDate.now());
        workout.setStartTime(LocalDateTime.now());
        workout.setExerciseType(exerciseType);
        workout.setStatus("in_progress");
        workout.setCreatedAt(LocalDateTime.now());
        
        Workout savedWorkout = workoutRepository.save(workout);
        return savedWorkout;
    }
    
    /**
     * ユーザーの目標運動時間を取得（分）
     * 
     * @param userId ユーザーID
     * @return 目標運動時間（分）
     * @throws RuntimeException アクティブな目標が設定されていない場合
     */
    public int getUserTargetSessionTime(String userId) {
        UserGoal activeGoal = userGoalRepository.findActiveGoalByUserId(userId)
            .orElseThrow(() -> new RuntimeException("アクティブな目標が設定されていません"));
        return activeGoal.getSessionTimeMinutes();
    }
    
    /**
     * ユーザーの目標運動種別を取得
     * 
     * @param userId ユーザーID
     * @return 目標運動種別
     * @throws RuntimeException アクティブな目標が設定されていない場合
     */
    public String getUserTargetExerciseType(String userId) {
        UserGoal activeGoal = userGoalRepository.findActiveGoalByUserId(userId)
            .orElseThrow(() -> new RuntimeException("アクティブな目標が設定されていません"));
        return activeGoal.getExerciseType();
    }
    
    /**
     * 進行中の運動記録を取得
     * 
     * @param userId ユーザーID
     * @return 進行中の運動記録（存在しない場合は空）
     */
    public Optional<Workout> getCurrentWorkout(String userId) {
        return workoutRepository.findByUserIdAndStatus(userId, "in_progress");
    }
    
    /**
     * 運動を完了
     * 
     * <p>進行中の運動を完了し、運動時間を計算して記録を更新します。
     * 完了後、バディへの通知を自動的に送信します。</p>
     * 
     * @param workoutId 運動ID
     * @param comment 運動に関するコメント
     * @return 更新された運動記録
     * @throws RuntimeException 運動記録が見つからない場合、または完了できないステータスの場合
     */
    public Workout completeWorkout(String workoutId, String comment) {
        // 運動取得
        Workout workout = workoutRepository.findById(workoutId)
            .orElseThrow(() -> new RuntimeException("運動記録が見つかりません"));
        
        if (!"in_progress".equals(workout.getStatus())) {
            throw new RuntimeException("完了できない運動ステータスです");
        }
        
        // 完了処理
        LocalDateTime endTime = LocalDateTime.now();
        workout.setEndTime(endTime);
        workout.setDurationSeconds(
            (int) java.time.Duration.between(workout.getStartTime(), endTime).getSeconds()
        );
        workout.setComment(comment);
        workout.setStatus("completed");
        
        Workout savedWorkout = workoutRepository.save(workout);
        
        return savedWorkout;
    }
    
    /**
     * 週間進捗を計算
     * 
     * <p>今週（月曜日から日曜日）の運動日数と目標に対する進捗率を計算します。
     * 進捗率に応じた励ましメッセージも生成します。</p>
     * 
     * @param userId ユーザーID
     * @return 週間進捗情報
     * @throws RuntimeException 目標が設定されていない場合
     */
    @Transactional(readOnly = true)
    public ProgressDto getWeeklyProgress(String userId) {
        // 目標取得
        UserGoal goal = userGoalRepository.findActiveGoalByUserId(userId)
            .orElseThrow(() -> new RuntimeException("目標が設定されていません"));
        
        // 今週の期間計算
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        
        // 今週の運動日数取得
        int workoutDays = workoutRepository.countDistinctWorkoutDays(userId, weekStart, weekEnd);
        
        // 進捗率計算
        int targetFrequency = goal.getWeeklyFrequency();
        int progressPercentage = Math.min(100, (workoutDays * 100) / targetFrequency);
        
        // 励ましメッセージ生成
        String encouragementMessage = generateEncouragementMessage(progressPercentage);
        
        return new ProgressDto(
            workoutDays,
            targetFrequency,
            progressPercentage,
            encouragementMessage
        );
    }
    
    /**
     * ユーザーの運動記録を取得
     * 
     * @param userId ユーザーID
     * @return 運動記録のリスト（運動日降順）
     */
    @Transactional(readOnly = true)
    public List<Workout> getUserWorkouts(String userId) {
        return workoutRepository.findByUserIdOrderByWorkoutDateDesc(userId);
    }
    
    
    /**
     * ワークアウトIDでワークアウトを取得
     * 
     * @param workoutId 運動ID
     * @return 運動記録（存在しない場合は空）
     */
    @Transactional(readOnly = true)
    public Optional<Workout> findById(String workoutId) {
        return workoutRepository.findById(workoutId);
    }
    
    /**
     * ワークアウトにリアクションを追加
     * 
     * <p>指定された運動記録にリアクション（いいね、素晴らしい等）を追加します。
     * 現在の実装では、コメントにリアクション情報を追記する形で実装しています。</p>
     * 
     * @param workoutId 運動ID
     * @param userId リアクション送信者ID
     * @param reactionType リアクション種別（like, great, fire等）
     * @throws RuntimeException ワークアウトが見つからない場合
     */
    public void addReaction(String workoutId, String userId, String reactionType) {
        // ワークアウトが存在するかチェック
        Workout workout = workoutRepository.findById(workoutId)
            .orElseThrow(() -> new RuntimeException("ワークアウトが見つかりません"));
        
        // リアクションを保存（実際の実装ではWorkoutReactionエンティティを使用）
        // ここでは簡単な実装として、ワークアウトのコメントにリアクション情報を追加
        String currentComment = workout.getComment() != null ? workout.getComment() : "";
        String reactionInfo = String.format(" [%s by %s]", reactionType, userId);
        workout.setComment(currentComment + reactionInfo);
        
        workoutRepository.save(workout);
    }
    
    /**
     * 励ましメッセージを生成
     * 
     * <p>進捗率に応じて適切な励ましメッセージを生成します。</p>
     * 
     * @param progressPercentage 進捗率（0-100）
     * @return 励ましメッセージ
     */
    private String generateEncouragementMessage(int progressPercentage) {
        if (progressPercentage >= 100) {
            return "目標達成！素晴らしいです！";
        } else if (progressPercentage >= 80) {
            return "もう少しです！がんばりましょう！";
        } else if (progressPercentage >= 50) {
            return "良いペースです！続けましょう！";
        } else if (progressPercentage >= 20) {
            return "コツコツ頑張りましょう！";
        } else {
            return "今週も運動を始めましょう！";
        }
    }

    /**
     * 運動開始結果を表すDTO
     * 
     * @author nagahama
     */
    public static class StartWorkoutResult {
        private final boolean success;
        private final String workoutId;
        private final String error;
        
        /**
         * コンストラクタ
         * 
         * @param success 成功フラグ
         * @param workoutId 運動ID（成功時のみ）
         * @param error エラーメッセージ（失敗時のみ）
         */
        public StartWorkoutResult(boolean success, String workoutId, String error) {
            this.success = success;
            this.workoutId = workoutId;
            this.error = error;
        }
        
        public boolean isSuccess() { return success; }
        public String getWorkoutId() { return workoutId; }
        public String getError() { return error; }
    }

    /**
     * 運動開始を試行
     * 
     * @param userId ユーザーID
     * @param exerciseType 運動種別
     * @return 運動開始結果
     */
    public StartWorkoutResult tryStartWorkout(String userId, String exerciseType) {
        try {
            Workout workout = startWorkout(userId, exerciseType);
            return new StartWorkoutResult(true, workout.getWorkoutId(), null);
        } catch (Exception e) {
            return new StartWorkoutResult(false, null, e.getMessage());
        }
    }
    
    /**
     * 目標の運動種別で運動を開始
     * 
     * @param userId ユーザーID
     * @return 運動開始結果
     */
    public StartWorkoutResult tryStartWorkoutWithGoal(String userId) {
        try {
            String exerciseType = getUserTargetExerciseType(userId);
            Workout workout = startWorkout(userId, exerciseType);
            return new StartWorkoutResult(true, workout.getWorkoutId(), null);
        } catch (Exception e) {
            return new StartWorkoutResult(false, null, e.getMessage());
        }
    }

    /**
     * 運動中画面データを表すDTO
     * 
     * @author nagahama
     */
    public static class InProgressResult {
        private final boolean valid;
        private final Workout workout;
        private final int targetSessionTime;
        private final String userName;
        
        /**
         * コンストラクタ
         * 
         * @param valid 有効なデータかどうか
         * @param workout 運動記録
         * @param targetSessionTime 目標運動時間（分）
         * @param userName ユーザー名
         */
        public InProgressResult(boolean valid, Workout workout, int targetSessionTime, String userName) {
            this.valid = valid;
            this.workout = workout;
            this.targetSessionTime = targetSessionTime;
            this.userName = userName;
        }
        
        public boolean isValid() { return valid; }
        public Workout getWorkout() { return workout; }
        public int getTargetSessionTime() { return targetSessionTime; }
        public String getUserName() { return userName; }
    }

    /**
     * 運動中画面のデータを取得
     * 
     * @param userId ユーザーID
     * @param workoutId 運動ID
     * @return 運動中画面データ
     */
    public InProgressResult getInProgressData(String userId, String workoutId) {
        try {
            Workout workout = findById(workoutId).orElse(null);
            if (workout == null) {
                return new InProgressResult(false, null, 0, "");
            }
            if (!workout.isInProgress()) {
                return new InProgressResult(false, null, 0, "");
            }
            
            int targetSessionTime = getUserTargetSessionTime(userId);
            String userName = userRepository.findById(userId).map(User::getUserName).orElse("");
            
            return new InProgressResult(true, workout, targetSessionTime, userName);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new InProgressResult(false, null, 0, "");
        }
    }

    /**
     * 運動完了画面データを表すDTO
     * 
     * @author nagahama
     */
    public static class CompleteResult {
        private final boolean valid;
        private final Workout workout;
        private final String workoutDuration;
        
        /**
         * コンストラクタ
         * 
         * @param valid 有効なデータかどうか
         * @param workout 運動記録
         * @param workoutDuration 運動時間（HH:mm形式）
         */
        public CompleteResult(boolean valid, Workout workout, String workoutDuration) {
            this.valid = valid;
            this.workout = workout;
            this.workoutDuration = workoutDuration;
        }
        
        public boolean isValid() { return valid; }
        public Workout getWorkout() { return workout; }
        public String getWorkoutDuration() { return workoutDuration; }
    }

    /**
     * 運動完了画面のデータを取得
     * 
     * @param userId ユーザーID
     * @param workoutId 運動ID
     * @return 運動完了画面データ
     */
    public CompleteResult getCompleteData(String userId, String workoutId) {
        try {
            Workout workout = findById(workoutId).orElse(null);
            if (workout == null || !workout.isInProgress()) {
                return new CompleteResult(false, null, "");
            }
            long durationSeconds = java.time.Duration.between(workout.getStartTime(), java.time.LocalDateTime.now()).getSeconds();
            int durationMinutes = (int) (durationSeconds / 60);
            int durationSecondsRemainder = (int) (durationSeconds % 60);
            String workoutDuration = String.format("%02d:%02d", durationMinutes, durationSecondsRemainder);
            return new CompleteResult(true, workout, workoutDuration);
        } catch (RuntimeException e) {
            return new CompleteResult(false, null, "");
        }
    }

    /**
     * 運動完了処理結果を表すDTO
     * 
     * @author nagahama
     */
    public static class CompleteWorkoutResult {
        private final boolean success;
        private final String error;
        
        /**
         * コンストラクタ
         * 
         * @param success 成功フラグ
         * @param error エラーメッセージ（失敗時のみ）
         */
        public CompleteWorkoutResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
        
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }

    /**
     * 運動完了を試行
     * 
     * @param userId ユーザーID
     * @param workoutId 運動ID
     * @param comment 運動コメント
     * @return 運動完了処理結果
     */
    public CompleteWorkoutResult tryCompleteWorkout(String userId, String workoutId, String comment) {
        try {
            Workout workout = completeWorkout(workoutId, comment);
            notificationService.notifyBuddiesOfWorkoutCompletion(workout);
            return new CompleteWorkoutResult(true, null);
        } catch (RuntimeException e) {
            return new CompleteWorkoutResult(false, e.getMessage());
        }
    }

    /**
     * リアクション結果を表すDTO
     * 
     * @author nagahama
     */
    public static class ReactionResult {
        private final boolean success;
        private final String message;
        
        /**
         * コンストラクタ
         * 
         * @param success 成功フラグ
         * @param message 結果メッセージ
         */
        public ReactionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    /**
     * リアクション追加を試行
     * 
     * @param userId リアクション送信者ID
     * @param request リアクション情報を含むリクエストマップ
     * @return リアクション結果
     */
    public ReactionResult tryAddReaction(String userId, java.util.Map<String, Object> request) {
        try {
            String workoutId = (String) request.get("workoutId");
            String reactionType = (String) request.get("reactionType");
            addReaction(workoutId, userId, reactionType);
            Workout workout = findById(workoutId).orElse(null);
            if (workout != null && !workout.getUserId().equals(userId)) {
                notificationService.createReactionNotification(userId, workout.getUserId(), workoutId);
            }
            return new ReactionResult(true, "リアクションを送信しました");
        } catch (Exception e) {
            return new ReactionResult(false, e.getMessage());
        }
    }

    /**
     * 運動記録一覧データを表すDTO
     * 
     * @author nagahama
     */
    public static class RecordsResult {
        private final java.util.List<Workout> workouts;
        private final String userName;
        
        /**
         * コンストラクタ
         * 
         * @param workouts 運動記録リスト
         * @param userName ユーザー名
         */
        public RecordsResult(java.util.List<Workout> workouts, String userName) {
            this.workouts = workouts;
            this.userName = userName;
        }
        
        public java.util.List<Workout> getWorkouts() { return workouts; }
        public String getUserName() { return userName; }
    }

    /**
     * 運動記録一覧データを取得
     * 
     * @param userId ユーザーID
     * @return 運動記録一覧データ
     */
    public RecordsResult getRecordsData(String userId) {
        try {
            java.util.List<Workout> workouts = getUserWorkouts(userId);
            String userName = userRepository.findById(userId).map(User::getUserName).orElse("");
            return new RecordsResult(workouts, userName);
        } catch (RuntimeException e) {
            return new RecordsResult(java.util.List.of(), "");
        }
    }
} 