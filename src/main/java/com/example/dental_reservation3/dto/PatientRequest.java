package com.example.dental_reservation3.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "名前は必須です")
    private String name;

    @Email(message = "有効なメールアドレスを入力してください")
    @NotBlank(message = "メールアドレスは必須です")
    private String email;

    @NotBlank(message = "電話番号は必須です")
    private String phone;

    @NotNull(message = "生年月日は必須です")
    private LocalDate birthday;

    private String memo;  // 任意入力
}
