package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.PageResponse;

import com.example.studentmanagement.dto.TeacherRequest;
import com.example.studentmanagement.entity.Teacher;
import com.example.studentmanagement.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public PageResponse<Teacher> getTeachers(
            @RequestParam(name = "keyword", defaultValue = "")
            String keyword,

            @RequestParam(name = "departmentId", required = false)
            Long departmentId,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size
    ) {
        return teacherService.getTeachers(
                keyword,
                departmentId,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public Teacher getTeacher(@PathVariable("id") Long id) {
        return teacherService.getTeacher(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Teacher createTeacher(
            @Valid @RequestBody TeacherRequest request
    ) {
        return teacherService.createTeacher(request);
    }

    @PutMapping("/{id}")
    public Teacher updateTeacher(
            @PathVariable("id") Long id,
            @Valid @RequestBody TeacherRequest request
    ) {
        return teacherService.updateTeacher(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacher(@PathVariable("id") Long id) {
        teacherService.deleteTeacher(id);
    }
}