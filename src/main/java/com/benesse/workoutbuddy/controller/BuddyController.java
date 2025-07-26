package com.benesse.workoutbuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.benesse.workoutbuddy.service.BuddyService;
import com.benesse.workoutbuddy.service.UserService;
import com.benesse.workoutbuddy.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/buddy")
public class BuddyController {
    @Autowired
    private BuddyService buddyService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String showBuddyList(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        BuddyService.BuddyListResult result = buddyService.getBuddyListPageData(userId);
        model.addAttribute("buddies", result.getBuddies());
        model.addAttribute("pendingRequests", result.getPendingRequests());
        model.addAttribute("userName", result.getUserName());
        if (result.getError() != null) {
            model.addAttribute("error", result.getError());
        }
        return "buddy/list";
    }

    @GetMapping("/search")
    public String showBuddySearch(Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("userName", userService.getUserNameSafe(userId));
        return "buddy/search";
    }

    @PostMapping("/search")
    public String searchBuddies(@RequestParam String searchTerm, Model model, HttpSession session) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        BuddyService.BuddySearchResult result = buddyService.searchBuddies(userId, searchTerm);
        model.addAttribute("searchResults", result.getSearchResults());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("userName", result.getUserName());
        if (result.getError() != null) {
            model.addAttribute("error", result.getError());
        }
        return "buddy/search";
    }

    @PostMapping("/request")
    public String sendBuddyRequest(@RequestParam String requestedUserId, HttpSession session, RedirectAttributes redirectAttributes) {
        String requesterId = SecurityUtil.getCurrentUserId();
        if (requesterId == null) {
            return "redirect:/login";
        }
        BuddyService.ActionResult result = buddyService.trySendBuddyRequest(requesterId, requestedUserId);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "バディリクエストを送信しました");
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
        }
        return "redirect:/buddy/search";
    }

    @PostMapping("/accept")
    public String acceptBuddyRequest(@RequestParam Long buddyId, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        BuddyService.ActionResult result = buddyService.tryAcceptBuddyRequest(buddyId);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "バディリクエストを承認しました");
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
        }
        return "redirect:/buddy";
    }

    @PostMapping("/reject")
    public String rejectBuddyRequest(@RequestParam Long buddyId, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        BuddyService.ActionResult result = buddyService.tryRejectBuddyRequest(buddyId);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "バディリクエストを拒否しました");
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
        }
        return "redirect:/buddy";
    }

    @PostMapping("/remove")
    public String removeBuddy(@RequestParam String buddyId, HttpSession session, RedirectAttributes redirectAttributes) {
        String userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        BuddyService.ActionResult result = buddyService.tryRemoveBuddy(userId, buddyId);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "バディ関係を解除しました");
        } else {
            redirectAttributes.addFlashAttribute("error", result.getError());
        }
        return "redirect:/buddy";
    }
} 