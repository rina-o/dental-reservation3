package com.example.dental_reservation3.service;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {
    boolean canReserve(LocalDate date, LocalTime time);

    void reserve(Patient patient, LocalDate date, LocalTime time, Reservation.ReservationType type, String memo);

    List<Reservation> getReservationsByPatient(Patient patient);

    // 現在時刻以降の予約を取得
    List<Reservation> getFutureReservationsByPatient(Patient patient);

    List<LocalDate> getAvailableDates();

    List<LocalTime> getAvailableTimesForDate(LocalDate date);

    void cancelReservation(Reservation reservation);

    Patient saveNewPatient(Patient patient);  // 必要に応じて残す

    void registerNewPatientWithReservation(Patient patient, LocalDate date, LocalTime time, Reservation.ReservationType type, String memo); // 統一メソッド
}
