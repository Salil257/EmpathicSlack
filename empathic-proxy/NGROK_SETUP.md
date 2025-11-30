# ngrok Setup Guide

## Why ngrok?

Slack's servers need to reach your app, but they can't access `localhost`. ngrok creates a public HTTPS URL that tunnels to your local server.

## Install ngrok

### Option 1: Homebrew (macOS)
```bash
brew install ngrok
```

### Option 2: Direct Download
1. Visit: https://ngrok.com/download
2. Download for macOS
3. Extract and add to PATH, or use directly

### Option 3: Using ngrok CLI
```bash
# If you have ngrok account
ngrok config add-authtoken YOUR_AUTH_TOKEN
```

## Start ngrok

```bash
ngrok http 8080
```

This will:
- Create a public HTTPS URL (e.g., `https://abc123.ngrok.io`)
- Tunnel all traffic to `http://localhost:8080`
- Show a web interface at `http://localhost:4040`

## Get Your ngrok URL

After starting ngrok, you'll see output like:
```
Forwarding   https://abc123.ngrok.io -> http://localhost:8080
```

Copy the HTTPS URL (the one starting with `https://`)

## Update Slack App Settings

### 1. OAuth & Permissions
- Go to: https://api.slack.com/apps → Your App → OAuth & Permissions
- Scroll to **Redirect URLs**
- Click **Add New Redirect URL**
- Add: `https://your-ngrok-url.ngrok.io/oauth/callback`
- Click **Save URLs**

### 2. Event Subscriptions
- Go to: Event Subscriptions
- Set **Request URL** to: `https://your-ngrok-url.ngrok.io/events`
- Wait for Slack to verify (should show ✅)
- Save changes

### 3. Interactivity & Shortcuts
- Go to: Interactivity & Shortcuts
- Set **Request URL** to: `https://your-ngrok-url.ngrok.io/interactivity`
- Save changes

## Update App Configuration

After getting your ngrok URL, restart the app with:

```bash
# Stop current app
pkill -f "spring-boot:run"

# Set redirect URL
export SLACK_REDIRECT_URL=https://your-ngrok-url.ngrok.io/oauth/callback

# Restart with all credentials
export SLACK_CLIENT_ID=10013814890389.10017287381410
export SLACK_CLIENT_SECRET=8093413e2fa41b3fdb1ca5935ff4f193
export SLACK_SIGNING_SECRET=1e0ed2256d57dd21e7ae516c769b55b0
export LLM_PROVIDER=localai

mvn spring-boot:run
```

## Verify Setup

1. **Check ngrok is running:**
   ```bash
   curl http://localhost:4040/api/tunnels
   ```

2. **Test ngrok URL:**
   ```bash
   curl https://your-ngrok-url.ngrok.io/oauth/install
   ```

3. **Install app:**
   ```
   https://your-ngrok-url.ngrok.io/oauth/install
   ```

## Quick Setup Script

Use the provided script:
```bash
./setup-ngrok.sh
```

This will:
- Check if ngrok is installed
- Start ngrok if not running
- Display your ngrok URL
- Show instructions for updating Slack settings

## Important Notes

- **Free ngrok URLs change** each time you restart ngrok (unless you have a paid plan)
- **Keep ngrok running** while testing
- **Update Slack URLs** if you restart ngrok and get a new URL
- **ngrok web interface**: http://localhost:4040 (shows all requests)

## Troubleshooting

### ngrok not found
```bash
# Install with Homebrew
brew install ngrok

# Or download from ngrok.com
```

### Can't connect to ngrok
- Check ngrok is running: `pgrep -f ngrok`
- Check port 4040: `lsof -ti:4040`
- Restart ngrok: `pkill ngrok && ngrok http 8080`

### Slack can't verify URL
- Make sure ngrok is running
- Check the URL is exactly correct (https, no trailing slash except path)
- Wait a few seconds for Slack to verify
- Check ngrok web interface for incoming requests

