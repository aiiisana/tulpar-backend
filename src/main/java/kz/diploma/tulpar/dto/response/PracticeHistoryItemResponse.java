package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One practice exchange returned by GET /practice/history.
 * Maps 1-to-1 with a practice_messages row.
 */
@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class PracticeHistoryItemResponse {

    private UUID id;
    private String userText;
    private String aiReply;
    private boolean hasErrors;
    private List<PracticeResponse.Correction> corrections;
    private Instant createdAt;
}
