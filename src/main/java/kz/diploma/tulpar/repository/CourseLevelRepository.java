package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.CourseLevel;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseLevelRepository extends JpaRepository<CourseLevel, UUID> {
    List<CourseLevel> findAllByCourseIdOrderByOrderIndexAsc(UUID courseId);
    Optional<CourseLevel> findByCourseIdAndDifficultyLevel(UUID courseId, DifficultyLevel difficultyLevel);
}
