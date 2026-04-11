package kz.diploma.tulpar.service;

import kz.diploma.tulpar.config.properties.AiProperties;
import kz.diploma.tulpar.domain.entity.ChatMessage;
import kz.diploma.tulpar.domain.enums.MessageRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAiService implements AiService {

    private static final String SYSTEM_PROMPT =
            "You are a helpful Kazakh language tutor. Help users learn the Kazakh language " +
                    "through explanations, examples, grammar tips, and vocabulary practice. " +
                    "Respond in the language the user writes in (Kazakh, Russian, or English). " +
                    "Be encouraging and patient.";

    private final RestClient restClient;
    private final AiProperties properties;

    private static final int MAX_RETRIES    = 2;   // total attempts
    private static final long RETRY_DELAY_MS = 1_500; // pause between retries

    public OpenAiService(AiProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);  // 10 s connect
        factory.setReadTimeout(60_000);     // 60 s read (Perplexity can be slow)

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String chat(List<ChatMessage> history, String userMessage) {
        // If the API key looks like an unresolved placeholder or is empty, skip the call.
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            log.warn("[AI] API key not configured — returning stub response");
            return "Кешіріңіз, AI кілті конфигурацияланбаған. / AI key not configured.";
        }

        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        // Conversation history (skip messages with null content, just in case)
        for (ChatMessage msg : history) {
            if (msg.getContent() == null) continue;
            String role = msg.getRole() == MessageRole.USER ? "user" : "assistant";
            messages.add(Map.of("role", role, "content", msg.getContent()));
        }

        // Latest user message
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", messages,
                "max_tokens", 1024,
                "temperature", 0.7
        );

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("[AI] Attempt {}/{}, model={}", attempt, MAX_RETRIES, properties.getModel());

                @SuppressWarnings("unchecked")
                Map<String, Object> response = restClient.post()
                        .uri("/v1/chat/completions")
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

                // ── Validate response structure ───────────────────────────
                if (response == null) {
                    log.warn("[AI] Null response on attempt {}", attempt);
                    lastException = new IllegalStateException("Null response");
                    sleepQuietly(RETRY_DELAY_MS);
                    continue;
                }

                if (!response.containsKey("choices")) {
                    log.warn("[AI] No 'choices' field. Keys: {}", response.keySet());
                    // Could be an error response from Perplexity/OpenAI
                    Object errorObj = response.get("error");
                    if (errorObj instanceof Map<?,?> err) {
                        String errMsg = String.valueOf(err.get("message"));
                        log.error("[AI] API error: {}", errMsg);
                        // Auth errors — no point retrying
                        if (errMsg.contains("Unauthorized") || errMsg.contains("invalid_api_key")) {
                            return "Кешіріңіз, AI кілті жарамсыз. / Invalid AI key.";
                        }
                    }
                    lastException = new IllegalStateException("Missing 'choices'");
                    sleepQuietly(RETRY_DELAY_MS);
                    continue;
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices == null || choices.isEmpty()) {
                    log.warn("[AI] Empty choices list on attempt {}", attempt);
                    lastException = new IllegalStateException("Empty choices");
                    sleepQuietly(RETRY_DELAY_MS);
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                Object content = message != null ? message.get("content") : null;

                if (content == null || content.toString().isBlank()) {
                    log.warn("[AI] Blank content on attempt {}", attempt);
                    lastException = new IllegalStateException("Blank content");
                    sleepQuietly(RETRY_DELAY_MS);
                    continue;
                }

                log.info("[AI] Success on attempt {}", attempt);
                return content.toString();

            } catch (ResourceAccessException e) {
                // Timeout or connection refused
                log.warn("[AI] Timeout/connection error on attempt {}: {}", attempt, e.getMessage());
                lastException = e;
                sleepQuietly(RETRY_DELAY_MS);
            } catch (RestClientException e) {
                log.warn("[AI] HTTP error on attempt {}: {}", attempt, e.getMessage());
                lastException = e;
                sleepQuietly(RETRY_DELAY_MS);
            } catch (Exception e) {
                log.error("[AI] Unexpected error on attempt {}: {}", attempt, e.getMessage());
                lastException = e;
                sleepQuietly(RETRY_DELAY_MS);
            }
        }

        log.error("[AI] All {} attempts failed. Last error: {}",
                MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown");
        return "Кешіріңіз, AI уақытша жұмыс істемейді. Кейінірек қайталаңыз. / AI is temporarily unavailable.";
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
