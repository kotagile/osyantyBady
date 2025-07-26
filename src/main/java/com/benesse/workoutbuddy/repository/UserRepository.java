package com.benesse.workoutbuddy.repository;

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
 * ユーザーリポジトリ（JdbcTemplate版）
 */
@Repository
public class UserRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getString("user_id"));
            user.setUserName(rs.getString("user_name"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            user.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
            user.setIsActive(rs.getBoolean("is_active"));
            return user;
        }
    };
    
    /**
     * ユーザーIDでユーザーを検索
     */
    public Optional<User> findByUserId(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, userId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    /**
     * ユーザー名でユーザーを検索
     */
    public Optional<User> findByUserName(String userName) {
        String sql = "SELECT * FROM users WHERE user_name = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, userName);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    /**
     * アクティブなユーザーのみを取得
     */
    public List<User> findByIsActiveTrue() {
        String sql = "SELECT * FROM users WHERE is_active = 1";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    
    /**
     * ユーザーIDの存在確認
     */
    public boolean existsByUserId(String userId) {
        System.out.println("=== UserRepository.existsByUserId 開始 ===");
        System.out.println("チェック対象ユーザーID: " + userId);
        
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            System.out.println("SQL: " + sql);
            int count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            boolean exists = count > 0;
            System.out.println("結果: " + (exists ? "存在する" : "存在しない") + " (count: " + count + ")");
            System.out.println("=== UserRepository.existsByUserId 完了 ===");
            return exists;
        } catch (Exception e) {
            System.out.println("=== UserRepository.existsByUserId エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * ユーザー名の存在確認
     */
    public boolean existsByUserName(String userName) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_name = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, userName);
        return count > 0;
    }
    
    /**
     * 全ユーザーを取得（デバッグ用）
     */
    public List<User> findAll() {
        System.out.println("=== UserRepository.findAll 開始 ===");
        try {
            String sql = "SELECT * FROM users WHERE is_active = 1";
            System.out.println("SQL: " + sql);
            
            List<User> results = jdbcTemplate.query(sql, userRowMapper);
            System.out.println("全ユーザー数: " + results.size());
            for (User user : results) {
                System.out.println("  - ユーザーID: " + user.getUserId() + ", ユーザー名: " + user.getUserName());
            }
            System.out.println("=== UserRepository.findAll 完了 ===");
            return results;
        } catch (Exception e) {
            System.out.println("=== UserRepository.findAll エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ユーザーID完全一致検索
     */
    public List<User> findByUserIdExact(String userId) {
        System.out.println("=== UserRepository.findByUserIdExact 開始 ===");
        System.out.println("検索ユーザーID: " + userId);
        
        try {
            String sql = "SELECT * FROM users WHERE user_id = ? AND is_active = 1";
            System.out.println("SQL: " + sql);
            
            List<User> results = jdbcTemplate.query(sql, userRowMapper, userId);
            System.out.println("検索結果数: " + results.size());
            for (User user : results) {
                System.out.println("  - ユーザーID: " + user.getUserId() + ", ユーザー名: " + user.getUserName());
            }
            System.out.println("=== UserRepository.findByUserIdExact 完了 ===");
            return results;
        } catch (Exception e) {
            System.out.println("=== UserRepository.findByUserIdExact エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ユーザー名で部分検索
     */
    public List<User> findByUserNameContaining(String userName) {
        System.out.println("=== UserRepository.findByUserNameContaining 開始 ===");
        System.out.println("検索キーワード: " + userName);
        
        try {
            String sql = "SELECT * FROM users WHERE user_name LIKE ? AND is_active = 1";
            String searchPattern = "%" + userName + "%";
            System.out.println("SQL: " + sql);
            System.out.println("検索パターン: " + searchPattern);
            
            List<User> results = jdbcTemplate.query(sql, userRowMapper, searchPattern);
            System.out.println("検索結果数: " + results.size());
            for (User user : results) {
                System.out.println("  - ユーザーID: " + user.getUserId() + ", ユーザー名: " + user.getUserName());
            }
            System.out.println("=== UserRepository.findByUserNameContaining 完了 ===");
            return results;
        } catch (Exception e) {
            System.out.println("=== UserRepository.findByUserNameContaining エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * ユーザーを保存
     */
    public User save(User user) {
        System.out.println("=== UserRepository.save 開始 ===");
        System.out.println("ユーザーID: " + user.getUserId());
        System.out.println("ユーザー名: " + user.getUserName());
        
        try {
            // ユーザーが既に存在するかチェック
            boolean exists = existsByUserId(user.getUserId());
            System.out.println("ユーザー存在チェック: " + (exists ? "存在する" : "存在しない"));
            
            if (!exists) {
                // 新規作成
                System.out.println("=== 新規ユーザー作成 ===");
                String sql = "INSERT INTO users (user_id, user_name, password_hash, created_at, updated_at, is_active) VALUES (?, ?, ?, ?, ?, ?)";
                System.out.println("SQL: " + sql);
                jdbcTemplate.update(sql,
                    user.getUserId(),
                    user.getUserName(),
                    user.getPasswordHash(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getIsActive()
                );
                System.out.println("=== 新規ユーザー作成完了 ===");
            } else {
                // 更新
                System.out.println("=== ユーザー更新 ===");
                String sql = "UPDATE users SET user_name = ?, password_hash = ?, updated_at = ?, is_active = ? WHERE user_id = ?";
                System.out.println("SQL: " + sql);
                jdbcTemplate.update(sql,
                    user.getUserName(),
                    user.getPasswordHash(),
                    user.getUpdatedAt(),
                    user.getIsActive(),
                    user.getUserId()
                );
                System.out.println("=== ユーザー更新完了 ===");
            }
            System.out.println("=== UserRepository.save 完了 ===");
            return user;
        } catch (Exception e) {
            System.out.println("=== UserRepository.save エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * ユーザーIDでユーザーを取得
     */
    public Optional<User> findById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, userId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    /**
     * ユーザーのバディ一覧を取得
     */
    public List<User> findBuddiesByUserId(String userId) {
        String sql = "SELECT u.* FROM users u " +
                    "INNER JOIN user_buddies ub ON (u.user_id = ub.requester_id OR u.user_id = ub.requested_id) " +
                    "WHERE (ub.requester_id = ? OR ub.requested_id = ?) " +
                    "AND ub.status = 'accepted' " +
                    "AND u.user_id != ?";
        return jdbcTemplate.query(sql, userRowMapper, userId, userId, userId);
    }
} 