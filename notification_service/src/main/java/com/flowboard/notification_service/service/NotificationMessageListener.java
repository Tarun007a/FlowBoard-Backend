package com.flowboard.notification_service.service;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;

public interface NotificationMessageListener {
    public void processNotification(BulkNotificationRequestDto bulkNotificationRequestDto);
}
