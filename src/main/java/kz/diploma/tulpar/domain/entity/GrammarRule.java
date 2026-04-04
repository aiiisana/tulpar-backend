package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "grammar_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GrammarRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Convert(converter = StringListConverter.class)
    @Column(name = "examples", nullable = false, columnDefinition = "TEXT")
    private List<String> examples;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
