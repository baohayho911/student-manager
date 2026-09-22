package com.example.studentmanagement.controller;

import com.example.studentmanagement.service.GradebookViewService;
import com.example.studentmanagement.service.GradebookViewService.Gradebook;
import com.example.studentmanagement.service.GradebookViewService.StudentGrade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class GradebookViewController {

    private final GradebookViewService service;

    public GradebookViewController(GradebookViewService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/gradebook/{sectionId}")
    public Gradebook getAdminGradebook(
            @PathVariable Long sectionId
    ) {
        return service.getAdminGradebook(sectionId);
    }

    @GetMapping("/api/teaching/gradebook/{sectionId}")
    public Gradebook getTeacherGradebook(
            Principal principal,
            @PathVariable Long sectionId
    ) {
        return service.getTeacherGradebook(
                principal.getName(),
                sectionId
        );
    }

    @GetMapping("/api/me/grade-results")
    public List<StudentGrade> getStudentGrades(
            Principal principal
    ) {
        return service.getStudentGrades(principal.getName());
    }
}