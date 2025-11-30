package com.empathic.proxy.service;

import com.empathic.proxy.model.TeamInstallation;
import com.empathic.proxy.repository.TeamInstallationRepository;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class SlackService {

    private final Slack slack;
    private final TeamInstallationRepository installationRepository;

    public SlackService(Slack slack, TeamInstallationRepository installationRepository) {
        this.slack = slack;
        this.installationRepository = installationRepository;
    }

    public MethodsClient getClient(String teamId) {
        Optional<TeamInstallation> installation = installationRepository.findByTeamId(teamId);
        if (installation.isEmpty()) {
            throw new RuntimeException("Team not installed: " + teamId);
        }
        return slack.methods(installation.get().getAccessToken());
    }

    public void postMessage(String teamId, String channelId, String threadTs, String text) 
            throws IOException, SlackApiException {
        MethodsClient client = getClient(teamId);
        
        ChatPostMessageRequest.ChatPostMessageRequestBuilder builder = ChatPostMessageRequest.builder()
            .channel(channelId)
            .text(text);
        
        // Only add threadTs if it's different from the message timestamp (i.e., it's actually a thread)
        if (threadTs != null && !threadTs.isEmpty()) {
            builder.threadTs(threadTs);
        }

        ChatPostMessageResponse response = client.chatPostMessage(builder.build());
        if (!response.isOk()) {
            throw new RuntimeException("Failed to post message: " + response.getError());
        }
    }
}

