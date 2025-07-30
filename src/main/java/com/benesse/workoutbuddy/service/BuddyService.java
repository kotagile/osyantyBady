package com.benesse.workoutbuddy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.benesse.workoutbuddy.dto.ProgressDto;
import com.benesse.workoutbuddy.entity.User;
import com.benesse.workoutbuddy.entity.UserBuddy;
import com.benesse.workoutbuddy.repository.BuddyInsertRepository;
import com.benesse.workoutbuddy.repository.UserRepository;

/**
 * バディサービス
 */
@Service
public class BuddyService {

    @Autowired
    private BuddyInsertRepository buddyInsertRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * ユーザーを固定（Sessionが実装されていたらそのセッションから取得）
     * @param searchId
     * searchIdを"RequestId"に変更。DTOとテーブルのカラム名に統一したため
     */
    public void addBuddy(String searchId) {
        // UserBuddyオブジェクトを作成、将来的にはDTOかEntityにしたい
        UserBuddy userBuddy = new UserBuddy();
        userBuddy.setRequesterId("1234567890"); // 固定値
        userBuddy.setRequestedId(searchId);
        
        // リポジトリでデータベースに保存
        buddyInsertRepository.insertBuddy(userBuddy);
    }

    /**
     * バディ検索機能
     * @param searchTerm 検索キーワード
     * @return 検索結果のユーザーリスト
     */
    public List<User> searchBuddy(String searchTerm) {
        // ユーザーIDで完全一致検索
        List<User> userIdResults = userRepository.findByUserIdExact(searchTerm);
        if (!userIdResults.isEmpty()) {
            return userIdResults;
        }
        
        // ユーザー名で部分検索
        return userRepository.findByUserNameContaining(searchTerm);
    }

    /**
     * バディの進捗を取得
     * @param userId ユーザーID
     * @return バディの進捗リスト
     */
    public List<ProgressDto> getBuddyProgress(String userId) {
        // TODO: バディの進捗を取得する実装
        // 現在は空のリストを返す
        return List.of();
    }
}