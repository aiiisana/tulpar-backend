package kz.diploma.tulpar.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin wrapper around Firebase Cloud Messaging (FCM).
 * Uses the Firebase Admin SDK that is already on the classpath.
 */
@Slf4j
@Service
public class FcmService {

    /**
     * Send a push notification to a single device token.
     *
     * @return true if delivered, false if the token is invalid / stale
     */
    public boolean sendToDevice(String deviceToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            log.debug("[FCM] Sent to token={} messageId={}", deviceToken, messageId);
            return true;

        } catch (FirebaseMessagingException e) {
            // UNREGISTERED / INVALID_ARGUMENT means the token is stale
            log.warn("[FCM] Failed to send to token={}: {} ({})",
                    deviceToken, e.getMessage(), e.getMessagingErrorCode());
            return false;
        }
    }

    /**
     * Send one notification to up to 500 device tokens at once (FCM multicast limit).
     * Logs per-token failures but does not throw.
     */
    public void sendToDevices(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) return;

        // FCM multicast: max 500 tokens per call
        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));
            sendBatch(batch, title, body);
        }
    }

    private void sendBatch(List<String> tokens, String title, String body) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            var result = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("[FCM] Multicast: {} success, {} failure out of {} tokens",
                    result.getSuccessCount(), result.getFailureCount(), tokens.size());

        } catch (FirebaseMessagingException e) {
            log.error("[FCM] Multicast failed: {}", e.getMessage(), e);
        }
    }
}
