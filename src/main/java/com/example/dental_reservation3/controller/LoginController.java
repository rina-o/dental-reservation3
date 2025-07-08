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
    public String showLoginForm(@RequestParam(defaultValue = "/select-treatment") String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String emailOrId,
            @RequestParam String birthday,
            @RequestParam(defaultValue = "/select-treatment") String redirect,
            HttpSession session,
            Model model
    ) {
        try {
            // yyyyMMdd フォーマットで日付をパース
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate birthdayDate = LocalDate.parse(birthday, formatter);

            // ✅ 修正済：emailまたは患者番号 + 生年月日で検索
            Optional<Patient> optionalPatient =
                    patientService.findByEmailOrPatientNumberAndBirthday(emailOrId, birthdayDate);

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

    @GetMapping("/select-treatment")
    public String showSelectTreatment(HttpSession session, Model model) {
        Patient loginPatient = (Patient) session.getAttribute("loginPatient");
        if (loginPatient == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginPatient", loginPatient);
        return "select-treatment";
    }

    @PostMapping("/select-treatment")
    public String selectTreatment(@RequestParam String type, HttpSession session) {
        session.setAttribute("reservationType", type);
        return "redirect:/select-date";
    }
}
