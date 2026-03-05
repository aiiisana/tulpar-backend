package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.enums.ProgressStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a single user's attempt history on a single exercise.
 * A unique constraint on (user_id, exercise_id) enforces one record per pair;
 * re-attempts update the existing row.
 */
@Entity
@Table(
    name = "user_progress",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_exercise",
        columnNames = {"user_id", "exercise_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProgressStatus status;

    /** Total number of answer submissions (including retries). */
    @Column(name = "attempts", nullable = false)
    private int attempts;

    /** Timestamp of the last successful completion, null if not yet completed. */
    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_attempted_at", nullable = false)
    private Instant lastAttemptedAt;

    @PrePersist
    protected void onCreate() {
        lastAttemptedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastAttemptedAt = Instant.now();
    }
}
