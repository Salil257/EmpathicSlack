#!/bin/bash

# Install and Test Empathic Proxy

NGROK_URL="https://prologlike-neville-vicious.ngrok-free.dev"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Empathic Proxy - Install & Test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."
echo ""

# Check ngrok
if pgrep -f "ngrok http" > /dev/null; then
    echo "✅ ngrok is running"
else
    echo "❌ ngrok is not running"
    echo "   Start with: ./setup-ngrok.sh"
    exit 1
fi

# Check Spring Boot app
if pgrep -f "spring-boot:run" > /dev/null; then
    echo "✅ Spring Boot app is running"
else
    echo "❌ Spring Boot app is not running"
    echo "   Start with: mvn spring-boot:run"
    exit 1
fi

# Check app is accessible
if curl -s "$NGROK_URL/oauth/install" > /dev/null 2>&1; then
    echo "✅ App is accessible through ngrok"
else
    echo "⚠️  App may not be accessible (this is okay, ngrok free tier shows warning page)"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔗 Installation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Open this URL in your browser to install:"
echo ""
echo "   $NGROK_URL/oauth/install"
echo ""
echo "After clicking 'Allow', you should see 'Installation Successful!'"
echo ""
read -p "Press Enter after you've installed the app..."

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 Testing Guide"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Open Slack and click 'Empathic Proxy' in Apps"
echo "2. Click the 'Home' tab to see your inbox"
echo "3. Send a DM to @Empathic Proxy: 'Hey, can you help me?'"
echo "4. Check the Home tab - the message should appear in your inbox"
echo "5. Try mentioning the bot in a channel: @Empathic Proxy Can you review this?"
echo "6. Right-click any message → 'Compose Reply' to test the composer"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Monitor Requests"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Watch incoming requests in real-time:"
echo "   http://localhost:4040"
echo ""
echo "View app logs:"
echo "   tail -f /tmp/empathic-proxy.log"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

