package com.multimarket.controllers;

import com.multimarket.dto.AdminDashboardResponse;
import com.multimarket.services.impl.AdminDashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock private AdminDashboardServiceImpl dashboardService;

    @Test
    void getSummaryShouldReturnDashboardPayload() {
        AdminDashboardController controller = new AdminDashboardController(dashboardService);
        AdminDashboardResponse response = new AdminDashboardResponse(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new AdminDashboardResponse.CriticalAlerts(0, 0, 0, 0, 0),
                new AdminDashboardResponse.KafkaStatus("ONLINE", 0, 0, 0),
                new AdminDashboardResponse.SoapStatus("DISPONIBLE", 0, 0, "N/D"),
                new AdminDashboardResponse.LogsSummary(0, 0, 0, 0),
                new AdminDashboardResponse.SystemStatus(0, "0 GB", 0, 1)
        );
        when(dashboardService.getSummary()).thenReturn(response);

        var result = controller.getSummary();

        assertEquals(200, result.getStatusCode().value());
        assertEquals("ONLINE", result.getBody().kafkaStatus().status());
    }
}
