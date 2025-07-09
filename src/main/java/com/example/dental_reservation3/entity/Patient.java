package com.example.dental_reservation3.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // 自動採番の主キー

    @Column(nullable = false)
    private String name;  // 患者名

    @Column(nullable = false, unique = true)
    private String email;  // メールアドレス（ログインに使用）

    @Column(name = "phone_number")
    private String phone;  // 電話番号（任意）

    @Column(nullable = false)
    private LocalDate birthday;  // 生年月日（yyyy-MM-dd）

    @Column(unique = true)
    private String patientCode;  // 患者番号（再診ログインに使用）

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // 登録日時（自動）

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 更新日時（自動）

    // 登録時の自動設定
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 更新時の自動設定
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
