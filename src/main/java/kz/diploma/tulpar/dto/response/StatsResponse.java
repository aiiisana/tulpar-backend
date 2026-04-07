package kz.diploma.tulpar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatsResponse {
    private int currentStreak;
    private int longestStreak;
    private int totalXp;
    private LocalDate lastActivityDate;
}
