package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.EnrollmentRequest;
import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.entity.*;
import com.example.studentmanagement.service.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/me")
public class StudentPortalController {

    private final PortalService portalService;
    private final EnrollmentService enrollmentService;
    private final ScoreService scoreService;
    private final CourseSectionService sectionService;

    public StudentPortalController(
            PortalService portalService,
            EnrollmentService enrollmentService,
            ScoreService scoreService,
            CourseSectionService sectionService
    ) {
        this.portalService = portalService;
        this.enrollmentService = enrollmentService;
        this.scoreService = scoreService;
        this.sectionService = sectionService;
    }

    public record RegisterRequest(
            @NotNull @Positive Long courseSectionId
    ) {
    }

    @GetMapping("/profile")
    public Student profile(Principal principal) {
        return portalService.student(principal.getName());
    }

    @GetMapping("/enrollments")
    public List<Enrollment> enrollments(Principal principal) {
        Long studentId = portalService.student(principal.getName()).getId();

        return enrollmentService.getEnrollments(studentId, null);
    }

    @GetMapping("/scores")
    public List<Score> scores(Principal principal) {
        Long studentId = portalService.student(principal.getName()).getId();

        return scoreService.getScores(studentId, null);
    }

    @GetMapping("/course-sections")
    public PageResponse<CourseSection> availableSections(
            Principal principal,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "semesterId", required = false) Long semesterId,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        portalService.student(principal.getName());

        return sectionService.getSections(
                keyword,
                semesterId,
                subjectId,
                null,
                CourseSection.Status.OPEN,
                page,
                size
        );
    }

    @PostMapping("/enrollments")
    public Enrollment register(
            Principal principal,
            @Valid @RequestBody RegisterRequest request
    ) {
        Long studentId = portalService.student(principal.getName()).getId();

        return enrollmentService.register(
                new EnrollmentRequest(studentId, request.courseSectionId())
        );
    }

    @PatchMapping("/enrollments/{id}/cancel")
    public Enrollment cancel(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        portalService.requireStudentEnrollment(principal.getName(), id);

        return enrollmentService.cancel(id);
    }
}