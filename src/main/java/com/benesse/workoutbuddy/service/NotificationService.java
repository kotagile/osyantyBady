package com.benesse.workoutbuddy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benesse.workoutbuddy.entity.Notification;
import com.benesse.workoutbuddy.entity.User;
import com.benesse.workoutbuddy.entity.Workout;
import com.benesse.workoutbuddy.repository.NotificationRepository;
import com.benesse.workoutbuddy.repository.UserRepository;

/**
 * 通知サービス
 */
@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * ユーザーの通知一覧を取得
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(String userId) {
        return notificationRepository.findByToUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * ユーザーの新しい通知を取得（未読）
     */
    @Transactional(readOnly = true)
    public List<Notification> getNewNotifications(String userId) {
        return notificationRepository.findByToUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 未読通知数を取得
     */
    @Transactional(readOnly = true)
    public int getUnreadCount(String userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }
    
    /**
     * 通知を既読にする
     */
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId);
        if (notification != null && notification.getToUserId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
    
    /**
     * 全通知を既読にする
     */
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }
    
    /**
     * バディの運動完了通知を作成
     */
    public void createWorkoutCompletedNotification(Workout workout, String buddyUserId) {
        try {
            User workoutUser = userRepository.findByUserId(workout.getUserId()).orElse(null);
            if (workoutUser == null) {
                return;
            }
            
            String notificationId = UUID.randomUUID().toString();
            String title = workoutUser.getUserName() + "が運動完了!";
            String message = "\"" + (workout.getComment() != null ? workout.getComment() : "頑張った!!") + "\"";
            
            // 関連データをJSON形式で保存
            String relatedData = String.format(
                "{\"workoutId\":\"%s\",\"workoutUserId\":\"%s\",\"liked\":false}",
                workout.getWorkoutId(),
                workout.getUserId()
            );
            
            Notification notification = new Notification();
            notification.setNotificationId(notificationId);
            notification.setFromUserId(workout.getUserId());
            notification.setToUserId(buddyUserId);
            notification.setNotificationType("workout_completed");
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRelatedData(relatedData);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            // ログ出力（実際の実装では適切なログライブラリを使用）
            System.err.println("通知作成エラー: " + e.getMessage());
        }
    }
    
    /**
     * バディリクエスト通知を作成
     */
    public void createBuddyRequestNotification(String requesterId, String requestedId) {
        try {
            User requester = userRepository.findByUserId(requesterId).orElse(null);
            if (requester == null) {
                return;
            }
            
            String notificationId = UUID.randomUUID().toString();
            String title = "バディリクエスト";
            String message = requester.getUserName() + "さんからバディリクエストが届きました";
            
            Notification notification = new Notification();
            notification.setNotificationId(notificationId);
            notification.setFromUserId(requesterId);
            notification.setToUserId(requestedId);
            notification.setNotificationType("buddy_request");
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRelatedData("{\"requesterId\":\"" + requesterId + "\"}");
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            System.err.println("バディリクエスト通知作成エラー: " + e.getMessage());
        }
    }
    
    /**
     * リアクション通知を作成
     */
    public void createReactionNotification(String fromUserId, String toUserId, String workoutId) {
        try {
            User fromUser = userRepository.findByUserId(fromUserId).orElse(null);
            if (fromUser == null) {
                return;
            }
            
            String notificationId = UUID.randomUUID().toString();
            String title = "いいねバッジ";
            String message = fromUser.getUserName() + "さんがあなたの運動にいいねを送りました";
            
            String relatedData = String.format(
                "{\"workoutId\":\"%s\",\"fromUserId\":\"%s\",\"reactionType\":\"like\"}",
                workoutId,
                fromUserId
            );
            
            Notification notification = new Notification();
            notification.setNotificationId(notificationId);
            notification.setFromUserId(fromUserId);
            notification.setToUserId(toUserId);
            notification.setNotificationType("reaction");
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRelatedData(relatedData);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            System.err.println("リアクション通知作成エラー: " + e.getMessage());
        }
    }
    
    /**
     * ユーザーのバディ全員に運動完了通知を送信
     */
    public void notifyBuddiesOfWorkoutCompletion(Workout workout) {
        try {
            // ユーザーのバディ一覧を取得
            List<User> buddies = userRepository.findBuddiesByUserId(workout.getUserId());
            
            // 各バディに通知を送信
            for (User buddy : buddies) {
                createWorkoutCompletedNotification(workout, buddy.getUserId());
            }
            
        } catch (Exception e) {
            System.err.println("バディ通知送信エラー: " + e.getMessage());
        }
    }
    
    /**
     * バディリクエスト通知を送信
     */
    public void notifyBuddyRequest(String requesterId, String requestedId) {
        createBuddyRequestNotification(requesterId, requestedId);
    }
    
    /**
     * 運動完了通知を送信
     */
    public void notifyWorkoutCompleted(String userId, Workout workout) {
        notifyBuddiesOfWorkoutCompletion(workout);
    }
    
    /**
     * バディ承認通知を送信
     */
    public void notifyBuddyAccepted(String requesterId, String requestedId) {
        try {
            User requestedUser = userRepository.findByUserId(requestedId).orElse(null);
            if (requestedUser == null) {
                return;
            }
            
            String notificationId = UUID.randomUUID().toString();
            String title = "バディリクエスト承認";
            String message = requestedUser.getUserName() + "さんがあなたのバディリクエストを承認しました";
            
            Notification notification = new Notification();
            notification.setNotificationId(notificationId);
            notification.setFromUserId(requestedId);
            notification.setToUserId(requesterId);
            notification.setNotificationType("buddy_accepted");
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRelatedData("{\"requestedId\":\"" + requestedId + "\"}");
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            System.err.println("バディ承認通知作成エラー: " + e.getMessage());
        }
    }

    // 通知一覧画面データDTO
    public static class NotificationsResult {
        private final java.util.List<Notification> notifications;
        private final java.util.List<Notification> newNotifications;
        public NotificationsResult(java.util.List<Notification> notifications, java.util.List<Notification> newNotifications) {
            this.notifications = notifications;
            this.newNotifications = newNotifications;
        }
        public java.util.List<Notification> getNotifications() { return notifications; }
        public java.util.List<Notification> getNewNotifications() { return newNotifications; }
    }

    public NotificationsResult getNotificationsPageData(String userId) {
        try {
            java.util.List<Notification> notifications = getNotifications(userId);
            java.util.List<Notification> newNotifications = getNewNotifications(userId);
            
            // nullのnotificationTypeをフィルタリング
            notifications = notifications.stream()
                .filter(n -> n.getNotificationType() != null)
                .collect(java.util.stream.Collectors.toList());
            newNotifications = newNotifications.stream()
                .filter(n -> n.getNotificationType() != null)
                .collect(java.util.stream.Collectors.toList());
            
            return new NotificationsResult(notifications, newNotifications);
        } catch (RuntimeException e) {
            return new NotificationsResult(java.util.List.of(), java.util.List.of());
        }
    }

    // 通知履歴画面データDTO
    public static class HistoryResult {
        private final java.util.List<Notification> notifications;
        private final int unreadCount;
        public HistoryResult(java.util.List<Notification> notifications, int unreadCount) {
            this.notifications = notifications;
            this.unreadCount = unreadCount;
        }
        public java.util.List<Notification> getNotifications() { return notifications; }
        public int getUnreadCount() { return unreadCount; }
    }

    public HistoryResult getHistoryPageData(String userId) {
        try {
            java.util.List<Notification> notifications = getNotifications(userId);
            int unreadCount = getUnreadCount(userId);
            
            // nullのnotificationTypeをフィルタリング
            notifications = notifications.stream()
                .filter(n -> n.getNotificationType() != null)
                .collect(java.util.stream.Collectors.toList());
            
            return new HistoryResult(notifications, unreadCount);
        } catch (RuntimeException e) {
            return new HistoryResult(java.util.List.of(), 0);
        }
    }

    // 新着通知チェック結果DTO
    public static class CheckNewResult {
        private final boolean hasNewNotifications;
        private final int count;
        public CheckNewResult(boolean hasNewNotifications, int count) {
            this.hasNewNotifications = hasNewNotifications;
            this.count = count;
        }
        public boolean hasNewNotifications() { return hasNewNotifications; }
        public int getCount() { return count; }
    }

    public CheckNewResult checkNewNotifications(String userId) {
        try {
            java.util.List<Notification> newNotifications = getNewNotifications(userId);
            return new CheckNewResult(!newNotifications.isEmpty(), newNotifications.size());
        } catch (Exception e) {
            return new CheckNewResult(false, 0);
        }
    }

    // アクション結果DTO
    public static class ActionResult {
        private final boolean success;
        private final String message;
        public ActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public ActionResult tryMarkAsRead(String notificationId, String userId) {
        try {
            markAsRead(notificationId, userId);
            return new ActionResult(true, "通知を既読にしました");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    public ActionResult tryMarkAllAsRead(String userId) {
        try {
            markAllAsRead(userId);
            return new ActionResult(true, "全ての通知を既読にしました");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    public ActionResult tryDeleteNotification(String notificationId, String userId) {
        try {
            Notification notification = notificationRepository.findById(notificationId);
            if (notification == null) {
                return new ActionResult(false, "通知が見つかりません");
            }
            
            if (!notification.getToUserId().equals(userId)) {
                return new ActionResult(false, "この通知を削除する権限がありません");
            }
            
            notificationRepository.delete(notification);
            return new ActionResult(true, "通知を削除しました");
        } catch (Exception e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    // 通知詳細結果DTO
    public static class NotificationDetailResult {
        private final boolean success;
        private final Notification notification;
        private final String message;
        
        public NotificationDetailResult(boolean success, Notification notification, String message) {
            this.success = success;
            this.notification = notification;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public Notification getNotification() { return notification; }
        public String getMessage() { return message; }
    }

    public NotificationDetailResult getNotificationDetails(String notificationId, String userId) {
        try {
            Notification notification = notificationRepository.findById(notificationId);
            if (notification == null) {
                return new NotificationDetailResult(false, null, "通知が見つかりません");
            }
            
            if (!notification.getToUserId().equals(userId)) {
                return new NotificationDetailResult(false, null, "この通知を表示する権限がありません");
            }
            
            // 通知を既読にする
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
            
            return new NotificationDetailResult(true, notification, null);
        } catch (Exception e) {
            return new NotificationDetailResult(false, null, e.getMessage());
        }
    }
} 