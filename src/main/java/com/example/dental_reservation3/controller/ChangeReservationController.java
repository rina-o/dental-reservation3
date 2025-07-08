package com.example.dental_reservation3.controller;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;
import com.example.dental_reservation3.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/reservation/change")
@RequiredArgsConstructor
public class ChangeReservationController {

    private final ReservationService reservationService;

    // 日付選択画面の表示（select-date-time.html を再利用）
    @GetMapping("/select")
    public String showChangeSelectDate(HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) {
            return "redirect:/login";
        }

        List<LocalDate> availableDates = reservationService.getAvailableDates();
        model.addAttribute("availableDates", availableDates);
        model.addAttribute("selectedDate", null);
        model.addAttribute("availableTimes", null);

        // 再利用のためURL渡す
        model.addAttribute("actionUrl", "/reservation/change/select");
        model.addAttribute("confirmUrl", "/reservation/change/confirm");

        return "select-date-time";
    }

    // 日付が選ばれたあと、空き時間を取得
    @PostMapping("/select")
    public String postChangeSelectDate(@RequestParam String selectedDate, HttpSession session, Model model) {
        session.setAttribute("changeSelectedDate", selectedDate);

        List<LocalDate> availableDates = reservationService.getAvailableDates();
        LocalDate date = LocalDate.parse(selectedDate);
        List<LocalTime> availableTimes = reservationService.getAvailableTimesForDate(date);

        model.addAttribute("availableDates", availableDates);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableTimes", availableTimes);

        model.addAttribute("actionUrl", "/reservation/change/select");
        model.addAttribute("confirmUrl", "/reservation/change/confirm");

        return "select-date-time";
    }
    @PostMapping("/confirm")
    public String confirmChangeReservation(
            @RequestParam String selectedTime,
            HttpSession session,
            Model model) {

        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) {
            return "redirect:/login";
        }

        // 変更前予約（1件のみ前提）
        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        Reservation oldReservation = reservations.isEmpty() ? null : reservations.get(0);

        String selectedDate = (String) session.getAttribute("changeSelectedDate");
        LocalDate newDate = LocalDate.parse(selectedDate);
        LocalTime newTime = LocalTime.parse(selectedTime);

        model.addAttribute("oldReservation", oldReservation);
        model.addAttribute("newDate", newDate);
        model.addAttribute("newTime", newTime);

        return "confirm-change-reservation";
    }

    @PostMapping("/complete")
    public String completeChangeReservation(
            @RequestParam String selectedDate,
            @RequestParam String selectedTime,
            @RequestParam(required = false) String memo,
            HttpSession session,
            Model model) {

        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) {
            return "redirect:/login";
        }

        // 旧予約を取得してキャンセル（1件のみ前提）
        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        if (!reservations.isEmpty()) {
            Reservation oldReservation = reservations.get(0);
            reservationService.cancelReservation(oldReservation);
        }

        // 新予約を登録
        Reservation.ReservationType type =
                Reservation.ReservationType.valueOf((String) session.getAttribute("reservationType"));
        LocalDate date = LocalDate.parse(selectedDate);
        LocalTime time = LocalTime.parse(selectedTime);

        reservationService.reserve(patient, date, time, type, memo);

        model.addAttribute("newDate", selectedDate);
        model.addAttribute("newTime", selectedTime);
        return "change-complete";
    }

}
