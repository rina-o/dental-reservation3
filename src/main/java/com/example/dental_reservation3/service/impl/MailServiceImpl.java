package com.example.dental_reservation3.service.impl;

import com.example.dental_reservation3.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendReservationConfirmation(String toEmail, String patientName, String reservationDate, String reservationTime) {
        try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("【スマイル歯科】予約完了のお知らせ");
        message.setText(
                patientName + " 様\n\n" +
                        "以下の内容で予約が完了しました。\n\n" +
                        "予約日： " + reservationDate + "\n" +
                        "時間　： " + reservationTime + "\n\n" +
                        "▼予約の確認・変更はこちらから（ログインが必要です）\n" +
                        "http://localhost:8080/login?redirect=/reservation/check-reservation\n" +
                        "※実際のURLは本番環境に合わせて設定してください。\n\n" +
                        "スマイル歯科"
        );

        mailSender.send(message);
        } catch (Exception e) {
            // メール送信に失敗しても予約処理は続行
            System.err.println("メール送信に失敗しました: " + e.getMessage());
        }
    }

    @Override
    public void sendReservationNotification(String toEmail, String patientName, String subject, String messageBody) {
        try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(
                patientName + " 様\n\n" +
                        messageBody + "\n\n" +
                        "▼予約の確認・変更はこちらから（ログインが必要です）\n" +
                        "http://localhost:8080/login?redirect=/reservation/check-reservation\n" +
                        "※実際のURLは本番環境に合わせて設定してください。\n\n" +
                        "スマイル歯科"
        );
        mailSender.send(message);
        } catch (Exception e) {
            // メール送信に失敗しても処理は続行
            System.err.println("メール送信に失敗しました: " + e.getMessage());
        }
    }
}
