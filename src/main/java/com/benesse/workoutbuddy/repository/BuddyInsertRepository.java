package com.benesse.workoutbuddy.repository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.benesse.workoutbuddy.entity.UserBuddy;

@Repository
public class BuddyInsertRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * バディ関係を挿入
     * @param userBuddy 挿入するバディ関係
     */
    public void insertBuddy(UserBuddy userBuddy) {
        String sql = "INSERT INTO user_buddies (requester_id, requested_id, status, requested_at, responded_at) VALUES (?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            userBuddy.getRequesterId(),    // "1234"
            userBuddy.getRequestedId(),    // 選択されたバディのID
            "accepted",                    // 固定値
            LocalDateTime.now(),           // 現在時刻
            null                          // responded_atはnull
        );
    }
}
