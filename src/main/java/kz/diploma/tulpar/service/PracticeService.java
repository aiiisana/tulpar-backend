package kz.diploma.tulpar.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.diploma.tulpar.config.properties.AiProperties;
import kz.diploma.tulpar.domain.entity.PracticeMessage;
import kz.diploma.tulpar.dto.response.PracticeHistoryItemResponse;
import kz.diploma.tulpar.dto.response.PracticeResponse;
import kz.diploma.tulpar.repository.PracticeMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conversational Kazakh practice service.
 *
 * Each call to {@link #practice(String, String)} persists the exchange
 * (user message + AI reply + corrections) in {@code practice_messages},
 * so history is per-user and device-independent.
 */
@Slf4j
@Service
public class PracticeService {

    private static final String SYSTEM_PROMPT = """
            You are a Kazakh language conversation tutor.
            The user will write a message in Kazakh (possibly with errors).
            Your task:
            1. Reply naturally in Kazakh to continue the conversation.
            2. Identify any grammar, vocabulary, or spelling mistakes.

            You MUST respond in valid JSON ONLY, with NO markdown, NO backticks, NO extra text.
            Use EXACTLY this structure:
            {
              "reply": "<your Kazakh reply>",
              "corrections": [
                {
                  "original": "<what the user wrote>",
                  "corrected": "<correct form>",
                  "explanation": "<short explanation in Russian>"
                }
              ]
            }
            If there are no errors, return an empty array for corrections.
            Keep reply under 3 sentences. Keep explanations brief (1 sentence each).
            """;

    private final RestClient restClient;
    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final PracticeMessageRepository practiceMessageRepository;

    public PracticeService(AiProperties properties,
                           ObjectMapper objectMapper,
                           PracticeMessageRepository practiceMessageRepository) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.practiceMessageRepository = practiceMessageRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Sends the user's text to the AI and persists the full exchange.
     * History is tied to {@code userId} (Firebase UID).
     */
    @Transactional
    public PracticeResponse practice(String userId, String userText) {
        String apiKey = properties.getApiKey();
        PracticeResponse response;

        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            log.warn("[Practice] API key not configured — returning stub");
            response = stub();
        } else {
            response = callAi(userText);
        }

        // Persist exchange regardless of outcome
        saveExchange(userId, userText, response);
        return response;
    }

    /** Returns the last 100 exchanges for a user, oldest-first. */
    @Transactional(readOnly = true)
    public List<PracticeHistoryItemResponse> getHistory(String userId) {
        return practiceMessageRepository
                .findTop100ByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(this::toHistoryItem)
                .toList();
    }

    /** Deletes all practice messages for a user. */
    @Transactional
    public void clearHistory(String userId) {
        practiceMessageRepository.deleteAllByUserId(userId);
    }

    // ─── Persistence helper ───────────────────────────────────────────────────

    private void saveExchange(String userId, String userText, PracticeResponse response) {
        try {
            String correctionsJson = objectMapper.writeValueAsString(response.getCorrections());
            practiceMessageRepository.save(
                    PracticeMessage.builder()
                            .userId(userId)
                            .userText(userText)
                            .aiReply(response.getReply())
                            .hasErrors(response.isHasErrors())
                            .corrections(correctionsJson)
                            .build()
            );
        } catch (Exception e) {
            log.error("[Practice] Failed to persist exchange for user={}: {}", userId, e.getMessage());
        }
    }

    private PracticeHistoryItemResponse toHistoryItem(PracticeMessage msg) {
        List<PracticeResponse.Correction> corrections;
        try {
            corrections = objectMapper.readValue(
                    msg.getCorrections(),
                    new TypeReference<List<PracticeResponse.Correction>>() {}
            );
        } catch (Exception e) {
            corrections = List.of();
        }

        return PracticeHistoryItemResponse.builder()
                .id(msg.getId())
                .userText(msg.getUserText())
                .aiReply(msg.getAiReply())
                .hasErrors(msg.isHasErrors())
                .corrections(corrections)
                .createdAt(msg.getCreatedAt())
                .build();
    }

    // ─── AI call ──────────────────────────────────────────────────────────────

    private PracticeResponse callAi(String userText) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user",   "content", userText)
        );

        Map<String, Object> requestBody = Map.of(
                "model",       properties.getModel(),
                "messages",    messages,
                "max_tokens",  512,
                "temperature", 0.5
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return parseJson(extractContent(response));

        } catch (Exception e) {
            log.error("[Practice] AI call failed: {}", e.getMessage());
            return errorResponse();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) throw new IllegalStateException("Null response");
        var choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) throw new IllegalStateException("Empty choices");
        var message = (Map<String, Object>) choices.get(0).get("message");
        Object content = message != null ? message.get("content") : null;
        if (content == null) throw new IllegalStateException("Null content");
        return content.toString().trim();
    }

    private PracticeResponse parseJson(String json) {
        String cleaned = json
                .replaceAll("(?s)^```[a-z]*\\s*", "")
                .replaceAll("(?s)\\s*```$", "")
                .trim();
        try {
            Map<String, Object> parsed = objectMapper.readValue(cleaned,
                    new TypeReference<>() {});

            String reply = (String) parsed.getOrDefault("reply", "");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawCorrections =
                    (List<Map<String, Object>>) parsed.getOrDefault("corrections", List.of());

            List<PracticeResponse.Correction> corrections = rawCorrections.stream()
                    .map(c -> PracticeResponse.Correction.builder()
                            .original((String) c.getOrDefault("original", ""))
                            .corrected((String) c.getOrDefault("corrected", ""))
                            .explanation((String) c.getOrDefault("explanation", ""))
                            .build())
                    .toList();

            return PracticeResponse.builder()
                    .reply(reply)
                    .hasErrors(!corrections.isEmpty())
                    .corrections(corrections)
                    .build();

        } catch (Exception e) {
            log.warn("[Practice] JSON parse failed, raw={}", json, e);
            return PracticeResponse.builder()
                    .reply(json)
                    .hasErrors(false)
                    .corrections(List.of())
                    .build();
        }
    }

    private PracticeResponse stub() {
        return PracticeResponse.builder()
                .reply("Кешіріңіз, AI кілті орнатылмаған. / AI key not configured.")
                .hasErrors(false)
                .corrections(new ArrayList<>())
                .build();
    }

    private PracticeResponse errorResponse() {
        return PracticeResponse.builder()
                .reply("Кешіріңіз, AI уақытша жұмыс істемейді. / AI temporarily unavailable.")
                .hasErrors(false)
                .corrections(new ArrayList<>())
                .build();
    }
}
