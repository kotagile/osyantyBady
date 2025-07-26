# データベース設計・バックエンド設計方針書

## 1. システムアーキテクチャ概要

### 1.1 技術スタック
- **フロントエンド**: HTML5, CSS3, Vanilla JavaScript
- **バックエンド**: Java 17, Spring Boot 3.x
- **テンプレートエンジン**: Thymeleaf
- **データベース**: Microsoft SQL Server
- **ビルドツール**: Maven
- **アーキテクチャパターン**: MVC (Model-View-Controller)

### 1.2 システム構成図
```
[Browser] 
    ↓ HTTP Request
[Spring Boot Application]
    ├── Controller Layer
    ├── Service Layer  
    ├── Repository Layer
    └── Entity Layer
         ↓ JDBC
[SQL Server Database]
```

## 2. データベース設計

### 2.1 論理ERD
```
Users (1) --------< (M) Workouts
  |                      
  |                      
  ├---< (M) UserBuddies >--- (1) Users
  |
  ├---< (M) Notifications
  |
  ├---< (M) HealthData
  └---< (1) UserGoals
```

### 2.2 物理テーブル設計

#### 2.2.1 users テーブル
```sql
CREATE TABLE users (
    user_id NVARCHAR(50) PRIMARY KEY,
    user_name NVARCHAR(100) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_active BIT DEFAULT 1
);
```

#### 2.2.2 user_goals テーブル
```sql
CREATE TABLE user_goals (
    goal_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id NVARCHAR(50) NOT NULL,
    goal_duration NVARCHAR(20) NOT NULL, -- '3months', '6months', '1year'
    weekly_frequency INT NOT NULL,
    exercise_type NVARCHAR(50) NOT NULL,
    session_time_minutes INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_active BIT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

#### 2.2.3 workouts テーブル
```sql
CREATE TABLE workouts (
    workout_id NVARCHAR(50) PRIMARY KEY,
    user_id NVARCHAR(50) NOT NULL,
    workout_date DATE NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2,
    duration_seconds INT,
    exercise_type NVARCHAR(50) NOT NULL,
    comment NVARCHAR(500),
    status NVARCHAR(20) DEFAULT 'completed', -- 'completed', 'paused', 'cancelled'
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

#### 2.2.4 user_buddies テーブル
```sql
CREATE TABLE user_buddies (
    buddy_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    requester_id NVARCHAR(50) NOT NULL,
    requested_id NVARCHAR(50) NOT NULL,
    status NVARCHAR(20) DEFAULT 'pending', -- 'pending', 'accepted', 'rejected'
    requested_at DATETIME2 DEFAULT GETDATE(),
    responded_at DATETIME2,
    FOREIGN KEY (requester_id) REFERENCES users(user_id),
    FOREIGN KEY (requested_id) REFERENCES users(user_id),
    UNIQUE(requester_id, requested_id)
);
```

#### 2.2.5 notifications テーブル
```sql
CREATE TABLE notifications (
    notification_id NVARCHAR(50) PRIMARY KEY,
    from_user_id NVARCHAR(50),
    to_user_id NVARCHAR(50) NOT NULL,
    notification_type NVARCHAR(30) NOT NULL, -- 'workout_completed', 'buddy_request', 'reaction'
    title NVARCHAR(100) NOT NULL,
    message NVARCHAR(500),
    related_data NVARCHAR(MAX), -- JSON形式でデータ保存
    is_read BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (from_user_id) REFERENCES users(user_id),
    FOREIGN KEY (to_user_id) REFERENCES users(user_id)
);
```

#### 2.2.6 health_data テーブル
```sql
CREATE TABLE health_data (
    health_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id NVARCHAR(50) NOT NULL,
    data_type NVARCHAR(20) NOT NULL, -- 'weight', 'body_fat', 'sleep'
    data_value DECIMAL(10,2),
    data_text NVARCHAR(100), -- 睡眠時間等のテキストデータ用
    recorded_date DATE NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

#### 2.2.7 workout_reactions テーブル
```sql
CREATE TABLE workout_reactions (
    reaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    workout_id NVARCHAR(50) NOT NULL,
    user_id NVARCHAR(50) NOT NULL,
    reaction_type NVARCHAR(20) DEFAULT 'like', -- 'like', 'great', 'fire'
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (workout_id) REFERENCES workouts(workout_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE(workout_id, user_id)
);
```

### 2.3 インデックス設計
```sql
-- パフォーマンス向上のためのインデックス
CREATE INDEX IX_workouts_user_date ON workouts(user_id, workout_date);
CREATE INDEX IX_notifications_to_user_read ON notifications(to_user_id, is_read);
CREATE INDEX IX_user_buddies_status ON user_buddies(requester_id, requested_id, status);
CREATE INDEX IX_health_data_user_type_date ON health_data(user_id, data_type, recorded_date);
```

## 3. バックエンド設計

### 3.1 パッケージ構成
```
src/main/java/com/benesse/workoutbuddy/
├── WorkoutBuddyApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── DatabaseConfig.java
├── controller/
│   ├── AuthController.java
│   ├── HomeController.java
│   ├── WorkoutController.java
│   ├── BuddyController.java
│   └── RecordController.java
├── service/
│   ├── UserService.java
│   ├── WorkoutService.java
│   ├── BuddyService.java
│   └── NotificationService.java
├── repository/
│   ├── UserRepository.java
│   ├── WorkoutRepository.java
│   ├── BuddyRepository.java
│   └── NotificationRepository.java
├── entity/
│   ├── User.java
│   ├── Workout.java
│   ├── UserBuddy.java
│   └── Notification.java
├── dto/
│   ├── WorkoutDto.java
│   ├── ProgressDto.java
│   └── NotificationDto.java
└── util/
    ├── DateUtil.java
    └── PasswordUtil.java
```

### 3.2 主要エンティティクラス

#### 3.2.1 User エンティティ
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserGoal userGoal;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Workout> workouts = new ArrayList<>();
}
```

#### 3.2.2 Workout エンティティ
```java
@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @Column(name = "workout_id")
    private String workoutId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "workout_date")
    private LocalDate workoutDate;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "exercise_type")
    private String exerciseType;
    
    @Column(name = "comment")
    private String comment;
    
    @Column(name = "status")
    private String status;
}
```

### 3.3 主要サービスクラス

#### 3.3.1 WorkoutService
```java
@Service
@Transactional
public class WorkoutService {
    
    @Autowired
    private WorkoutRepository workoutRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public Workout startWorkout(String userId, String exerciseType) {
        // 運動開始処理
    }
    
    public Workout completeWorkout(String workoutId, String comment) {
        // 運動完了処理
        // バディへの通知送信
    }
    
    public ProgressDto getWeeklyProgress(String userId) {
        // 週間進捗計算
    }
}
```

#### 3.3.2 BuddyService
```java
@Service
@Transactional
public class BuddyService {
    
    @Autowired
    private UserBuddyRepository buddyRepository;
    
    public void sendBuddyRequest(String requesterId, String requestedId) {
        // バディリクエスト送信
    }
    
    public void acceptBuddyRequest(Long buddyId) {
        // バディリクエスト承認
    }
    
    public List<User> getBuddyList(String userId) {
        // バディ一覧取得
    }
}
```

### 3.4 コントローラー設計

#### 3.4.1 HomeController
```java
@Controller
public class HomeController {
    
    @Autowired
    private WorkoutService workoutService;
    
    @Autowired
    private BuddyService buddyService;
    
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        
        // 進捗データ取得
        ProgressDto progress = workoutService.getWeeklyProgress(userId);
        model.addAttribute("progress", progress);
        
        // バディ進捗取得
        List<ProgressDto> buddyProgress = buddyService.getBuddyProgress(userId);
        model.addAttribute("buddyProgress", buddyProgress);
        
        return "home";
    }
}
```

### 3.5 リポジトリ設計

#### 3.5.1 WorkoutRepository
```java
@Repository
public interface WorkoutRepository extends JpaRepository<Workout, String> {
    
    @Query("SELECT w FROM Workout w WHERE w.user.userId = :userId AND w.workoutDate BETWEEN :startDate AND :endDate")
    List<Workout> findByUserIdAndDateRange(@Param("userId") String userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user.userId = :userId AND w.workoutDate = :date")
    int countByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);
}
```

## 4. セキュリティ設計

### 4.1 認証・認可
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );
        return http.build();
    }
}
```

### 4.2 SQL インジェクション対策
- JPA/Hibernateの使用
- パラメータ化クエリの徹底
- 入力値バリデーション

### 4.3 CSRF対策
- Spring SecurityのCSRF保護機能を有効化
- Thymeleafテンプレートでのトークン埋め込み

## 5. 開発環境設定

### 5.1 application.yml
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=WorkoutBuddyDB;encrypt=false
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServerDialect
        format_sql: true
  
  thymeleaf:
    cache: false
    
server:
  port: 8080
  
logging:
  level:
    com.benesse.workoutbuddy: DEBUG
    org.springframework.security: DEBUG
```

### 5.2 pom.xml 主要依存関係
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

## 6. パフォーマンス考慮事項

### 6.1 データベース最適化
- 適切なインデックス設計
- ページネーション実装
- N+1問題の回避（@EntityGraphの活用）

### 6.2 キャッシュ戦略
- Spring Cacheの活用
- セッション情報のキャッシュ
- 静的リソースのキャッシュ設定

### 6.3 トランザクション管理
- サービス層での@Transactional適用
- 読み取り専用トランザクションの活用
- トランザクション境界の最適化