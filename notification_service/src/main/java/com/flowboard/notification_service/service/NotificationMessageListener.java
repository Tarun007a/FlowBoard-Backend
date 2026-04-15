package com.flowboard.notification_service.service;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;

public interface NotificationMessageListener {
    public void processNotification(Object object);
}
