# Slack App Manifest Setup

## Using the Manifest

### Option 1: Create New App from Manifest

1. Go to [api.slack.com/apps](https://api.slack.com/apps)
2. Click **"Create New App"**
3. Select **"From an app manifest"**
4. Choose your workspace
5. Copy and paste the contents of `slack-manifest.json`
6. Review and create

### Option 2: Update Existing App

1. Go to [api.slack.com/apps](https://api.slack.com/apps)
2. Select your app
3. Go to **"App Manifest"** in the sidebar
4. Click **"Update"**
5. Paste the manifest JSON
6. Save changes

## Manifest Files

- **`slack-manifest.json`** - For localhost testing
- **`slack-manifest-ngrok.json`** - Template for ngrok (replace YOUR-NGROK-URL)

## For ngrok Setup

1. Start ngrok:
   ```bash
   ngrok http 8080
   ```

2. Copy the HTTPS URL (e.g., `https://abc123.ngrok.io`)

3. Update `slack-manifest-ngrok.json`:
   - Replace `YOUR-NGROK-URL.ngrok.io` with your actual ngrok URL
   - In all three places:
     - `redirect_urls`
     - `event_subscriptions.request_url`
     - `interactivity.request_url`

4. Use the updated manifest in your Slack app

## Required Scopes Explained

### Bot Token Scopes
- `app_mentions:read` - Listen when app is mentioned
- `channels:history` - Read channel messages
- `chat:write` - Post messages as bot
- `im:history` - Read direct messages
- `im:read` - Access DM channels
- `im:write` - Send direct messages
- `users:read` - Get user information

### User Token Scopes
- `chat:write` - Post messages as the user (for composer)

## After Creating/Updating

1. Go to **"Install App"** in the sidebar
2. Click **"Install to Workspace"**
3. Copy the **Bot User OAuth Token** (starts with `xoxb-`)
4. Copy the **Signing Secret** from **"Basic Information"**
5. Copy the **Client ID** and **Client Secret** from **"OAuth & Permissions"**

Set these as environment variables:
```bash
export SLACK_CLIENT_ID=your_client_id
export SLACK_CLIENT_SECRET=your_client_secret
export SLACK_SIGNING_SECRET=your_signing_secret
```

Then restart your Spring Boot app.

