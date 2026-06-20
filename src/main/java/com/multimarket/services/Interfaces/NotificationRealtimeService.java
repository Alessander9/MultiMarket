package com.multimarket.services.Interfaces;

import com.multimarket.dto.NotificacionResponse;

public interface NotificationRealtimeService {
    void broadcastNewNotification(NotificacionResponse notification);
}
