package com.benesse.workoutbuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sample")
public class SampleShowController {

	@GetMapping("/home")
	public String showHome() {
		return "front-home";
	}
	@GetMapping("/buddy")
	public String showBuddy() {
		return "front-buddy";
	}
	
	@GetMapping("/workout")
	public String showWorkout() {
		return "front-workout";
	}
	
	@GetMapping("/record")
	public String showRecord() {
		return "front-record";
	}
	
	@GetMapping("/goal")
	public String showGoal() {
		return "front-goal";
	}
	
	@GetMapping("/workout/complete")
	public String showWorkoutComplete() {
		return "front-workout-complete";
	}
}
