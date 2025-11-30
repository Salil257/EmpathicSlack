# Quick Start Guide

## Prerequisites Check

1. **Java 17+**: `java -version`
2. **Maven 3.6+**: `mvn -version`
3. **Slack App Created**: Follow README.md setup steps 1-6

## Environment Setup

Create a `.env` file or export these variables:

```bash
export SLACK_CLIENT_ID=your_client_id
export SLACK_CLIENT_SECRET=your_client_secret
export SLACK_SIGNING_SECRET=your_signing_secret
export OPENAI_API_KEY=your_openai_key  # Or use LocalAI
```

## Build & Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run
```

## Testing with ngrok

In a separate terminal:

```bash
ngrok http 8080
```

Use the ngrok HTTPS URL in your Slack app settings:
- Event Subscriptions: `https://your-ngrok-url.ngrok.io/events`
- Interactivity: `https://your-ngrok-url.ngrok.io/interactivity`
- OAuth Redirect: `https://your-ngrok-url.ngrok.io/oauth/callback`

## Install App

1. Visit: `http://localhost:8080/oauth/install` (or use ngrok URL)
2. Click "Install to Workspace"
3. Authorize the app

## Test the App

1. Open Slack
2. Navigate to the app (click app name in sidebar)
3. Go to **Home** tab - you should see your inbox
4. Mention the app or send a DM to test
5. Click "Reply" on an inbox item to open composer
6. Draft, transform, and post a message

## Troubleshooting

- **Maven build fails**: Check network/proxy settings, or use `mvn clean install -U`
- **Events not received**: Verify ngrok is running and URLs match Slack app settings
- **OAuth fails**: Check client ID/secret and redirect URL match exactly

