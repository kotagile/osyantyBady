package com.benesse.workoutbuddy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.AssertTrue;

/**
 * ユーザー登録DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {
    
    @NotBlank(message = "ユーザーIDは必須です")
    @Size(min = 3, max = 20, message = "ユーザーIDは3文字以上20文字以下で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "ユーザーIDは英数字のみ使用できます")
    private String userId;
    
    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 50, message = "ユーザー名は50文字以下で入力してください")
    private String userName;
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, message = "パスワードは6文字以上で入力してください")
    @Pattern(regexp = "^(?=.*[a-zA-Z]).*$", 
             message = "パスワードは英字を含む必要があります")
    private String password;
    
    @NotBlank(message = "パスワード確認は必須です")
    private String confirmPassword;
    
    // バリデーションメソッド
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
    
    // カスタムバリデーション
    @AssertTrue(message = "パスワードが一致しません")
    public boolean isPasswordConfirmValid() {
        return isPasswordMatch();
    }
} 