package kz.diploma.tulpar.service;

import kz.diploma.tulpar.config.properties.AiProperties;
import kz.diploma.tulpar.domain.entity.ChatMessage;
import kz.diploma.tulpar.domain.enums.MessageRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    public OpenAiService(AiProperties properties) {
        this.properties = properties;

        // 30 s connect + 60 s read — prevents the thread from hanging indefinitely
        // when OpenAI is slow or unreachable.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(60_000);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String chat(List<ChatMessage> history, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        // Conversation history
        for (ChatMessage msg : history) {
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

        try {
            log.info("➡️ Sending request to AI...");

            Map<String, Object> response = restClient.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            log.info("📦 AI response: {}", response);

            if (response == null || !response.containsKey("choices")) {
                return "Ошибка AI (нет choices)";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");

            if (choices.isEmpty()) {
                return "Ошибка AI (пустой ответ)";
            }

            Map<String, String> message = (Map<String, String>) choices.get(0).get("message");

            return message.get("content");

        } catch (Exception e) {
            log.error("❌ OpenAI API call failed", e);
            return "Кешіріңіз, AI уақытша жұмыс істемейді";
        }
    }
}
