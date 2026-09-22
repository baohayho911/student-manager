package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.Gender;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record StudentRequest(

        @NotBlank(message = "Phải nhập mã sinh viên")
        @Size(max = 20, message = "Mã sinh viên không được quá 20 ký tự")
        String studentCode,

        @NotBlank(message = "Phải nhập họ tên")
        @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
        String fullName,

        @Past(message = "Ngày sinh phải trước ngày hiện tại")
        LocalDate dateOfBirth,

        Gender gender,

        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không được quá 100 ký tự")
        String email,

        @Pattern(
                regexp = "^$|^[0-9]{10,15}$",
                message = "Điện thoại phải gồm 10 đến 15 chữ số"
        )
        String phone,

        @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
        String address,

        @NotNull(message = "Phải chọn lớp hành chính")
        @Positive(message = "ID lớp phải lớn hơn 0")
        Long classId

) {
}