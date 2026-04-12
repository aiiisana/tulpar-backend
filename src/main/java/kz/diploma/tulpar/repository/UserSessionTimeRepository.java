package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.UserSessionTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionTimeRepository extends JpaRepository<UserSessionTime, UUID> {

    Optional<UserSessionTime> findByUserIdAndSessionDate(String userId, LocalDate date);

    List<UserSessionTime> findAllBySessionDate(LocalDate date);

    /** Increment total_seconds for (user, date) using native upsert. */
    @Modifying
    @Query(value = """
        INSERT INTO user_session_time (id, user_id, session_date, total_seconds)
        VALUES (gen_random_uuid(), :userId, :date, :seconds)
        ON CONFLICT (user_id, session_date)
        DO UPDATE SET total_seconds = user_session_time.total_seconds + EXCLUDED.total_seconds
        """, nativeQuery = true)
    void upsertAddSeconds(
            @Param("userId") String userId,
            @Param("date") LocalDate date,
            @Param("seconds") int seconds);
}
