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
import com.example.dental_reservation3.service.MailService;


@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final MailService mailService;
    // ▼ 1. 通常予約フロー ▼

    @GetMapping("/select-date")
    public String showSelectDate(Model model, HttpSession session) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", null);
        model.addAttribute("availableTimes", null);
        return "select-date-time";
    }

    @PostMapping("/select-date")
    public String postSelectDate(@RequestParam String selectedDate, Model model, HttpSession session) {
        session.setAttribute("selectedDate", selectedDate);
        LocalDate date = LocalDate.parse(selectedDate);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableTimes", reservationService.getAvailableTimesForDate(date));
        return "select-date-time";
    }

    @PostMapping("/confirm")
    public String confirmReservation(@RequestParam String selectedTime, HttpSession session, Model model) {
        String selectedDate = (String) session.getAttribute("selectedDate");
        String reservationType = (String) session.getAttribute("reservationType");
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedTime", selectedTime);
        model.addAttribute("reservationType", reservationType);
        return "confirm-reservation";
    }

    // 完了画面に遷移する予約確定処理
    @PostMapping("/complete")
    public String completeReservation(
            @RequestParam String selectedDate,
            @RequestParam String selectedTime,
            @RequestParam(required = false) String memo,
            HttpSession session,
            Model model) {

        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) {
            return "redirect:/login";
        }

        Reservation.ReservationType type =
                Reservation.ReservationType.valueOf((String) session.getAttribute("reservationType"));

        LocalDate date = LocalDate.parse(selectedDate);
        LocalTime time = LocalTime.parse(selectedTime);

        reservationService.reserve(patient, date, time, type, memo);

        // ★ メール送信処理を追加 ★
        mailService.sendReservationConfirmation(
                patient.getEmail(),
                patient.getName(),
                selectedDate,
                selectedTime
        );

        model.addAttribute("reservedDate", selectedDate);
        model.addAttribute("reservedTime", selectedTime);
        model.addAttribute("completeMessage", "予約が完了しました");
        return "reservation-complete";
    }

    // ▼ 2. 予約確認画面 ▼

    @GetMapping("/check-reservation")
    public String showCheckReservation(HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        model.addAttribute("loginPatient", patient);
        model.addAttribute("reservations", reservations);
        return "check-reservation";
    }

    // ▼ 3. 予約キャンセル ▼

    @PostMapping("/cancel")
    public String cancelReservation(HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        if (!reservations.isEmpty()) {
            Reservation toCancel = reservations.get(0);
            reservationService.cancelReservation(toCancel);

            // ★ メール通知を追加
            String subject = "【スマイル歯科】予約キャンセルのお知らせ";
            String body = "以下の予約はキャンセルされました。\n\n" +
                    "予約日：" + toCancel.getReservationDate() + "\n" +
                    "時間　：" + toCancel.getReservationTime();
            mailService.sendReservationNotification(patient.getEmail(), patient.getName(), subject, body);

            model.addAttribute("message", "予約をキャンセルしました");
        } else {
            model.addAttribute("message", "キャンセル対象の予約が見つかりませんでした");
        }

        return "cancel-complete";
    }


    // ▼ 4. 予約変更（日時選択 → 確認 → 確定）▼

    @GetMapping("/change")
    public String showChangeForm(HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        if (reservations.isEmpty()) return "redirect:/reservation/check-reservation";

        Reservation current = reservations.get(0);
        session.setAttribute("currentReservation", current);

        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", null);
        model.addAttribute("availableTimes", null);
        return "change-select-date-time";
    }

    @PostMapping("/change/select")
    public String postChangeDate(@RequestParam String selectedDate, HttpSession session, Model model) {
        LocalDate date = LocalDate.parse(selectedDate);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableTimes", reservationService.getAvailableTimesForDate(date));
        return "change-select-date-time";
    }

    @PostMapping("/confirm-change")
    public String confirmChange(
            @RequestParam String newDate,
            @RequestParam String newTime,
            HttpSession session,
            Model model) {

        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        if (reservations.isEmpty()) return "redirect:/reservation/check-reservation";

        Reservation oldReservation = reservations.get(0);
        model.addAttribute("oldReservation", oldReservation);
        model.addAttribute("newDate", newDate);
        model.addAttribute("newTime", newTime);
        return "confirm-change";
    }

    @PostMapping("/change-complete")
    public String completeChangeReservation(
            @RequestParam String newDate,
            @RequestParam String newTime,
            HttpSession session,
            Model model) {

        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        List<Reservation> reservations = reservationService.getReservationsByPatient(patient);
        if (reservations.isEmpty()) return "redirect:/reservation/check-reservation";

        Reservation oldReservation = reservations.get(0);
        reservationService.cancelReservation(oldReservation);

        Reservation.ReservationType type = oldReservation.getType();
        String memo = oldReservation.getMemo();

        reservationService.reserve(patient, LocalDate.parse(newDate), LocalTime.parse(newTime), type, memo);

        // ★ メール送信
        mailService.sendReservationConfirmation(
                patient.getEmail(),
                patient.getName(),
                newDate,
                newTime
        );

        model.addAttribute("reservedDate", newDate);
        model.addAttribute("reservedTime", newTime);
        model.addAttribute("completeMessage", "予約の変更が完了しました");
        return "reservation-complete";
    }

}
