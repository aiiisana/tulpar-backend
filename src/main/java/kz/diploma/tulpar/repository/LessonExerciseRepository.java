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

    @Query("SELECT COUNT(le) FROM LessonExercise le WHERE le.lesson.id = :lessonId")
    long countByLessonId(@Param("lessonId") UUID lessonId);
}
