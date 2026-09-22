package com.example.studentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        @NotBlank(message = "Phải nhập mã khoa")
        @Size(max = 20, message = "Mã khoa không được quá 20 ký tự")
        String departmentCode,

        @NotBlank(message = "Phải nhập tên khoa")
        @Size(max = 150, message = "Tên khoa không được quá 150 ký tự")
        String departmentName

) {
}