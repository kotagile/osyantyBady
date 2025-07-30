package com.benesse.workoutbuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.benesse.workoutbuddy.service.BuddyService;

@Controller
public class BuddyController {
	@Autowired
	private BuddyService buddyService;
    @GetMapping("/buddy")
    public String displayAddBuddy() {
           
  
                      
           return "buddy/editedlist";
       }
    
    /**
     * @param searchId
     * @param model
     * @return
     */
    @PostMapping("/buddy/add")
     public String addBuddy(@RequestParam String searchId, Model model) {
            
            // バディ追加処理
            buddyService.addBuddy(searchId);
            
            // 成功メッセージを設定
            model.addAttribute("successMessage", "バディを追加しました");
                       
            return "buddy/editedlist";
        }
        
    
} 