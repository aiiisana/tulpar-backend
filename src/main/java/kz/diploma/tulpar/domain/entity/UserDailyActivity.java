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

    @Column(name = "completed", nullable = false)
    @Builder.Default
    private boolean completed = false;
}
