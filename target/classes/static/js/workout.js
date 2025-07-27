// Workout Start Page
document.addEventListener('DOMContentLoaded', function() {
    const exerciseButtons = document.querySelectorAll('.exercise-type-btn');
    const startButton = document.getElementById('startWorkoutBtn');
    let selectedExercise = '';

    exerciseButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Remove selection from all buttons
            exerciseButtons.forEach(btn => btn.classList.remove('selected'));
            // Add selection to clicked button
            this.classList.add('selected');
            selectedExercise = this.getAttribute('data-exercise-type');
            startButton.disabled = false;
        });
    });

    if (startButton) {
        startButton.addEventListener('click', function() {
            if (selectedExercise) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/workout/start';
                
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'exerciseType';
                input.value = selectedExercise;
                
                form.appendChild(input);
                document.body.appendChild(form);
                form.submit();
            }
        });
    }
});

// Workout In-Progress Page
document.addEventListener('DOMContentLoaded', function() {
    const timerElement = document.getElementById('timer');
    let timerInterval;

    function updateTimer() {
        console.log('updateTimer called');
        console.log('window.targetSessionTime:', window.targetSessionTime);
        console.log('window.startTimeStr:', window.startTimeStr);
        
        // 目標時間（分）を秒に変換
        const targetSeconds = window.targetSessionTime * 60;
        console.log('targetSeconds:', targetSeconds);
        
        // 開始時刻から経過時間を計算
        const startTime = new Date(window.startTimeStr);
        const now = new Date();
        console.log('startTime:', startTime);
        console.log('now:', now);
        
        const elapsedSeconds = Math.floor((now - startTime) / 1000);
        console.log('elapsedSeconds:', elapsedSeconds);
        
        // 残り時間を計算
        const remainingSeconds = Math.max(0, targetSeconds - elapsedSeconds);
        console.log('remainingSeconds:', remainingSeconds);
        
        // 残り時間が0以下の場合は0を表示
        if (remainingSeconds <= 0) {
            timerElement.textContent = '00:00';
            timerElement.style.color = '#e74c3c'; // 赤色で表示
            return;
        }
        
        const minutes = Math.floor(remainingSeconds / 60);
        const seconds = remainingSeconds % 60;
        const timeString = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        console.log('timeString:', timeString);
        timerElement.textContent = timeString;
        
        // 残り時間が少なくなったら色を変更
        if (remainingSeconds <= 60) { // 1分以下
            timerElement.style.color = '#e74c3c'; // 赤色
        } else if (remainingSeconds <= 300) { // 5分以下
            timerElement.style.color = '#f39c12'; // オレンジ色
        } else {
            timerElement.style.color = '#C49A47'; // 通常のゴールド色
        }
    }

    if (timerElement && window.targetSessionTime && window.startTimeStr) {
        console.log('Timer initialization started');
        updateTimer();
        timerInterval = setInterval(updateTimer, 1000);
        console.log('Timer interval set');
    } else {
        console.log('Timer initialization failed:');
        console.log('timerElement:', timerElement);
        console.log('window.targetSessionTime:', window.targetSessionTime);
        console.log('window.startTimeStr:', window.startTimeStr);
    }

    // Cleanup on page unload
    window.addEventListener('beforeunload', function() {
        if (timerInterval) {
            clearInterval(timerInterval);
        }
    });
});

// Workout Complete Page
document.addEventListener('DOMContentLoaded', function() {
    const commentTextarea = document.querySelector('.comment-form textarea');
    const finishButton = document.getElementById('finishWorkoutBtn');

    if (finishButton) {
        finishButton.addEventListener('click', function() {
            const comment = commentTextarea ? commentTextarea.value : '';
            
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/workout/complete';
            
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'comment';
            input.value = comment;
            
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        });
    }
}); 