package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.VocabularyExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VocabularyExerciseRepository extends JpaRepository<VocabularyExercise, UUID> {
}
