package com.flowboard.subscription_service.scheduledJob;

import com.flowboard.subscription_service.entity.UserSubscription;
import com.flowboard.subscription_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScheduledJob {
    // cron = sec min hr dayofmonth month dayofweek

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void removeExpiredSubscription() {
        log.info("Scheduled job running to remove expired subscription");
        LocalDate now = LocalDate.now();
        List<UserSubscription> subscriptions = subscriptionRepository.findByExpiryDateBefore(now);

        for(UserSubscription userSubscription : subscriptions) {
            log.info("Subscription expired for user id={}", userSubscription.getUserId());
            subscriptionRepository.delete(userSubscription);
        }
    }
}
