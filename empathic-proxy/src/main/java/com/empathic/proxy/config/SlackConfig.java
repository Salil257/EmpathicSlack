package com.empathic.proxy.config;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

    @Value("${slack.client-id:}")
    private String clientId;

    @Value("${slack.client-secret:}")
    private String clientSecret;

    @Value("${slack.signing-secret:}")
    private String signingSecret;

    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }

    @Bean
    public String slackClientId() {
        return clientId;
    }

    @Bean
    public String slackClientSecret() {
        return clientSecret;
    }

    @Bean
    public String slackSigningSecret() {
        return signingSecret;
    }
}

