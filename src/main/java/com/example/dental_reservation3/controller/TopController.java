package com.example.dental_reservation3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    @GetMapping("/")
    public String showTopPage() {
        return "top";
    }
}
