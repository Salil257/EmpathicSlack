package com.empathic.proxy.service;

import com.empathic.proxy.model.InboxItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocalAILLMService {

    private final String baseUrl;
    private final String model;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LocalAILLMService(@Value("${llm.localai.base-url:http://localhost:11434/v1}") String baseUrl,
                            @Value("${llm.localai.model:llama3.2:1b}") String model) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, String> extractActionAndReply(InboxItem item) {
        String prompt = String.format(
            "You are analyzing a Slack message for a busy professional. " +
            "Extract a concise action item (max 50 words) and suggest a brief reply (max 200 words).\n\n" +
            "Message Type: %s\n" +
            "Message: %s\n\n" +
            "Respond in JSON format: {\"action\": \"...\", \"suggestedReply\": \"...\"}",
            item.getType(),
            item.getMessageText()
        );

        try {
            String response = callLocalAI(prompt);
            return parseJsonResponse(response);
        } catch (Exception e) {
            return Map.of(
                "action", "Review message",
                "suggestedReply", "Thanks for reaching out! I'll get back to you soon."
            );
        }
    }

    public String transformDraft(String draft, String tone, String length) {
        String prompt = String.format(
            "Transform the following draft message with the specified tone and length:\n\n" +
            "Tone: %s\n" +
            "Length: %s\n" +
            "Draft: %s\n\n" +
            "Return only the transformed message, no explanations.",
            tone, length, draft
        );

        try {
            return callLocalAI(prompt).trim();
        } catch (Exception e) {
            return draft;
        }
    }

    private String callLocalAI(String prompt) throws IOException {
        String url = baseUrl + "/chat/completions";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }
            
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        }
    }

    private Map<String, String> parseJsonResponse(String response) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            result.put("action", jsonNode.path("action").asText("Review message"));
            result.put("suggestedReply", jsonNode.path("suggestedReply").asText("Thanks for reaching out!"));
        } catch (Exception e) {
            // Fallback to simple parsing
            String action = extractJsonValue(response, "action");
            String reply = extractJsonValue(response, "suggestedReply");
            result.put("action", action != null ? action : "Review message");
            result.put("suggestedReply", reply != null ? reply : "Thanks for reaching out!");
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

