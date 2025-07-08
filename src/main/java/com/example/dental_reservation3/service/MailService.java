package com.example.dental_reservation3.service;

public interface MailService {

    // 予約完了メール（件名・本文固定）
    void sendReservationConfirmation(String toEmail, String patientName, String reservationDate, String reservationTime);

    // 自由な件名・本文で送信できる汎用メール
    void sendReservationNotification(String toEmail, String patientName, String subject, String messageBody);
}
