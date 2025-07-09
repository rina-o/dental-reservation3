package com.example.dental_reservation3.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "名前は必須です")
    @Size(max = 50, message = "名前は50文字以内で入力してください")
    private String name;

    @Email(message = "有効なメールアドレスを入力してください")
    @NotBlank(message = "メールアドレスは必須です")
    @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
    private String email;

    @NotBlank(message = "電話番号は必須です")
    @Pattern(regexp = "^0\\d{9,10}$", message = "電話番号はハイフンなしの10〜11桁で入力してください")
    private String phone;

    @NotBlank(message = "生年月日は必須です")
    @Pattern(regexp = "\\d{8}", message = "生年月日は8桁の数字で入力してください（例：19890101）")
    private String birthday;
}
