package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_achievements",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"user_id", "achievement_code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "achievement_code", nullable = false, length = 64)
    private String achievementCode;

    @Column(name = "earned_at", nullable = false)
    @Builder.Default
    private Instant earnedAt = Instant.now();
}
