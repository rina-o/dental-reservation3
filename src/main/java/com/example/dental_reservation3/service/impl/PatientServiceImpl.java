package com.example.dental_reservation3.service.impl;

import com.example.dental_reservation3.entity.Patient;
import com.example.dental_reservation3.repository.PatientRepository;
import com.example.dental_reservation3.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;




    @Override
    public Patient registerNewPatient(Patient patient) {
        return patientRepository.save(patient);
    }
    // PatientServiceImpl.java にこれがあるかどうか
    @Override
    public Optional<Patient> findByEmailOrPatientNumberAndBirthday(String inputValue, LocalDate birthday) {
        return patientRepository.findByEmailOrPatientNumberAndBirthday(inputValue, birthday);
    }


}
