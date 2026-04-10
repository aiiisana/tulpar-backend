package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.LessonExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonExerciseRepository extends JpaRepository<LessonExercise, UUID> {
    List<LessonExercise> findAllByLessonIdOrderByOrderIndexAsc(UUID lessonId);

    /**
     * Returns only the exercise UUIDs for a lesson, ordered by orderIndex.
     * Using a scalar JPQL projection avoids placing base {@code Exercise}
     * Hibernate proxies into the L1-cache before the subtypes are loaded.
     * This is crucial for correct pattern-matching in ExerciseService.toResponse().
     */
    @Query("SELECT le.exercise.id FROM LessonExercise le WHERE le.lesson.id = :lessonId ORDER BY le.orderIndex ASC")
    List<UUID> findExerciseIdsByLessonIdOrdered(@Param("lessonId") UUID lessonId);

    @Query("SELECT COUNT(le) FROM LessonExercise le WHERE le.lesson.id = :lessonId")
    long countByLessonId(@Param("lessonId") UUID lessonId);
}
