package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.ChatMessage;
import kz.diploma.tulpar.domain.entity.User;
import kz.diploma.tulpar.domain.enums.MessageRole;
import kz.diploma.tulpar.dto.response.ChatMessageResponse;
import kz.diploma.tulpar.dto.response.PageResponse;
import kz.diploma.tulpar.repository.ChatMessageRepository;
import kz.diploma.tulpar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    @Transactional
    public ChatMessageResponse sendMessage(String userId, String userMessage) {
        User user = userRepository.getReferenceById(userId);

        // Load history for context
        List<ChatMessage> history = chatMessageRepository.findTop50ByUserIdOrderByCreatedAtAsc(userId);

        // Persist user message
        chatMessageRepository.save(ChatMessage.builder()
                .user(user)
                .role(MessageRole.USER)
                .content(userMessage)
                .build());

        // Call AI
        String aiReply = aiService.chat(history, userMessage);

        // Persist assistant response
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .user(user)
                .role(MessageRole.ASSISTANT)
                .content(aiReply)
                .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> getHistory(String userId, int page, int size) {
        return PageResponse.from(
                chatMessageRepository.findAllByUserIdOrderByCreatedAtDesc(
                        userId, PageRequest.of(page, size))
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
