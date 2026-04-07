package kz.diploma.tulpar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CalendarDayResponse {
    private LocalDate date;
    private boolean completed;
}
