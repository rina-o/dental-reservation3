package com.example.dental_reservation3.controller;

import com.example.dental_reservation3.dto.PatientRequest;
import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;
import com.example.dental_reservation3.service.MailService;
import com.example.dental_reservation3.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/new")
@RequiredArgsConstructor
public class NewPatientController {

    private final ReservationService reservationService;
    private final MailService mailService;

    // STEP① 日付選択画面の表示
    @GetMapping("/select-date")
    public String showDateSelection(Model model) {
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("availableTimes", null);
        return "new/select-date-time";
    }

    // STEP② 日付選択後、空き時間表示
    @PostMapping("/select-date")
    public String postDateSelection(@RequestParam String selectedDate, Model model, HttpSession session) {
        session.setAttribute("selectedDate", selectedDate);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableTimes", reservationService.getAvailableTimesForDate(LocalDate.parse(selectedDate)));
        return "new/select-date-time";
    }

    // STEP③ 入力フォームの表示
    @GetMapping("/input-info")
    public String showInputForm(Model model) {
        model.addAttribute("patientRequest", new PatientRequest());
        return "new/input-info";
    }

    // STEP④ 入力内容の受け取りとバリデーション
    @PostMapping("/input-info")
    public String processInputForm(
            @Valid @ModelAttribute("patientRequest") PatientRequest patientRequest,
            BindingResult result,
            HttpSession session,
            Model model) {

        if (result.hasErrors()) {
            return "new/input-info";
        }

        // セッションから予約情報取得
        String selectedDate = (String) session.getAttribute("selectedDate");
        String selectedTime = (String) session.getAttribute("selectedTime");
        String reservationType = (String) session.getAttribute("reservationType");

        // DTO → Entity
        Patient patient = new Patient();
        patient.setName(patientRequest.getName());
        patient.setEmail(patientRequest.getEmail());
        patient.setPhoneNumber(patientRequest.getPhone());
        patient.setBirthday(patientRequest.getBirthday());

        // 予約処理をまとめて保存
        reservationService.registerNewPatientWithReservation(
                patient,
                LocalDate.parse(selectedDate),
                LocalTime.parse(selectedTime),
                Reservation.ReservationType.valueOf(reservationType),
                patientRequest.getMemo()
        );

        // メール送信
        mailService.sendReservationConfirmation(
                patient.getEmail(),
                patient.getName(),
                selectedDate,
                selectedTime
        );

        // 完了画面へ
        model.addAttribute("reservedDate", selectedDate);
        model.addAttribute("reservedTime", selectedTime);
        model.addAttribute("completeMessage", "予約が完了しました");

        return "reservation-complete";
    }
}
