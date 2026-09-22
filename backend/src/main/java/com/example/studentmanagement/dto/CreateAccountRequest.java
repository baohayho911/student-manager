package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.UserAccount;

import jakarta.validation.constraints.*;

public record CreateAccountRequest(

        @NotBlank(message = "Phải nhập tên đăng nhập")
        @Pattern(
                regexp = "^[a-z0-9._-]{3,50}$",
                message = "Tên đăng nhập gồm 3-50 ký tự: chữ thường, số, dấu chấm, gạch dưới hoặc gạch ngang"
        )
        String username,

        @NotBlank(message = "Phải nhập mật khẩu")
        @Size(min = 12, max = 72,
                message = "Mật khẩu phải có từ 12 đến 72 ký tự")
        String password,

        @NotNull(message = "Phải chọn vai trò")
        UserAccount.Role role,

        @Positive(message = "ID hồ sơ phải lớn hơn 0")
        Long profileId

) {
}