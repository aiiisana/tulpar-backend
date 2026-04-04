package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Notification> findByIdAndUserId(UUID id, String userId);
}
