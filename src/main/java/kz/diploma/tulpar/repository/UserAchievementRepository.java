package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findAllByUserIdOrderByEarnedAtDesc(String userId);
    boolean existsByUserIdAndAchievementCode(String userId, String code);
    long countByUserId(String userId);
}
