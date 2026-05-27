package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.PlacementQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlacementQuestionRepository extends JpaRepository<PlacementQuestion, UUID> {
    List<PlacementQuestion> findAllByOrderByOrderIndexAsc();
}
