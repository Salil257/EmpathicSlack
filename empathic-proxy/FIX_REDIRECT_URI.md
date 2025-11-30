# Fix Redirect URI Mismatch

## Error
```
redirect_uri did not match any configured URIs. Passed URI: http://localhost:8080/oauth/callback
```

## Solution

You need to add the redirect URI to your Slack app settings.

### Step 1: Go to Slack App Settings

1. Visit: https://api.slack.com/apps
2. Select your app (Empathic Proxy)
3. Click **"OAuth & Permissions"** in the sidebar
4. Scroll down to **"Redirect URLs"** section

### Step 2: Add Redirect URI

Click **"Add New Redirect URL"** and add:

**For localhost:**
```
http://localhost:8080/oauth/callback
```

**For ngrok (if using):**
```
https://your-ngrok-url.ngrok.io/oauth/callback
```

### Step 3: Save Changes

Click **"Save URLs"** at the bottom

### Step 4: Restart App (if needed)

The redirect URI is now configurable. You can set it via environment variable:

```bash
export SLACK_REDIRECT_URL=http://localhost:8080/oauth/callback
```

Or it will default to `http://localhost:8080/oauth/callback`

## Verify

After adding the redirect URI in Slack:
1. Try installing again: http://localhost:8080/oauth/install
2. The OAuth flow should complete successfully

## Important Notes

- The redirect URI must match **EXACTLY** (including http vs https, port, path)
- You can add multiple redirect URIs (localhost for dev, ngrok for testing)
- After adding, wait a few seconds for Slack to update

