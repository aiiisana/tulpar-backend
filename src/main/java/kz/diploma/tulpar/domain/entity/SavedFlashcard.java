package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_saved_flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedFlashcard {

    @EmbeddedId
    private SavedFlashcardId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("flashcardId")
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private Instant savedAt;

    @PrePersist
    protected void onCreate() {
        savedAt = Instant.now();
    }
}
