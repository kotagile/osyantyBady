// Notification Page
document.addEventListener('DOMContentLoaded', function() {
    // Mark notification as read when clicked
    const notificationItems = document.querySelectorAll('.notification-item');
    notificationItems.forEach(item => {
        item.addEventListener('click', function() {
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

    // Check for new notifications periodically
    checkNewNotifications();
    setInterval(checkNewNotifications, 30000); // Check every 30 seconds
});

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
        }
    })
    .catch(error => {
        console.error('Error marking all notifications as read:', error);
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