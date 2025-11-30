#!/bin/bash

# Setup ngrok and configure redirect URI

echo "🚀 Setting up ngrok for Empathic Proxy..."
echo ""

# Check if ngrok is installed
if ! command -v ngrok &> /dev/null; then
    echo "❌ ngrok is not installed"
    echo ""
    echo "Install with:"
    echo "  brew install ngrok"
    echo ""
    echo "Or download from: https://ngrok.com/download"
    echo ""
    echo "After installing, you may need to authenticate:"
    echo "  ngrok config add-authtoken YOUR_TOKEN"
    echo "  (Get token from: https://dashboard.ngrok.com/get-started/your-authtoken)"
    exit 1
fi

# Check if ngrok is already running
if pgrep -f "ngrok http" > /dev/null; then
    echo "✅ ngrok is already running"
else
    echo "📡 Starting ngrok on port 8080..."
    echo "   (This will run in the background)"
    nohup ngrok http 8080 > /tmp/ngrok.log 2>&1 &
    sleep 5
fi

# Get ngrok URL
NGROK_URL=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    tunnels = data.get('tunnels', [])
    https_tunnels = [t for t in tunnels if t.get('proto') == 'https']
    if https_tunnels:
        print(https_tunnels[0]['public_url'])
except Exception as e:
    pass
" 2>/dev/null)

if [ -z "$NGROK_URL" ]; then
    echo "⏳ Waiting for ngrok to initialize..."
    sleep 3
    NGROK_URL=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    tunnels = data.get('tunnels', [])
    https_tunnels = [t for t in tunnels if t.get('proto') == 'https']
    if https_tunnels:
        print(https_tunnels[0]['public_url'])
except Exception as e:
    pass
" 2>/dev/null)
fi

if [ -n "$NGROK_URL" ]; then
    echo ""
    echo "✅ ngrok is running!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📋 Your ngrok HTTPS URL: $NGROK_URL"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "🔧 STEP 1: Update Slack App Settings"
    echo ""
    echo "   Go to: https://api.slack.com/apps"
    echo "   Select your app: Empathic Proxy"
    echo ""
    echo "   a) OAuth & Permissions → Redirect URLs"
    echo "      Add: ${NGROK_URL}/oauth/callback"
    echo "      Click 'Save URLs'"
    echo ""
    echo "   b) Event Subscriptions → Request URL"
    echo "      Set to: ${NGROK_URL}/events"
    echo "      Wait for ✅ verification"
    echo ""
    echo "   c) Interactivity & Shortcuts → Request URL"
    echo "      Set to: ${NGROK_URL}/interactivity"
    echo "      Click 'Save Changes'"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🔧 STEP 2: Restart App with ngrok URL"
    echo ""
    echo "   Stop current app (Ctrl+C or):"
    echo "   pkill -f 'spring-boot:run'"
    echo ""
    echo "   Then restart with:"
    echo ""
    echo "   export SLACK_CLIENT_ID=10013814890389.10017287381410"
    echo "   export SLACK_CLIENT_SECRET=8093413e2fa41b3fdb1ca5935ff4f193"
    echo "   export SLACK_SIGNING_SECRET=1e0ed2256d57dd21e7ae516c769b55b0"
    echo "   export SLACK_REDIRECT_URL=${NGROK_URL}/oauth/callback"
    echo "   export LLM_PROVIDER=localai"
    echo "   mvn spring-boot:run"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🔧 STEP 3: Install App"
    echo ""
    echo "   Visit: ${NGROK_URL}/oauth/install"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "🌐 ngrok web interface: http://localhost:4040"
    echo "   (Shows all incoming requests in real-time)"
    echo ""
else
    echo "❌ Could not get ngrok URL"
    echo ""
    echo "Possible issues:"
    echo "  1. ngrok needs authentication"
    echo "     Run: ngrok config add-authtoken YOUR_TOKEN"
    echo "     Get token: https://dashboard.ngrok.com/get-started/your-authtoken"
    echo ""
    echo "  2. Check ngrok status:"
    echo "     curl http://localhost:4040/api/tunnels"
    echo ""
    echo "  3. View ngrok web interface:"
    echo "     http://localhost:4040"
    echo ""
    echo "  4. Try starting manually:"
    echo "     ngrok http 8080"
    echo "     (Then copy the HTTPS URL from the output)"
    echo ""
fi

