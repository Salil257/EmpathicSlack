# Install Empathic Proxy - Direct Method

## Current Status
✅ App is running with your Slack credentials
✅ OAuth endpoint is ready

## Install URL

**Visit this URL to install:**
```
http://localhost:8080/oauth/install
```

Or click the link in the install page.

## Important Note

For direct install to work, your Slack app's URLs must be configured to point to `http://localhost:8080`. However, **Slack's servers cannot reach localhost**, so you have two options:

### Option A: Use ngrok (Recommended)
1. Start ngrok: `ngrok http 8080`
2. Update Slack app URLs with ngrok HTTPS URL
3. Then install

### Option B: If URLs are already set to localhost
- This won't work for events/interactivity (Slack can't reach localhost)
- OAuth install might work if done from your browser
- But events won't be received

## Installation Steps

1. **Open the install URL** in your browser:
   ```
   http://localhost:8080/oauth/install
   ```

2. **Click "Click here to install the app to your workspace"**

3. **Authorize the app** in Slack

4. **You should see**: "Installation Successful! You can close this window."

5. **Open Slack** and navigate to the app

## After Installation

1. **Open Slack**
2. **Click "Empathic Proxy"** in the sidebar
3. **Go to Home tab** - you should see "Your Inbox"
4. **Test by sending a DM** or mentioning the app

## Verify Installation

Check if installation was successful:
```bash
sqlite3 empathic_proxy.db "SELECT team_id, bot_user_id, installed_at FROM team_installations;"
```

## Troubleshooting

### Installation fails
- Check app logs: `tail -f /tmp/empathic-proxy.log`
- Verify credentials are correct
- Check OAuth redirect URL matches in Slack app settings

### Events not received
- You'll need ngrok for events to work (Slack can't reach localhost)
- Start ngrok and update Slack app URLs

### Home tab empty
- Normal if no messages received yet
- Send a DM or mention the app to create inbox items

