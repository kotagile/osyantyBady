package com.benesse.workoutbuddy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.benesse.workoutbuddy.dto.UserRegistrationDto;
import com.benesse.workoutbuddy.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "ログインに失敗しました。IDとパスワードを確認してください。");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistrationDto", userService.createRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Validated @ModelAttribute UserRegistrationDto userRegistrationDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        UserService.RegistrationResult result = userService.tryRegisterUser(userRegistrationDto);
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("success", "ユーザー登録が完了しました。ログインしてください。");
            return "redirect:/login";
        } else {
            model.addAttribute("error", result.getErrorMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
} 