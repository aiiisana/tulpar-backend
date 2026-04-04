package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, String> {
}
