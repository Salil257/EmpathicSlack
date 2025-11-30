package com.empathic.proxy.controller;

import com.empathic.proxy.model.AuditLog;
import com.empathic.proxy.model.TeamInstallation;
import com.empathic.proxy.repository.TeamInstallationRepository;
import com.empathic.proxy.service.AuditService;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.oauth.OAuthV2AccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final Slack slack;
    private final TeamInstallationRepository installationRepository;
    private final AuditService auditService;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;

    public OAuthController(Slack slack, 
                          TeamInstallationRepository installationRepository,
                          AuditService auditService,
                          @Value("${slack.client-id:}") String clientId,
                          @Value("${slack.client-secret:}") String clientSecret,
                          @Value("${slack.redirect-url:http://localhost:8080/oauth/callback}") String redirectUrl) {
        this.slack = slack;
        this.installationRepository = installationRepository;
        this.auditService = auditService;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
    }

    @GetMapping("/install")
    public ResponseEntity<String> install() {
        if (clientId == null || clientId.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("Slack Client ID not configured. Please set SLACK_CLIENT_ID environment variable.");
        }

        String scopes = "app_mentions:read,channels:history,chat:write,im:history,im:read,im:write,users:read";
        String userScopes = "chat:write";
        
        String installUrl = String.format(
            "https://slack.com/oauth/v2/authorize?client_id=%s&scope=%s&user_scope=%s&redirect_uri=%s",
            clientId, scopes, userScopes, redirectUrl
        );

        return ResponseEntity.ok(String.format(
            "<html><body><h1>Install Empathic Proxy</h1>" +
            "<p><a href=\"%s\">Click here to install the app to your workspace</a></p>" +
            "</body></html>",
            installUrl
        ));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String code,
                                          @RequestParam(required = false) String error) {
        if (error != null) {
            return ResponseEntity.badRequest()
                .body("<html><body><h1>Installation Failed</h1><p>Error: " + error + "</p></body></html>");
        }

        if (code == null) {
            return ResponseEntity.badRequest()
                .body("<html><body><h1>Installation Failed</h1><p>No authorization code provided.</p></body></html>");
        }

        try {
            OAuthV2AccessResponse response = slack.methods().oauthV2Access(r -> r
                .clientId(clientId)
                .clientSecret(clientSecret)
                .code(code)
            );

            if (response.isOk()) {
                String teamId = response.getTeam().getId();
                String accessToken = response.getAccessToken();
                String botUserId = response.getBotUserId();

                // Check if already installed
                installationRepository.findByTeamId(teamId).ifPresent(installationRepository::delete);

                TeamInstallation installation = new TeamInstallation();
                installation.setTeamId(teamId);
                installation.setAccessToken(accessToken);
                installation.setBotUserId(botUserId);
                installationRepository.save(installation);

                auditService.log("system", teamId, AuditLog.ActionType.OAUTH_INSTALLED,
                               "system", null, "App installed to workspace");

                return ResponseEntity.ok(
                    "<html><body><h1>Installation Successful!</h1>" +
                    "<p>The app has been installed to your workspace. You can close this window.</p>" +
                    "<p>Open Slack and navigate to the app's Home tab to see your inbox.</p>" +
                    "</body></html>"
                );
            } else {
                return ResponseEntity.badRequest()
                    .body("<html><body><h1>Installation Failed</h1><p>" + response.getError() + "</p></body></html>");
            }
        } catch (IOException | SlackApiException e) {
            return ResponseEntity.internalServerError()
                .body("<html><body><h1>Installation Error</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }
}

