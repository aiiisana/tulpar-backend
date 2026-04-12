package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.SavedFlashcard;
import kz.diploma.tulpar.domain.entity.SavedFlashcardId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedFlashcardRepository extends JpaRepository<SavedFlashcard, SavedFlashcardId> {

    List<SavedFlashcard> findByIdUserId(String userId);

    boolean existsByIdUserIdAndIdFlashcardId(String userId, UUID flashcardId);

    void deleteByIdUserIdAndIdFlashcardId(String userId, UUID flashcardId);
}
