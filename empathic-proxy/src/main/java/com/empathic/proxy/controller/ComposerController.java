package com.empathic.proxy.controller;

import com.empathic.proxy.model.InboxItem;
import com.empathic.proxy.service.InboxService;
import com.empathic.proxy.service.LLMService;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.element.BlockElements;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/composer")
public class ComposerController {

    private final InboxService inboxService;
    private final LLMService llmService;

    public ComposerController(InboxService inboxService, LLMService llmService) {
        this.inboxService = inboxService;
        this.llmService = llmService;
    }

    @PostMapping("/open")
    public Map<String, Object> openComposer(@RequestBody Map<String, String> request) {
        String itemId = request.get("item_id");
        String channelId = request.get("channel_id");
        String threadTs = request.get("thread_ts");
        String triggerId = request.get("trigger_id");

        InboxItem item = null;
        String suggestedReply = "";
        if (itemId != null) {
            item = inboxService.getItem(Long.parseLong(itemId));
            if (item != null) {
                suggestedReply = item.getSuggestedReply() != null ? item.getSuggestedReply() : "";
                channelId = item.getChannelId();
                threadTs = item.getThreadTs();
            }
        }
        
        final String finalSuggestedReply = suggestedReply;
        final String finalChannelId = channelId != null ? channelId : "";
        final String finalThreadTs = threadTs != null ? threadTs : "";
        final String finalItemId = itemId != null ? itemId : "";

        View view = Views.view(v -> v
            .type("modal")
            .title(Views.viewTitle(t -> t.type("plain_text").text("Compose Reply")))
            .submit(Views.viewSubmit(s -> s.type("plain_text").text("Preview")))
            .close(Views.viewClose(c -> c.type("plain_text").text("Cancel")))
            .privateMetadata(String.format("%s:%s:%s", finalChannelId, finalThreadTs, finalItemId))
            .blocks(Blocks.asBlocks(
                Blocks.section(s -> s
                    .text(BlockCompositions.markdownText("Draft your reply below. You can transform it using AI."))
                ),
                Blocks.input(i -> i
                    .blockId("draft_block")
                    .label(BlockCompositions.plainText("Draft"))
                    .element(PlainTextInputElement.builder()
                        .actionId("draft_input")
                        .multiline(true)
                        .placeholder(BlockCompositions.plainText("Type your message here..."))
                        .initialValue(finalSuggestedReply)
                        .build())
                ),
                Blocks.section(s -> s
                    .text(BlockCompositions.markdownText("Transform options:"))
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

        Map<String, Object> response = new HashMap<>();
        response.put("trigger_id", triggerId);
        response.put("view", view);

        return response;
    }

    @PostMapping("/transform")
    public Map<String, Object> transformDraft(@RequestBody Map<String, String> request) {
        String draft = request.get("draft");
        String transformType = request.get("transform_type");

        String tone = "professional";
        String length = "medium";

        if (transformType.contains("professional") || transformType.contains("casual")) {
            tone = transformType;
        }
        if (transformType.contains("short") || transformType.contains("long")) {
            length = transformType;
        }

        String transformed = llmService.transformDraft(draft, tone, length);

        Map<String, Object> response = new HashMap<>();
        response.put("transformed", transformed);
        return response;
    }
}

