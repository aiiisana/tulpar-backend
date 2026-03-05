package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Handles both "choose correct translation" and "word matching" sub-types.
 * The sub-type distinction is encoded in the question text or a separate field
 * if needed in future.
 */
@Entity
@Table(name = "vocabulary_exercises")
@DiscriminatorValue("VOCABULARY")
@PrimaryKeyJoinColumn(name = "exercise_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyExercise extends Exercise {

    /** The Kazakh (or Russian) word being tested. */
    @Column(name = "word", nullable = false, length = 255)
    private String word;

    /** The correct translation of the word. */
    @Column(name = "translation", nullable = false, length = 255)
    private String translation;

    /** Distractor options including the correct translation. Stored as JSON array. */
    @Convert(converter = StringListConverter.class)
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private List<String> options;

    /** The correct answer (must be one of the options). */
    @Column(name = "correct_answer", nullable = false, length = 255)
    private String correctAnswer;
}
