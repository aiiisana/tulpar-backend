package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PracticeMessageRequest {

    @NotBlank
    @Size(max = 1000)
    private String text;
}
