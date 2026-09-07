package com.scheduler.email.repositories;

import com.scheduler.email.data.entities.MailAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MailAccountRepository extends JpaRepository<MailAccount, UUID> {

    List<MailAccount> findByUserId(UUID userId);

    // idx_mail_accounts_default — O(1) default account lookup
    Optional<MailAccount> findByUserIdAndIsDefaultTrueAndIsActiveTrue(UUID userId);

    boolean existsByUserIdAndEmail(UUID userId, String email);

    // Scheduler: cooldown check — kaunse accounts available hain abhi
    // idx_mail_accounts_cooldown
    @Query("SELECT m FROM MailAccount m WHERE m.cooldownUntil IS NOT NULL AND m.cooldownUntil <= :now AND m.isActive = true")
    List<MailAccount> findAvailableAfterCooldown(OffsetDateTime now);

    // Daily reset: idx_mail_accounts_daily_count
    @Query("SELECT m FROM MailAccount m WHERE m.dailySentCount > 0")
    List<MailAccount> findAccountsWithPendingDailyCount();

    @Modifying
    @Query("UPDATE MailAccount m SET m.dailySentCount = 0 WHERE m.dailySentCount > 0")
    void resetAllDailySentCounts();

    // Check if user owns this account (security validation)
    boolean existsByIdAndUserId(UUID accountId, UUID userId);

    List<MailAccount> findByUserIdAndIsActiveTrue(UUID userId);
}
