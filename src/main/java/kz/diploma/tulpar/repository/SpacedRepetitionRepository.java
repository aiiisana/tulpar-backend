package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.SpacedRepetitionState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpacedRepetitionRepository extends JpaRepository<SpacedRepetitionState, UUID> {

    Optional<SpacedRepetitionState> findByUserIdAndExerciseId(String userId, UUID exerciseId);

    /** Cards that are due for review right now, ordered by most-overdue first. */
    @Query("""
            SELECT s FROM SpacedRepetitionState s
            WHERE s.userId = :userId
              AND s.nextReviewAt <= :now
            ORDER BY s.nextReviewAt ASC
            """)
    List<SpacedRepetitionState> findDueCards(
            @Param("userId") String userId,
            @Param("now") Instant now,
            Pageable pageable);

    long countByUserIdAndNextReviewAtBefore(String userId, Instant now);
}
