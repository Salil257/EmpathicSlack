package com.empathic.proxy.controller;

import com.empathic.proxy.model.InboxItem;
import com.empathic.proxy.service.InboxService;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.util.json.GsonFactory;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {

    private final InboxService inboxService;

    public HomeController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> handleHomeTab(@RequestParam MultiValueMap<String, String> formData) {
        String userId = formData.getFirst("user_id");
        String teamId = formData.getFirst("team_id");

        if (userId == null || teamId == null) {
            // Try parsing as JSON (for some Slack request formats)
            String payload = formData.getFirst("payload");
            if (payload != null) {
                Map<String, Object> payloadMap = GsonFactory.createSnakeCase().fromJson(payload, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> event = (Map<String, Object>) payloadMap.get("event");
                if (event != null) {
                    userId = (String) event.get("user");
                    teamId = (String) payloadMap.get("team_id");
                }
            }
        }

        if (userId == null || teamId == null) {
            return Map.of("error", "Missing user_id or team_id");
        }

        List<InboxItem> items = inboxService.getUnprocessedItems(userId, teamId);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "home");
        response.put("blocks", buildHomeBlocks(items, teamId));

        return response;
    }

    private List<Object> buildHomeBlocks(List<InboxItem> items, String teamId) {
        List<Object> blocks = new java.util.ArrayList<>();
        
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

        return blocks;
    }
}

