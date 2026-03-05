package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.Exercise;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ExerciseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    Page<Exercise> findAllByType(ExerciseType type, Pageable pageable);

    Page<Exercise> findAllByDifficultyLevel(DifficultyLevel level, Pageable pageable);

    Page<Exercise> findAllByTypeAndDifficultyLevel(ExerciseType type, DifficultyLevel level, Pageable pageable);
}
