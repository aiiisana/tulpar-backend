package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SavedFlashcardId implements Serializable {

    @Column(name = "user_id", length = 128)
    private String userId;

    @Column(name = "flashcard_id")
    private UUID flashcardId;
}
