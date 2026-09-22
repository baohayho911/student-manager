package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.EnrollmentRequest;
import com.example.studentmanagement.entity.Enrollment;
import com.example.studentmanagement.service.EnrollmentService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(
            EnrollmentService enrollmentService
    ) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<Enrollment> getEnrollments(
            @RequestParam(name = "studentId", required = false)
            Long studentId,

            @RequestParam(name = "courseSectionId", required = false)
            Long courseSectionId
    ) {
        return enrollmentService.getEnrollments(
                studentId,
                courseSectionId
        );
    }

    @GetMapping("/{id}")
    public Enrollment getEnrollment(
            @PathVariable("id") Long id
    ) {
        return enrollmentService.getEnrollment(id);
    }

    @PostMapping
    public Enrollment register(
            @Valid @RequestBody EnrollmentRequest request
    ) {
        return enrollmentService.register(request);
    }

    @PatchMapping("/{id}/cancel")
    public Enrollment cancel(
            @PathVariable("id") Long id
    ) {
        return enrollmentService.cancel(id);
    }
}