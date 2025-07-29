package com.benesse.workoutbuddy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 運動記録エンティティクラス
 * 
 * <p>ユーザーの運動記録を表現するエンティティです。
 * 運動の開始・終了時刻、運動時間、種別、コメントなどの情報を保持します。</p>
 * 
 * <p>運動ステータス：</p>
 * <ul>
 *   <li>in_progress: 進行中</li>
 *   <li>completed: 完了</li>
 *   <li>paused: 一時停止</li>
 *   <li>cancelled: キャンセル</li>
 * </ul>
 * 
 * @author nagahama
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workout {
    /** 運動ID（主キー） */
    private String workoutId;
    
    /** ユーザーID（外部キー） */
    private String userId;
    
    /** 運動日 */
    private LocalDate workoutDate;
    
    /** 運動開始時刻 */
    private LocalDateTime startTime;
    
    /** 運動終了時刻 */
    private LocalDateTime endTime;
    
    /** 運動時間（秒） */
    private Integer durationSeconds;
    
    /** 運動種別（例：ランニング、筋トレ、ウォーキング） */
    private String exerciseType;
    
    /** 運動に関するコメント */
    private String comment;
    
    /** 運動ステータス（in_progress, completed, paused, cancelled） */
    private String status;
    
    /** 作成日時 */
    private LocalDateTime createdAt;
    
    /** 関連するユーザーエンティティ（JdbcTemplateで手動設定） */
    private User user;
    
    /**
     * 運動が進行中かどうかを判定
     * 
     * @return 進行中の場合はtrue、そうでなければfalse
     */
    public boolean isInProgress() {
        return "in_progress".equals(status);
    }
    
    /**
     * 運動が完了しているかどうかを判定
     * 
     * @return 完了している場合はtrue、そうでなければfalse
     */
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    /**
     * 運動時間を分単位で取得
     * 
     * <p>durationSecondsを60で割って分単位に変換します。
     * durationSecondsがnullの場合は0を返します。</p>
     * 
     * @return 運動時間（分）
     */
    public Integer getDurationMinutes() {
        return durationSeconds != null ? durationSeconds / 60 : 0;
    }
} 