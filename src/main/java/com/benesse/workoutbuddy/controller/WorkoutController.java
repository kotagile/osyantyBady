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

import com.benesse.workoutbuddy.service.UserService;
import com.benesse.workoutbuddy.service.WorkoutService;
import com.benesse.workoutbuddy.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/workout")
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;
    @Autowired
    private UserService userService;

    @GetMapping("/start")
    public String showWorkoutStart(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("userName", userService.getUserNameSafe(userId));
        return "workout/start";
    }

    @PostMapping("/start")
    public String startWorkout(@RequestParam String exerciseType, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        WorkoutService.StartWorkoutResult result = workoutService.tryStartWorkout(userId, exerciseType);
        if (result.isSuccess()) {
            session.setAttribute("currentWorkoutId", result.getWorkoutId());
            return "redirect:/workout/in-progress";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
            return "redirect:/workout/start";
        }
    }
    
    /**
     * 目標の運動種別で直接運動を開始
     */
    @PostMapping("/start-with-goal")
    public String startWorkoutWithGoal(HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        WorkoutService.StartWorkoutResult result = workoutService.tryStartWorkoutWithGoal(userId);
        if (result.isSuccess()) {
            session.setAttribute("currentWorkoutId", result.getWorkoutId());
            return "redirect:/workout/in-progress";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
            return "redirect:/goal/set";
        }
    }

    @GetMapping("/in-progress")
    public String showWorkoutInProgress(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        String workoutId = (String) session.getAttribute("currentWorkoutId");
        
        if (userId == null || workoutId == null) {
            return "redirect:/";
        }
        
        WorkoutService.InProgressResult result = workoutService.getInProgressData(userId, workoutId);
        
        if (result.isValid()) {
            model.addAttribute("workout", result.getWorkout());
            model.addAttribute("targetSessionTime", result.getTargetSessionTime());
            model.addAttribute("userName", result.getUserName());
            return "workout/in-progress";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/complete")
    public String showWorkoutComplete(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        String workoutId = (String) session.getAttribute("currentWorkoutId");
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

    @PostMapping("/complete")
    public String completeWorkout(@RequestParam String comment, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        String workoutId = (String) session.getAttribute("currentWorkoutId");
        if (userId == null || workoutId == null) {
            return "redirect:/";
        }
        WorkoutService.CompleteWorkoutResult result = workoutService.tryCompleteWorkout(userId, workoutId, comment);
        if (result.isSuccess()) {
            session.removeAttribute("currentWorkoutId");
            redirectAttributes.addFlashAttribute("success", "運動完了！お疲れさまでした！");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
            return "redirect:/workout/in-progress";
        }
    }

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