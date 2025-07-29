package com.benesse.workoutbuddy.util;

/**
 * 通知ユーティリティクラス
 */
public class NotificationUtil {
    
    /**
     * 通知種別のラベルを取得
     */
    public static String getNotificationTypeLabel(String notificationType) {
        if (notificationType == null) {
            return "その他";
        }
        
        switch (notificationType) {
            case "workout_completed":
                return "運動完了";
            case "reaction":
                return "リアクション";
            case "buddy_request":
                return "バディリクエスト";
            case "buddy_accepted":
                return "バディ承認";
            default:
                return "その他";
        }
    }
    
    /**
     * 通知種別のアイコンを取得
     */
    public static String getNotificationTypeIcon(String notificationType) {
        if (notificationType == null) {
            return "🔔";
        }
        
        switch (notificationType) {
            case "workout_completed":
                return "🏃‍♂️";
            case "reaction":
                return "⭐";
            case "buddy_request":
                return "👤";
            case "buddy_accepted":
                return "✅";
            default:
                return "🔔";
        }
    }
} 