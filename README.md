# Empathic Proxy - Slack-first MVP

A Slack App (bot) that consolidates mentions, DMs, and priority channel signals into an in-Slack "inbox" (Home tab), uses an LLM to extract concise actions and suggested replies, and provides a lightweight composer for drafting and transforming messages.

## Features

- **Inbox Consolidation**: Collects mentions, DMs into a unified Home tab inbox
- **LLM-Powered Analysis**: Extracts action items and suggests replies using OpenAI or LocalAI
- **Smart Composer**: Modal interface for drafting, transforming (tone/length), previewing, and posting messages
- **OAuth Installation**: Full OAuth 2.0 flow for Slack app installation
- **Event Subscriptions**: Handles `app_mention` and `message.im` events
- **Message Shortcuts**: Quick access to composer from message context
- **Audit Logging**: Simple audit trail for all actions
- **Minimal Storage**: SQLite database for persistence

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for LocalAI)
- Slack App configured (see Setup below)
- OpenAI API key **OR** LocalAI running in Docker (see LocalAI Setup below)

## Setup

### 1. Create a Slack App

1. Go to [api.slack.com/apps](https://api.slack.com/apps)
2. Click "Create New App" → "From scratch"
3. Name your app and select your workspace

### 2. Configure OAuth & Permissions

1. Navigate to **OAuth & Permissions** in the sidebar
2. Add the following **Bot Token Scopes** (least privilege):
   - `app_mentions:read` - Listen to app mentions
   - `channels:history` - Read channel messages
   - `chat:write` - Post messages
   - `im:history` - Read direct messages
   - `im:read` - Access direct messages
   - `im:write` - Send direct messages
   - `users:read` - Read user information
3. Add the following **User Token Scopes**:
   - `chat:write` - Post messages as user
4. Scroll to **Redirect URLs** and add: `http://localhost:8080/oauth/callback`
5. Save changes

### 3. Configure Event Subscriptions

1. Navigate to **Event Subscriptions**
2. Enable Events
3. Set **Request URL** to: `http://localhost:8080/events`
   - For local testing, use a tool like [ngrok](https://ngrok.com/) to expose your local server
   - Example: `ngrok http 8080` then use the provided URL
4. Subscribe to **bot events**:
   - `app_mentions` - When the app is mentioned
   - `message.im` - Direct messages to the app
5. Save changes

### 4. Configure Interactivity & Shortcuts

1. Navigate to **Interactivity & Shortcuts**
2. Enable Interactivity
3. Set **Request URL** to: `http://localhost:8080/interactivity`
4. Create a **Message Shortcut**:
   - Name: "Compose Reply"
   - Short Description: "Open composer for this message"
5. Save changes

### 5. Configure Home Tab

1. Navigate to **App Home**
2. Enable **Home Tab**
3. Save changes

### 6. Install App to Workspace

1. Navigate to **Install App** in the sidebar
2. Click "Install to Workspace"
3. Authorize the app
4. Copy the **Bot User OAuth Token** (starts with `xoxb-`)

### 7. Setup Local LLM (Optional but Recommended)

For local development, you can run a local LLM using Docker instead of using OpenAI:

#### Quick Setup (Recommended)

```bash
# Make setup script executable
chmod +x setup-local-llm.sh

# Run the setup script
./setup-local-llm.sh
```

This will:
- Start LocalAI in Docker
- Download a lightweight model (TinyLlama, ~600MB)
- Configure it to run on port 8081
- Test the connection

#### Manual Setup

```bash
# Start LocalAI with Docker Compose
docker-compose -f docker-compose.simple.yml up -d

# Check logs to see when it's ready
docker-compose -f docker-compose.simple.yml logs -f

# Test the API
curl -X POST http://localhost:8081/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [{"role": "user", "content": "Say hello"}],
    "max_tokens": 10
  }'
```

#### Stop LocalAI

```bash
docker-compose -f docker-compose.simple.yml down
```

### 8. Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Slack Configuration
SLACK_CLIENT_ID=your_client_id
SLACK_CLIENT_SECRET=your_client_secret
SLACK_SIGNING_SECRET=your_signing_secret
SLACK_SOCKET_MODE=false
SLACK_APP_TOKEN=your_app_token  # Only if using Socket Mode

# LLM Configuration
# Option 1: Use LocalAI (recommended for local dev)
LLM_PROVIDER=localai
LOCALAI_BASE_URL=http://localhost:8081/v1

# Option 2: Use OpenAI
# LLM_PROVIDER=openai
# OPENAI_API_KEY=your_openai_api_key
# OPENAI_MODEL=gpt-4o-mini
```

### 9. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR
java -jar target/empathic-proxy-1.0.0.jar
```

The server will start on `http://localhost:8080`

## LocalAI Model Options

The default setup uses **TinyLlama** (~600MB), which is fast but basic. For better quality, you can edit `localai-config/models.yaml` to use:

- **TinyLlama** (default): Fast, ~600MB, good for testing
- **Phi-2**: Better quality, ~1.6GB, still fast
- **Llama 2 7B**: Best quality, ~4GB, slower

To change models, edit `localai-config/models.yaml` and restart:
```bash
docker-compose -f docker-compose.simple.yml restart
```

## Local Testing with ngrok

Since Slack needs to reach your local server, use ngrok:

```bash
# Install ngrok (if not already installed)
# macOS: brew install ngrok
# Or download from https://ngrok.com/

# Start ngrok tunnel
ngrok http 8080

# Use the provided HTTPS URL (e.g., https://abc123.ngrok.io) in your Slack app configuration:
# - Event Subscriptions Request URL: https://abc123.ngrok.io/events
# - Interactivity Request URL: https://abc123.ngrok.io/interactivity
# - OAuth Redirect URL: https://abc123.ngrok.io/oauth/callback
```

## Usage

### Installing the App

1. Visit: `http://localhost:8080/oauth/install` (or create a simple install page)
2. Or use the OAuth URL from your Slack app settings:
   ```
   https://slack.com/oauth/v2/authorize?client_id=YOUR_CLIENT_ID&scope=app_mentions:read,channels:history,chat:write,im:history,im:read,im:write,users:read&user_scope=chat:write
   ```

### Using the Inbox

1. Open your Slack workspace
2. Click on the app name in the sidebar
3. Navigate to the **Home** tab
4. View all unprocessed mentions and DMs
5. Click "Reply" on any item to open the composer

### Using the Composer

1. Click "Reply" on an inbox item or use a message shortcut
2. Draft your message in the text area
3. Use transform buttons to adjust tone (Professional/Casual) or length (Short/Long)
4. Click "Preview" to review before posting
5. Click "Post" to send the message to the original channel/thread

## Architecture

- **Controllers**: Handle HTTP requests (OAuth, Events, Interactivity, Home, Composer)
- **Services**: Business logic (Inbox, LLM, Slack API, Audit)
- **Models**: JPA entities (InboxItem, AuditLog, TeamInstallation)
- **Repositories**: Data access layer
- **Config**: Spring configuration for Slack SDK

## Database

The app uses SQLite (`empathic_proxy.db`) for persistence:
- `inbox_items`: Stores mentions, DMs, and priority signals
- `audit_logs`: Audit trail of all actions
- `team_installations`: OAuth tokens per workspace

## Security

- **Least Privilege Scopes**: Only requests necessary Slack permissions
- **Preview Before Posting**: Users can preview messages before sending
- **Audit Logging**: All actions are logged for accountability
- **Signing Secret Validation**: (To be implemented for production)

## LLM Providers

### LocalAI (Recommended for Local Development)

1. Start LocalAI using Docker (see Setup step 7 above)
2. Set `LLM_PROVIDER=localai` in your environment
3. The default `LOCALAI_BASE_URL` is `http://localhost:8081/v1`

**Advantages:**
- No API costs
- Works offline
- Privacy (data stays local)
- Good for development/testing

**Note:** Local models are smaller and may have lower quality than OpenAI. For production, consider using OpenAI or a larger local model.

### OpenAI (Production)

1. Get an API key from [OpenAI](https://platform.openai.com/api-keys)
2. Set `LLM_PROVIDER=openai`
3. Set `OPENAI_API_KEY=your_key`
4. Optionally set `OPENAI_MODEL` (default: `gpt-4o-mini`)

**Advantages:**
- Higher quality responses
- Faster inference
- Better for production workloads

## Troubleshooting

### Events not received

- Verify ngrok is running and the URL is correct
- Check Slack app Event Subscriptions configuration
- Review server logs for errors

### OAuth callback fails

- Verify redirect URL matches in Slack app settings
- Check that client ID and secret are correct
- Review server logs for detailed error messages

### LLM not working

- Verify API key is set correctly
- For OpenAI: Check API key validity and quota
- For LocalAI: Ensure the service is running and accessible

## Development

### Project Structure

```
src/main/java/com/empathic/proxy/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── model/           # JPA entities
├── repository/      # Data repositories
└── service/         # Business logic services
```

### Adding Features

- New event types: Add handlers in `EventsController`
- New interactivity: Extend `InteractivityController`
- New LLM features: Extend `LLMService` and provider implementations

## License

MIT

