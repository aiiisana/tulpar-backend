package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Stores cumulative seconds a user has spent in the app for a given calendar day.
 * The client sends increments; the backend upserts (adds) them.
 */
@Entity
@Table(
    name = "user_session_time",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_session_date",
        columnNames = {"user_id", "session_date"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionTime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    /** Cumulative seconds for this day. */
    @Column(name = "total_seconds", nullable = false)
    @Builder.Default
    private int totalSeconds = 0;
}
