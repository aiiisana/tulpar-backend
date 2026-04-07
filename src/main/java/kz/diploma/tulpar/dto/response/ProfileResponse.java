package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.DailyGoal;
import kz.diploma.tulpar.domain.enums.DifficultyLevel;
import kz.diploma.tulpar.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String email;
    private String username;
    private String avatarUrl;
    private UserRole role;
    private DifficultyLevel level;
    private DailyGoal dailyGoal;
    private boolean notificationsEnabled;
    private boolean onboardingCompleted;
    private Instant createdAt;
    private int currentStreak;
    private int longestStreak;
    private int totalXp;
}
