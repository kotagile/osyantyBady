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
    const startTime = new Date();
    let timerInterval;

    function updateTimer() {
        const now = new Date();
        const elapsed = Math.floor((now - startTime) / 1000);
        const minutes = Math.floor(elapsed / 60);
        const seconds = elapsed % 60;
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    if (timerElement) {
        updateTimer();
        timerInterval = setInterval(updateTimer, 1000);
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