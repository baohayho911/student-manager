package com.example.studentmanagement.controller;

import com.example.studentmanagement.service.DashboardService;
import com.example.studentmanagement.service.DashboardService.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/dashboard")
    public DashboardResponse getAdminDashboard() {
        return service.getAdminDashboard();
    }

    @GetMapping("/api/teaching/dashboard")
    public DashboardResponse getTeacherDashboard(
            Principal principal
    ) {
        return service.getTeacherDashboard(principal.getName());
    }

    @GetMapping("/api/me/dashboard")
    public DashboardResponse getStudentDashboard(
            Principal principal
    ) {
        return service.getStudentDashboard(principal.getName());
    }
}
