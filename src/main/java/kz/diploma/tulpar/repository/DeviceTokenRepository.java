package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findAllByUserId(String userId);

    Optional<DeviceToken> findByToken(String token);

    /** All distinct tokens for users who have notifications enabled. */
    @Query("""
        SELECT dt FROM DeviceToken dt
        JOIN dt.user u
        WHERE u.notificationsEnabled = true
    """)
    List<DeviceToken> findAllForUsersWithNotificationsEnabled();
}
