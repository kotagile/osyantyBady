package com.benesse.workoutbuddy.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.benesse.workoutbuddy.dto.EditedGoalDto;
import com.benesse.workoutbuddy.service.GoalService;

@Controller
public class GoalController {
	@Autowired
	private GoalService goalService;
	
	@GetMapping("goal/set")
	/**目標設定画面表示コントローラーメソッド
	 * @param model
	 * @return 空の目標DTOを含んだ状態で目標設定htmlを表示
	 */
	public String showGoalSettingForm(Model model) {
		model.addAttribute("goalDto", new EditedGoalDto());
		return "goal/editedSet";
	}
	
	@PostMapping("goal/set")
	/**目標設定DB挿入コントローラーメソッド
	 * @param goalDto
	 * @param bindingResult
	 * @param model
	 * @return
	 */
	public String setGoal(
	        EditedGoalDto goalDto,
	        BindingResult bindingResult,
	        Model model) {
		//    	セッションを後回しにするのでここでは仮のユーザーIDを設定
		String userId = "123456";
		
		//    	エラー時にリダイレクト
		if (bindingResult.hasErrors()) {
			return "goal/editedSet";
		}
		
		//    	セッションに格納されているID(DB内の過去目標論理削除用)と目標設定Dtoを受け取りながらサービスの目標設定メソッドたたき
		try {
			model.addAttribute("success", "目標の設定が完了しました。");
			goalService.setGoal(userId, goalDto);
		} catch (Exception e) {
			model.addAttribute("error", "エラーが発生しました。");
			return "goal/editedSet";
		}
		
		//    	登録完了後はホーム画面遷移
		return "home";
	}
	
	@GetMapping("goal/get")
	/**
	 * 登録済みの最新目標の取得メソッド（とりあえず自分の目標のみ、バディの目標取得は別スプリントにて）
	 * @param model
	 * @return ホーム画面などの目標を表示したい画面
	 */
	public String showLatestGoal(Model model) {
		
		//      本当は引数にセッションを入れてID特定に使う
		String userId = "123456";
		
		//DBからIDをもとに最新の目標を取得
		EditedGoalDto latestGoal = goalService.getLatestGoal(userId);
		model.addAttribute("latestGoal", latestGoal);
		
		return "home";
	}
}