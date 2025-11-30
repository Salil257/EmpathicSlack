package com.empathic.proxy.service;

import com.empathic.proxy.model.InboxItem;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OpenAILLMService {

    private final OpenAiService openAiService;
    private final String model;

    public OpenAILLMService(@Value("${llm.openai.api-key:}") String apiKey,
                           @Value("${llm.openai.model:gpt-4o-mini}") String model) {
        this.model = model;
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey);
        } else {
            this.openAiService = null;
        }
    }

    public Map<String, String> extractActionAndReply(InboxItem item) {
        if (openAiService == null) {
            return Map.of(
                "action", "Review message",
                "suggestedReply", "Thanks for reaching out! I'll get back to you soon."
            );
        }

        String prompt = String.format(
            "You are analyzing a Slack message for a busy professional. " +
            "Extract a concise action item (max 50 words) and suggest a brief reply (max 200 words).\n\n" +
            "Message Type: %s\n" +
            "Message: %s\n\n" +
            "Respond in JSON format: {\"action\": \"...\", \"suggestedReply\": \"...\"}",
            item.getType(),
            item.getMessageText()
        );

        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(userMessage))
            .temperature(0.7)
            .maxTokens(300)
            .build();

        try {
            String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
            
            // Simple JSON parsing (in production, use proper JSON parser)
            return parseJsonResponse(response);
        } catch (Exception e) {
            return Map.of(
                "action", "Review message",
                "suggestedReply", "Thanks for reaching out! I'll get back to you soon."
            );
        }
    }

    public String transformDraft(String draft, String tone, String length) {
        if (openAiService == null) {
            return draft;
        }

        String prompt = String.format(
            "Transform the following draft message with the specified tone and length:\n\n" +
            "Tone: %s\n" +
            "Length: %s\n" +
            "Draft: %s\n\n" +
            "Return only the transformed message, no explanations.",
            tone, length, draft
        );

        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(userMessage))
            .temperature(0.7)
            .maxTokens(500)
            .build();

        try {
            return openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent().trim();
        } catch (Exception e) {
            return draft;
        }
    }

    private Map<String, String> parseJsonResponse(String response) {
        Map<String, String> result = new HashMap<>();
        try {
            // Simple JSON parsing
            String action = extractJsonValue(response, "action");
            String reply = extractJsonValue(response, "suggestedReply");
            result.put("action", action != null ? action : "Review message");
            result.put("suggestedReply", reply != null ? reply : "Thanks for reaching out!");
        } catch (Exception e) {
            result.put("action", "Review message");
            result.put("suggestedReply", "Thanks for reaching out!");
        }
        return result;
    }

    private String extractJsonValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return null;
            int colonIndex = json.indexOf(":", keyIndex);
            int startIndex = json.indexOf("\"", colonIndex) + 1;
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }
}

