-- ユーザーテーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_active BIT DEFAULT 1
);

-- ユーザー目標テーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='user_goals' AND xtype='U')
CREATE TABLE user_goals (
    goal_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    goal_duration VARCHAR(20) NOT NULL,
    weekly_frequency INT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    session_time_minutes INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_active BIT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 運動記録テーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='workouts' AND xtype='U')
CREATE TABLE workouts (
    workout_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    workout_date DATE NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2,
    duration_seconds INT,
    exercise_type VARCHAR(50) NOT NULL,
    comment VARCHAR(500),
    status VARCHAR(20) DEFAULT 'in_progress',
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ユーザーバディテーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='user_buddies' AND xtype='U')
CREATE TABLE user_buddies (
    buddy_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    requester_id VARCHAR(50) NOT NULL,
    requested_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    requested_at DATETIME2 DEFAULT GETDATE(),
    responded_at DATETIME2,
    FOREIGN KEY (requester_id) REFERENCES users(user_id),
    FOREIGN KEY (requested_id) REFERENCES users(user_id)
);

-- 通知テーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='notifications' AND xtype='U')
CREATE TABLE notifications (
    notification_id VARCHAR(50) PRIMARY KEY,
    from_user_id VARCHAR(50) NOT NULL,
    to_user_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(500) NOT NULL,
    related_data NVARCHAR(MAX),
    is_read BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (from_user_id) REFERENCES users(user_id),
    FOREIGN KEY (to_user_id) REFERENCES users(user_id)
);

-- 健康データテーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='health_data' AND xtype='U')
CREATE TABLE health_data (
    health_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    data_value DECIMAL(10,2),
    data_text VARCHAR(100),
    recorded_date DATE NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 運動リアクションテーブル
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='workout_reactions' AND xtype='U')
CREATE TABLE workout_reactions (
    reaction_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    workout_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    reaction_type VARCHAR(20) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (workout_id) REFERENCES workouts(workout_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- インデックス作成
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_users_user_name' AND object_id = OBJECT_ID('users'))
CREATE INDEX idx_users_user_name ON users(user_name);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_user_goals_user_id' AND object_id = OBJECT_ID('user_goals'))
CREATE INDEX idx_user_goals_user_id ON user_goals(user_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_user_goals_active' AND object_id = OBJECT_ID('user_goals'))
CREATE INDEX idx_user_goals_active ON user_goals(user_id, is_active);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_workouts_user_id' AND object_id = OBJECT_ID('workouts'))
CREATE INDEX idx_workouts_user_id ON workouts(user_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_workouts_date' AND object_id = OBJECT_ID('workouts'))
CREATE INDEX idx_workouts_date ON workouts(workout_date);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_workouts_status' AND object_id = OBJECT_ID('workouts'))
CREATE INDEX idx_workouts_status ON workouts(user_id, status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_user_buddies_requester' AND object_id = OBJECT_ID('user_buddies'))
CREATE INDEX idx_user_buddies_requester ON user_buddies(requester_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_user_buddies_requested' AND object_id = OBJECT_ID('user_buddies'))
CREATE INDEX idx_user_buddies_requested ON user_buddies(requested_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_user_buddies_status' AND object_id = OBJECT_ID('user_buddies'))
CREATE INDEX idx_user_buddies_status ON user_buddies(requester_id, requested_id, status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_notifications_to_user' AND object_id = OBJECT_ID('notifications'))
CREATE INDEX idx_notifications_to_user ON notifications(to_user_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_notifications_read' AND object_id = OBJECT_ID('notifications'))
CREATE INDEX idx_notifications_read ON notifications(to_user_id, is_read);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_health_data_user_id' AND object_id = OBJECT_ID('health_data'))
CREATE INDEX idx_health_data_user_id ON health_data(user_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idx_workout_reactions_workout_id' AND object_id = OBJECT_ID('workout_reactions'))
CREATE INDEX idx_workout_reactions_workout_id ON workout_reactions(workout_id); 