package com.example.dental_reservation3.repository;

import com.example.dental_reservation3.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByEmailAndBirthday(String email, LocalDate birthday);

    Optional<Patient> findByPatientCodeAndBirthday(String patientCode, LocalDate birthday);

    @Query("SELECT p FROM Patient p WHERE (p.email = :value OR p.patientCode = :value) AND p.birthday = :birthday")
    Optional<Patient> findByEmailOrPatientCodeAndBirthday(
            @Param("value") String value,
            @Param("birthday") LocalDate birthday
    );
}
