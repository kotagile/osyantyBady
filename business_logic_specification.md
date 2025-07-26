# ビジネスロジック仕様書

## 1. 認証・ユーザー管理ロジック

### 1.1 ユーザー登録処理
**処理クラス**: `UserService.registerUser()`

#### 処理フロー
1. **入力値検証**
   - ユーザーID: 3-20文字、英数字のみ
   - ユーザー名: 1-50文字
   - パスワード: 8文字以上、英数字含む
   
2. **重複チェック**
   - 既存ユーザーIDとの重複確認
   - 重複時は例外スロー
   
3. **パスワードハッシュ化**
   - BCryptPasswordEncoderでハッシュ化
   - ソルト自動生成
   
4. **ユーザー作成**
   - Userエンティティ生成
   - データベース保存
   
5. **初期設定**
   - UserGoalエンティティ生成（デフォルト値）
   - セッション情報設定

```java
public User registerUser(UserRegistrationDto dto) {
    // 入力値検証
    validateUserRegistration(dto);
    
    // 重複チェック
    if (userRepository.existsById(dto.getUserId())) {
        throw new UserAlreadyExistsException("既に存在するユーザーIDです");
    }
    
    // ユーザー作成
    User user = new User();
    user.setUserId(dto.getUserId());
    user.setUserName(dto.getUserName());
    user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
    user.setCreatedAt(LocalDateTime.now());
    user.setIsActive(true);
    
    User savedUser = userRepository.save(user);
    
    // 初期目標設定
    createDefaultGoal(savedUser.getUserId());
    
    return savedUser;
}
```

### 1.2 ログイン処理
**処理クラス**: `UserService.authenticateUser()`

#### 処理フロー
1. **ユーザー存在確認**
2. **パスワード照合**
3. **セッション作成**
4. **ログイン履歴記録**

## 2. 目標設定ロジック

### 2.1 目標設定・更新処理
**処理クラス**: `GoalService.setGoal()`

#### 処理フロー
1. **入力値検証**
   - 継続期間: 3months, 6months, 1year
   - 週間頻度: 1-7回
   - 運動時間: 10-180分
   
2. **既存目標の無効化**
   - 現在のアクティブ目標をis_active = false
   
3. **新規目標作成**
   - UserGoalエンティティ作成
   - データベース保存
   
4. **進捗リセット**
   - 進捗カウンターリセット
   - 新しい目標期間の開始日設定

```java
public UserGoal setGoal(String userId, GoalDto goalDto) {
    // 既存目標を無効化
    List<UserGoal> activeGoals = goalRepository.findActiveGoalsByUserId(userId);
    activeGoals.forEach(goal -> {
        goal.setIsActive(false);
        goal.setUpdatedAt(LocalDateTime.now());
    });
    goalRepository.saveAll(activeGoals);
    
    // 新規目標作成
    UserGoal newGoal = new UserGoal();
    newGoal.setUserId(userId);
    newGoal.setGoalDuration(goalDto.getDuration());
    newGoal.setWeeklyFrequency(goalDto.getFrequency());
    newGoal.setExerciseType(goalDto.getExerciseType());
    newGoal.setSessionTimeMinutes(goalDto.getSessionTime());
    newGoal.setCreatedAt(LocalDateTime.now());
    newGoal.setIsActive(true);
    
    return goalRepository.save(newGoal);
}
```

## 3. 運動記録ロジック

### 3.1 運動開始処理
**処理クラス**: `WorkoutService.startWorkout()`

#### 処理フロー
1. **ユーザー目標取得**
   - アクティブな目標の確認
   - 目標時間の取得
   
2. **運動セッション作成**
   - 一意のworkoutId生成
   - 開始時刻記録
   - ステータス: "in_progress"
   
3. **セッション管理**
   - HttpSessionに運動情報格納
   - タイマー開始フラグ設定

```java
public Workout startWorkout(String userId, String exerciseType) {
    // 目標取得
    UserGoal activeGoal = goalRepository.findActiveGoalByUserId(userId)
        .orElseThrow(() -> new GoalNotFoundException("アクティブな目標が設定されていません"));
    
    // 重複チェック（同日の進行中運動）
    Optional<Workout> ongoingWorkout = workoutRepository
        .findOngoingWorkoutByUserIdAndDate(userId, LocalDate.now());
    
    if (ongoingWorkout.isPresent()) {
        throw new WorkoutAlreadyInProgressException("既に進行中の運動があります");
    }
    
    // 運動開始
    Workout workout = new Workout();
    workout.setWorkoutId(UUID.randomUUID().toString());
    workout.setUserId(userId);
    workout.setWorkoutDate(LocalDate.now());
    workout.setStartTime(LocalDateTime.now());
    workout.setExerciseType(exerciseType);
    workout.setStatus("in_progress");
    
    return workoutRepository.save(workout);
}
```

### 3.2 運動完了処理
**処理クラス**: `WorkoutService.completeWorkout()`

#### 処理フロー
1. **運動セッション取得**
   - workoutIdから対象セッション取得
   - ステータス確認
   
2. **時間計算**
   - 終了時刻記録
   - 運動時間計算（秒単位）
   
3. **データ更新**
   - 運動記録更新
   - ステータス: "completed"
   
4. **バディ通知**
   - バディリスト取得
   - 通知作成・送信
   
5. **進捗更新**
   - 週間進捗カウンター更新

```java
@Transactional
public Workout completeWorkout(String workoutId, String comment) {
    // 運動取得
    Workout workout = workoutRepository.findById(workoutId)
        .orElseThrow(() -> new WorkoutNotFoundException("運動記録が見つかりません"));
    
    if (!"in_progress".equals(workout.getStatus())) {
        throw new IllegalStateException("完了できない運動ステータスです");
    }
    
    // 完了処理
    LocalDateTime endTime = LocalDateTime.now();
    workout.setEndTime(endTime);
    workout.setDurationSeconds(
        (int) Duration.between(workout.getStartTime(), endTime).getSeconds()
    );
    workout.setComment(comment);
    workout.setStatus("completed");
    
    Workout savedWorkout = workoutRepository.save(workout);
    
    // バディ通知
    notifyBuddies(workout.getUserId(), savedWorkout);
    
    return savedWorkout;
}

private void notifyBuddies(String userId, Workout workout) {
    List<String> buddyIds = buddyService.getBuddyIds(userId);
    
    for (String buddyId : buddyIds) {
        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setFromUserId(userId);
        notification.setToUserId(buddyId);
        notification.setNotificationType("workout_completed");
        notification.setTitle("バディが運動を完了しました！");
        notification.setMessage(String.format("運動時間: %d分", 
            workout.getDurationSeconds() / 60));
        notification.setRelatedData(createWorkoutNotificationData(workout));
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
}
```

### 3.3 運動一時停止・再開処理
**処理クラス**: `WorkoutService.pauseWorkout()`, `WorkoutService.resumeWorkout()`

#### 一時停止処理フロー
1. **セッション取得・検証**
2. **一時停止時刻記録**
3. **ステータス更新**: "paused"
4. **累積時間計算**

#### 再開処理フロー
1. **セッション取得・検証**
2. **再開時刻記録**
3. **ステータス更新**: "in_progress"
4. **開始時刻調整**（一時停止時間を除外）

## 4. 進捗管理ロジック

### 4.1 週間進捗計算処理
**処理クラス**: `ProgressService.calculateWeeklyProgress()`

#### 処理フロー
1. **期間設定**
   - 今週の開始日・終了日計算
   - 月曜日を週の開始とする
   
2. **運動記録取得**
   - 期間内の完了済み運動取得
   - 重複排除（1日1回カウント）
   
3. **進捗率計算**
   - 実績回数 / 週間目標回数 × 100
   - 100%を上限とする
   
4. **メッセージ生成**
   - 進捗率に応じた励ましメッセージ

```java
public ProgressDto calculateWeeklyProgress(String userId) {
    // 目標取得
    UserGoal goal = goalRepository.findActiveGoalByUserId(userId)
        .orElseThrow(() -> new GoalNotFoundException("目標が設定されていません"));
    
    // 今週の期間計算
    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.with(DayOfWeek.MONDAY);
    LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
    
    // 今週の運動日数取得
    int workoutDays = workoutRepository.countDistinctWorkoutDays(
        userId, weekStart, weekEnd
    );
    
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
```

### 4.2 月間進捗計算処理
**処理クラス**: `ProgressService.calculateMonthlyProgress()`

#### 処理フロー
1. **月間期間設定**
2. **月間目標計算**
   - 週間目標 × 4.3（月の週数平均）
3. **実績集計**
4. **達成率計算**

## 5. バディ管理ロジック

### 5.1 バディリクエスト送信処理
**処理クラス**: `BuddyService.sendBuddyRequest()`

#### 処理フロー
1. **対象ユーザー存在確認**
2. **重複リクエストチェック**
   - 既存の pending リクエスト確認
   - 既にバディ関係の確認
3. **リクエスト作成**
4. **通知送信**

```java
public void sendBuddyRequest(String requesterId, String requestedUserId) {
    // 対象ユーザー存在確認
    User requestedUser = userRepository.findById(requestedUserId)
        .orElseThrow(() -> new UserNotFoundException("ユーザーが見つかりません"));
    
    // 自分自身へのリクエストチェック
    if (requesterId.equals(requestedUserId)) {
        throw new IllegalArgumentException("自分自身にバディリクエストは送信できません");
    }
    
    // 重複チェック
    boolean existingRequest = buddyRepository.existsPendingRequest(requesterId, requestedUserId);
    if (existingRequest) {
        throw new DuplicateRequestException("既にバディリクエストが送信されています");
    }
    
    // 既存のバディ関係チェック
    boolean alreadyBuddies = buddyRepository.areBuddies(requesterId, requestedUserId);
    if (alreadyBuddies) {
        throw new AlreadyBuddiesException("既にバディ関係です");
    }
    
    // リクエスト作成
    UserBuddy buddyRequest = new UserBuddy();
    buddyRequest.setRequesterId(requesterId);
    buddyRequest.setRequestedId(requestedUserId);
    buddyRequest.setStatus("pending");
    buddyRequest.setRequestedAt(LocalDateTime.now());
    
    buddyRepository.save(buddyRequest);
    
    // 通知送信
    sendBuddyRequestNotification(requesterId, requestedUserId);
}
```

### 5.2 バディリクエスト承認処理
**処理クラス**: `BuddyService.acceptBuddyRequest()`

#### 処理フロー
1. **リクエスト取得・検証**
2. **ステータス更新**: "accepted"
3. **相互バディ関係作成**
4. **承認通知送信**

### 5.3 バディ進捗取得処理
**処理クラス**: `BuddyService.getBuddyProgress()`

#### 処理フロー
1. **バディリスト取得**
2. **各バディの週間進捗計算**
3. **進捗データ統合**
4. **プライバシー考慮**（詳細データは非表示）

## 6. 通知管理ロジック

### 6.1 通知作成・送信処理
**処理クラス**: `NotificationService.createNotification()`

#### 通知種別
- **workout_completed**: 運動完了通知
- **buddy_request**: バディリクエスト
- **buddy_accepted**: バディ承認通知
- **reaction**: リアクション通知

#### 処理フロー
1. **通知データ作成**
2. **データベース保存**
3. **リアルタイム通知**（WebSocket実装時）

### 6.2 通知履歴取得処理
**処理クラス**: `NotificationService.getNotificationHistory()`

#### 処理フロー
1. **ユーザー通知取得**
2. **ページネーション適用**
3. **既読・未読分類**
4. **通知種別でのフィルタリング**

## 7. エラーハンドリング

### 7.1 共通例外処理
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(WorkoutNotFoundException.class)
    public String handleWorkoutNotFound(WorkoutNotFoundException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/workout-not-found";
    }
    
    @ExceptionHandler(GoalNotFoundException.class)
    public String handleGoalNotFound(GoalNotFoundException e, Model model) {
        model.addAttribute("errorMessage", "目標が設定されていません");
        return "goal/setup";
    }
}
```

### 7.2 バリデーション処理
```java
public class WorkoutValidator {
    
    public static void validateWorkoutComment(String comment) {
        if (comment != null && comment.length() > 500) {
            throw new ValidationException("コメントは500文字以内で入力してください");
        }
    }
    
    public static void validateExerciseTime(int minutes) {
        if (minutes < 1 || minutes > 300) {
            throw new ValidationException("運動時間は1分以上300分以内で設定してください");
        }
    }
}
```

## 8. トランザクション境界

### 8.1 サービス層トランザクション
- **@Transactional**: 全サービスメソッドに適用
- **readOnly = true**: 参照系メソッドに適用
- **rollbackFor**: RuntimeException時のロールバック

### 8.2 分離レベル
- **READ_COMMITTED**: 通常処理
- **REPEATABLE_READ**: 進捗計算処理（一貫性重視）

## 9. パフォーマンス最適化

### 9.1 N+1問題対策
```java
// Buddy進捗取得時のJOIN FETCH使用
@Query("SELECT b FROM UserBuddy b JOIN FETCH b.requestedUser WHERE b.requesterId = :userId AND b.status = 'accepted'")
List<UserBuddy> findAcceptedBuddiesWithUser(@Param("userId") String userId);
```

### 9.2 キャッシュ戦略
```java
@Cacheable(value = "weeklyProgress", key = "#userId")
public ProgressDto calculateWeeklyProgress(String userId) {
    // 計算処理
}

@CacheEvict(value = "weeklyProgress", key = "#userId")
public Workout completeWorkout(String workoutId, String comment) {
    // 完了処理
}
```