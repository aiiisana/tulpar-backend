package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.PracticeMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeMessageRepository extends JpaRepository<PracticeMessage, UUID> {

    /** Returns the last 100 exchanges for a user, oldest-first (for chat display). */
    List<PracticeMessage> findTop100ByUserIdOrderByCreatedAtAsc(String userId);

    /** Clears the full conversation history for a user. */
    void deleteAllByUserId(String userId);
}
