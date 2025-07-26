package com.benesse.workoutbuddy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.benesse.workoutbuddy.dto.UserRegistrationDto;
import com.benesse.workoutbuddy.entity.User;
import com.benesse.workoutbuddy.repository.UserRepository;

/**
 * ユーザーサービス
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * ユーザー登録
     */
    public User registerUser(UserRegistrationDto dto) {
        System.out.println("=== UserService.registerUser 開始 ===");
        
        try {
            // 入力値検証
            System.out.println("=== 入力値検証開始 ===");
            validateUserRegistration(dto);
            System.out.println("=== 入力値検証完了 ===");
            
            // 重複チェック
            System.out.println("=== 重複チェック開始 ===");
            if (userRepository.existsByUserId(dto.getUserId())) {
                System.out.println("重複エラー: " + dto.getUserId());
                throw new RuntimeException("既に存在するユーザーIDです");
            }
            System.out.println("=== 重複チェック完了 ===");
            
            // ユーザー作成
            System.out.println("=== ユーザー作成開始 ===");
            User user = new User();
            user.setUserId(dto.getUserId());
            user.setUserName(dto.getUserName());
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setIsActive(true);
            
            System.out.println("=== ユーザー保存開始 ===");
            User savedUser = userRepository.save(user);
            System.out.println("=== ユーザー保存完了: " + savedUser.getUserId() + " ===");
            
            // 初期目標設定（一時的に無効化）
            // System.out.println("=== 初期目標設定開始 ===");
            // createDefaultGoal(savedUser.getUserId());
            // System.out.println("=== 初期目標設定完了 ===");
            
            System.out.println("=== UserService.registerUser 完了 ===");
            return savedUser;
            
        } catch (Exception e) {
            System.out.println("=== UserService.registerUser エラー ===");
            System.out.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * ユーザー認証
     */
    public Optional<User> authenticateUser(String userId, String password) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash()) && user.getIsActive()) {
                return userOpt;
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * ユーザーIDでユーザーを取得
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * ユーザー名でユーザーを検索
     */
    @Transactional(readOnly = true)
    public List<User> searchByUserName(String userName) {
        return userRepository.findByUserNameContaining(userName);
    }
    
    /**
     * ユーザーIDの存在確認
     */
    @Transactional(readOnly = true)
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }
    
    /**
     * ユーザー名の存在確認
     */
    @Transactional(readOnly = true)
    public boolean existsByUserName(String userName) {
        return userRepository.existsByUserName(userName);
    }
    
    /**
     * ユーザー情報更新
     */
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    

    
    /**
     * ユーザー登録のバリデーション
     */
    private void validateUserRegistration(UserRegistrationDto dto) {
        if (dto.getUserId() == null || dto.getUserId().length() < 3 || dto.getUserId().length() > 20) {
            throw new RuntimeException("ユーザーIDは3文字以上20文字以下で入力してください");
        }
        
        if (!dto.getUserId().matches("^[a-zA-Z0-9]+$")) {
            throw new RuntimeException("ユーザーIDは英数字のみ使用できます");
        }
        
        if (dto.getUserName() == null || dto.getUserName().length() > 50) {
            throw new RuntimeException("ユーザー名は50文字以下で入力してください");
        }
        
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("パスワードは6文字以上で入力してください");
        }
        
        if (!dto.getPassword().matches("^(?=.*[a-zA-Z]).*$")) {
            throw new RuntimeException("パスワードは英字を含む必要があります");
        }
        
        if (!dto.isPasswordMatch()) {
            throw new RuntimeException("パスワードが一致しません");
        }
    }

    // 登録画面用DTO生成
    public UserRegistrationDto createRegistrationDto() {
        return new UserRegistrationDto();
    }

    // 登録処理結果DTO
    public static class RegistrationResult {
        private final boolean success;
        private final String errorMessage;
        public RegistrationResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    // 登録処理（例外をキャッチして結果DTOで返す）
    public RegistrationResult tryRegisterUser(UserRegistrationDto dto) {
        try {
            registerUser(dto);
            return new RegistrationResult(true, null);
        } catch (RuntimeException e) {
            return new RegistrationResult(false, e.getMessage());
        } catch (Exception e) {
            return new RegistrationResult(false, "登録処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    // userIdから安全にユーザー名を取得
    public String getUserNameSafe(String userId) {
        return findByUserId(userId).map(User::getUserName).orElse("");
    }
} 