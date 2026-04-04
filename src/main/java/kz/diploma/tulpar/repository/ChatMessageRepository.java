package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findTop50ByUserIdOrderByCreatedAtAsc(String userId);
    Page<ChatMessage> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
