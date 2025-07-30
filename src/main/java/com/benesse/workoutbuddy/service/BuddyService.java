package com.benesse.workoutbuddy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.benesse.workoutbuddy.dto.ProgressDto;
import com.benesse.workoutbuddy.entity.UserBuddy;
import com.benesse.workoutbuddy.repository.BuddyInsertRepository;

/**
 * バディサービス
 */
@Service
public class BuddyService {

    
    @Autowired
    private  BuddyInsertRepository  buddyInsertRepository;

    /**
     * ユーザーを固定（Sessionが実装されていたらそのセッションから取得）
     * @param seachId
     * searchIdを"RequestId"に変更。DTOとテーブルのカラム名に統一したため
     */
    public void addBuddy(String seachId) {
        // UserBuddyオブジェクトを作成、将来的にはDTOかEntityにしたい
        UserBuddy userBuddy = new UserBuddy();
        userBuddy.setRequesterId("1234"); // 固定値
        userBuddy.setRequestedId(seachId);
        
        // リポジトリでデータベースに保存
        buddyInsertRepository.insertBuddy(userBuddy);
    }

	public List<ProgressDto> getBuddyProgress(String userId) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}