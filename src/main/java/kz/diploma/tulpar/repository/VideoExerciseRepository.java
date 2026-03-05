package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.VideoExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoExerciseRepository extends JpaRepository<VideoExercise, UUID> {
}
