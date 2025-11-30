#!/bin/bash

# Fix OAuth redirect loop issue

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 Fixing OAuth Redirect Loop"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

NGROK_URL="https://prologlike-neville-vicious.ngrok-free.dev"
REDIRECT_URI="${NGROK_URL}/oauth/callback"

echo "📋 Current Configuration:"
echo "   ngrok URL: $NGROK_URL"
echo "   Redirect URI: $REDIRECT_URI"
echo ""

# Check if redirect URI is in Slack settings
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Step 1: Verify Slack App Settings"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Go to: https://api.slack.com/apps"
echo "Select: Empathic Proxy"
echo "Click: OAuth & Permissions"
echo ""
echo "In 'Redirect URLs' section, make sure you have:"
echo "   $REDIRECT_URI"
echo ""
echo "If it's missing or different:"
echo "   1. Click 'Add New Redirect URL'"
echo "   2. Enter: $REDIRECT_URI"
echo "   3. Click 'Save URLs'"
echo ""
read -p "Press Enter after verifying/updating Slack settings..."

# Restart app with correct env vars
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔄 Step 2: Restart App with Correct Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Stop existing app
if pgrep -f "spring-boot:run" > /dev/null; then
    echo "Stopping existing app..."
    pkill -f "spring-boot:run"
    sleep 2
fi

# Set environment variables
export SLACK_CLIENT_ID=10013814890389.10017287381410
export SLACK_CLIENT_SECRET=8093413e2fa41b3fdb1ca5935ff4f193
export SLACK_SIGNING_SECRET=1e0ed2256d57dd21e7ae516c769b55b0
export SLACK_REDIRECT_URL="$REDIRECT_URI"
export LLM_PROVIDER=localai

echo "✅ Environment variables set:"
echo "   SLACK_CLIENT_ID: $SLACK_CLIENT_ID"
echo "   SLACK_REDIRECT_URL: $SLACK_REDIRECT_URL"
echo ""

# Start app in background
echo "Starting Spring Boot app..."
cd /Users/salil.kasaudhan/Downloads/empathic-proxy
nohup mvn spring-boot:run > /tmp/empathic-proxy.log 2>&1 &

echo "Waiting for app to start..."
sleep 8

# Check if app started
if pgrep -f "spring-boot:run" > /dev/null; then
    echo "✅ App is starting..."
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🧪 Step 3: Test Installation"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Open in your browser:"
    echo "   ${NGROK_URL}/oauth/install"
    echo ""
    echo "If you see ngrok warning page, click 'Visit Site'"
    echo ""
    echo "Monitor logs: tail -f /tmp/empathic-proxy.log"
    echo "Monitor requests: http://localhost:4040"
else
    echo "❌ App failed to start. Check logs: tail -f /tmp/empathic-proxy.log"
fi

