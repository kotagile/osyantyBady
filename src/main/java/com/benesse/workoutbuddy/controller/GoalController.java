package com.benesse.workoutbuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.benesse.workoutbuddy.dto.GoalDto;
import com.benesse.workoutbuddy.service.GoalService;
import com.benesse.workoutbuddy.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/goal")
public class GoalController {
    @Autowired
    private GoalService goalService;

    @GetMapping("/set")
    public String showGoalSettingForm(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        GoalDto goalDto = goalService.getGoalDtoForUser(userId);
        model.addAttribute("goalDto", goalDto);
        return "goal/set";
    }

    @PostMapping("/set")
    public String setGoal(@Validated @ModelAttribute GoalDto goalDto,
                         BindingResult bindingResult,
                         Model model,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            return "goal/set";
        }
        try {
            goalService.setGoal(userId, goalDto);
            redirectAttributes.addFlashAttribute("success", "目標設定が完了しました！");
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "goal/set";
        }
    }

    @GetMapping("/history")
    public String showGoalHistory(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("goalHistory", goalService.getGoalHistorySafe(userId));
        return "goal/history";
    }
} 