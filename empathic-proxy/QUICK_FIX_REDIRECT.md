# Quick Fix: Redirect URI Error

## The Problem
```
redirect_uri did not match any configured URIs. Passed URI: http://localhost:8080/oauth/callback
```

Slack's servers can't reach `localhost` - they need a public HTTPS URL.

## The Solution: Use ngrok

### Step 1: Start ngrok

```bash
# Install if needed
brew install ngrok

# Start ngrok (exposes localhost:8080)
ngrok http 8080
```

You'll see output like:
```
Forwarding   https://abc123.ngrok.io -> http://localhost:8080
```

**Copy the HTTPS URL** (e.g., `https://abc123.ngrok.io`)

### Step 2: Update Slack App Settings

Go to: https://api.slack.com/apps → Your App

#### a) OAuth & Permissions
- Click **OAuth & Permissions** in sidebar
- Scroll to **Redirect URLs**
- Click **Add New Redirect URL**
- Enter: `https://your-ngrok-url.ngrok.io/oauth/callback`
- Click **Save URLs**

#### b) Event Subscriptions
- Click **Event Subscriptions** in sidebar
- Set **Request URL** to: `https://your-ngrok-url.ngrok.io/events`
- Wait for ✅ verification checkmark
- Save changes

#### c) Interactivity & Shortcuts
- Click **Interactivity & Shortcuts** in sidebar
- Set **Request URL** to: `https://your-ngrok-url.ngrok.io/interactivity`
- Save changes

### Step 3: Restart App with ngrok URL

```bash
# Stop current app (if running)
pkill -f 'spring-boot:run'

# Set environment variables with ngrok URL
export SLACK_CLIENT_ID=10013814890389.10017287381410
export SLACK_CLIENT_SECRET=8093413e2fa41b3fdb1ca5935ff4f193
export SLACK_SIGNING_SECRET=1e0ed2256d57dd21e7ae516c769b55b0
export SLACK_REDIRECT_URL=https://your-ngrok-url.ngrok.io/oauth/callback
export LLM_PROVIDER=localai

# Restart app
mvn spring-boot:run
```

### Step 4: Install App

Visit: `https://your-ngrok-url.ngrok.io/oauth/install`

Click the install link and authorize the app.

## Automated Setup

Or use the setup script:

```bash
./setup-ngrok.sh
```

This will:
- Check if ngrok is installed
- Start ngrok automatically
- Show your ngrok URL
- Display all the steps above

## Important Notes

- **Keep ngrok running** while testing
- **Free ngrok URLs change** when you restart (unless you have a paid plan)
- **Update Slack URLs** if you restart ngrok and get a new URL
- **ngrok web interface**: http://localhost:4040 (shows all requests)

## Troubleshooting

### ngrok needs authentication
```bash
ngrok config add-authtoken YOUR_TOKEN
```
Get token from: https://dashboard.ngrok.com/get-started/your-authtoken

### Can't get ngrok URL
- Check ngrok is running: `pgrep -f ngrok`
- Visit: http://localhost:4040 (web interface)
- Try starting manually: `ngrok http 8080`

### Slack can't verify URL
- Make sure ngrok is running
- Check URL is exactly correct (https, correct path)
- Wait a few seconds for verification
- Check ngrok web interface for incoming requests

