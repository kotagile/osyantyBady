package com.benesse.workoutbuddy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.benesse.workoutbuddy.service.NotificationService;
import com.benesse.workoutbuddy.util.NotificationUtil;
import com.benesse.workoutbuddy.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String showNotifications(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        NotificationService.NotificationsResult result = notificationService.getNotificationsPageData(userId);
        model.addAttribute("notifications", result.getNotifications());
        model.addAttribute("newNotifications", result.getNewNotifications());
        return "notifications";
    }

    @GetMapping("/history")
    public String showNotificationHistory(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        NotificationService.HistoryResult result = notificationService.getHistoryPageData(userId);
        model.addAttribute("notifications", result.getNotifications());
        model.addAttribute("unreadNotificationCount", result.getUnreadCount());
        return "notifications/history";
    }

    @ModelAttribute("getNotificationTypeLabel")
    public String getNotificationTypeLabel(String notificationType) {
        return NotificationUtil.getNotificationTypeLabel(notificationType);
    }

    @GetMapping("/check-new")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkNewNotifications(HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("hasNewNotifications", false));
        }
        NotificationService.CheckNewResult result = notificationService.checkNewNotifications(userId);
        return ResponseEntity.ok(Map.of(
            "hasNewNotifications", result.hasNewNotifications(),
            "count", result.getCount()
        ));
    }

    @PostMapping("/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@RequestParam String notificationId, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "ログインが必要です"));
        }
        NotificationService.ActionResult result = notificationService.tryMarkAsRead(notificationId, userId);
        return ResponseEntity.ok(Map.of("success", result.isSuccess(), "message", result.getMessage()));
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "ログインが必要です"));
        }
        NotificationService.ActionResult result = notificationService.tryMarkAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", result.isSuccess(), "message", result.getMessage()));
    }
} 