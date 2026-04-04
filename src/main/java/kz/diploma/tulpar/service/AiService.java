package kz.diploma.tulpar.service;

import kz.diploma.tulpar.domain.entity.ChatMessage;

import java.util.List;

public interface AiService {
    /**
     * Sends a message to the AI model with conversation history.
     *
     * @param history      Previous messages for context (up to 50)
     * @param userMessage  Latest user message
     * @return             AI assistant's response text
     */
    String chat(List<ChatMessage> history, String userMessage);
}
