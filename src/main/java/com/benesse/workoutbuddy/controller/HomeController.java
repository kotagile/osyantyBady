package com.benesse.workoutbuddy.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.benesse.workoutbuddy.dto.ProgressDto;
import com.benesse.workoutbuddy.service.BuddyService;
import com.benesse.workoutbuddy.service.NotificationService;
import com.benesse.workoutbuddy.service.UserService;
import com.benesse.workoutbuddy.service.WorkoutService;

@Controller
public class HomeController {
    @Autowired
    private WorkoutService workoutService;
    @Autowired
    private BuddyService buddyService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/")
    public String home(Model model) {
        String userId = "05201";
        List<String> buddys=new ArrayList<String>();
        String buddyId1="123456789";
        String buddyId2="0520";
        buddys.add(buddyId1);
        buddys.add(buddyId2);
        
        
//        if (userId == null) {
//            return "redirect:/login";
//        }
//        
        try {
        	System.out.println("userName:"+workoutService.getUserInfo(userId));
        	System.out.println("progress:"+workoutService.getWeeklyProgress(userId));
    		List<String> buddyNames=new ArrayList<String>();
    		List<ProgressDto> buddyProgressList=new ArrayList<ProgressDto>();
        	for (int i=0;i<buddys.size();i++) {
        		buddyNames.add(workoutService.getUserInfo(buddys.get(i)));
        		
        		buddyProgressList.add(workoutService.getWeeklyProgress(buddys.get(i)));
        	}
        	
        	System.out.println("buddyNames:"+buddyNames.get(0)+buddyNames.get(1));
        	System.out.println("buddyProgressList:"+buddyProgressList.get(0)+buddyProgressList.get(1));
        	
            model.addAttribute("userName", workoutService.getUserInfo(userId));
            model.addAttribute("progress", workoutService.getWeeklyProgress(userId));
            model.addAttribute("buddyNames", buddyNames);
            model.addAttribute("buddyProgress", buddyProgressList);
            return "home";
        } catch (Exception e) {
        	e.printStackTrace();
            return "redirect:/login";
        }
    }
}

// HomeController専用のServiceクラス
class HomeService {
    private final UserService userService;
    private final WorkoutService workoutService;
    private final BuddyService buddyService;
    private final NotificationService notificationService;

    public HomeService(UserService userService, WorkoutService workoutService, BuddyService buddyService, NotificationService notificationService) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.buddyService = buddyService;
        this.notificationService = notificationService;
    }

    public static class HomeData {
        private final String userName;
        private final com.benesse.workoutbuddy.dto.ProgressDto progress;
        private final java.util.List<com.benesse.workoutbuddy.dto.ProgressDto> buddyProgress;
        private final int unreadNotificationCount;

        public HomeData(String userName, com.benesse.workoutbuddy.dto.ProgressDto progress, 
                       java.util.List<com.benesse.workoutbuddy.dto.ProgressDto> buddyProgress, int unreadNotificationCount) {
            this.userName = userName;
            this.progress = progress;
            this.buddyProgress = buddyProgress;
            this.unreadNotificationCount = unreadNotificationCount;
        }

        public String getUserName() { return userName; }
        public com.benesse.workoutbuddy.dto.ProgressDto getProgress() { return progress; }
        public java.util.List<com.benesse.workoutbuddy.dto.ProgressDto> getBuddyProgress() { return buddyProgress; }
        public int getUnreadNotificationCount() { return unreadNotificationCount; }
    }

    public HomeData getHomeData(String userId) {
        try {
            String userName = userService.getUserNameSafe(userId);
            com.benesse.workoutbuddy.dto.ProgressDto progress = workoutService.getWeeklyProgress(userId);
            java.util.List<com.benesse.workoutbuddy.dto.ProgressDto> buddyProgress = buddyService.getBuddyProgress(userId);
            int unreadNotificationCount = notificationService.getUnreadCount(userId);
            return new HomeData(userName, progress, buddyProgress, unreadNotificationCount);
        } catch (RuntimeException e) {
            return new HomeData("", 
                new com.benesse.workoutbuddy.dto.ProgressDto(0, 3, 0, "目標を設定してください"), 
                java.util.List.of(), 0);
        }
    }
} 