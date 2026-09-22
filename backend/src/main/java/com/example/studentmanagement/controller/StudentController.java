package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.PageResponse;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public PageResponse<Student> getStudents(
            @RequestParam(name = "keyword", defaultValue = "")
            String keyword,

            @RequestParam(name = "classId", required = false)
            Long classId,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size
    ) {
        return studentService.getStudents(
                keyword,
                classId,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable("id") Long id) {
        return studentService.getStudent(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student createStudent(
            @Valid @RequestBody StudentRequest request
    ) {
        return studentService.createStudent(request);
    }

    @PutMapping("/{id}")
    public Student updateStudent(
            @PathVariable("id") Long id,
            @Valid @RequestBody StudentRequest request
    ) {
        return studentService.updateStudent(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
    }
}