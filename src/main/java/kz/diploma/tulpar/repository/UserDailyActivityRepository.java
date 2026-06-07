package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.UserDailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDailyActivityRepository extends JpaRepository<UserDailyActivity, UUID> {
    Optional<UserDailyActivity> findByUserIdAndActivityDate(String userId, LocalDate date);
    List<UserDailyActivity> findAllByUserIdAndActivityDateBetweenOrderByActivityDateAsc(
            String userId, LocalDate from, LocalDate to);

    /** True only if the user specifically completed the daily challenge on the given date. */
    boolean existsByUserIdAndActivityDateAndChallengeCompletedTrue(String userId, LocalDate date);

    /** Count days with challenge_completed=true in a date range — used for DAILY_7 achievement. */
    long countByUserIdAndActivityDateBetweenAndChallengeCompletedTrue(
            String userId, LocalDate from, LocalDate to);
}
