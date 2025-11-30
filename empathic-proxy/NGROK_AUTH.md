# ngrok Authentication Required

ngrok needs an authentication token to work. Here's how to set it up:

## Step 1: Get Your ngrok Auth Token

1. **Sign up or log in** to ngrok:
   - Visit: https://dashboard.ngrok.com/signup
   - Or log in: https://dashboard.ngrok.com/login

2. **Get your authtoken**:
   - After logging in, go to: https://dashboard.ngrok.com/get-started/your-authtoken
   - Copy your authtoken (it looks like: `2abc123def456ghi789jkl012mno345pqr678stu901vwx234yz_5A6B7C8D9E0F1G2H3I4J5K`)

## Step 2: Configure ngrok

Run this command with your authtoken:

```bash
ngrok config add-authtoken YOUR_AUTH_TOKEN_HERE
```

Replace `YOUR_AUTH_TOKEN_HERE` with the token you copied.

## Step 3: Start ngrok

After authentication, you can start ngrok:

```bash
ngrok http 8080
```

You'll see output like:
```
Session Status                online
Account                       your-email@example.com
Forwarding                    https://abc123.ngrok.io -> http://localhost:8080
```

**Copy the HTTPS URL** (e.g., `https://abc123.ngrok.io`)

## Step 4: Update Slack & Restart App

Once you have your ngrok URL, follow the steps in `QUICK_FIX_REDIRECT.md`:

1. Update Slack app settings with your ngrok URL
2. Restart app with: `export SLACK_REDIRECT_URL=https://your-url.ngrok.io/oauth/callback`

## Quick Command Reference

```bash
# 1. Authenticate (one-time setup)
ngrok config add-authtoken YOUR_TOKEN

# 2. Start ngrok
ngrok http 8080

# 3. Copy HTTPS URL from output

# 4. Update Slack settings and restart app
```

## Alternative: Use ngrok in Background

If you want to run ngrok in the background:

```bash
# Authenticate first (one-time)
ngrok config add-authtoken YOUR_TOKEN

# Start in background
nohup ngrok http 8080 > /tmp/ngrok.log 2>&1 &

# Get URL
sleep 3
curl -s http://localhost:4040/api/tunnels | python3 -c "import sys, json; data = json.load(sys.stdin); tunnels = [t for t in data.get('tunnels', []) if t.get('proto') == 'https']; print(tunnels[0]['public_url'] if tunnels else 'Not ready')"
```

## Free ngrok Account

- **Free accounts** are available
- **No credit card required** for basic usage
- **URLs change** when you restart (unless you have a paid plan with reserved domains)

