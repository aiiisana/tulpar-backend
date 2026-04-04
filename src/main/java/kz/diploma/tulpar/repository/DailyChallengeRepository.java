package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.DailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, UUID> {
    Optional<DailyChallenge> findByChallengeDate(LocalDate date);
}
