package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;

import java.util.List;

/**
 * Audio-based exercise: play audio, choose the correct answer.
 */
@Entity
@Table(name = "listening_exercises")
@DiscriminatorValue("LISTENING")
@PrimaryKeyJoinColumn(name = "exercise_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListeningExercise extends Exercise {

    /** MinIO object URL for the audio file. */
    @Column(name = "audio_url", nullable = false, length = 1024)
    private String audioUrl;

    /** Full transcript of the audio clip. */
    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;

    @Convert(converter = StringListConverter.class)
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private List<String> options;

    @Column(name = "correct_answer", nullable = false, length = 512)
    private String correctAnswer;
}
