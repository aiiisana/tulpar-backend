package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;

import java.util.List;

/**
 * Image-based exercise: view an image, choose the correct Kazakh phrase.
 */
@Entity
@Table(name = "image_exercises")
@DiscriminatorValue("IMAGE_CONTEXT")
@PrimaryKeyJoinColumn(name = "exercise_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageExercise extends Exercise {

    /** MinIO object URL for the image file. */
    @Column(name = "image_url", nullable = false, length = 1024)
    private String imageUrl;

    @Convert(converter = StringListConverter.class)
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private List<String> options;

    @Column(name = "correct_answer", nullable = false, length = 512)
    private String correctAnswer;
}
