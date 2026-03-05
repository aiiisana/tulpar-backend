package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.UserProgress;
import kz.diploma.tulpar.domain.enums.ProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

    Optional<UserProgress> findByUserIdAndExerciseId(String userId, UUID exerciseId);

    Page<UserProgress> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM UserProgress p WHERE p.user.id = :userId AND p.status = :status")
    long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") ProgressStatus status);

    boolean existsByUserIdAndExerciseId(String userId, UUID exerciseId);
}
