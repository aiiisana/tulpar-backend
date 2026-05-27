package kz.diploma.tulpar.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "placement_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlacementQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Type(JsonType.class)
    @Column(name = "options", nullable = false, columnDefinition = "jsonb")
    private List<String> options;

    /** 0-based index into options that is the correct answer. */
    @Column(name = "correct_index", nullable = false)
    private int correctIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 32)
    private DifficultyLevel difficulty;
}
