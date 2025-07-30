package com.benesse.workoutbuddy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 運動記録データ転送オブジェクト（DTO）
 * 
 * <p>運動記録のデータを画面間で転送するためのDTOクラスです。
 * エンティティと画面表示の間でデータの変換を行います。</p>
 * 
 * <p>主な用途：</p>
 * <ul>
 *   <li>運動記録一覧の表示</li>
 *   <li>運動記録の詳細表示</li>
 *   <li>APIレスポンス</li>
 *   <li>データの一時保存</li>
 * </ul>
 * 
 * @author nagahama
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutDto {
    /** 運動ID */
    private String workoutId;
    
    /** ユーザーID */
    private String userId;
    
    /** 運動日（文字列形式） */
    private String workoutDate;
    
    /** 運動開始時刻（文字列形式） */
    private String startTime;
    
    /** 運動終了時刻（文字列形式） */
    private String endTime;
    
    /** 運動時間（秒） */
    private Integer durationSeconds;
    
    /** 運動種別 */
    private String exerciseType;
    
    /** 運動に関するコメント */
    private String comment;
    
    /** 運動ステータス */
    private String status;
    
    /** 作成日時（文字列形式） */
    private String createdAt;
} 