package com.example.dental_reservation3.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@Slf4j
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Object exception = request.getAttribute("javax.servlet.error.exception");

        log.error("エラーが発生しました: status={}, message={}, exception={}", status, message, exception);

        model.addAttribute("error", "エラーが発生しました");
        model.addAttribute("message", message != null ? message.toString() : "不明なエラー");
        model.addAttribute("timestamp", LocalDateTime.now());

        return "error";
    }
} 