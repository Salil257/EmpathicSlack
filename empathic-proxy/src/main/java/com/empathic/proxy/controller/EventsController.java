package com.empathic.proxy.controller;

import com.empathic.proxy.model.AuditLog;
import com.empathic.proxy.model.InboxItem;
import com.empathic.proxy.service.AuditService;
import com.empathic.proxy.service.InboxService;
import com.empathic.proxy.service.SlackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;

@RestController
@RequestMapping("/events")
public class EventsController {

    private static final Logger logger = LoggerFactory.getLogger(EventsController.class);
    private final InboxService inboxService;
    private final AuditService auditService;
    private final SlackService slackService;

    public EventsController(InboxService inboxService, AuditService auditService, SlackService slackService) {
        this.inboxService = inboxService;
        this.auditService = auditService;
        this.slackService = slackService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleEvent(@RequestBody Map<String, Object> event) {
        String type = (String) event.get("type");
        
        if ("url_verification".equals(type)) {
            String challenge = (String) event.get("challenge");
            return ResponseEntity.ok(Map.of("challenge", challenge));
        }

        if ("event_callback".equals(type)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> eventData = (Map<String, Object>) event.get("event");
            String eventType = (String) eventData.get("type");
            String teamId = (String) event.get("team_id");

            if ("app_mention".equals(eventType)) {
                handleAppMention(eventData, teamId);
            } else if ("message".equals(eventType)) {
                handleMessage(eventData, teamId);
            } else if ("app_home_opened".equals(eventType)) {
                handleAppHomeOpened(eventData, teamId);
            }
        }

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private void handleAppMention(Map<String, Object> eventData, String teamId) {
        try {
            String userId = (String) eventData.get("user");
            String channelId = (String) eventData.get("channel");
            String messageTs = (String) eventData.get("ts");
            String threadTs = (String) eventData.get("thread_ts");
            String text = (String) eventData.get("text");

            if (userId == null || channelId == null || messageTs == null) {
                logger.warn("Missing required fields in app_mention event");
                return;
            }

            inboxService.createItem(
                userId, teamId, channelId, messageTs, 
                threadTs != null ? threadTs : messageTs, 
                text, InboxItem.ItemType.MENTION
            );

            auditService.log(userId, teamId, AuditLog.ActionType.REPLY_GENERATED, 
                           channelId, messageTs, "App mention processed");
        } catch (Exception e) {
            logger.error("Error handling app_mention", e);
        }
    }

    private void handleMessage(Map<String, Object> eventData, String teamId) {
        try {
            String subtype = (String) eventData.get("subtype");
            if (subtype != null && !subtype.isEmpty()) {
                return; // Skip bot messages and other subtypes
            }

            String channelType = (String) eventData.get("channel_type");
            if (!"im".equals(channelType)) {
                return; // Only process DMs
            }

            String userId = (String) eventData.get("user");
            String channelId = (String) eventData.get("channel");
            String messageTs = (String) eventData.get("ts");
            String threadTs = (String) eventData.get("thread_ts");
            String text = (String) eventData.get("text");

            if (userId == null || channelId == null || messageTs == null) {
                logger.warn("Missing required fields in message event");
                return;
            }

            inboxService.createItem(
                userId, teamId, channelId, messageTs,
                threadTs != null ? threadTs : messageTs,
                text, InboxItem.ItemType.DM
            );

            auditService.log(userId, teamId, AuditLog.ActionType.REPLY_GENERATED,
                           channelId, messageTs, "DM processed");
        } catch (Exception e) {
            logger.error("Error handling message", e);
        }
    }

    private void handleAppHomeOpened(Map<String, Object> eventData, String teamId) {
        try {
            String userId = (String) eventData.get("user");
            if (userId == null) {
                logger.warn("Missing user in app_home_opened event");
                return;
            }

            List<InboxItem> items = inboxService.getUnprocessedItems(userId, teamId);
            publishHomeView(teamId, userId, items);
            
            auditService.log(userId, teamId, AuditLog.ActionType.REPLY_GENERATED,
                           null, null, "Home tab opened");
        } catch (Exception e) {
            logger.error("Error handling app_home_opened", e);
        }
    }

    private void publishHomeView(String teamId, String userId, List<InboxItem> items) {
        try {
            var client = slackService.getClient(teamId);
            
            var blocks = new java.util.ArrayList<LayoutBlock>();
            blocks.add(Blocks.header(h -> h.text(BlockCompositions.plainText("Your Inbox"))));
            blocks.add(Blocks.divider());

            if (items.isEmpty()) {
                blocks.add(Blocks.section(s -> s.text(BlockCompositions.plainText("No unprocessed items. You're all caught up! 🎉"))));
            } else {
                for (InboxItem item : items) {
                    String messagePreview = item.getMessageText().length() > 200 
                        ? item.getMessageText().substring(0, 200) + "..."
                        : item.getMessageText();
                    
                    String replyPreview = item.getSuggestedReply() != null && item.getSuggestedReply().length() > 100
                        ? item.getSuggestedReply().substring(0, 100) + "..."
                        : (item.getSuggestedReply() != null ? item.getSuggestedReply() : "");

                    blocks.add(Blocks.section(s -> s
                        .text(BlockCompositions.markdownText(
                            String.format("*%s*\n%s\n\n*Action:* %s", 
                                item.getType(), 
                                messagePreview,
                                item.getAction() != null ? item.getAction() : "Review")
                        ))
                        .accessory(BlockElements.button(b -> b
                            .text(BlockCompositions.plainText("Reply"))
                            .actionId("open_composer")
                            .value(String.format("%s:%s:%s:%s", item.getId(), item.getChannelId(), item.getThreadTs(), teamId))
                            .style("primary")
                        ))
                    ));
                    if (replyPreview != null && !replyPreview.isEmpty()) {
                        blocks.add(Blocks.context(c -> c
                            .elements(List.of(BlockCompositions.markdownText(
                                String.format("💡 *Suggested reply:* %s", replyPreview)
                            )))
                        ));
                    }
                    blocks.add(Blocks.divider());
                }
            }

            var view = com.slack.api.model.view.View.builder()
                .type("home")
                .blocks(blocks)
                .build();

            var response = client.viewsPublish(r -> r
                .userId(userId)
                .view(view)
            );

            if (!response.isOk()) {
                logger.error("Failed to publish home view: " + response.getError());
            }
        } catch (IOException | SlackApiException e) {
            logger.error("Error publishing home view", e);
        }
    }
}

