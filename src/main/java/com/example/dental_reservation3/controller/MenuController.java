package com.example.dental_reservation3.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MenuController {

    @GetMapping("/menu")
    public String showMenu() {
        return "menu"; // menu.html に遷移
    }
}
