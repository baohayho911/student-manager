package com.example.studentmanagement.dto;

import jakarta.validation.constraints.*;

public record MajorRequest(

        @NotBlank(message = "Phải nhập mã ngành")
        @Size(max = 20, message = "Mã ngành không được quá 20 ký tự")
        String majorCode,

        @NotBlank(message = "Phải nhập tên ngành")
        @Size(max = 150, message = "Tên ngành không được quá 150 ký tự")
        String majorName,

        @NotNull(message = "Phải chọn khoa")
        @Positive(message = "ID khoa phải lớn hơn 0")
        Long departmentId

) {
}