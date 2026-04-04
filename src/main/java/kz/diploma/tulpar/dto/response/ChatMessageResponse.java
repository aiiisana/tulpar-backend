package kz.diploma.tulpar.dto.response;

import kz.diploma.tulpar.domain.enums.MessageRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class ChatMessageResponse {
    private UUID id;
    private MessageRole role;
    private String content;
    private Instant createdAt;
}
