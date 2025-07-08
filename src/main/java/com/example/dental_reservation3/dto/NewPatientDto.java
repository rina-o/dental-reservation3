package com.example.dental_reservation3.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NewPatientDto {

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    @NotBlank(message = "電話番号は必須です")
    private String phone;

    @NotBlank(message = "生年月日は必須です")
    private String birthday; // フォーム入力時は文字列で受け取り、変換はControllerで対応
}
