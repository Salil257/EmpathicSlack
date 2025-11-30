#!/bin/bash

# Quick test script after manifest update

NGROK_URL="https://prologlike-neville-vicious.ngrok-free.dev"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 Testing Empathic Proxy"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check services
echo "📋 Checking services..."
if pgrep -f "ngrok http" > /dev/null; then
    echo "✅ ngrok is running"
else
    echo "❌ ngrok is not running - start with: ./setup-ngrok.sh"
    exit 1
fi

if pgrep -f "spring-boot:run" > /dev/null; then
    echo "✅ Spring Boot app is running"
else
    echo "❌ Spring Boot app is not running"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔗 Step 1: Install the App"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "If you haven't installed yet, visit:"
echo "   $NGROK_URL/oauth/install"
echo ""
read -p "Press Enter after installing (or if already installed)..."

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 Step 2: Test Features"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Test 1: Home Tab"
echo "  1. Open Slack"
echo "  2. Click 'Empathic Proxy' in Apps"
echo "  3. Click 'Home' tab"
echo "  4. Should see your inbox"
echo ""
read -p "Press Enter when ready for next test..."

echo ""
echo "Test 2: Direct Message"
echo "  1. Send DM to @Empathic Proxy: 'Hey, can you help me with this task?'"
echo "  2. Wait a few seconds"
echo "  3. Check Home tab - message should appear in inbox"
echo "  4. Should see action extraction and suggested reply"
echo ""
read -p "Press Enter when ready for next test..."

echo ""
echo "Test 3: App Mention"
echo "  1. In any channel: @Empathic Proxy Can you review this document?"
echo "  2. Wait a few seconds"
echo "  3. Check Home tab - mention should appear"
echo ""
read -p "Press Enter when ready for next test..."

echo ""
echo "Test 4: Message Shortcut (Composer)"
echo "  1. Right-click any message in Slack"
echo "  2. Select 'Compose Reply'"
echo "  3. Modal should open with composer"
echo "  4. Try typing a draft and clicking 'Transform'"
echo "  5. Click 'Post' to send"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Monitoring"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Watch requests in real-time:"
echo "   http://localhost:4040"
echo ""
echo "View app logs:"
echo "   tail -f /tmp/empathic-proxy.log"
echo ""
echo "✅ Testing complete! Check the monitoring tools above for any issues."

