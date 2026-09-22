package com.example.studentmanagement.dto;

import jakarta.validation.constraints.*;

public record AdministrativeClassRequest(

        @NotBlank(message = "Mã lớp không được để trống")
        @Size(max = 30, message = "Mã lớp tối đa 30 ký tự")
        String classCode,

        @NotBlank(message = "Tên lớp không được để trống")
        @Size(max = 150, message = "Tên lớp tối đa 150 ký tự")
        String className,

        @NotNull(message = "Phải nhập năm tuyển sinh")
        @Min(value = 1900, message = "Năm tuyển sinh phải từ 1900")
        @Max(value = 2100, message = "Năm tuyển sinh không vượt quá 2100")
        Integer courseYear,

        @NotNull(message = "Phải chọn ngành học")
        @Positive(message = "ID ngành phải lớn hơn 0")
        Long majorId

) {
}