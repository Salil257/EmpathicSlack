package com.empathic.proxy.service;

import com.empathic.proxy.model.InboxItem;
import com.empathic.proxy.repository.InboxItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InboxService {

    private final InboxItemRepository repository;
    private final LLMService llmService;

    public InboxService(InboxItemRepository repository, LLMService llmService) {
        this.repository = repository;
        this.llmService = llmService;
    }

    public InboxItem createItem(String userId, String teamId, String channelId, 
                                String messageTs, String threadTs, String messageText, 
                                InboxItem.ItemType type) {
        InboxItem item = new InboxItem();
        item.setUserId(userId);
        item.setTeamId(teamId);
        item.setChannelId(channelId);
        item.setMessageTs(messageTs);
        item.setThreadTs(threadTs);
        item.setMessageText(messageText);
        item.setType(type);
        item.setProcessed(false);

        // Extract action and reply using LLM
        Map<String, String> analysis = llmService.extractActionAndReply(item);
        item.setAction(analysis.get("action"));
        item.setSuggestedReply(analysis.get("suggestedReply"));

        return repository.save(item);
    }

    public List<InboxItem> getUnprocessedItems(String userId, String teamId) {
        return repository.findByUserIdAndTeamIdAndProcessedFalseOrderByCreatedAtDesc(userId, teamId);
    }

    public InboxItem getItem(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public void markAsProcessed(Long id) {
        repository.findById(id).ifPresent(item -> {
            item.setProcessed(true);
            repository.save(item);
        });
    }
}

