package com.example.studentmanagement.dto;

import jakarta.validation.constraints.*;

public record SubjectRequest(

        @NotBlank(message = "Mã môn không được để trống")
        @Size(max = 20, message = "Mã môn tối đa 20 ký tự")
        String subjectCode,

        @NotBlank(message = "Tên môn không được để trống")
        @Size(max = 150, message = "Tên môn tối đa 150 ký tự")
        String subjectName,

        @NotNull(message = "Phải nhập số tín chỉ")
        @Positive(message = "Số tín chỉ phải lớn hơn 0")
        Integer credits,

        @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
        String description

) {
}