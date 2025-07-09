package com.example.dental_reservation3.service;

import com.example.dental_reservation3.entity.Patient;
import java.time.LocalDate;
import java.util.Optional;

public interface PatientService {


    Patient registerNewPatient(Patient patient);

    Optional<Patient> findByEmailOrPatientCodeAndBirthday(String inputValue, LocalDate birthday);
}

