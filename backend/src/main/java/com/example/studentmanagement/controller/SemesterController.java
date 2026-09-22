package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.SemesterRequest;
import com.example.studentmanagement.entity.Semester;
import com.example.studentmanagement.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    public List<Semester> getSemesters() {
        return semesterService.getSemesters();
    }

    @GetMapping("/{id}")
    public Semester getSemester(@PathVariable("id") Long id) {
        return semesterService.getSemester(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Semester createSemester(
            @Valid @RequestBody SemesterRequest request
    ) {
        return semesterService.createSemester(request);
    }

    @PutMapping("/{id}")
    public Semester updateSemester(
            @PathVariable("id") Long id,
            @Valid @RequestBody SemesterRequest request
    ) {
        return semesterService.updateSemester(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSemester(@PathVariable("id") Long id) {
        semesterService.deleteSemester(id);
    }
}