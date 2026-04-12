package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import kz.diploma.tulpar.domain.enums.DailyGoal;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.UserRole;
import lombok.*;

import java.time.Instant;

/**
 * Application user. The primary key is the Firebase UID (String).
 * No password is stored — authentication is fully delegated to Firebase.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Firebase UID — used as-is from the verified ID token. */
    @Id
    @Column(name = "id", nullable = false, length = 128)
    private String id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private UserRole role;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    /** R2 object key for the user's avatar (set when uploaded via the backend). */
    @Column(name = "avatar_object_key", length = 1024)
    private String avatarObjectKey;

    @Column(name = "notifications_enabled", nullable = false)
    @Builder.Default
    private boolean notificationsEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 32)
    private DifficultyLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_goal", length = 32)
    private DailyGoal dailyGoal;

    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private boolean onboardingCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (role == null) {
            role = UserRole.USER;
        }
    }
}
