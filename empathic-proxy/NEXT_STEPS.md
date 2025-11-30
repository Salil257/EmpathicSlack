# Next Steps - Quick Reference

## ✅ Current Status
- ✅ Spring Boot app running on port 8080
- ✅ Ollama LLM running on port 11434
- ✅ Manifest created and validated
- ✅ All code compiled successfully
- ⚠️  Need Slack credentials

## 🚀 Quick Start

### 1. Create Slack App (2 minutes)

**Option A: Use Manifest (Easiest)**
```bash
# Copy manifest
cat slack-manifest.json

# Go to: https://api.slack.com/apps
# Click: "Create New App" → "From an app manifest"
# Paste manifest → Create
```

**Option B: Manual Setup**
- Follow README.md setup steps 1-6

### 2. Get Credentials (1 minute)

From your Slack app:
- **Signing Secret**: Basic Information → App Credentials
- **Client ID**: OAuth & Permissions
- **Client Secret**: OAuth & Permissions

### 3. Set Environment & Restart (1 minute)

```bash
# Stop current app
pkill -f "spring-boot:run"

# Set credentials
export SLACK_CLIENT_ID=your_client_id
export SLACK_CLIENT_SECRET=your_client_secret
export SLACK_SIGNING_SECRET=your_signing_secret
export LLM_PROVIDER=localai

# Restart
mvn spring-boot:run
```

### 4. Setup ngrok (1 minute)

```bash
# In new terminal
ngrok http 8080

# Copy HTTPS URL (e.g., https://abc123.ngrok.io)
```

### 5. Update Slack App URLs

In your Slack app settings, update:
- **Event Subscriptions**: `https://your-ngrok-url.ngrok.io/events`
- **Interactivity**: `https://your-ngrok-url.ngrok.io/interactivity`
- **OAuth Redirect**: `https://your-ngrok-url.ngrok.io/oauth/callback`

### 6. Install & Test (2 minutes)

1. Visit: `https://your-ngrok-url.ngrok.io/oauth/install`
2. Install to workspace
3. Open Slack → Click app → Home tab
4. Send DM or mention: `@Empathic Proxy Hello!`
5. Check Home tab for inbox item with LLM-generated action/reply

## 📝 Testing Checklist

- [ ] App installs successfully
- [ ] Home tab shows inbox
- [ ] App mention creates inbox item
- [ ] DM creates inbox item
- [ ] LLM generates action and reply
- [ ] Composer opens from inbox
- [ ] Message shortcut works
- [ ] Can post messages

## 🔧 Useful Commands

```bash
# Check status
./check-status.sh

# View app logs
tail -f /tmp/empathic-proxy.log

# Test Ollama
curl -X POST http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model": "llama3.2:1b", "messages": [{"role": "user", "content": "Test"}], "max_tokens": 10}'

# Check database
sqlite3 empathic_proxy.db "SELECT * FROM inbox_items;"

# Restart Ollama
docker-compose -f docker-compose.ollama.yml restart
```

## 📚 Documentation

- **README.md** - Full setup guide
- **TESTING_GUIDE.md** - Detailed testing steps
- **MANIFEST_SETUP.md** - Manifest configuration
- **LOCAL_LLM_SUMMARY.md** - LLM setup details

## 🎯 You're Ready!

Everything is built and running. Just need to:
1. Create Slack app (use manifest)
2. Add credentials
3. Start ngrok
4. Test!

