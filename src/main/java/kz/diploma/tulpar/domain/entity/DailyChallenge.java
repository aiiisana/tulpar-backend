package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.converter.StringListConverter;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "daily_challenges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "challenge_date", nullable = false, unique = true)
    private LocalDate challengeDate;

    @Column(name = "correct_word", nullable = false, length = 255)
    private String correctWord;

    @Convert(converter = StringListConverter.class)
    @Column(name = "letters", nullable = false, columnDefinition = "TEXT")
    private List<String> letters;

    @Convert(converter = StringListConverter.class)
    @Column(name = "image_urls", nullable = false, columnDefinition = "TEXT")
    private List<String> imageUrls;
}
