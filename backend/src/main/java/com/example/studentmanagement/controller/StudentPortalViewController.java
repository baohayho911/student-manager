package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.PageResponse;
import com.example.studentmanagement.service.StudentPortalViewService;
import com.example.studentmanagement.service.StudentPortalViewService.EnrollmentView;
import com.example.studentmanagement.service.StudentPortalViewService.ProfileView;
import com.example.studentmanagement.service.StudentPortalViewService.SectionView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/me/portal")
public class StudentPortalViewController {

    private final StudentPortalViewService service;

    public StudentPortalViewController(
            StudentPortalViewService service
    ) {
        this.service = service;
    }

    @GetMapping("/profile")
    public ProfileView getProfile(Principal principal) {
        return service.getProfile(principal.getName());
    }

    @GetMapping("/sections")
    public PageResponse<SectionView> getOpenSections(
            Principal principal,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getOpenSections(
                principal.getName(),
                keyword,
                page,
                size
        );
    }

    @GetMapping("/enrollments")
    public List<EnrollmentView> getEnrollments(
            Principal principal
    ) {
        return service.getEnrollments(principal.getName());
    }
}