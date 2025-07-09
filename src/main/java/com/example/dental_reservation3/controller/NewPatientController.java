package com.example.dental_reservation3.controller;

import com.example.dental_reservation3.dto.PatientRequest;
import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;
import com.example.dental_reservation3.service.MailService;
import com.example.dental_reservation3.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/new")
@RequiredArgsConstructor
@Slf4j
public class NewPatientController {

    private final ReservationService reservationService;
    private final MailService mailService;

    // 入力フォームの表示
    @GetMapping("/input-info")
    public String showInputForm(HttpSession session, Model model) {
        model.addAttribute("patientRequest", new PatientRequest());
        
        // セッションから予約情報を取得してモデルに追加
        String selectedDate = (String) session.getAttribute("selectedDate");
        String selectedTime = (String) session.getAttribute("selectedTime");
        String reservationType = (String) session.getAttribute("reservationType");
        String memo = (String) session.getAttribute("memo");
        
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("selectedTime", selectedTime);
        model.addAttribute("reservationType", reservationType);
        model.addAttribute("memo", memo);
        
        return "new/input-info";
    }

    // 患者情報入力処理
    @PostMapping("/input-info")
    public String processInputForm(
            @Valid @ModelAttribute("patientRequest") PatientRequest patientRequest,
            BindingResult result,
            HttpSession session,
            Model model) {

        if (result.hasErrors()) {
            return "new/input-info";
        }

        // セッションに患者情報を保存
        session.setAttribute("inputPatientName", patientRequest.getName());
        session.setAttribute("inputPatientEmail", patientRequest.getEmail());
        session.setAttribute("inputPatientPhone", patientRequest.getPhone());
        session.setAttribute("inputPatientBirthday", patientRequest.getBirthday());

        // 統合確認画面へ遷移
        return "new/confirm-reservation";
    }

    // 予約確定処理
    @PostMapping("/confirm")
    public String confirmReservation(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String birthday,
            HttpSession session,
            Model model) {

        log.info("予約確定処理開始");
        log.info("受信パラメータ: name={}, email={}, phone={}, birthday={}", name, email, phone, birthday);

        try {
        // セッションから予約情報取得
        String selectedDate = (String) session.getAttribute("selectedDate");
        String selectedTime = (String) session.getAttribute("selectedTime");
        String reservationType = (String) session.getAttribute("reservationType");

            log.info("セッション情報: selectedDate={}, selectedTime={}, reservationType={}", 
                    selectedDate, selectedTime, reservationType);
            
            // セッションの全属性をログ出力
            log.info("セッションの全属性:");
            java.util.Enumeration<String> sessionAttributes = session.getAttributeNames();
            while (sessionAttributes.hasMoreElements()) {
                String attrName = sessionAttributes.nextElement();
                Object attrValue = session.getAttribute(attrName);
                log.info("  {} = {}", attrName, attrValue);
            }

            // セッション情報の検証
            if (selectedDate == null || selectedTime == null || reservationType == null) {
                log.error("セッション情報が不足しています: selectedDate={}, selectedTime={}, reservationType={}", 
                        selectedDate, selectedTime, reservationType);
                throw new RuntimeException("予約情報が正しく設定されていません。最初からやり直してください。");
            }

            // reservationTypeの値を確認
            log.info("reservationTypeの値: '{}'", reservationType);
            log.info("reservationTypeの長さ: {}", reservationType.length());
            log.info("reservationTypeの文字コード: {}", (int) reservationType.charAt(0));
            
            // 有効な値かチェック
            if (!reservationType.equals("treatment") && !reservationType.equals("checkup")) {
                log.error("無効なreservationType: '{}'", reservationType);
                throw new RuntimeException("予約タイプが正しく設定されていません。最初からやり直してください。");
            }

        // DTO → Entity
        Patient patient = new Patient();
            patient.setName(name);
            patient.setEmail(email);
            patient.setPhone(phone);
            
            // 生年月日のパース（YYYYMMDD形式からLocalDateに変換）
            LocalDate birthDate;
            try {
                int year = Integer.parseInt(birthday.substring(0, 4));
                int month = Integer.parseInt(birthday.substring(4, 6));
                int day = Integer.parseInt(birthday.substring(6, 8));
                birthDate = LocalDate.of(year, month, day);
                log.info("生年月日パース成功: {} -> {}", birthday, birthDate);
            } catch (Exception e) {
                log.error("生年月日のパースに失敗: {}", birthday, e);
                throw new RuntimeException("生年月日の形式が正しくありません。YYYYMMDD形式で入力してください。");
            }
            patient.setBirthday(birthDate);

            // セッションからメモを取得
            String memo = (String) session.getAttribute("memo");
            
            log.info("患者情報作成完了: {}", patient.getName());

        // 予約処理をまとめて保存
        reservationService.registerNewPatientWithReservation(
                patient,
                LocalDate.parse(selectedDate),
                LocalTime.parse(selectedTime),
                Reservation.ReservationType.valueOf(reservationType),
                    memo
        );

            log.info("予約保存完了");

        // メール送信
        mailService.sendReservationConfirmation(
                patient.getEmail(),
                patient.getName(),
                selectedDate,
                selectedTime
        );

            log.info("メール送信完了");

            // 完了画面に直接遷移
        model.addAttribute("reservedDate", selectedDate);
        model.addAttribute("reservedTime", selectedTime);
        model.addAttribute("completeMessage", "予約が完了しました");
        model.addAttribute("patientName", patient.getName());
        model.addAttribute("patientEmail", patient.getEmail());
        model.addAttribute("patientPhone", patient.getPhone());
        model.addAttribute("reservationType", reservationType);
        model.addAttribute("memo", memo);

            log.info("完了画面へ遷移");
            
            // セッションクリア（予約完了後はログアウト）
            session.removeAttribute("selectedDate");
            session.removeAttribute("selectedTime");
            session.removeAttribute("reservationType");
            session.removeAttribute("memo");
            session.removeAttribute("inputPatientName");
            session.removeAttribute("inputPatientEmail");
            session.removeAttribute("inputPatientPhone");
            session.removeAttribute("inputPatientBirthday");
            
        return "reservation-complete";
            
        } catch (Exception e) {
            log.error("予約確定処理でエラーが発生しました", e);
            log.error("エラーの詳細: {}", e.getMessage());
            e.printStackTrace();
            
            // エラーページにリダイレクト
            model.addAttribute("error", "予約処理中にエラーが発生しました");
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
}
