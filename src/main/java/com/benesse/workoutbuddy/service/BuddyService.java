package com.benesse.workoutbuddy.service;

import com.benesse.workoutbuddy.dto.ProgressDto;
import com.benesse.workoutbuddy.entity.User;
import com.benesse.workoutbuddy.entity.UserBuddy;
import com.benesse.workoutbuddy.repository.UserRepository;
import com.benesse.workoutbuddy.repository.UserBuddyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * バディサービス
 */
@Service
@Transactional
public class BuddyService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserBuddyRepository userBuddyRepository;
    
    @Autowired
    private WorkoutService workoutService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * バディリクエスト送信
     */
    public void sendBuddyRequest(String requesterId, String requestedId) {
        // 対象ユーザー存在確認
        userRepository.findById(requestedId)
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        // 自分自身へのリクエストチェック
        if (requesterId.equals(requestedId)) {
            throw new RuntimeException("自分自身にバディリクエストは送信できません");
        }
        
        // 重複チェック
        boolean existingRequest = userBuddyRepository.existsPendingRequest(requesterId, requestedId);
        if (existingRequest) {
            throw new RuntimeException("既にバディリクエストが送信されています");
        }
        
        // 既存のバディ関係チェック
        boolean alreadyBuddies = userBuddyRepository.areBuddies(requesterId, requestedId);
        if (alreadyBuddies) {
            throw new RuntimeException("既にバディ関係です");
        }
        
        // リクエスト作成
        UserBuddy buddyRequest = new UserBuddy();
        buddyRequest.setRequesterId(requesterId);
        buddyRequest.setRequestedId(requestedId);
        buddyRequest.setStatus("pending");
        buddyRequest.setRequestedAt(LocalDateTime.now());
        
        userBuddyRepository.save(buddyRequest);
        
        // 通知送信
        notificationService.notifyBuddyRequest(requesterId, requestedId);
    }
    
    /**
     * バディリクエスト承認
     */
    public void acceptBuddyRequest(Long buddyId) {
        UserBuddy buddyRequest = userBuddyRepository.findById(buddyId)
            .orElseThrow(() -> new RuntimeException("バディリクエストが見つかりません"));
        
        if (!"pending".equals(buddyRequest.getStatus())) {
            throw new RuntimeException("承認できないリクエストステータスです");
        }
        
        // ステータス更新
        buddyRequest.setStatus("accepted");
        buddyRequest.setRespondedAt(LocalDateTime.now());
        
        userBuddyRepository.save(buddyRequest);
        
        // 承認通知送信
        notificationService.notifyBuddyAccepted(buddyRequest.getRequesterId(), buddyRequest.getRequestedId());
    }
    
    /**
     * バディリクエスト拒否
     */
    public void rejectBuddyRequest(Long buddyId) {
        UserBuddy buddyRequest = userBuddyRepository.findById(buddyId)
            .orElseThrow(() -> new RuntimeException("バディリクエストが見つかりません"));
        
        if (!"pending".equals(buddyRequest.getStatus())) {
            throw new RuntimeException("拒否できないリクエストステータスです");
        }
        
        // ステータス更新
        buddyRequest.setStatus("rejected");
        buddyRequest.setRespondedAt(LocalDateTime.now());
        
        userBuddyRepository.save(buddyRequest);
    }
    
    /**
     * バディ一覧取得
     */
    @Transactional(readOnly = true)
    public List<User> getBuddyList(String userId) {
        List<UserBuddy> buddyRelations = userBuddyRepository.findAcceptedBuddiesByUserId(userId);
        
        return buddyRelations.stream()
            .map(buddy -> {
                if (buddy.getRequesterId().equals(userId)) {
                    return userRepository.findById(buddy.getRequestedId()).orElse(null);
                } else {
                    return userRepository.findById(buddy.getRequesterId()).orElse(null);
                }
            })
            .filter(user -> user != null)
            .collect(Collectors.toList());
    }
    
    /**
     * バディ進捗取得
     */
    @Transactional(readOnly = true)
    public List<ProgressDto> getBuddyProgress(String userId) {
        List<User> buddies = getBuddyList(userId);
        
        return buddies.stream()
            .map(buddy -> {
                try {
                    ProgressDto progress = workoutService.getWeeklyProgress(buddy.getUserId());
                    progress.setUserName(buddy.getUserName());
                    return progress;
                } catch (Exception e) {
                    // 目標が設定されていない場合はデフォルト値を返す
                    return new ProgressDto(0, 3, 0, "目標未設定", buddy.getUserName(), "未設定");
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 保留中のリクエスト一覧取得
     */
    @Transactional(readOnly = true)
    public List<UserBuddy> getPendingRequests(String userId) {
        return userBuddyRepository.findPendingRequestsByUserId(userId);
    }
    
    /**
     * バディ関係解除
     */
    public void removeBuddy(String userId, String buddyId) {
        List<UserBuddy> buddyRelations = userBuddyRepository.findAcceptedBuddiesByUserId(userId);
        
        UserBuddy relationToRemove = buddyRelations.stream()
            .filter(buddy -> 
                (buddy.getRequesterId().equals(userId) && buddy.getRequestedId().equals(buddyId)) ||
                (buddy.getRequesterId().equals(buddyId) && buddy.getRequestedId().equals(userId))
            )
            .findFirst()
            .orElseThrow(() -> new RuntimeException("バディ関係が見つかりません"));
        
        userBuddyRepository.delete(relationToRemove);
    }

    // バディ一覧・保留リクエスト・ユーザー名・エラーをまとめて返すDTO
    public static class BuddyListResult {
        private final java.util.List<User> buddies;
        private final java.util.List<UserBuddy> pendingRequests;
        private final String userName;
        private final String error;
        public BuddyListResult(java.util.List<User> buddies, java.util.List<UserBuddy> pendingRequests, String userName, String error) {
            this.buddies = buddies;
            this.pendingRequests = pendingRequests;
            this.userName = userName;
            this.error = error;
        }
        public java.util.List<User> getBuddies() { return buddies; }
        public java.util.List<UserBuddy> getPendingRequests() { return pendingRequests; }
        public String getUserName() { return userName; }
        public String getError() { return error; }
    }

    public BuddyListResult getBuddyListPageData(String userId) {
        try {
            java.util.List<User> buddies = getBuddyList(userId);
            java.util.List<UserBuddy> pendingRequests = getPendingRequests(userId);
            String userName = userRepository.findById(userId).map(User::getUserName).orElse("");
            return new BuddyListResult(buddies, pendingRequests, userName, null);
        } catch (RuntimeException e) {
            return new BuddyListResult(java.util.List.of(), java.util.List.of(), "", e.getMessage());
        }
    }

    // バディ検索結果DTO
    public static class BuddySearchResult {
        private final java.util.List<User> searchResults;
        private final String userName;
        private final String error;
        public BuddySearchResult(java.util.List<User> searchResults, String userName, String error) {
            this.searchResults = searchResults;
            this.userName = userName;
            this.error = error;
        }
        public java.util.List<User> getSearchResults() { return searchResults; }
        public String getUserName() { return userName; }
        public String getError() { return error; }
    }

    public BuddySearchResult searchBuddies(String userId, String searchTerm) {
        System.out.println("=== BuddyService.searchBuddies 開始 ===");
        System.out.println("検索ユーザーID: " + userId);
        System.out.println("検索キーワード: " + searchTerm);
        
        try {
            // デバッグ用：全ユーザーを確認
            System.out.println("=== 全ユーザー確認 ===");
            java.util.List<User> allUsers = userRepository.findAll();
            
            java.util.List<User> searchResults = userRepository.findByUserIdExact(searchTerm);
            System.out.println("検索結果（自分を除く前）: " + searchResults.size() + "件");
            
            searchResults.removeIf(user -> user.getUserId().equals(userId));
            System.out.println("検索結果（自分を除く後）: " + searchResults.size() + "件");
            
            String userName = userRepository.findById(userId).map(User::getUserName).orElse("");
            System.out.println("=== BuddyService.searchBuddies 完了 ===");
            return new BuddySearchResult(searchResults, userName, null);
        } catch (RuntimeException e) {
            System.out.println("=== BuddyService.searchBuddies エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            return new BuddySearchResult(java.util.List.of(), "", e.getMessage());
        }
    }

    // アクション結果DTO
    public static class ActionResult {
        private final boolean success;
        private final String error;
        public ActionResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }

    public ActionResult trySendBuddyRequest(String requesterId, String requestedUserId) {
        try {
            sendBuddyRequest(requesterId, requestedUserId);
            return new ActionResult(true, null);
        } catch (RuntimeException e) {
            return new ActionResult(false, e.getMessage());
        }
    }
    public ActionResult tryAcceptBuddyRequest(Long buddyId) {
        try {
            acceptBuddyRequest(buddyId);
            return new ActionResult(true, null);
        } catch (RuntimeException e) {
            return new ActionResult(false, e.getMessage());
        }
    }
    public ActionResult tryRejectBuddyRequest(Long buddyId) {
        try {
            rejectBuddyRequest(buddyId);
            return new ActionResult(true, null);
        } catch (RuntimeException e) {
            return new ActionResult(false, e.getMessage());
        }
    }
    public ActionResult tryRemoveBuddy(String userId, String buddyId) {
        try {
            removeBuddy(userId, buddyId);
            return new ActionResult(true, null);
        } catch (RuntimeException e) {
            return new ActionResult(false, e.getMessage());
        }
    }
} 