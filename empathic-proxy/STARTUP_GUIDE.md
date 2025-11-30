# Startup Guide

## Current Status

✅ **Ollama (Local LLM)** - Running on port 11434
⏳ **Spring Boot App** - Starting up (first run takes time to download dependencies)

## What's Happening

The Spring Boot app is currently starting in the background. On first run, Maven needs to:
1. Download all dependencies (~200MB)
2. Compile the Java code
3. Start the embedded Tomcat server

This can take **2-5 minutes** on first run.

## Monitor Startup

### Option 1: Check Status Script
```bash
./check-status.sh
```

### Option 2: Check Port
```bash
lsof -ti:8080 && echo "App is running!" || echo "Still starting..."
```

### Option 3: Test Endpoint
```bash
curl http://localhost:8080/oauth/install
```

## Once App is Running

### 1. Verify Configuration
```bash
export LLM_PROVIDER=localai
export SLACK_CLIENT_ID=your_client_id
export SLACK_SIGNING_SECRET=your_signing_secret
export SLACK_CLIENT_SECRET=your_client_secret
```

### 2. For Local Testing with ngrok

Start ngrok in a separate terminal:
```bash
ngrok http 8080
```

Then update your Slack app settings with the ngrok HTTPS URL:
- Event Subscriptions: `https://your-url.ngrok.io/events`
- Interactivity: `https://your-url.ngrok.io/interactivity`
- OAuth Redirect: `https://your-url.ngrok.io/oauth/callback`

### 3. Install the App

Visit: `http://localhost:8080/oauth/install` (or use ngrok URL)

### 4. Test the Integration

1. Open Slack
2. Navigate to your app's Home tab
3. Send a DM to the app or mention it in a channel
4. Check the Home tab inbox - you should see:
   - LLM-extracted action items
   - Suggested replies
   - Ability to compose and transform messages

## Troubleshooting

### App won't start
- Check Java version: `java -version` (need Java 17+)
- Check Maven: `mvn -version`
- Check for port conflicts: `lsof -ti:8080`

### Ollama not responding
```bash
docker-compose -f docker-compose.ollama.yml restart
docker exec empathic-proxy-ollama ollama list
```

### LLM not working
- Verify Ollama is running: `curl http://localhost:11434/api/tags`
- Check environment: `echo $LLM_PROVIDER` (should be "localai")
- Check app logs for errors

## Quick Commands

```bash
# Check everything
./check-status.sh

# Restart Ollama
docker-compose -f docker-compose.ollama.yml restart

# View Ollama logs
docker-compose -f docker-compose.ollama.yml logs -f

# Stop everything
docker-compose -f docker-compose.ollama.yml down
# (Spring Boot: Ctrl+C or kill the process)
```

