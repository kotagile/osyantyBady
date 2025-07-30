// Notification Page
document.addEventListener('DOMContentLoaded', function() {
    // Mark notification as read when clicked
    const notificationItems = document.querySelectorAll('.notification-item');
    notificationItems.forEach(item => {
        item.addEventListener('click', function(e) {
            // アクションボタンがクリックされた場合は既読処理をスキップ
            if (e.target.classList.contains('action-btn')) {
                return;
            }
            
            const notificationId = this.getAttribute('data-notification-id');
            if (notificationId) {
                markAsRead(notificationId);
            }
        });
    });

    // Mark all as read button
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    if (markAllReadBtn) {
        markAllReadBtn.addEventListener('click', function() {
            markAllAsRead();
        });
    }

    // アクションボタンのイベントリスナーを設定
    setupActionButtons();

    // Check for new notifications periodically
    checkNewNotifications();
    setInterval(checkNewNotifications, 30000); // Check every 30 seconds
});

function setupActionButtons() {
    // 運動詳細を見るボタン
    const viewWorkoutBtns = document.querySelectorAll('.view-workout-btn');
    viewWorkoutBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const workoutId = this.getAttribute('data-workout-id');
            if (workoutId) {
                viewWorkoutDetails(workoutId);
            }
        });
    });

    // バディ承認ボタン
    const acceptBuddyBtns = document.querySelectorAll('.accept-buddy-btn');
    acceptBuddyBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const requesterId = this.getAttribute('data-requester-id');
            if (requesterId) {
                acceptBuddyRequest(requesterId);
            }
        });
    });

    // バディ拒否ボタン
    const rejectBuddyBtns = document.querySelectorAll('.reject-buddy-btn');
    rejectBuddyBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const requesterId = this.getAttribute('data-requester-id');
            if (requesterId) {
                rejectBuddyRequest(requesterId);
            }
        });
    });
}

function markAsRead(notificationId) {
    fetch('/notifications/mark-read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `notificationId=${notificationId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove unread styling
            const notificationItem = document.querySelector(`[data-notification-id="${notificationId}"]`);
            if (notificationItem) {
                notificationItem.classList.remove('unread');
            }
        }
    })
    .catch(error => {
        console.error('Error marking notification as read:', error);
    });
}

function markAllAsRead() {
    fetch('/notifications/mark-all-read', {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove unread styling from all notifications
            const unreadNotifications = document.querySelectorAll('.notification-item.unread');
            unreadNotifications.forEach(item => {
                item.classList.remove('unread');
            });
            
            // Disable mark all read button
            const markAllReadBtn = document.getElementById('markAllReadBtn');
            if (markAllReadBtn) {
                markAllReadBtn.disabled = true;
                markAllReadBtn.textContent = '全て既読';
            }
            
            // Show success message
            showMessage('全ての通知を既読にしました', 'success');
        } else {
            showMessage(data.message || 'エラーが発生しました', 'error');
        }
    })
    .catch(error => {
        console.error('Error marking all notifications as read:', error);
        showMessage('エラーが発生しました', 'error');
    });
}

function checkNewNotifications() {
    fetch('/notifications/check-new')
    .then(response => response.json())
    .then(data => {
        if (data.hasNewNotifications) {
            showNotificationPopup(data.count);
        }
    })
    .catch(error => {
        console.error('Error checking new notifications:', error);
    });
}

function showNotificationPopup(count) {
    // Remove existing popup
    const existingPopup = document.querySelector('.notification-popup');
    if (existingPopup) {
        existingPopup.remove();
    }

    // Create new popup
    const popup = document.createElement('div');
    popup.className = 'notification-popup';
    popup.innerHTML = `
        <div class="notification-popup-title">新しい通知</div>
        <div class="notification-popup-message">${count}件の新しい通知があります</div>
    `;

    document.body.appendChild(popup);

    // Remove popup after 5 seconds
    setTimeout(() => {
        if (popup.parentNode) {
            popup.remove();
        }
    }, 5000);
}

function viewWorkoutDetails(workoutId) {
    // 運動詳細ページに遷移
    window.location.href = `/workout/details/${workoutId}`;
}

function acceptBuddyRequest(requesterId) {
    fetch('/buddy/accept', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `requesterId=${requesterId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showMessage('バディリクエストを承認しました', 'success');
            // 通知アイテムを更新または削除
            updateNotificationItem(requesterId, 'accepted');
        } else {
            showMessage(data.message || 'エラーが発生しました', 'error');
        }
    })
    .catch(error => {
        console.error('Error accepting buddy request:', error);
        showMessage('エラーが発生しました', 'error');
    });
}

function rejectBuddyRequest(requesterId) {
    if (!confirm('バディリクエストを拒否しますか？')) {
        return;
    }
    
    fetch('/buddy/reject', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `requesterId=${requesterId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showMessage('バディリクエストを拒否しました', 'success');
            // 通知アイテムを更新または削除
            updateNotificationItem(requesterId, 'rejected');
        } else {
            showMessage(data.message || 'エラーが発生しました', 'error');
        }
    })
    .catch(error => {
        console.error('Error rejecting buddy request:', error);
        showMessage('エラーが発生しました', 'error');
    });
}

function updateNotificationItem(requesterId, action) {
    // 該当する通知アイテムを探して更新
    const notificationItems = document.querySelectorAll('.notification-item');
    notificationItems.forEach(item => {
        const acceptBtn = item.querySelector(`.accept-buddy-btn[data-requester-id="${requesterId}"]`);
        const rejectBtn = item.querySelector(`.reject-buddy-btn[data-requester-id="${requesterId}"]`);
        
        if (acceptBtn || rejectBtn) {
            // アクションボタンを削除
            const actionsDiv = item.querySelector('.notification-actions');
            if (actionsDiv) {
                actionsDiv.remove();
            }
            
            // メッセージを更新
            const messageElement = item.querySelector('.notification-message');
            if (messageElement) {
                if (action === 'accepted') {
                    messageElement.textContent = 'バディリクエストを承認しました';
                } else if (action === 'rejected') {
                    messageElement.textContent = 'バディリクエストを拒否しました';
                }
            }
            
            // 既読にする
            item.classList.remove('unread');
        }
    });
}

function showMessage(message, type) {
    // 既存のメッセージを削除
    const existingMessage = document.querySelector('.message-popup');
    if (existingMessage) {
        existingMessage.remove();
    }

    // 新しいメッセージを作成
    const messageDiv = document.createElement('div');
    messageDiv.className = `message-popup ${type}`;
    messageDiv.textContent = message;

    // スタイルを設定
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        background: ${type === 'success' ? '#4caf50' : '#f44336'};
        color: white;
        padding: 12px 24px;
        border-radius: 6px;
        z-index: 1001;
        font-size: 0.9rem;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;

    document.body.appendChild(messageDiv);

    // 3秒後に削除
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.remove();
        }
    }, 3000);
} 