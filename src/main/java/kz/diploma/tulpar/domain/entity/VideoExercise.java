package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;

import java.util.List;

/**
 * Short movie clip exercise: watch a segment, choose the missing phrase.
 */
@Entity
@Table(name = "video_exercises")
@DiscriminatorValue("VIDEO_CONTEXT")
@PrimaryKeyJoinColumn(name = "exercise_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoExercise extends Exercise {

    /** MinIO object URL for the video file. */
    @Column(name = "video_url", nullable = false, length = 1024)
    private String videoUrl;

    /** Clip start offset in seconds. */
    @Column(name = "start_time")
    private Double startTime;

    /** Clip end offset in seconds. */
    @Column(name = "end_time")
    private Double endTime;

    @Convert(converter = StringListConverter.class)
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private List<String> options;

    @Column(name = "correct_answer", nullable = false, length = 512)
    private String correctAnswer;
}
