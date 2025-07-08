package com.example.dental_reservation3.repository;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // 同じ日時の予約数をカウント（同時間帯3人まで制限用）
    int countByReservationDateAndReservationTime(LocalDate reservationDate, LocalTime reservationTime);

    // 患者ごとの予約を取得（確認・変更用）
    List<Reservation> findByPatient(Patient patient);
}
