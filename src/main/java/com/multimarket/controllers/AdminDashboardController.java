package com.multimarket.controllers;

import com.multimarket.dto.AdminDashboardResponse;
import com.multimarket.services.impl.AdminDashboardServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class AdminDashboardController {

    private final AdminDashboardServiceImpl dashboardService;

    public AdminDashboardController(AdminDashboardServiceImpl dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
