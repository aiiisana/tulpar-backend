package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * One practice exchange: the user's Kazakh text and the AI's reply + corrections.
 * Stored per-user so history survives across devices and sessions.
 */
@Entity
@Table(name = "practice_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PracticeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Firebase UID — not a FK entity reference to avoid join overhead. */
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "user_text", nullable = false, columnDefinition = "TEXT")
    private String userText;

    @Column(name = "ai_reply", columnDefinition = "TEXT")
    private String aiReply;

    @Column(name = "has_errors", nullable = false)
    private boolean hasErrors;

    /** JSON array of {original, corrected, explanation} objects. */
    @Column(name = "corrections", nullable = false, columnDefinition = "TEXT")
    private String corrections;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
