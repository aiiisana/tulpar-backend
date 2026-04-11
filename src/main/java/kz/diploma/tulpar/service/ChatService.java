package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.ChatMessage;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.enums.MessageRole;
import kz.diploma.tulpar.dto.response.ChatMessageResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.repository.ChatMessageRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

/**
 * Chat service — manages persistence of chat messages and delegates to AiService.
 *
 * WHY TransactionTemplate instead of a single @Transactional:
 *   The AI call (OpenAI) can take 10-60 seconds.  A single @Transactional wrapper
 *   would hold the JDBC connection idle the whole time. PostgreSQL's
 *   idle_in_transaction_session_timeout kills such connections, so both saves
 *   get rolled back and nothing lands in the DB.
 *
 *   With TransactionTemplate we explicitly commit Tx-1 (user message) BEFORE
 *   calling OpenAI, then open Tx-2 (assistant message) AFTER getting the reply.
 *   Each DB round-trip is fast; no long idle connections.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AiService aiService;
    private final PlatformTransactionManager txManager;

    public ChatMessageResponse sendMessage(String userId, String userMessage) {

        // ── Tx 1: load context + save user message (commits immediately) ─────
        List<ChatMessage> history = new TransactionTemplate(txManager).execute(status -> {
            User user = userRepository.getReferenceById(userId);

            List<ChatMessage> ctx =
                    chatMessageRepository.findTop50ByUserIdOrderByCreatedAtAsc(userId);

            chatMessageRepository.save(
                    ChatMessage.builder()
                            .user(user)
                            .role(MessageRole.USER)
                            .content(userMessage)
                            .build()
            );
            return ctx;
        });

        // ── AI call — no DB connection held during this ──────────────────────
        log.info("[Chat] Calling AI for user={}", userId);
        String aiReply = aiService.chat(
                history != null ? history : List.of(),
                userMessage
        );
        log.info("[Chat] AI reply received ({} chars)", aiReply.length());

        // ── Tx 2: save assistant message + build response (commits immediately)
        ChatMessageResponse result = new TransactionTemplate(txManager).execute(status -> {
            User user = userRepository.getReferenceById(userId);

            ChatMessage saved = chatMessageRepository.save(
                    ChatMessage.builder()
                            .user(user)
                            .role(MessageRole.ASSISTANT)
                            .content(aiReply)
                            .build()
            );

            return ChatMessageResponse.builder()
                    .id(saved.getId())
                    .role(saved.getRole())
                    .content(saved.getContent())
                    .createdAt(saved.getCreatedAt())
                    .build();
        });

        if (result == null) {
            throw new IllegalStateException("Failed to save assistant message");
        }
        return result;
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> getHistory(String userId, int page, int size) {
        return PageResponse.from(
                chatMessageRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                        .map(this::toResponse));
    }

    private ChatMessageResponse toResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .role(msg.getRole())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
