package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data @Builder
public class StatsResponse {
    private int currentStreak;
    private int longestStreak;
    private int totalXp;
    private LocalDate lastActivityDate;
}
