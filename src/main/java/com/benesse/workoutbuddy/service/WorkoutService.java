package com.benesse.workoutbuddy.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * 運動記録サービス
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
     * 運動開始
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
     */
    public int getUserTargetSessionTime(String userId) {
        UserGoal activeGoal = userGoalRepository.findActiveGoalByUserId(userId)
            .orElseThrow(() -> new RuntimeException("アクティブな目標が設定されていません"));
        return activeGoal.getSessionTimeMinutes();
    }
    
    /**
     * ユーザーの目標運動種別を取得
     */
    public String getUserTargetExerciseType(String userId) {
        UserGoal activeGoal = userGoalRepository.findActiveGoalByUserId(userId)
            .orElseThrow(() -> new RuntimeException("アクティブな目標が設定されていません"));
        return activeGoal.getExerciseType();
    }
    
    /**
     * 進行中の運動記録を取得
     */
    public Optional<Workout> getCurrentWorkout(String userId) {
        return workoutRepository.findByUserIdAndStatus(userId, "in_progress");
    }
    
    /**
     * 運動完了
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
        
        // バディ通知
        notificationService.notifyWorkoutCompleted(workout.getUserId(), savedWorkout);
        
        return savedWorkout;
    }
    
    /**
     * 週間進捗計算
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
        Map<LocalDate, Duration> workoutData=summarizeWorkoutTimes(workoutRepository.findByUserIdAndDateRange(userId, weekStart, weekEnd));
        
        int workoutDays = countDaysExceedingTarget(workoutData, goal.getSessionTimeMinutes());
        

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
     * 与えられた Workout のリストから、日付ごとの合計運動時間を計算して返します。
     * 
     * @param workouts 運動のリスト（各 Workout は開始時刻、終了時刻、運動日を持つ）
     * @return LocalDate をキー、Duration（その日の合計運動時間）を値とする Map
     */
    public static Map<LocalDate, Duration> summarizeWorkoutTimes(List<Workout> workouts) {
        // 結果を格納するマップ。キー：日付、値：その日の合計運動時間
        Map<LocalDate, Duration> result = new HashMap<>();

//        nullチェック
        if (workouts.size()==0||workouts.isEmpty())    {
        	return result;
        }
        
        // すべての Workout を処理
        for (Workout workout : workouts) {
            // 運動の日付を取得
            LocalDate date = workout.getWorkoutDate();

            // 運動時間を計算（開始〜終了の差分）
            Duration duration = Duration.between(workout.getStartTime(), workout.getEndTime());

            // すでにその日の記録が存在する場合は加算、なければ新規登録
            if (result.containsKey(date)) {
                Duration updated = result.get(date).plus(duration); // 既存の時間に加算
                result.put(date, updated); // 更新
            } else {
                result.put(date, duration); // 新規登録
            }
        }

        return result;
    }
    
    /**
     * 与えられた運動記録の中で、目標時間（分）を超えた日数をカウントして返します。
     *
     * @param workoutDurations 各日の運動時間をまとめたマップ（キー：日付、値：Duration）
     * @param targetMinutes 目標運動時間（単位：分）
     * @return 目標時間を超えた日数
     */
    public static int countDaysExceedingTarget(Map<LocalDate, Duration> workoutDurations, int targetMinutes) {
        int count = 0;

//        nullチェック
        if (workoutDurations.size()==0||workoutDurations.isEmpty()) {
        	return count;
        }
        
        for (Map.Entry<LocalDate, Duration> entry : workoutDurations.entrySet()) {
            Duration duration = entry.getValue();
            int minutes = (int) duration.toMinutes();

            // 目標を超えていればカウント
            if (minutes > targetMinutes) {
                count++;
            }
        }

        return count;
    }
    
    /**
     * ユーザーの運動記録取得
     */
    @Transactional(readOnly = true)
    public List<Workout> getUserWorkouts(String userId) {
        return workoutRepository.findByUserIdOrderByWorkoutDateDesc(userId);
    }
    
    /**
     * 運動記録取得
     */
    @Transactional(readOnly = true)
    public Optional<Workout> getWorkout(String workoutId) {
        return workoutRepository.findById(workoutId);
    }
    
    /**
     * ワークアウトIDでワークアウトを取得
     */
    @Transactional(readOnly = true)
    public Optional<Workout> findById(String workoutId) {
        return workoutRepository.findById(workoutId);
    }
    
    /**
     * ワークアウトにリアクションを追加
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
     * 励ましメッセージ生成
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

    // 運動開始結果DTO
    public static class StartWorkoutResult {
        private final boolean success;
        private final String workoutId;
        private final String error;
        public StartWorkoutResult(boolean success, String workoutId, String error) {
            this.success = success;
            this.workoutId = workoutId;
            this.error = error;
        }
        public boolean isSuccess() { return success; }
        public String getWorkoutId() { return workoutId; }
        public String getError() { return error; }
    }

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

    // 運動中画面データDTO
    public static class InProgressResult {
        private final boolean valid;
        private final Workout workout;
        private final int targetSessionTime;
        private final String userName;
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

    public InProgressResult getInProgressData(String userId, String workoutId) {
        try {
            Workout workout = getWorkout(workoutId).orElse(null);
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

    // 運動完了画面データDTO
    public static class CompleteResult {
        private final boolean valid;
        private final Workout workout;
        private final String workoutDuration;
        public CompleteResult(boolean valid, Workout workout, String workoutDuration) {
            this.valid = valid;
            this.workout = workout;
            this.workoutDuration = workoutDuration;
        }
        public boolean isValid() { return valid; }
        public Workout getWorkout() { return workout; }
        public String getWorkoutDuration() { return workoutDuration; }
    }

    public CompleteResult getCompleteData(String userId, String workoutId) {
        try {
            Workout workout = getWorkout(workoutId).orElse(null);
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

    // 運動完了処理結果DTO
    public static class CompleteWorkoutResult {
        private final boolean success;
        private final String error;
        public CompleteWorkoutResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }

    public CompleteWorkoutResult tryCompleteWorkout(String userId, String workoutId, String comment) {
        try {
            Workout workout = completeWorkout(workoutId, comment);
            notificationService.notifyBuddiesOfWorkoutCompletion(workout);
            return new CompleteWorkoutResult(true, null);
        } catch (RuntimeException e) {
            return new CompleteWorkoutResult(false, e.getMessage());
        }
    }

    // リアクション結果DTO
    public static class ReactionResult {
        private final boolean success;
        private final String message;
        public ReactionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

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

    // 運動記録一覧データDTO
    public static class RecordsResult {
        private final java.util.List<Workout> workouts;
        private final String userName;
        public RecordsResult(java.util.List<Workout> workouts, String userName) {
            this.workouts = workouts;
            this.userName = userName;
        }
        public java.util.List<Workout> getWorkouts() { return workouts; }
        public String getUserName() { return userName; }
    }

    public RecordsResult getRecordsData(String userId) {
        try {
            java.util.List<Workout> workouts = getUserWorkouts(userId);
            String userName = userRepository.findById(userId).map(User::getUserName).orElse("");
            return new RecordsResult(workouts, userName);
        } catch (RuntimeException e) {
            return new RecordsResult(java.util.List.of(), "");
        }
    }

	public String getUserInfo(String userId) {
		Optional<User> users= userRepository.findById(userId);
		if (users.isPresent()) {
			return users.get().getUserName();
		}
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
} 