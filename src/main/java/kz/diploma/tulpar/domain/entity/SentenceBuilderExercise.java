package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;

import java.util.List;

/**
 * Sentence builder exercise: arrange shuffled Kazakh words into a correct sentence.
 */
@Entity
@Table(name = "sentence_builder_exercises")
@DiscriminatorValue("SENTENCE_BUILDER")
@PrimaryKeyJoinColumn(name = "exercise_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SentenceBuilderExercise extends Exercise {

    /** The grammatically correct target sentence. Used for answer validation. */
    @Column(name = "correct_sentence", nullable = false, columnDefinition = "TEXT")
    private String correctSentence;

    /** The words of the sentence in randomised order, sent to the client. */
    @Convert(converter = StringListConverter.class)
    @Column(name = "shuffled_words", nullable = false, columnDefinition = "TEXT")
    private List<String> shuffledWords;
}
