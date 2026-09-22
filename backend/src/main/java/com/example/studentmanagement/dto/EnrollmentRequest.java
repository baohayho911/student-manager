package com.example.studentmanagement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnrollmentRequest(

        @NotNull(message = "Phải chọn sinh viên")
        @Positive(message = "ID sinh viên phải lớn hơn 0")
        Long studentId,

        @NotNull(message = "Phải chọn lớp học phần")
        @Positive(message = "ID lớp học phần phải lớn hơn 0")
        Long courseSectionId

) {
}
