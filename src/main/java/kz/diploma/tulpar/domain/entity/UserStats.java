package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_stats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserStats {

    @Id
    @Column(name = "user_id", length = 128)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    private int longestStreak = 0;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private int totalXp = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;
}
