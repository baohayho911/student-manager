package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.CourseSection.Status;
import jakarta.validation.constraints.*;

public record CourseSectionRequest(

        @NotBlank(message = "Mã lớp học phần không được để trống")
        @Size(max = 30, message = "Mã lớp học phần tối đa 30 ký tự")
        String sectionCode,

        @NotNull(message = "Phải chọn môn học")
        @Positive(message = "ID môn học phải lớn hơn 0")
        Long subjectId,

        @Positive(message = "ID giảng viên phải lớn hơn 0")
        Long teacherId,

        @NotNull(message = "Phải chọn học kỳ")
        @Positive(message = "ID học kỳ phải lớn hơn 0")
        Long semesterId,

        @Size(max = 50, message = "Phòng học tối đa 50 ký tự")
        String room,

        @Size(max = 100, message = "Lịch học tối đa 100 ký tự")
        String schedule,

        @NotNull(message = "Phải nhập sĩ số tối đa")
        @Positive(message = "Sĩ số tối đa phải lớn hơn 0")
        Integer maximumStudents,

        @NotNull(message = "Phải chọn trạng thái")
        Status status

) {
}
