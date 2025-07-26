# UI設計仕様書

## 1. 全体UI設計方針

### 1.1 デザインコンセプト
- **カラーパレット**: 
  - プライマリ: オレンジ系 (#D4A574)
  - セカンダリ: ダークグレー (#333333)
  - 背景: ライトグレー (#F5F5F5)
  - アクセント: 白 (#FFFFFF)

### 1.2 技術実装方針
- **レスポンシブデザイン**: モバイルファースト
- **CSS Framework**: カスタムCSS + CSS Grid/Flexbox
- **JavaScript**: Vanilla JavaScript（ES6+）
- **テンプレートエンジン**: Thymeleaf
- **アイコン**: Font Awesome / Custom SVG

### 1.3 共通UI要素

#### 1.3.1 ナビゲーションバー
```html
<!-- templates/fragments/navbar.html -->
<nav class="bottom-nav" th:fragment="navbar">
    <div class="nav-container">
        <a href="/" class="nav-item" th:classappend="${currentPage == 'home'} ? 'active' : ''">
            <i class="icon-home"></i>
            <span>ホーム</span>
        </a>
        <a href="/buddy" class="nav-item" th:classappend="${currentPage == 'buddy'} ? 'active' : ''">
            <i class="icon-users"></i>
            <span>バディ</span>
        </a>
        <div class="nav-item center-action">
            <button class="workout-start-btn" onclick="startWorkout()">
                <i class="icon-play"></i>
            </button>
        </div>
        <a href="/records" class="nav-item" th:classappend="${currentPage == 'records'} ? 'active' : ''">
            <i class="icon-chart"></i>
            <span>記録</span>
        </a>
        <a href="/notifications" class="nav-item" th:classappend="${currentPage == 'notifications'} ? 'active' : ''">
            <i class="icon-bell"></i>
            <span>通知</span>
        </a>
    </div>
</nav>
```

#### 1.3.2 共通CSS
```css
/* static/css/common.css */
:root {
    --primary-color: #D4A574;
    --secondary-color: #333333;
    --background-color: #F5F5F5;
    --card-background: #FFFFFF;
    --text-primary: #333333;
    --text-secondary: #666666;
    --border-radius: 8px;
    --shadow: 0 2px 8px rgba(0,0,0,0.1);
}

body {
    font-family: 'Noto Sans JP', sans-serif;
    background-color: var(--background-color);
    margin: 0;
    padding: 0;
    padding-bottom: 80px; /* ナビゲーションバーの高さ分 */
}

.container {
    max-width: 400px;
    margin: 0 auto;
    padding: 20px;
    background-color: var(--background-color);
    min-height: 100vh;
}

.card {
    background-color: var(--card-background);
    border-radius: var(--border-radius);
    box-shadow: var(--shadow);
    padding: 20px;
    margin-bottom: 16px;
}

.btn-primary {
    background-color: var(--primary-color);
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: var(--border-radius);
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    width: 100%;
    transition: background-color 0.3s;
}

.btn-primary:hover {
    background-color: #C19660;
}
```

## 2. 各画面詳細設計

### 2.1 ログイン画面 (`/login`)

#### 2.1.1 Thymeleafテンプレート
```html
<!-- templates/auth/login.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ログイン - 運動バディ</title>
    <link rel="stylesheet" th:href="@{/css/common.css}">
    <link rel="stylesheet" th:href="@{/css/auth.css}">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <h1 class="auth-title">ログイン</h1>
            
            <form th:action="@{/login}" method="post" class="auth-form">
                <div class="form-group">
                    <label for="userId">ID</label>
                    <input type="text" id="userId" name="username" 
                           placeholder="IDを入力してください" required>
                </div>
                
                <div class="form-group">
                    <label for="password">パスワード</label>
                    <input type="password" id="password" name="password" 
                           placeholder="パスワードを入力してください" required>
                </div>
                
                <button type="submit" class="btn-primary">ログイン</button>
                
                <div class="auth-link">
                    <a th:href="@{/register}">アカウントをお持ちでない方はこちら</a>
                </div>
            </form>
            
            <div th:if="${error}" class="error-message">
                <span th:text="${error}">エラーメッセージ</span>
            </div>
        </div>
    </div>
</body>
</html>
```

#### 2.1.2 専用CSS
```css
/* static/css/auth.css */
.auth-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 20px;
    background-color: var(--background-color);
}

.auth-card {
    background-color: var(--card-background);
    border-radius: var(--border-radius);
    box-shadow: var(--shadow);
    padding: 40px 30px;
    width: 100%;
    max-width: 360px;
}

.auth-title {
    text-align: center;
    color: var(--text-primary);
    margin-bottom: 30px;
    font-size: 24px;
    font-weight: bold;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    color: var(--text-primary);
    font-weight: 500;
}

.form-group input {
    width: 100%;
    padding: 12px;
    border: 1px solid #ddd;
    border-radius: var(--border-radius);
    font-size: 16px;
    box-sizing: border-box;
}

.form-group input:focus {
    outline: none;
    border-color: var(--primary-color);
}

.auth-link {
    text-align: center;
    margin-top: 20px;
}

.auth-link a {
    color: var(--primary-color);
    text-decoration: none;
}
```

### 2.2 ユーザー登録画面 (`/register`)

#### 2.2.1 Thymeleafテンプレート
```html
<!-- templates/auth/register.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ユーザー登録 - 運動バディ</title>
    <link rel="stylesheet" th:href="@{/css/common.css}">
    <link rel="stylesheet" th:href="@{/css/auth.css}">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <h1 class="auth-title">ユーザー登録画面</h1>
            
            <form th:action="@{/register}" method="post" class="auth-form" th:object="${userRegistrationDto}">
                <div class="form-group">
                    <label for="userName">名前</label>
                    <input type="text" id="userName" th:field="*{userName}" 
                           placeholder="名前を入力してください" required>
                    <div th:if="${#fields.hasErrors('userName')}" class="field-error">
                        <span th:errors="*{userName}">名前エラー</span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="userId">ID</label>
                    <input type="text" id="userId" th:field="*{userId}" 
                           placeholder="IDを入力してください" required>
                    <div th:if="${#fields.hasErrors('userId')}" class="field-error">
                        <span th:errors="*{userId}">IDエラー</span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="password">パスワード</label>
                    <input type="password" id="password" th:field="*{password}" 
                           placeholder="パスワードを入力してください" required>
                    <div th:if="${#fields.hasErrors('password')}" class="field-error">
                        <span th:errors="*{password}">パスワードエラー</span>
                    </div>
                </div>
                
                <button type="submit" class="btn-primary">登録</button>
            </form>
        </div>
    </div>

    <script th:src="@{/js/register.js}"></script>
</body>
</html>
```

### 2.3 ホーム画面 (`/`)

#### 2.3.1 Thymeleafテンプレート
```html
<!-- templates/home.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ホーム - 運動バディ</title>
    <link rel="stylesheet" th:href="@{/css/common.css}">
    <link rel="stylesheet" th:href="@{/css/home.css}">
</head>
<body>
    <div class="container">
        <header class="page-header">
            <h1>ホーム画面</h1>
        </header>
        
        <!-- 自分の進捗カード -->
        <div class="progress-card card">
            <div class="progress-header">
                <span class="progress-period">週間目標・第4回</span>
                <span class="progress-user">あなたの進歩</span>
                <span class="progress-percentage" th:text="${myProgress.percentage + '%'}">67%</span>
            </div>
            <div class="progress-bar-container">
                <div class="progress-bar">
                    <div class="progress-fill" th:style="'width: ' + ${myProgress.percentage} + '%'"></div>
                </div>
            </div>
            <div class="progress-message" th:text="${myProgress.message}">
                いつも頑張っていてえらい！あと少しで目標達成ですよ！
            </div>
            
            <div class="workout-start-section">
                <button class="workout-start-button" onclick="startWorkout()">
                    <i class="icon-run"></i>
                    <span>今すぐ運動を始める</span>
                </button>
            </div>
        </div>
        
        <!-- バディの進捗カード -->
        <div th:each="buddyProgress : ${buddyProgressList}" class="progress-card card buddy-progress">
            <div class="progress-header">
                <span class="progress-period">週間目標・第2回</span>
                <span class="progress-user" th:text="${buddyProgress.userName}">バディオの進歩</span>
                <span class="progress-percentage" th:text="${buddyProgress.percentage + '%'}">80%</span>
            </div>
            <div class="progress-bar-container">
                <div class="progress-bar">
                    <div class="progress-fill" th:style="'width: ' + ${buddyProgress.percentage} + '%'"></div>
                </div>
            </div>
            <div class="progress-message" th:text="${buddyProgress.message}">
                あと少しで週間目標を達成
            </div>
        </div>
    </div>
    
    <nav th:replace="~{fragments/navbar :: navbar}"></nav>
    
    <script th:src="@{/js/home.js}"></script>
</body>
</html>
```

#### 2.3.2 専用CSS
```css
/* static/css/home.css */
.page-header h1 {
    color: var(--text-primary);
    margin: 0 0 20px 0;
    font-size: 24px;
}

.progress-card {
    background: linear-gradient(135deg, #4a4a4a 0%, #2d2d2d 100%);
    color: white;
    border-radius: var(--border-radius);
    padding: 20px;
    margin-bottom: 20px;
}

.progress-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
}

.progress-period {
    font-size: 12px;
    opacity: 0.8;
}

.progress-user {
    font-size: 14px;
    font-weight: 500;
}

.progress-percentage {
    font-size: 32px;
    font-weight: bold;
    color: var(--primary-color);
}

.progress-bar-container {
    margin-bottom: 16px;
}

.progress-bar {
    width: 100%;
    height: 8px;
    background-color: rgba(255,255,255,0.2);
    border-radius: 4px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background-color: var(--primary-color);
    transition: width 0.5s ease;
}

.progress-message {
    font-size: 14px;
    margin-bottom: 20px;
    line-height: 1.4;
}

.workout-start-section {
    border-top: 1px solid rgba(255,255,255,0.2);
    padding-top: 20px;
}

.workout-start-button {
    background-color: var(--primary-color);
    color: white;
    border: none;
    padding: 16px 24px;
    border-radius: var(--border-radius);
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: background-color 0.3s;
}

.workout-start-button:hover {
    background-color: #C19660;
}

.buddy-progress .workout-start-section {
    display: none;
}
```

#### 2.3.3 JavaScript
```javascript
// static/js/home.js
function startWorkout() {
    // 運動開始確認ダイアログ
    if (confirm('運動を開始しますか？')) {
        // 運動中画面に遷移
        window.location.href = '/workout/start';
    }
}

// 進捗バーアニメーション
document.addEventListener('DOMContentLoaded', function() {
    const progressFills = document.querySelectorAll('.progress-fill');
    
    progressFills.forEach(fill => {
        const targetWidth = fill.style.width;
        fill.style.width = '0%';
        
        setTimeout(() => {
            fill.style.width = targetWidth;
        }, 300);
    });
});
```

### 2.4 運動中画面 (`/workout/timer`)

#### 2.4.1 Thymeleafテンプレート
```html
<!-- templates/workout/timer.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>運動中 - 運動バディ</title>
    <link rel="stylesheet" th:href="@{/css/common.css}">
    <link rel="stylesheet" th:href="@{/css/workout.css}">
</head>
<body>
    <div class="workout-container">
        <button class="close-btn" onclick="confirmExit()">×</button>
        
        <div class="timer-section">
            <div class="timer-label">目標達成まであと</div>
            <div class="timer-display" id="timerDisplay">20:00</div>
            
            <div class="timer-controls">
                <button class="control-btn pause-btn" id="pauseBtn" onclick="pauseWorkout()">
                    休憩する
                </button>
                <button class="control-btn complete-btn" id="completeBtn" onclick="completeWorkout()">
                    運動終了
                </button>
            </div>
        </div>
    </div>
    
    <script>
        // Thymeleafからデータを受け取り
        const workoutData = {
            workoutId: /*[[${workout.workoutId}]]*/ '',
            targetTimeSeconds: /*[[${workout.targetTimeSeconds}]]*/ 1200,
            startTime: /*[[${workout.startTime}]]*/ ''
        };
    </script>
    <script th:src="@{/js/workout-timer.js}"></script>
</body>
</html>
```

#### 2.4.2 専用CSS
```css
/* static/css/workout.css */
.workout-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #1a1a1a 0%, #000 100%);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: white;
    z-index: 1000;
}

.close-btn {
    position: absolute;
    top: 20px;
    right: 20px;
    background: none;
    border: none;
    color: white;
    font-size: 32px;
    cursor: pointer;
    opacity: 0.7;
    transition: opacity 0.3s;
}

.close-btn:hover {
    opacity: 1;
}

.timer-section {
    text-align: center;
}

.timer-label {
    font-size: 18px;
    margin-bottom: 20px;
    opacity: 0.8;
}

.timer-display {
    font-size: 72px;
    font-weight: bold;
    margin-bottom: 60px;
    font-family: 'Courier New', monospace;
    color: var(--primary-color);
}

.timer-controls {
    display: flex;
    gap: 20px;
    justify-content: center;
}

.control-btn {
    padding: 15px 30px;
    border: none;
    border-radius: var(--border-radius);
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.3s;
    min-width: 120px;
}

.pause-btn {
    background-color: #666;
    color: white;
}

.pause-btn:hover {
    background-color: #555;
}

.complete-btn {
    background-color: var(--primary-color);
    color: white;
}

.complete-btn:hover {
    background-color: #C19660;
}

.paused .timer-display {
    opacity: 0.5;
}

.paused .pause-btn {
    background-color: var(--primary-color);
}
```

#### 2.4.3 JavaScript
```javascript
// static/js/workout-timer.js
class WorkoutTimer {
    constructor(workoutData) {
        this.workoutId = workoutData.workoutId;
        this.targetTimeSeconds = workoutData.targetTimeSeconds;
        this.startTime = new Date(workoutData.startTime);
        this.isPaused = false;
        this.pausedTime = 0;
        this.timerInterval = null;
        
        this.init();
    }
    
    init() {
        this.startTimer();
    }
    
    startTimer() {
        this.timerInterval = setInterval(() => {
            if (!this.isPaused) {
                this.updateDisplay();
            }
        }, 1000);
    }
    
    updateDisplay() {
        const now = new Date();
        const elapsedSeconds = Math.floor((now - this.startTime) / 1000) - this.pausedTime;
        const remainingSeconds = Math.max(0, this.targetTimeSeconds - elapsedSeconds);
        
        const minutes = Math.floor(remainingSeconds / 60);
        const seconds = remainingSeconds % 60;
        
        document.getElementById('timerDisplay').textContent = 
            `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
            
        if (remainingSeconds === 0) {
            this.onTimerComplete();
        }
    }
    
    pauseWorkout() {
        if (this.isPaused) {
            // 再開
            this.isPaused = false;
            document.body.classList.remove('paused');
            document.getElementById('pauseBtn').textContent = '休憩する';
            this.pauseStartTime = null;
        } else {
            // 一時停止
            this.isPaused = true;
            document.body.classList.add('paused');
            document.getElementById('pauseBtn').textContent = '再開する';
            this.pauseStartTime = new Date();
        }
    }
    
    completeWorkout() {
        if (confirm('運動を終了しますか？')) {
            clearInterval(this.timerInterval);
            
            // 運動完了データを送信
            fetch('/api/workout/complete', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    workoutId: this.workoutId
                })
            })
            .then(response => response.json())
            .then(data => {
                // 報告画面に遷移
                window.location.href = `/workout/report/${this.workoutId}`;
            })
            .catch(error => {
                console.error('Error:', error);
                alert('運動終了の処理でエラーが発生しました');
            });
        }
    }
    
    onTimerComplete() {
        clearInterval(this.timerInterval);
        alert('目標時間に達しました！お疲れ様でした！');
        this.completeWorkout();
    }
}

// タイマー初期化
let timer;
document.addEventListener('DOMContentLoaded', function() {
    timer = new WorkoutTimer(workoutData);
});

function pauseWorkout() {
    timer.pauseWorkout();
}

function completeWorkout() {
    timer.completeWorkout();
}

function confirmExit() {
    if (confirm('運動を中断しますか？進捗は保存されません。')) {
        window.location.href = '/';
    }
}
```

### 2.5 運動報告画面 (`/workout/report`)

#### 2.5.1 Thymeleafテンプレート
```html
<!-- templates/workout/report.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>運動報告 - 運動バディ</title>
    <link rel="stylesheet" th:href="@{/css/common.css}">
    <link rel="stylesheet" th:href="@{/css/report.css}">
</head>
<body>
    <div class="report-overlay">
        <div class="report-modal">
            <button class="close-btn" onclick="closeReport()">×</button>
            
            <div class="report-content">
                <h2 class="report-title">運動終了！</h2>
                <div class="workout-summary">
                    <div class="summary-item">
                        <span class="summary-label">本日の運動時間</span>
                        <span class="summary-value" th:text="${workout.durationMinutes + '分'}">30:00</span>
                    </div>
                </div>
                
                <form th:action="@{/workout/report}" method="post" class="report-form">
                    <input type="hidden" name="workoutId" th:value="${workout.workoutId}">
                    
                    <div class="form-group">
                        <label for="comment">今日の運動についてひとこと</label>
                        <textarea id="comment" name="comment" 
                                placeholder="バディに伝えたいことを書いてみましょう" 
                                rows="4"></textarea>
                    </div>
                    
                    <div class="form-actions">
                        <button type="button" class="btn-secondary" onclick="closeReport()">
                            バディに報告
                        </button>
                        <button type="submit" class="btn-primary">
                            運動を続ける
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <script th:src="@{/js/report.js}"></script>
</body>
</html>
```

### 2.6 バディ追加画面 (`/buddy`)

#### 2.6.1 Thymeleafテンプレート
```html
<!-- templates/buddy/index.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>バディ追加 - 運動バディ</title>
    <link rel="stylesheet" th:href="@{/css/common.css}">
    <link rel="stylesheet" th:href="@{/css/buddy.css}">
</head>
<body>
    <div class="container">
        <header class="page-header">
            <h1>バディ追加画面</h1>
        </header>
        
        <!-- バディ追加セクション -->
        <div class="buddy-add-section card">
            <h3>バディを追加</h3>
            <form class="buddy-search-form" onsubmit="searchUser(event)">
                <div class="search-input-group">
                    <input type="text" id="searchUserId" placeholder="相手のユーザーIDを入力" required>
                    <button type="submit" class="btn-primary search-btn">バディを追加</button>
                </div>
            </form>
        </div>
        
        <!-- バディリクエストセクション -->
        <div class="buddy-requests-section card">
            <h3>バディリクエスト</h3>
            <div class="request-item">
                <div class="request-info">
                    <span class="request-name">バディ</span>
                    <div class="request-actions">
                        <button class="btn-accept" onclick="acceptRequest(1)">承諾</button>
                        <button class="btn-reject" onclick="rejectRequest(1)">拒否</button>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 現在のバディセクション -->
        <div class="current-buddies-section card">
            <h3>現在のバディ: 1人</h3>
            <div class="buddy-item" th:each="buddy : ${buddyList}">
                <div class="buddy-info">
                    <span class="buddy-name" th:text="${buddy.userName}">バディオ</span>
                    <button class="btn-buddy-detail" th:onclick="'showBuddyDetail(\'' + ${buddy.userId} + '\')'">
                        バディ詳細
                    </button>
                </div>
            </div>
        </div>
    </div>
    
    <nav th:replace="~{fragments/navbar :: navbar}"></nav>
    
    <script th:src="@{/js/buddy.js}"></script>
</body>
</html>
```

### 2.7 通知完了ポップアップ

#### 2.7.1 通知ポップアップComponent
```html
<!-- templates/fragments/notification-popup.html -->
<div class="notification-popup" th:fragment="workout-completed-notification" th:if="${notification}">
    <div class="notification-overlay" onclick="closeNotification()"></div>
    <div class="notification-content">
        <button class="notification-close" onclick="closeNotification()">×</button>
        
        <div class="notification-header">
            <div class="notification-icon">
                <i class="icon-trophy"></i>
            </div>
            <h3 class="notification-title">バディが運動完了！</h3>
            <p class="notification-message" th:text="${notification.message}">
                「今日も頑張りました！」
            </p>
        </div>
        
        <div class="notification-body">
            <div class="buddy-workout-info">
                <span class="workout-duration" th:text="'運動時間: ' + ${notification.workoutDuration} + '分'">
                    運動時間: 30分
                </span>
            </div>
            
            <div class="reaction-buttons">
                <button class="reaction-btn" onclick="sendReaction('like')" data-reaction="like">
                    <i class="icon-thumbs-up"></i>
                    <span>いいね</span>
                </button>
                <button class="reaction-btn" onclick="sendReaction('fire')" data-reaction="fire">
                    <i class="icon-fire"></i>
                    <span>すごい</span>
                </button>
            </div>
            
            <div class="notification-actions">
                <button class="btn-start-workout" onclick="startWorkoutFromNotification()">
                    運動を今すぐ始める
                </button>
                <button class="btn-close" onclick="closeNotification()">
                    ホームに戻る
                </button>
            </div>
        </div>
    </div>
</div>
```

#### 2.7.2 通知ポップアップCSS
```css
/* static/css/notification-popup.css */
.notification-popup {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 2000;
}

.notification-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.7);
}

.notification-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: white;
    border-radius: var(--border-radius);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    max-width: 350px;
    width: 90%;
    padding: 0;
    overflow: hidden;
}

.notification-close {
    position: absolute;
    top: 15px;
    right: 15px;
    background: none;
    border: none;
    font-size: 24px;
    color: #999;
    cursor: pointer;
    z-index: 10;
}

.notification-header {
    background: linear-gradient(135deg, #4a4a4a 0%, #2d2d2d 100%);
    color: white;
    padding: 30px 20px 20px;
    text-align: center;
}

.notification-icon {
    font-size: 40px;
    margin-bottom: 15px;
    color: var(--primary-color);
}

.notification-title {
    margin: 0 0 10px 0;
    font-size: 18px;
    font-weight: bold;
}

.notification-message {
    margin: 0;
    font-size: 16px;
    opacity: 0.9;
}

.notification-body {
    padding: 20px;
}

.buddy-workout-info {
    text-align: center;
    margin-bottom: 20px;
    padding: 10px;
    background-color: #f8f9fa;
    border-radius: var(--border-radius);
}

.workout-duration {
    font-weight: bold;
    color: var(--text-primary);
}

.reaction-buttons {
    display: flex;
    gap: 10px;
    margin-bottom: 25px;
    justify-content: center;
}

.reaction-btn {
    flex: 1;
    padding: 12px;
    border: 2px solid #e0e0e0;
    background-color: white;
    border-radius: var(--border-radius);
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
}

.reaction-btn:hover {
    border-color: var(--primary-color);
    background-color: #fff5f0;
}

.reaction-btn.selected {
    border-color: var(--primary-color);
    background-color: var(--primary-color);
    color: white;
}

.notification-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.btn-start-workout {
    background-color: var(--primary-color);
    color: white;
    border: none;
    padding: 15px;
    border-radius: var(--border-radius);
    font-size: 16px;
    font-weight: bold;
    cursor: pointer;
}

.btn-close {
    background-color: #f8f9fa;
    color: var(--text-primary);
    border: 1px solid #e0e0e0;
    padding: 12px;
    border-radius: var(--border-radius);
    font-size: 14px;
    cursor: pointer;
}
```

## 3. レスポンシブデザイン対応

### 3.1 メディアクエリ
```css
/* static/css/responsive.css */
/* タブレット対応 */
@media (min-width: 768px) {
    .container {
        max-width: 600px;
    }
    
    .timer-display {
        font-size: 96px;
    }
    
    .progress-card {
        padding: 30px;
    }
}

/* デスクトップ対応 */
@media (min-width: 1024px) {
    .container {
        max-width: 800px;
    }
    
    .auth-card {
        max-width: 450px;
    }
    
    .notification-content {
        max-width: 400px;
    }
}
```

## 4. アクセシビリティ対応

### 4.1 ARIA属性
```html
<!-- アクセシビリティ向上 -->
<button class="workout-start-button" 
        onclick="startWorkout()" 
        aria-label="運動を開始する">
    <i class="icon-run" aria-hidden="true"></i>
    <span>今すぐ運動を始める</span>
</button>

<div class="progress-bar" 
     role="progressbar" 
     th:attr="aria-valuenow=${progress.percentage},aria-valuemin=0,aria-valuemax=100">
    <div class="progress-fill" th:style="'width: ' + ${progress.percentage} + '%'"></div>
</div>
```

### 4.2 キーボードナビゲーション
```css
/* フォーカス時のスタイル */
button:focus,
input:focus,
textarea:focus {
    outline: 2px solid var(--primary-color);
    outline-offset: 2px;
}

.nav-item:focus {
    background-color: rgba(212, 165, 116, 0.2);
}
```

## 5. パフォーマンス最適化

### 5.1 CSS最適化
```css
/* 重いプロパティの最適化 */
.progress-fill {
    transform: translateZ(0); /* GPU加速 */
    will-change: width;
}

.notification-popup {
    backface-visibility: hidden;
}
```

### 5.2 JavaScript最適化
```javascript
// デバウンス処理
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 検索のデバウンス適用
const debouncedSearch = debounce(searchUser, 300);
```

この仕様書に基づいて、各画面のHTML/CSS/JavaScriptを実装し、Thymeleafテンプレートとして Spring Bootアプリケーションに組み込むことで、運動バディアプリのフロントエンドを構築できます。