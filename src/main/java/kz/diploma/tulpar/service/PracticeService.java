package kz.diploma.tulpar.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.diploma.tulpar.config.properties.AiProperties;
import kz.diploma.tulpar.dto.response.PracticeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Conversational Kazakh practice service.
 *
 * The AI is asked to return a structured JSON response containing:
 *   - a natural Kazakh reply to the user's message
 *   - a list of grammar/vocabulary corrections (may be empty)
 *
 * Using a dedicated RestClient here (not the shared ChatService) because
 * practice sessions are stateless — we don't persist the dialogue.
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

    public PracticeService(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

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

    public PracticeResponse practice(String userText) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            log.warn("[Practice] API key not configured — returning stub");
            return stub(userText);
        }

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user",   "content", userText)
        );

        Map<String, Object> requestBody = Map.of(
                "model",      properties.getModel(),
                "messages",   messages,
                "max_tokens", 512,
                "temperature", 0.5
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String content = extractContent(response);
            return parseJson(content);

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
        // Strip markdown code fences if model adds them despite instructions
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
            // Return the raw text as reply with no corrections
            return PracticeResponse.builder()
                    .reply(json)
                    .hasErrors(false)
                    .corrections(List.of())
                    .build();
        }
    }

    private PracticeResponse stub(String userText) {
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
