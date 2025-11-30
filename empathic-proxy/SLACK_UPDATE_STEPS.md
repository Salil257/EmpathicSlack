# Update Slack App Settings

## Your ngrok URL
**https://prologlike-neville-vicious.ngrok-free.dev**

## Step-by-Step Instructions

### 1. Go to Slack App Settings
Visit: https://api.slack.com/apps

Select your app: **Empathic Proxy**

---

### 2. Update OAuth & Permissions

1. Click **OAuth & Permissions** in the left sidebar
2. Scroll down to **Redirect URLs**
3. Click **Add New Redirect URL**
4. Enter: `https://prologlike-neville-vicious.ngrok-free.dev/oauth/callback`
5. Click **Save URLs**

---

### 3. Update Event Subscriptions

1. Click **Event Subscriptions** in the left sidebar
2. Find **Request URL**
3. Enter: `https://prologlike-neville-vicious.ngrok-free.dev/events`
4. Wait for the ✅ verification checkmark (may take a few seconds)
5. If verified, click **Save Changes**

**Note:** If verification fails, make sure:
- ngrok is running (`./setup-ngrok.sh` or `ngrok http 8080`)
- Spring Boot app is running
- The URL is exactly correct (https, no trailing slash)

---

### 4. Update Interactivity & Shortcuts

1. Click **Interactivity & Shortcuts** in the left sidebar
2. Toggle **Interactivity** to **On** (if not already)
3. Find **Request URL**
4. Enter: `https://prologlike-neville-vicious.ngrok-free.dev/interactivity`
5. Click **Save Changes**

---

### 5. Install the App

After updating all settings, install the app:

Visit: **https://prologlike-neville-vicious.ngrok-free.dev/oauth/install**

Click the install link and authorize the app to your workspace.

---

## Quick Checklist

- [ ] OAuth Redirect URL added: `https://prologlike-neville-vicious.ngrok-free.dev/oauth/callback`
- [ ] Event Subscriptions URL set: `https://prologlike-neville-vicious.ngrok-free.dev/events` (with ✅ verification)
- [ ] Interactivity URL set: `https://prologlike-neville-vicious.ngrok-free.dev/interactivity`
- [ ] App restarted with ngrok URL
- [ ] App installed via OAuth install page

---

## Troubleshooting

### Event Subscriptions won't verify
- Make sure ngrok is running: `curl http://localhost:4040/api/tunnels`
- Make sure Spring Boot app is running: `curl http://localhost:8080/oauth/install`
- Check ngrok web interface: http://localhost:4040 (shows all requests)
- Wait a few seconds and try again

### Redirect URI still doesn't match
- Double-check the URL in Slack settings matches exactly: `https://prologlike-neville-vicious.ngrok-free.dev/oauth/callback`
- Make sure you clicked "Save URLs" after adding it
- Restart the app with the correct `SLACK_REDIRECT_URL` environment variable

