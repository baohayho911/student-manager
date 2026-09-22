package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.DepartmentRequest;
import com.example.studentmanagement.entity.Department;
import com.example.studentmanagement.service.DepartmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(
            DepartmentService departmentService
    ) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<Department> getDepartments(
            @RequestParam(name = "keyword", defaultValue = "")
            String keyword
    ) {
        return departmentService.getDepartments(keyword);
    }

    @GetMapping("/{id}")
    public Department getDepartment(
            @PathVariable("id") Long id
    ) {
        return departmentService.getDepartment(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department createDepartment(
            @Valid @RequestBody DepartmentRequest request
    ) {
        return departmentService.createDepartment(request);
    }

    @PutMapping("/{id}")
    public Department updateDepartment(
            @PathVariable("id") Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return departmentService.updateDepartment(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(
            @PathVariable("id") Long id
    ) {
        departmentService.deleteDepartment(id);
    }
}