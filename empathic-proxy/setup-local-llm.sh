#!/bin/bash

# Setup script for LocalAI with Docker

set -e

echo "🚀 Setting up LocalAI for Empathic Proxy..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p models
mkdir -p localai-config

# Check if docker-compose file exists
if [ ! -f "docker-compose.simple.yml" ]; then
    echo "❌ docker-compose.simple.yml not found!"
    exit 1
fi

# Start LocalAI
echo "🐳 Starting LocalAI container..."
docker-compose -f docker-compose.simple.yml up -d

echo "⏳ Waiting for LocalAI to be ready (this may take a minute)..."
echo "   The first time will download the model (~600MB for TinyLlama)"

# Wait for health check
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -f http://localhost:8081/ready > /dev/null 2>&1; then
        echo "✅ LocalAI is ready!"
        break
    fi
    attempt=$((attempt + 1))
    echo "   Attempt $attempt/$max_attempts..."
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "⚠️  LocalAI may still be starting. Check logs with:"
    echo "   docker-compose -f docker-compose.simple.yml logs -f"
    exit 1
fi

# Test the API
echo "🧪 Testing LocalAI API..."
response=$(curl -s -X POST http://localhost:8081/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [{"role": "user", "content": "Say hello"}],
    "max_tokens": 10
  }' 2>/dev/null)

if echo "$response" | grep -q "choices"; then
    echo "✅ LocalAI API is working!"
    echo ""
    echo "📝 Next steps:"
    echo "   1. Set LLM_PROVIDER=localai in your environment"
    echo "   2. Start your Spring Boot app: mvn spring-boot:run"
    echo ""
    echo "📊 View logs: docker-compose -f docker-compose.simple.yml logs -f"
    echo "🛑 Stop LocalAI: docker-compose -f docker-compose.simple.yml down"
else
    echo "⚠️  API test failed. Check logs:"
    echo "   docker-compose -f docker-compose.simple.yml logs"
fi

