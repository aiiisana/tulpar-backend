package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks SM-2 spaced-repetition state for a single (user, exercise) pair.
 * The algorithm: https://www.supermemo.com/en/archives1990-2015/english/ol/sm2
 */
@Entity
@Table(
    name = "spaced_repetition_state",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_srs_user_exercise",
        columnNames = {"user_id", "exercise_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SpacedRepetitionState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /** SM-2 ease factor — minimum 1.3, initial value 2.5. */
    @Column(name = "ease_factor", nullable = false)
    private double easeFactor;

    /** Days until next review. Starts at 1. */
    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    /** Number of consecutive correct reviews. Resets to 0 on failure. */
    @Column(name = "repetitions", nullable = false)
    private int repetitions;

    /** Timestamp after which this card is due for review. */
    @Column(name = "next_review_at", nullable = false)
    private Instant nextReviewAt;

    @UpdateTimestamp
    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;
}
