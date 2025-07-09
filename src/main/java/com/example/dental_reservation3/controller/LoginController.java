package com.example.dental_reservation3.controller;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.service.PatientService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final PatientService patientService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(defaultValue = "/reservation/select-date") String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String emailOrId,
            @RequestParam String birthday,
            @RequestParam(defaultValue = "/reservation/select-date") String redirect,
            HttpSession session,
            Model model
    ) {
        try {
            // yyyyMMdd フォーマットで日付をパース
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate birthdayDate = LocalDate.parse(birthday, formatter);

            // ✅ 修正済：emailまたは患者番号 + 生年月日で検索
            Optional<Patient> optionalPatient =
                    patientService.findByEmailOrPatientCodeAndBirthday(emailOrId, birthdayDate);

            if (optionalPatient.isPresent()) {
                session.setAttribute("loginPatient", optionalPatient.get());
                return "redirect:" + redirect;
            } else {
                model.addAttribute("error", "ログイン情報が正しくありません");
                model.addAttribute("redirect", redirect);
                return "login";
            }

        } catch (DateTimeParseException e) {
            model.addAttribute("error", "生年月日の形式が正しくありません（例：19900101）");
            model.addAttribute("redirect", redirect);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // セッションからログイン患者情報を削除
        session.removeAttribute("loginPatient");
        // 予約関連のセッション情報も削除
        session.removeAttribute("selectedDate");
        session.removeAttribute("selectedTime");
        session.removeAttribute("reservationType");
        session.removeAttribute("memo");
        session.removeAttribute("inputPatientName");
        session.removeAttribute("inputPatientEmail");
        session.removeAttribute("inputPatientPhone");
        session.removeAttribute("inputPatientBirthday");
        
        return "redirect:/";
    }

}
