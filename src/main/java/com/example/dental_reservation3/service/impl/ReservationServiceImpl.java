package com.example.dental_reservation3.service.impl;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;
import com.example.dental_reservation3.repository.PatientRepository;
import com.example.dental_reservation3.repository.ReservationRepository;
import com.example.dental_reservation3.service.ReservationService;
import com.example.dental_reservation3.exception.ReservationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final PatientRepository patientRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public boolean canReserve(LocalDate date, LocalTime time) {
        int count = reservationRepository.countByReservationDateAndReservationTime(date, time);
        return count < 3;
    }

    @Override
    public void reserve(Patient patient, LocalDate date, LocalTime time, Reservation.ReservationType type, String memo) {
        System.out.println("=== reserve開始 ===");
        System.out.println("予約可能チェック: " + date + ", " + time);
        
        try {
            if (!canReserve(date, time)) {
                throw new ReservationException("この時間枠は定員に達しています。他の時間を選択してください。");
            }
            
        Reservation reservation = new Reservation();
        reservation.setPatient(patient);
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setType(type);
        reservation.setMemo(memo);
            
            System.out.println("予約オブジェクト作成: " + reservation);
            Reservation savedReservation = reservationRepository.save(reservation);
            System.out.println("予約保存完了: ID=" + savedReservation.getId());
            System.out.println("=== reserve完了 ===");
        } catch (Exception e) {
            System.err.println("reserveでエラーが発生しました: " + e.getMessage());
            System.err.println("エラーの種類: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Reservation> getReservationsByPatient(Patient patient) {
        return reservationRepository.findByPatient(patient);
    }

    @Override
    public List<Reservation> getFutureReservationsByPatient(Patient patient) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<Reservation> allReservations = reservationRepository.findByPatient(patient);
        
        return allReservations.stream()
                .filter(reservation -> {
                    LocalDate reservationDate = reservation.getReservationDate();
                    LocalTime reservationTime = reservation.getReservationTime();
                    
                    // 今日の予約の場合は現在時刻以降かチェック
                    if (reservationDate.equals(today)) {
                        return reservationTime.isAfter(now);
                    }
                    // 未来の日付の予約は全て含める
                    return reservationDate.isAfter(today);
                })
                .sorted((r1, r2) -> {
                    // 日付、時間順でソート
                    int dateCompare = r1.getReservationDate().compareTo(r2.getReservationDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    return r1.getReservationTime().compareTo(r2.getReservationTime());
                })
                .toList();
    }

    @Override
    public List<LocalDate> getAvailableDates() {
        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(2);
        for (LocalDate date = today; date.isBefore(twoMonthsLater); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.THURSDAY && day != DayOfWeek.SUNDAY) {
                availableDates.add(date);
            }
        }
        return availableDates;
    }

    @Override
    public List<LocalTime> getAvailableTimesForDate(LocalDate date) {
        List<LocalTime> times = new ArrayList<>();
        for (int hour = 10; hour <= 13; hour++) {
            times.add(LocalTime.of(hour, 0));
            times.add(LocalTime.of(hour, 30));
        }
        for (int hour = 15; hour <= 17; hour++) {
            times.add(LocalTime.of(hour, 0));
            times.add(LocalTime.of(hour, 30));
        }
        // 予約枠が埋まっていない時間のみ
        List<LocalTime> available = times.stream()
                .filter(time -> reservationRepository.countByReservationDateAndReservationTime(date, time) < 3)
            .toList();

        // ★本日なら現在時刻より前の時間を除外
        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            available = available.stream()
                .filter(time -> time.isAfter(now))
                .toList();
        }
        return available;
    }

    @Override
    public void cancelReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    @Override
    public Patient saveNewPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    public void registerNewPatientWithReservation(Patient patient, LocalDate date, LocalTime time, Reservation.ReservationType type, String memo) {
        System.out.println("=== registerNewPatientWithReservation開始 ===");
        System.out.println("患者情報: " + patient.getName() + ", " + patient.getEmail() + ", " + patient.getPhone() + ", " + patient.getBirthday());
        System.out.println("予約情報: " + date + ", " + time + ", " + type + ", " + memo);
        
        try {
            // 既存の患者をメールアドレスで検索
            Optional<Patient> existingPatientOpt = patientRepository.findByEmail(patient.getEmail());
            
            if (existingPatientOpt.isPresent()) {
                Patient existingPatient = existingPatientOpt.get();
                System.out.println("既存の患者が見つかりました: ID=" + existingPatient.getId());
                // 既存の患者情報を使用
                patient = existingPatient;
            } else {
                System.out.println("新規患者として登録します");
        String generatedCode = "P" + UUID.randomUUID().toString().substring(0, 8);
        patient.setPatientCode(generatedCode);
                System.out.println("患者コード生成: " + generatedCode);
                
                Patient savedPatient = patientRepository.save(patient);
                System.out.println("患者情報保存完了: ID=" + savedPatient.getId());
            }
            
        reserve(patient, date, time, type, memo);
            System.out.println("予約情報保存完了");
            System.out.println("=== registerNewPatientWithReservation完了 ===");
        } catch (Exception e) {
            System.err.println("registerNewPatientWithReservationでエラーが発生しました: " + e.getMessage());
            System.err.println("エラーの種類: " + e.getClass().getSimpleName());
            e.printStackTrace();
            throw e;
        }
    }
}
