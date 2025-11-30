package com.empathic.proxy.service;

import com.empathic.proxy.model.InboxItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LLMService {
    
    private final OpenAILLMService openAILLMService;
    private final LocalAILLMService localAILLMService;
    
    @Value("${llm.provider:openai}")
    private String provider;

    public LLMService(OpenAILLMService openAILLMService, LocalAILLMService localAILLMService) {
        this.openAILLMService = openAILLMService;
        this.localAILLMService = localAILLMService;
    }

    public Map<String, String> extractActionAndReply(InboxItem item) {
        if ("localai".equalsIgnoreCase(provider)) {
            return localAILLMService.extractActionAndReply(item);
        } else {
            return openAILLMService.extractActionAndReply(item);
        }
    }

    public String transformDraft(String draft, String tone, String length) {
        if ("localai".equalsIgnoreCase(provider)) {
            return localAILLMService.transformDraft(draft, tone, length);
        } else {
            return openAILLMService.transformDraft(draft, tone, length);
        }
    }
}

