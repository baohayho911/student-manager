package com.example.studentmanagement.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SemesterRequest(

        @NotBlank(message = "Tên học kỳ không được để trống")
        @Size(max = 50, message = "Tên học kỳ tối đa 50 ký tự")
        String semesterName,

        @NotBlank(message = "Năm học không được để trống")
        @Pattern(
                regexp = "^[0-9]{4}-[0-9]{4}$",
                message = "Năm học phải có dạng 2026-2027"
        )
        String academicYear,

        @NotNull(message = "Phải nhập ngày bắt đầu")
        LocalDate startDate,

        @NotNull(message = "Phải nhập ngày kết thúc")
        LocalDate endDate

) {
}