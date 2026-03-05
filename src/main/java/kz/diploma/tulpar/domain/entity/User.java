package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
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
