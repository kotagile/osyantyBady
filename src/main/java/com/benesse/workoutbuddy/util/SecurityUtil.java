package com.benesse.workoutbuddy.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security関連のユーティリティクラス
 */
public class SecurityUtil {
    
    /**
     * 現在の認証ユーザーのIDを取得
     * @return ユーザーID（未認証の場合はnull）
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }
    
    /**
     * 現在のユーザーが認証されているかチェック
     * @return 認証済みの場合true
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getName());
    }
} 