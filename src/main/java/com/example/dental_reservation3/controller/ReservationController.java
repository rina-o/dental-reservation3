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
import com.example.dental_reservation3.repository.ReservationRepository;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final MailService mailService;
    private final ReservationRepository reservationRepository;

    // 新患用の日時選択画面
    @GetMapping("/select-date-time")
    public String showSelectDateTime(Model model) {
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", null);
        model.addAttribute("availableTimes", null);
        return "select-date-time";
    }

    // 再診用の日時選択画面
    @GetMapping("/reservation/select-date")
    public String showSelectDateForReturningPatient(HttpSession session, Model model) {
        Patient loginPatient = (Patient) session.getAttribute("loginPatient");
        if (loginPatient == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginPatient", loginPatient);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", null);
        model.addAttribute("availableTimes", null);
        return "select-date-time";
    }

    // 新患用の日付選択後の時間表示
    @PostMapping("/select-date-time")
    public String processSelectDateTime(
            @RequestParam String selectedDate,
            @RequestParam String reservationType,
            Model model) {
        
        System.out.println("Selected Date: " + selectedDate);
        System.out.println("Reservation Type: " + reservationType);
        
        List<LocalTime> availableTimes = reservationService.getAvailableTimesForDate(LocalDate.parse(selectedDate));
        System.out.println("Available Times: " + availableTimes);
        
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("reservationType", reservationType);
        model.addAttribute("availableTimes", availableTimes);
        return "select-date-time";
    }

    // 再診用の日付選択後の時間表示
    @PostMapping("/reservation/select-date")
    public String processSelectDateForReturningPatient(
            @RequestParam String selectedDate,
            @RequestParam String reservationType,
            HttpSession session,
            Model model) {
        
        Patient loginPatient = (Patient) session.getAttribute("loginPatient");
        if (loginPatient == null) {
            return "redirect:/login";
        }
        
        System.out.println("Selected Date: " + selectedDate);
        System.out.println("Reservation Type: " + reservationType);
        
        List<LocalTime> availableTimes = reservationService.getAvailableTimesForDate(LocalDate.parse(selectedDate));
        System.out.println("Available Times: " + availableTimes);
        
        model.addAttribute("loginPatient", loginPatient);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("reservationType", reservationType);
        model.addAttribute("availableTimes", availableTimes);
        return "select-date-time";
    }

    // 新患・再診分岐画面への遷移
    @PostMapping("/entry")
    public String entryReservation(
            @RequestParam String selectedDate,
            @RequestParam String selectedTime,
            @RequestParam String reservationType,
            @RequestParam(required = false) String memo,
            HttpSession session) {

        // セッションに予約情報を保存
        session.setAttribute("selectedDate", selectedDate);
        session.setAttribute("selectedTime", selectedTime);
        session.setAttribute("reservationType", reservationType);
        session.setAttribute("memo", memo);

        // 新患は直接個人情報入力画面へリダイレクト
        return "redirect:/new/input-info";
    }

    // 再診用の予約確定処理
    @PostMapping("/reservation/complete")
    public String completeReturningPatientReservation(HttpSession session, Model model) {
        Patient loginPatient = (Patient) session.getAttribute("loginPatient");
        if (loginPatient == null) {
            return "redirect:/login";
        }

        String selectedDate = (String) session.getAttribute("selectedDate");
        String selectedTime = (String) session.getAttribute("selectedTime");
        String reservationType = (String) session.getAttribute("reservationType");
        String memo = (String) session.getAttribute("memo");
        
        // デバッグ情報をログ出力
        System.out.println("=== 予約確定処理のデバッグ情報 ===");
        System.out.println("selectedDate: " + selectedDate);
        System.out.println("selectedTime: " + selectedTime);
        System.out.println("reservationType: " + reservationType);
        System.out.println("memo: " + memo);
        System.out.println("loginPatient: " + loginPatient.getName());
        
        // セッションの全属性をログ出力
        System.out.println("セッションの全属性:");
        java.util.Enumeration<String> sessionAttributes = session.getAttributeNames();
        while (sessionAttributes.hasMoreElements()) {
            String attrName = sessionAttributes.nextElement();
            Object attrValue = session.getAttribute(attrName);
            System.out.println("  " + attrName + " = " + attrValue);
        }

        // セッション情報の検証
        if (selectedDate == null || selectedTime == null || reservationType == null) {
            System.out.println("セッション情報が不足しています");
            model.addAttribute("error", "予約情報が正しく設定されていません。最初からやり直してください。");
            return "error";
        }
        
        try {
            // 予約処理
            reservationService.reserve(
                loginPatient,
                LocalDate.parse(selectedDate),
                LocalTime.parse(selectedTime),
                Reservation.ReservationType.valueOf(reservationType),
                memo
            );

            // メール送信
        mailService.sendReservationConfirmation(
                loginPatient.getEmail(),
                loginPatient.getName(),
                selectedDate,
                selectedTime
        );

            // セッションクリア
            session.removeAttribute("selectedDate");
            session.removeAttribute("selectedTime");
            session.removeAttribute("reservationType");
            session.removeAttribute("memo");
            // ログイン患者情報も削除（予約完了後はログアウト）
            session.removeAttribute("loginPatient");

        model.addAttribute("reservedDate", selectedDate);
        model.addAttribute("reservedTime", selectedTime);
        model.addAttribute("completeMessage", "予約が完了しました");
        return "reservation-complete";

        } catch (Exception e) {
            model.addAttribute("error", "予約処理中にエラーが発生しました: " + e.getMessage());
            return "error";
        }
    }

    // 再診用の確認画面表示
    @PostMapping("/confirm")
    public String confirmReservation(
            @RequestParam String selectedTime,
            @RequestParam String selectedDate,
            @RequestParam String reservationType,
            @RequestParam(required = false) String memo,
            HttpSession session, 
            Model model) {
        
        Patient loginPatient = (Patient) session.getAttribute("loginPatient");
        if (loginPatient == null) {
            return "redirect:/login";
        }

        // セッションに予約情報を保存
        session.setAttribute("selectedDate", selectedDate);
        session.setAttribute("selectedTime", selectedTime);
        session.setAttribute("reservationType", reservationType);
        session.setAttribute("memo", memo);

        model.addAttribute("loginPatient", loginPatient);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedTime", selectedTime);
        model.addAttribute("reservationType", reservationType);
        model.addAttribute("memo", memo);
        return "confirm-reservation";
    }

    // ▼ 2. 予約確認画面 ▼

    @GetMapping("/check-reservation")
    public String showCheckReservation(HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        List<Reservation> futureReservations = reservationService.getFutureReservationsByPatient(patient);
        model.addAttribute("loginPatient", patient);
        model.addAttribute("reservations", futureReservations);
        return "check-reservation";
    }

    // ▼ 3. 予約キャンセル ▼

    @PostMapping("/reservation/cancel")
    public String cancelReservation(@RequestParam Integer reservationId, HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        // 予約IDで特定の予約を取得
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            
            // 患者の予約かどうか確認
            if (reservation.getPatient().getId().equals(patient.getId())) {
                reservationService.cancelReservation(reservation);

            // ★ メール通知を追加
            String subject = "【スマイル歯科】予約キャンセルのお知らせ";
            String body = "以下の予約はキャンセルされました。\n\n" +
                        "予約日：" + reservation.getReservationDate() + "\n" +
                        "時間　：" + reservation.getReservationTime();
            mailService.sendReservationNotification(patient.getEmail(), patient.getName(), subject, body);

            model.addAttribute("message", "予約をキャンセルしました");
            } else {
                model.addAttribute("message", "この予約をキャンセルする権限がありません");
            }
        } else {
            model.addAttribute("message", "キャンセル対象の予約が見つかりませんでした");
        }

        return "cancel-complete";
    }


    // ▼ 4. 予約変更（日時選択 → 確認 → 確定）▼

    @GetMapping("/change")
    public String showChangeForm(@RequestParam Integer reservationId, HttpSession session, Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        // 予約IDで特定の予約を取得
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();

            // 患者の予約かどうか確認
            if (reservation.getPatient().getId().equals(patient.getId())) {
                session.setAttribute("currentReservation", reservation);

                model.addAttribute("currentReservation", reservation);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
        model.addAttribute("selectedDate", null);
        model.addAttribute("availableTimes", null);
        return "change-select-date-time";
    }
        }
        
        return "redirect:/check-reservation";
    }

    @PostMapping("/change")
    public String processChangeDate(
            @RequestParam String newDate, 
            @RequestParam Integer reservationId,
            HttpSession session, 
            Model model) {
        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        // 予約IDで特定の予約を取得
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            
            // 患者の予約かどうか確認
            if (reservation.getPatient().getId().equals(patient.getId())) {
                LocalDate date = LocalDate.parse(newDate);
                List<LocalTime> availableTimes = reservationService.getAvailableTimesForDate(date);

                model.addAttribute("currentReservation", reservation);
        model.addAttribute("availableDates", reservationService.getAvailableDates());
                model.addAttribute("selectedDate", newDate);
                model.addAttribute("availableTimes", availableTimes);
        return "change-select-date-time";
            }
        }
        
        return "redirect:/check-reservation";
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
        if (reservations.isEmpty()) return "redirect:/check-reservation";

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
            @RequestParam Integer reservationId,
            HttpSession session,
            Model model) {

        Patient patient = (Patient) session.getAttribute("loginPatient");
        if (patient == null) return "redirect:/login";

        // 予約IDで特定の予約を取得
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);
        if (reservationOpt.isPresent()) {
            Reservation oldReservation = reservationOpt.get();
            
            // 患者の予約かどうか確認
            if (oldReservation.getPatient().getId().equals(patient.getId())) {
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
        
        return "redirect:/check-reservation";
    }

}
