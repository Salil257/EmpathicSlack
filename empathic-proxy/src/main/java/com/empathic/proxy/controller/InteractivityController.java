package com.empathic.proxy.controller;

import com.empathic.proxy.model.AuditLog;
import com.empathic.proxy.service.AuditService;
import com.empathic.proxy.service.InboxService;
import com.empathic.proxy.service.LLMService;
import com.empathic.proxy.service.SlackService;
import com.empathic.proxy.model.InboxItem;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.view.Views;
import com.slack.api.util.json.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/interactivity")
public class InteractivityController {

    private static final Logger logger = LoggerFactory.getLogger(InteractivityController.class);
    private final InboxService inboxService;
    private final LLMService llmService;
    private final SlackService slackService;
    private final AuditService auditService;

    public InteractivityController(InboxService inboxService, LLMService llmService,
                                  SlackService slackService, AuditService auditService) {
        this.inboxService = inboxService;
        this.llmService = llmService;
        this.slackService = slackService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<String> handleInteractivity(@RequestParam String payload) {
        try {
            Map<String, Object> payloadMap = GsonFactory.createSnakeCase().fromJson(payload, Map.class);
            String type = (String) payloadMap.get("type");

            if ("block_actions".equals(type)) {
                handleBlockActions(payload);
            } else if ("view_submission".equals(type)) {
                handleViewSubmission(payload);
            } else if ("shortcut".equals(type)) {
                handleShortcut(payload);
            }

            return ResponseEntity.ok("");
        } catch (Exception e) {
            logger.error("Error handling interactivity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    private void handleBlockActions(String payload) {
        try {
            BlockActionPayload actionPayload = GsonFactory.createSnakeCase()
                .fromJson(payload, BlockActionPayload.class);
            
            if (actionPayload.getActions() == null || actionPayload.getActions().isEmpty()) {
                return;
            }

            String actionId = actionPayload.getActions().get(0).getActionId();
            String userId = actionPayload.getUser().getId();
            String teamId = actionPayload.getTeam().getId();
            String triggerId = actionPayload.getTriggerId();

            if ("open_composer".equals(actionId)) {
                String value = actionPayload.getActions().get(0).getValue();
                // Value format: itemId:channelId:threadTs:teamId
                String[] parts = value.split(":");
                if (parts.length >= 3) {
                    String itemId = parts[0];
                    String channelId = parts[1];
                    String threadTs = parts[2];
                    
                    InboxItem item = inboxService.getItem(Long.parseLong(itemId));
                    openComposerModal(teamId, triggerId, item, channelId, threadTs);
                }
            } else if ("mark_processed".equals(actionId)) {
                String itemId = actionPayload.getActions().get(0).getValue();
                inboxService.markAsProcessed(Long.parseLong(itemId));
            } else if (actionId != null && actionId.startsWith("transform_")) {
                // Handle transform actions - would need to update the modal
                logger.info("Transform action: {}", actionId);
            }
        } catch (Exception e) {
            logger.error("Error handling block actions", e);
        }
    }

    private void openComposerModal(String teamId, String triggerId, InboxItem item, String channelId, String threadTs) {
        try {
            com.slack.api.methods.MethodsClient client = slackService.getClient(teamId);
            com.slack.api.model.view.View view = buildComposerView(item, channelId, threadTs);
            
            com.slack.api.methods.request.views.ViewsOpenRequest request = 
                com.slack.api.methods.request.views.ViewsOpenRequest.builder()
                    .triggerId(triggerId)
                    .view(view)
                    .build();
            
            client.viewsOpen(request);
        } catch (Exception e) {
            logger.error("Error opening composer modal", e);
        }
    }

    private com.slack.api.model.view.View buildComposerView(InboxItem item, String channelId, String threadTs) {
        final String suggestedReply = item != null && item.getSuggestedReply() != null 
            ? item.getSuggestedReply() 
            : "";
        final String finalChannelId = channelId != null ? channelId : "";
        final String finalThreadTs = threadTs != null ? threadTs : "";
        final String itemIdStr = item != null ? item.getId().toString() : "";
        final String metadata = String.format("%s:%s:%s", finalChannelId, finalThreadTs, itemIdStr);

        return Views.view(v -> v
            .type("modal")
            .title(Views.viewTitle(t -> t.type("plain_text").text("Compose Reply")))
            .submit(Views.viewSubmit(s -> s.type("plain_text").text("Post")))
            .close(Views.viewClose(c -> c.type("plain_text").text("Cancel")))
            .privateMetadata(metadata)
            .blocks(Blocks.asBlocks(
                Blocks.section(s -> s
                    .text(BlockCompositions.markdownText("Draft your reply below. Use transform buttons to adjust tone or length."))
                ),
                Blocks.input(i -> i
                    .blockId("draft_block")
                    .label(BlockCompositions.plainText("Draft"))
                    .element(PlainTextInputElement.builder()
                        .actionId("draft_input")
                        .multiline(true)
                        .placeholder(BlockCompositions.plainText("Type your message here..."))
                        .initialValue(suggestedReply)
                        .build())
                ),
                Blocks.section(s -> s
                    .text(BlockCompositions.markdownText("*Transform options:*"))
                ),
                Blocks.actions(a -> a
                    .elements(BlockElements.asElements(
                        BlockElements.button(b -> b
                            .text(BlockCompositions.plainText("Professional"))
                            .actionId("transform_professional")
                            .value("professional")
                        ),
                        BlockElements.button(b -> b
                            .text(BlockCompositions.plainText("Casual"))
                            .actionId("transform_casual")
                            .value("casual")
                        ),
                        BlockElements.button(b -> b
                            .text(BlockCompositions.plainText("Short"))
                            .actionId("transform_short")
                            .value("short")
                        ),
                        BlockElements.button(b -> b
                            .text(BlockCompositions.plainText("Long"))
                            .actionId("transform_long")
                            .value("long")
                        )
                    ))
                )
            ))
        );
    }

    private void handleViewSubmission(String payload) {
        try {
            Map<String, Object> payloadMap = GsonFactory.createSnakeCase().fromJson(payload, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> view = (Map<String, Object>) payloadMap.get("view");
            @SuppressWarnings("unchecked")
            Map<String, Object> state = (Map<String, Object>) view.get("state");
            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) state.get("values");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) payloadMap.get("user");
            @SuppressWarnings("unchecked")
            Map<String, Object> team = (Map<String, Object>) payloadMap.get("team");
            
            String userId = (String) user.get("id");
            String teamId = (String) team.get("id");
            String privateMetadata = (String) view.get("private_metadata");
            
            // Parse private metadata for channel/thread info
            String[] metadata = privateMetadata.split(":");
            String channelId = metadata[0];
            String threadTs = metadata[1];
            String itemId = metadata.length > 2 ? metadata[2] : null;

            // Get draft text
            @SuppressWarnings("unchecked")
            Map<String, Object> draftBlock = (Map<String, Object>) values.get("draft_block");
            @SuppressWarnings("unchecked")
            Map<String, Object> draftInput = (Map<String, Object>) draftBlock.get("draft_input");
            String draftText = (String) draftInput.get("value");

            // Post message
            slackService.postMessage(teamId, channelId, threadTs, draftText);
            if (itemId != null && !itemId.isEmpty()) {
                inboxService.markAsProcessed(Long.parseLong(itemId));
            }
            auditService.log(userId, teamId, AuditLog.ActionType.MESSAGE_POSTED,
                           channelId, threadTs, "Message posted via composer");
        } catch (Exception e) {
            logger.error("Error handling view submission", e);
        }
    }

    private void handleShortcut(String payload) {
        try {
            Map<String, Object> payloadMap = GsonFactory.createSnakeCase().fromJson(payload, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) payloadMap.get("user");
            @SuppressWarnings("unchecked")
            Map<String, Object> team = (Map<String, Object>) payloadMap.get("team");
            
            String userId = (String) user.get("id");
            String teamId = (String) team.get("id");
            String triggerId = (String) payloadMap.get("trigger_id");
            String callbackId = (String) payloadMap.get("callback_id");
            
            // Only handle compose_reply shortcut
            if (!"compose_reply".equals(callbackId)) {
                logger.warn("Unknown shortcut callback_id: {}", callbackId);
                return;
            }
            
            // Get channel and thread from shortcut context if available
            @SuppressWarnings("unchecked")
            Map<String, Object> channel = (Map<String, Object>) payloadMap.get("channel");
            String channelId = channel != null ? (String) channel.get("id") : null;
            String threadTs = (String) payloadMap.get("message_ts");
            
            // Open composer modal
            openComposerModal(teamId, triggerId, null, channelId, threadTs != null ? threadTs : "");
            logger.info("Compose Reply shortcut triggered by user: {}", userId);
        } catch (Exception e) {
            logger.error("Error handling shortcut", e);
        }
    }
}

