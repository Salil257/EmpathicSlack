# Install and Test Empathic Proxy

## Prerequisites Check

Before installing, make sure:
- ✅ ngrok is running
- ✅ Spring Boot app is running
- ✅ Slack app settings are updated with ngrok URLs

## Step 1: Install the App

### Option A: Direct Install Link
Visit this URL in your browser:
```
https://prologlike-neville-vicious.ngrok-free.dev/oauth/install
```

### Option B: Check Status First
Run:
```bash
./check-status.sh
```

## Step 2: Authorize the App

1. Click the install link
2. You'll be redirected to Slack's authorization page
3. Review the permissions requested
4. Click **"Allow"** to install the app to your workspace

## Step 3: Verify Installation

After installation, you should see:
- ✅ Success page: "Installation Successful!"
- ✅ App appears in your Slack workspace
- ✅ You can open the app's Home tab

## Step 4: Test Features

### Test 1: Home Tab (Inbox)
1. Open Slack
2. Click on **"Empathic Proxy"** in the Apps section (or search for it)
3. Click the **"Home"** tab
4. You should see your inbox (may be empty initially)

### Test 2: Direct Message
1. Send a DM to **@Empathic Proxy** (the bot)
2. Example: "Hey, can you help me with this task?"
3. Check the Home tab - the message should appear in your inbox
4. You should see:
   - The message content
   - An extracted action (if LLM processed it)
   - A suggested reply

### Test 3: App Mention
1. In any channel, mention the bot: `@Empathic Proxy`
2. Send a message: `@Empathic Proxy Can you review this document?`
3. Check the Home tab - the mention should appear in your inbox

### Test 4: Message Shortcut (Composer)
1. Right-click on any message in Slack
2. Select **"Compose Reply"** from the shortcuts menu
3. A modal should open with:
   - The original message
   - A text input for your draft
   - Buttons to transform, preview, and post

### Test 5: Transform Draft
1. In the composer modal, type a draft message
2. Click **"Transform"** button
3. Select a transformation (e.g., "Make it more professional")
4. The draft should be updated with the transformed text

### Test 6: Post Reply
1. In the composer modal, write or transform a draft
2. Click **"Preview"** to see how it will look
3. Click **"Post"** to send the reply
4. The reply should appear in the thread/channel

## Troubleshooting

### Installation Fails
- **Error: redirect_uri mismatch**
  - Make sure you added the exact ngrok URL to Slack OAuth settings
  - Check: https://api.slack.com/apps → OAuth & Permissions → Redirect URLs

- **Error: Invalid client**
  - Verify environment variables are set correctly
  - Check: `echo $SLACK_CLIENT_ID`

### Home Tab is Empty
- This is normal if you haven't received any DMs or mentions yet
- Send a test DM or mention to populate the inbox

### Events Not Appearing
- Check Event Subscriptions URL is verified in Slack settings
- Check ngrok web interface: http://localhost:4040
- Look for incoming requests from Slack

### Composer Shortcut Not Working
- Make sure Interactivity URL is set in Slack settings
- Check the shortcut callback_id matches: "compose_reply"
- Verify the app has the required scopes

### LLM Not Working
- Check if Ollama is running: `docker ps | grep ollama`
- Check LLM service logs in Spring Boot output
- Verify `LLM_PROVIDER=localai` is set

## Quick Test Checklist

- [ ] App installed successfully
- [ ] Home tab opens and displays
- [ ] DM to bot appears in inbox
- [ ] App mention appears in inbox
- [ ] Message shortcut opens composer
- [ ] Transform draft works
- [ ] Post reply works
- [ ] LLM generates actions and suggestions

## Next Steps

After successful testing:
1. Try using the app with real messages
2. Test with multiple team members
3. Monitor the inbox for mentions and DMs
4. Use the composer for quick replies

