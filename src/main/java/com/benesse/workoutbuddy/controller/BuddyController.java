package com.benesse.workoutbuddy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.benesse.workoutbuddy.entity.User;
import com.benesse.workoutbuddy.service.BuddyService;

@Controller
@RequestMapping("/buddy")
public class BuddyController {
	@Autowired
	private BuddyService buddyService;

    /**
     * バディ検索画面を表示
     * @param searchTerm 検索キーワード
     * @param model
     * @return
     */
    @GetMapping("/search")
    public String searchBuddies(@RequestParam(required = false) String searchTerm, Model model) {
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            model.addAttribute("searchTerm", searchTerm);
            List<User> buddyList = buddyService.searchBuddy(searchTerm);
            model.addAttribute("buddyList", buddyList);
        } else {
            model.addAttribute("searchTerm", "");
        }
        
        return "buddy/editedlist";
    }
    
    /**
     * バディ追加画面を表示
     * @param model
     * @return
     */
    @GetMapping("")
    public String displayAddBuddy(Model model) {
        return "buddy/editedlist";
    }
    
    /**
     * バディ追加処理
     * @param searchId 追加するユーザーID
     * @param model
     * @return
     */
    @PostMapping("/add")
    public String addBuddy(@RequestParam String searchId, Model model) {
        
        // バディ追加処理
        buddyService.addBuddy(searchId);
        
        // 成功メッセージを設定
        model.addAttribute("successMessage", "バディを追加しました");
                   
        return "buddy/editedlist";
    }
} 