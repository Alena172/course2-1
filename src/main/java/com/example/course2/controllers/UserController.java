package com.example.course2.controllers;

import com.example.course2.services.UserService;
import com.example.course2.models.User;
import lombok.extern.slf4j.Slf4j;  // Импортируем аннотацию Lombok
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping()
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getRegistration(Model model) {
        model.addAttribute("userReg", new User());
        return "form";
    }

    @PostMapping("reg")
    public String addNewUser(@ModelAttribute("userReg") User user, Model model) {
        userService.addUser(user);
        return "redirect:/login";
    }

}
