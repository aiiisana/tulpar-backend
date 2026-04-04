package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findAllByLevelIdOrderByOrderIndexAsc(UUID levelId);
    Optional<Lesson> findFirstByLevelIdAndOrderIndexLessThanOrderByOrderIndexDesc(UUID levelId, int orderIndex);
}
