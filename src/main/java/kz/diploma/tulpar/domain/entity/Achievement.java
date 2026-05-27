package kz.diploma.tulpar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "achievements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Key used on the Flutter side to pick the right icon/color. */
    @Column(name = "icon_name", nullable = false, length = 64)
    private String iconName;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
