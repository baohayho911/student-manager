package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.Gender;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record TeacherRequest(

        @NotBlank(message = "Phải nhập mã giảng viên")
        @Size(max = 20, message = "Mã giảng viên không được quá 20 ký tự")
        String teacherCode,

        @NotBlank(message = "Phải nhập họ tên")
        @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
        String fullName,

        @Past(message = "Ngày sinh phải trước ngày hiện tại")
        LocalDate dateOfBirth,

        Gender gender,

        @NotBlank(message = "Phải nhập email")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không được quá 100 ký tự")
        String email,

        @Pattern(
                regexp = "^$|^[0-9]{10,15}$",
                message = "Điện thoại phải gồm 10 đến 15 chữ số"
        )
        String phone,

        @Size(max = 50, message = "Học vị không được quá 50 ký tự")
        String academicDegree,

        @NotNull(message = "Phải chọn khoa")
        @Positive(message = "ID khoa phải lớn hơn 0")
        Long departmentId

) {
}