package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data @Builder
public class CalendarDayResponse {
    private LocalDate date;
    private boolean completed;
}
