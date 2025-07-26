package com.benesse.workoutbuddy.repository;

import com.benesse.workoutbuddy.entity.UserBuddy;
import com.benesse.workoutbuddy.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ユーザーバディリポジトリ（JdbcTemplate版）
 */
@Repository
public class UserBuddyRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<UserBuddy> userBuddyRowMapper = new RowMapper<UserBuddy>() {
        @Override
        public UserBuddy mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserBuddy userBuddy = new UserBuddy();
            userBuddy.setBuddyId(rs.getLong("buddy_id"));
            userBuddy.setRequesterId(rs.getString("requester_id"));
            userBuddy.setRequestedId(rs.getString("requested_id"));
            userBuddy.setStatus(rs.getString("status"));
            userBuddy.setRequestedAt(rs.getObject("requested_at", LocalDateTime.class));
            userBuddy.setRespondedAt(rs.getObject("responded_at", LocalDateTime.class));
            
            // リクエスト送信者の情報を設定
            User requester = new User();
            requester.setUserId(rs.getString("requester_id"));
            requester.setUserName(rs.getString("requester_user_name"));
            userBuddy.setRequester(requester);
            
            // リクエスト受信者の情報を設定
            User requested = new User();
            requested.setUserId(rs.getString("requested_id"));
            requested.setUserName(rs.getString("requested_user_name"));
            userBuddy.setRequested(requested);
            
            return userBuddy;
        }
    };
    
    /**
     * リクエスト送信者と受信者でバディ関係を検索
     */
    public Optional<UserBuddy> findByRequesterIdAndRequestedId(String requesterId, String requestedId) {
        String sql = "SELECT ub.*, u1.user_name as requester_user_name, u2.user_name as requested_user_name " +
                    "FROM user_buddies ub " +
                    "LEFT JOIN users u1 ON ub.requester_id = u1.user_id " +
                    "LEFT JOIN users u2 ON ub.requested_id = u2.user_id " +
                    "WHERE ub.requester_id = ? AND ub.requested_id = ?";
        List<UserBuddy> buddies = jdbcTemplate.query(sql, userBuddyRowMapper, requesterId, requestedId);
        return buddies.isEmpty() ? Optional.empty() : Optional.of(buddies.get(0));
    }
    
    /**
     * ユーザーIDで承認済みバディ一覧を取得
     */
    public List<UserBuddy> findAcceptedBuddiesByUserId(String userId) {
        String sql = "SELECT ub.*, u1.user_name as requester_user_name, u2.user_name as requested_user_name " +
                    "FROM user_buddies ub " +
                    "LEFT JOIN users u1 ON ub.requester_id = u1.user_id " +
                    "LEFT JOIN users u2 ON ub.requested_id = u2.user_id " +
                    "WHERE (ub.requester_id = ? OR ub.requested_id = ?) AND ub.status = 'accepted'";
        return jdbcTemplate.query(sql, userBuddyRowMapper, userId, userId);
    }
    
    /**
     * ユーザーIDで保留中のリクエスト一覧を取得
     */
    public List<UserBuddy> findPendingRequestsByUserId(String userId) {
        String sql = "SELECT ub.*, u1.user_name as requester_user_name, u2.user_name as requested_user_name " +
                    "FROM user_buddies ub " +
                    "LEFT JOIN users u1 ON ub.requester_id = u1.user_id " +
                    "LEFT JOIN users u2 ON ub.requested_id = u2.user_id " +
                    "WHERE ub.requested_id = ? AND ub.status = 'pending'";
        return jdbcTemplate.query(sql, userBuddyRowMapper, userId);
    }
    
    /**
     * 保留中のリクエストが存在するかチェック
     */
    public boolean existsPendingRequest(String requesterId, String requestedId) {
        String sql = "SELECT COUNT(*) FROM user_buddies WHERE requester_id = ? AND requested_id = ? AND status = 'pending'";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, requesterId, requestedId);
        return count > 0;
    }
    
    /**
     * バディ関係が存在するかチェック
     */
    public boolean areBuddies(String userId1, String userId2) {
        String sql = "SELECT COUNT(*) FROM user_buddies WHERE " +
                    "((requester_id = ? AND requested_id = ?) OR (requester_id = ? AND requested_id = ?)) " +
                    "AND status = 'accepted'";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userId1, userId2, userId2, userId1);
        return count > 0;
    }
    
    /**
     * バディ関係を保存
     */
    public UserBuddy save(UserBuddy userBuddy) {
        if (userBuddy.getBuddyId() == null) {
            // 新規作成
            String sql = "INSERT INTO user_buddies (requester_id, requested_id, status, requested_at, responded_at) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                userBuddy.getRequesterId(),
                userBuddy.getRequestedId(),
                userBuddy.getStatus(),
                userBuddy.getRequestedAt(),
                userBuddy.getRespondedAt()
            );
            
            // 生成されたIDを取得
            String idSql = "SELECT SCOPE_IDENTITY()";
            Long id = jdbcTemplate.queryForObject(idSql, Long.class);
            userBuddy.setBuddyId(id);
        } else {
            // 更新
            String sql = "UPDATE user_buddies SET status = ?, responded_at = ? WHERE buddy_id = ?";
            jdbcTemplate.update(sql,
                userBuddy.getStatus(),
                userBuddy.getRespondedAt(),
                userBuddy.getBuddyId()
            );
        }
        return userBuddy;
    }
    
    /**
     * バディ関係を削除
     */
    public void delete(UserBuddy userBuddy) {
        String sql = "DELETE FROM user_buddies WHERE buddy_id = ?";
        jdbcTemplate.update(sql, userBuddy.getBuddyId());
    }
    
    /**
     * IDでバディ関係を取得
     */
    public Optional<UserBuddy> findById(Long buddyId) {
        String sql = "SELECT ub.*, u1.user_name as requester_user_name, u2.user_name as requested_user_name " +
                    "FROM user_buddies ub " +
                    "LEFT JOIN users u1 ON ub.requester_id = u1.user_id " +
                    "LEFT JOIN users u2 ON ub.requested_id = u2.user_id " +
                    "WHERE ub.buddy_id = ?";
        List<UserBuddy> buddies = jdbcTemplate.query(sql, userBuddyRowMapper, buddyId);
        return buddies.isEmpty() ? Optional.empty() : Optional.of(buddies.get(0));
    }
} 