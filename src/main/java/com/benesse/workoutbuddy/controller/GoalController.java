package com.benesse.workoutbuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.benesse.workoutbuddy.dto.GoalDto;
import com.benesse.workoutbuddy.service.GoalService;




@Controller
@RequestMapping("/goal")
public class GoalController {
	@Autowired
	private GoalService goalService;

    
    //セッションを追加する必要あり？？
    @GetMapping
    public String showGoalSettingForm(Model model) {
    	
   
    	return "goal/set";
    	
    }
    
    @PostMapping
    public String setGoal(
    		GoalDto goalDto,
    		BindingResult bindingResult,
    		Model model    		
    		) {
//    	セッションを後回しにするので仮のユーザーIDを設定
    	String userId="729";
    	
    	if (bindingResult.hasErrors()) {
    		return "redirect:goal/set";
    	}
    	
    	try {
    		model.addAttribute("success","目標の設定が完了しました。");
    	goalService.setGoal(userId, goalDto);
    	}catch(Exception e) {
    		model.addAttribute("error","エラーが発生しました。");
    		return "redirect:goal/set";
    	}
        //TODO: process POST request
        
        return "home";
    }
    
} 