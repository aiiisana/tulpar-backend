package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.ExerciseType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Root entity for all exercise types.
 * Uses JOINED inheritance — each subtype has its own table containing
 * only the columns specific to that type, joined on exercise_id.
 */
@Entity
@Table(name = "exercises")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "exercise_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, insertable = false, updatable = false, length = 32)
    private ExerciseType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 32)
    private DifficultyLevel difficultyLevel;

    /** The question or prompt shown to the user. */
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    /** Optional explanation shown after the user answers. */
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
