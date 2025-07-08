package com.example.dental_reservation3.service.impl;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.entity.Reservation;
import com.example.dental_reservation3.repository.PatientRepository;
import com.example.dental_reservation3.repository.ReservationRepository;
import com.example.dental_reservation3.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
        Reservation reservation = new Reservation();
        reservation.setPatient(patient);
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setType(type);
        reservation.setMemo(memo);
        reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> getReservationsByPatient(Patient patient) {
        return reservationRepository.findByPatient(patient);
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
        return times.stream()
                .filter(time -> reservationRepository.countByReservationDateAndReservationTime(date, time) < 3)
                .toList();
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
        String generatedCode = "P" + UUID.randomUUID().toString().substring(0, 8);
        patient.setPatientCode(generatedCode);
        patientRepository.save(patient);
        reserve(patient, date, time, type, memo);
    }
}
