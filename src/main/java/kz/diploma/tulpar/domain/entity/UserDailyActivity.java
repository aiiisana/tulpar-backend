package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_daily_activity",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_daily_activity",
               columnNames = {"user_id", "activity_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    /** True when the user submitted any activity (lesson, exercise) today. */
    @Column(name = "completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    /** True ONLY when the user specifically completed the daily challenge today. */
    @Column(name = "challenge_completed", nullable = false)
    @Builder.Default
    private boolean challengeCompleted = false;
}
