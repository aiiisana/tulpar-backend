package kz.diploma.tulpar.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSessionTimeRequest {

    /**
     * Number of seconds to add for the current session flush.
     */
    @NotNull
    @Min(1)
    private Integer seconds;
}
