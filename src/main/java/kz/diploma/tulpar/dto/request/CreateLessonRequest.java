package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateLessonRequest {
    @NotNull
    private UUID levelId;
    @NotBlank
    private String title;
    private int orderIndex;
    private int xpReward = 10;
}
