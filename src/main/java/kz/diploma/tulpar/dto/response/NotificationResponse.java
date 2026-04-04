package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String body;
    private boolean read;
    private Instant createdAt;
}
