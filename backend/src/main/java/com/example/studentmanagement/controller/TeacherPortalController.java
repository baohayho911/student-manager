package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.dto.ScoreRequest;
import com.example.studentmanagement.entity.*;
import com.example.studentmanagement.service.*;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teaching")
public class TeacherPortalController {

    private final PortalService portalService;
    private final CourseSectionService sectionService;
    private final EnrollmentService enrollmentService;
    private final ScoreService scoreService;

    public TeacherPortalController(
            PortalService portalService,
            CourseSectionService sectionService,
            EnrollmentService enrollmentService,
            ScoreService scoreService
    ) {
        this.portalService = portalService;
        this.sectionService = sectionService;
        this.enrollmentService = enrollmentService;
        this.scoreService = scoreService;
    }

    @GetMapping("/profile")
    public Teacher profile(Principal principal) {
        return portalService.teacher(principal.getName());
    }

    @GetMapping("/sections")
    public PageResponse<CourseSection> sections(
            Principal principal,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "semesterId", required = false) Long semesterId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Long teacherId = portalService.teacher(principal.getName()).getId();

        return sectionService.getSections(
                keyword,
                semesterId,
                null,
                teacherId,
                null,
                page,
                size
        );
    }

    @GetMapping("/sections/{id}/enrollments")
    public List<Enrollment> enrollments(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        portalService.requireTeacherSection(principal.getName(), id);

        return enrollmentService.getEnrollments(null, id);
    }

    @GetMapping("/enrollments/{id}/score")
    public Score score(
            Principal principal,
            @PathVariable("id") Long id
    ) {
        portalService.requireTeacherEnrollment(principal.getName(), id);

        return scoreService.getScoreByEnrollment(id);
    }

    @PutMapping("/enrollments/{id}/score")
    public Score saveScore(
            Principal principal,
            @PathVariable("id") Long id,
            @Valid @RequestBody ScoreRequest request
    ) {
        return portalService.saveTeacherScore(
                principal.getName(),
                id,
                request
        );
    }
}