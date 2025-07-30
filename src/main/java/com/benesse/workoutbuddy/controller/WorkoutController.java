package com.benesse.workoutbuddy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.benesse.workoutbuddy.service.WorkoutService;
import com.benesse.workoutbuddy.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

/**
 * 運動管理機能のコントローラー
 * 
 * <p>運動の開始、進行中、完了、記録表示などの機能を提供します。
 * ユーザーの運動セッション管理とリアクション機能を担当します。</p>
 * 
 * @author nagahama
 * @version 1.0
 * @since 2024-01-01
 */
@Controller
@RequestMapping("/workout")
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;
    

    /**
     * 目標の運動種別で運動を開始
     * 
     * <p>ユーザーが設定した目標の運動種別で運動を開始します。
     * 目標が設定されていない場合はエラーを返します。</p>
     * 
     * @param session HTTPセッション
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先のURL
     */
    @PostMapping("/start-with-goal")
    public String startWorkoutWithGoal(RedirectAttributes redirectAttributes) {
        String userId = "1234567890";

        WorkoutService.StartWorkoutResult result = workoutService.tryStartWorkoutWithGoal(userId);
        if (result.isSuccess()) {
            return "redirect:/workout/in-progress";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
            return "redirect:/goal/set";
            
        }
    }

    /**
     * 運動進行中画面を表示
     * 
     * <p>現在進行中の運動のタイマーと進捗を表示します。
     * 運動の一時停止や完了の操作が可能です。</p>
     * 
     * @param model Spring MVCのモデル
     * @param session HTTPセッション
     * @return 運動進行中画面のテンプレート名
     */
    @GetMapping("/in-progress")
    public String showWorkoutInProgress(Model model) {
        String userId = "1234567890";
        
        
        // userIdから進行中の運動データを取得するように変更
        WorkoutService.InProgressResult result = workoutService.getInProgressData(userId);
        
        if (result.isValid() && result.getWorkout() != null) {
            model.addAttribute("workout", result.getWorkout());
            model.addAttribute("targetSessionTime", result.getTargetSessionTime());
            model.addAttribute("userName", result.getUserName());
            return "workout/in-progress";
        } else {
            return "redirect:/";
        }
    }

    /**
     * 運動完了画面を表示
     * 
     * <p>運動完了後の結果表示とコメント入力画面を表示します。
     * 運動時間や種別の確認ができます。</p>
     * 
     * @param model Spring MVCのモデル
     * @param session HTTPセッション
     * @return 運動完了画面のテンプレート名
     */
    @GetMapping("/complete")
    public String showWorkoutComplete(@RequestParam String workoutId, Model model) {
        String userId = "1234567890";
        if (userId == null || workoutId == null) {
            return "redirect:/";
        }
        WorkoutService.CompleteResult result = workoutService.getCompleteData(userId, workoutId);
        if (result.isValid()) {
            model.addAttribute("workout", result.getWorkout());
            model.addAttribute("workoutDuration", result.getWorkoutDuration());
            return "workout/complete";
        } else {
            return "redirect:/";
        }
    }

    /**
     * 運動を完了
     * 
     * <p>進行中の運動を完了し、コメントと共に記録を保存します。
     * バディへの通知も自動的に送信されます。</p>
     * 
     * @param comment 運動に関するコメント
     * @param session HTTPセッション
     * @param redirectAttributes リダイレクト時の属性
     * @return リダイレクト先のURL
     */
    @PostMapping("/complete")
    public String completeWorkout(@RequestParam String workoutId, @RequestParam String comment, RedirectAttributes redirectAttributes) {
        String userId = "1234567890";
        if (userId == null || workoutId == null) {
            return "redirect:/";
        }
//        通知機能は後ほど追加
        WorkoutService.CompleteWorkoutResult result = workoutService.tryCompleteWorkout(userId, workoutId, comment);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "運動完了！お疲れさまでした！");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
            return "redirect:/workout/in-progress";
        }
    }

    /**
     * 運動にリアクションを送信
     * 
     * <p>他のユーザーの運動記録に対してリアクション（いいね、素晴らしい等）を送信します。
     * リアクション送信者には通知が送信されます。</p>
     * 
     * @param request リアクション情報を含むリクエストマップ
     * @param session HTTPセッション
     * @return リアクション送信結果のJSONレスポンス
     */
    @PostMapping("/reaction")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendReaction(@RequestBody Map<String, Object> request, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "ログインが必要です"));
        }
        WorkoutService.ReactionResult result = workoutService.tryAddReaction(userId, request);
        return ResponseEntity.ok(Map.of("success", result.isSuccess(), "message", result.getMessage()));
    }

    /**
     * 運動記録一覧画面を表示
     * 
     * <p>ユーザーの過去の運動記録を一覧表示します。
     * 運動日、種別、時間、コメントを確認できます。</p>
     * 
     * @param model Spring MVCのモデル
     * @param session HTTPセッション
     * @return 運動記録一覧画面のテンプレート名
     */
    @GetMapping("/records")
    public String showWorkoutRecords(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        WorkoutService.RecordsResult result = workoutService.getRecordsData(userId);
        model.addAttribute("workouts", result.getWorkouts());
        model.addAttribute("userName", result.getUserName());
        return "workout/records";
    }
} 