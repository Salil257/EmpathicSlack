#!/bin/bash

echo "🔍 Checking Empathic Proxy Status..."
echo ""

# Check Ollama
echo "1. Ollama (Local LLM):"
if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "   ✅ Running on port 11434"
    docker ps | grep ollama | head -1
else
    echo "   ❌ Not running"
    echo "   Start with: docker-compose -f docker-compose.ollama.yml up -d"
fi
echo ""

# Check Spring Boot App
echo "2. Spring Boot App:"
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "   ✅ Running on port 8080"
    if curl -s http://localhost:8080/oauth/install > /dev/null 2>&1; then
        echo "   ✅ HTTP endpoint responding"
        echo ""
        echo "   📍 Access points:"
        echo "   - OAuth Install: http://localhost:8080/oauth/install"
        echo "   - Events: http://localhost:8080/events"
        echo "   - Interactivity: http://localhost:8080/interactivity"
        echo "   - Home: http://localhost:8080/home"
    else
        echo "   ⚠️  Port open but endpoint not responding yet"
    fi
else
    echo "   ⏳ Starting up (Maven may still be downloading dependencies)"
    echo "   Check logs or wait a bit longer"
fi
echo ""

# Check environment
echo "3. Environment:"
if [ -n "$LLM_PROVIDER" ]; then
    echo "   ✅ LLM_PROVIDER=$LLM_PROVIDER"
else
    echo "   ⚠️  LLM_PROVIDER not set (should be 'localai')"
    echo "   Set with: export LLM_PROVIDER=localai"
fi
echo ""

# Check Slack config
echo "4. Slack Configuration:"
if [ -n "$SLACK_CLIENT_ID" ] && [ -n "$SLACK_CLIENT_SECRET" ]; then
    echo "   ✅ Slack credentials configured"
else
    echo "   ⚠️  Slack credentials not set"
    echo "   Set: SLACK_CLIENT_ID, SLACK_CLIENT_SECRET, SLACK_SIGNING_SECRET"
fi
echo ""

echo "📝 Next Steps:"
echo "   1. Wait for Spring Boot to finish starting (check port 8080)"
echo "   2. Configure Slack app with ngrok URL (if testing locally)"
echo "   3. Install app via: http://localhost:8080/oauth/install"
echo "   4. Test by sending a DM or mentioning the app in Slack"

