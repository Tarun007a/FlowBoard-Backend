package com.flowboard.notification_service.service.impl;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.service.NotificationMessageListener;
import com.flowboard.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/*
This will listen all the messages and call the notification service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageListenerImpl implements NotificationMessageListener {
    private final NotificationService notificationService;
    @Override
//    @RabbitListener(queues = "notification-queue")
    public void processNotification(Object message) {
        if (message instanceof BulkNotificationRequestDto bulk) {
            log.info("Received send bulk request");
            notificationService.sendBulk(bulk);
        }
        else if (message instanceof NotificationRequestDto single) {
            log.info("Received send single request");
            notificationService.send(single);
        }
        else {
            throw new RuntimeException("Unknown message type");
        }
    }

//    @RabbitListener(queues = "notification-queue")
//    public void processSingleNotification(NotificationRequestDto notificationRequestDto) {
//        notificationService.send(notificationRequestDto);
//    }

    /*
    Not working currently what happen is that both the method read at same time and one
    get successful and other get error so it is a problem we need to fix it using a
    wrapper class that when adding to a queue we need two thing
    wrapper.setType("SINGLE");
    wrapper.setPayload(dto);
    such that we can receive in a single method and then process accordingly
     */

    @RabbitListener(queues = "notification-queue")
    public void handleBulk(BulkNotificationRequestDto bulk) {
        log.info("Bulk notification received " + bulk.toString());
        notificationService.sendBulk(bulk);
    }

    @RabbitListener(queues = "notification-queue")
    public void handleSingle(NotificationRequestDto single) {
        log.info("single notification received " + single.toString());
        notificationService.send(single);
    }
}
