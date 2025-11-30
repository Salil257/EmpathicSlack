# LocalAI Setup Guide

This guide helps you set up a local LLM using LocalAI in Docker for the Empathic Proxy app.

## Quick Start

```bash
# Run the automated setup script
chmod +x setup-local-llm.sh
./setup-local-llm.sh
```

## Manual Setup

### 1. Start LocalAI Container

```bash
docker-compose -f docker-compose.simple.yml up -d
```

### 2. Check Status

```bash
# View logs
docker-compose -f docker-compose.simple.yml logs -f

# Check if ready
curl http://localhost:8081/ready
```

### 3. Test the API

```bash
curl -X POST http://localhost:8081/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "gpt-3.5-turbo",
    "messages": [{"role": "user", "content": "Say hello in one word"}],
    "max_tokens": 10
  }'
```

## Model Options

Edit `localai-config/models.yaml` to change models:

### TinyLlama (Default - ~600MB)
- Fast and lightweight
- Good for testing
- Lower quality responses

### Phi-2 (~1.6GB)
- Better quality than TinyLlama
- Still relatively fast
- Good balance

### Llama 2 7B (~4GB)
- Best quality
- Slower inference
- Requires more RAM

After changing models, restart:
```bash
docker-compose -f docker-compose.simple.yml restart
```

## Configuration

### Port
LocalAI runs on port `8081` by default. To change:
1. Edit `docker-compose.simple.yml` ports mapping
2. Update `LOCALAI_BASE_URL` in your app config

### Performance Tuning
Edit `docker-compose.simple.yml`:
- `THREADS`: Number of CPU threads (default: 4)
- `CONTEXT_SIZE`: Context window size (default: 2048)

## Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose -f docker-compose.simple.yml logs

# Check Docker resources
docker stats
```

### Model download fails
- Check internet connection
- Verify HuggingFace is accessible
- Try a different model in `models.yaml`

### API returns errors
```bash
# Check if container is running
docker ps | grep localai

# Check health
curl http://localhost:8081/health

# View detailed logs
docker-compose -f docker-compose.simple.yml logs -f localai
```

### Slow responses
- Use a smaller model (TinyLlama)
- Reduce `CONTEXT_SIZE`
- Increase `THREADS` if you have more CPU cores

## Stopping LocalAI

```bash
# Stop container
docker-compose -f docker-compose.simple.yml down

# Stop and remove volumes (deletes downloaded models)
docker-compose -f docker-compose.simple.yml down -v
```

## Connecting Your App

1. Set environment variable:
   ```bash
   export LLM_PROVIDER=localai
   ```

2. Or in `application.properties`:
   ```properties
   llm.provider=localai
   llm.localai.base-url=http://localhost:8081/v1
   ```

3. Start your Spring Boot app:
   ```bash
   mvn spring-boot:run
   ```

## Resources

- [LocalAI Documentation](https://localai.io/)
- [Model Gallery](https://github.com/go-skynet/model-gallery)
- [HuggingFace Models](https://huggingface.co/models)

