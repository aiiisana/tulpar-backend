package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PlacementQuestionResponse {
    private UUID id;
    private int orderIndex;
    private String question;
    private List<String> options;
    // correctIndex intentionally omitted — evaluated server-side
}
