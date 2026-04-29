package com.flowboard.subscription_service.repository;

import com.flowboard.subscription_service.entity.SubscriptionPlan;
import com.flowboard.subscription_service.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    Optional<UserSubscription> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    List<UserSubscription> findByExpiryDateBefore(LocalDate now);
}
