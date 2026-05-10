# Open Agents

AI-powered autonomous browser agent system. Natural language commands drive a headless browser through a REST API, with an Android app for mobile control.

## Architecture

```
┌──────────────┐     REST API      ┌──────────────────┐     Playwright     ┌─────────────┐
│  Android App │ ◄──────────────► │  FastAPI Backend  │ ◄──────────────►  │  Chromium    │
│  (Kotlin)    │                  │  + AI Agent       │                   │  (Headless)  │
└──────────────┘                  └──────────────────┘                   └─────────────┘
                                         │
                                    ┌────┴────┐
                                    │ SQLite  │
                                    │ + Files │
                                    └─────────┘
```

### Components

| Component | Tech | Description |
|-----------|------|-------------|
| **Backend** | FastAPI + Python | REST API server with auth, task execution, file management |
| **Browser Engine** | Playwright + Chromium | Headless browser with multi-tab, DOM control, screenshots |
| **AI Agent** | Groq / OpenAI / Ollama | Converts natural language to browser action sequences |
| **Data System** | SQLite + encrypted storage | Isolated user workspaces, persistent memory |
| **Android App** | Kotlin + Jetpack Compose | Chat UI, remote browser viewer, file manager, task tracker |

## Quick Start

### Backend (Local)

```bash
cd backend

# Install dependencies
pip install -e ".[dev]"

# Install Playwright browsers
playwright install chromium

# Copy environment config
cp ../.env.example .env
# Edit .env with your API keys

# Run server
uvicorn backend.app.main:app --reload --host 0.0.0.0 --port 8000
```

Server runs at `http://localhost:8000`. API docs at `http://localhost:8000/docs`.

### Docker

```bash
# With OpenAI
OPENAI_API_KEY=sk-... docker compose up

# With local Ollama
docker compose --profile local-ai up
```

## API Reference

### Authentication

```bash
# Register
curl -X POST http://localhost:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "username": "user", "password": "secret"}'

# Login
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "secret"}'
```

### AI Agent

```bash
# Execute a task (natural language → browser actions)
curl -X POST http://localhost:8000/api/v1/tasks/execute \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"command": "Search Google for AI browser agents"}'

# Submit task for background execution
curl -X POST http://localhost:8000/api/v1/tasks/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"command": "Go to github.com and screenshot the page"}'

# Chat with AI agent
curl -X POST http://localhost:8000/api/v1/agent/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "What can you do?"}'
```

### Browser Control

```bash
# Navigate
curl -X POST http://localhost:8000/api/v1/browser/navigate \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"url": "https://example.com"}'

# Screenshot
curl -X POST http://localhost:8000/api/v1/browser/screenshot \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"full_page": false}'

# Click element
curl -X POST http://localhost:8000/api/v1/browser/click \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"selector": "#submit-btn"}'

# Type text
curl -X POST http://localhost:8000/api/v1/browser/type \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"selector": "input[name=q]", "text": "hello"}'

# Multi-tab: new tab, switch, close
curl -X POST http://localhost:8000/api/v1/browser/new-tab \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"url": "https://example.com"}'

# Extract text/links
curl -X POST http://localhost:8000/api/v1/browser/extract-text \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"selector": "body"}'

# Run JavaScript
curl -X POST http://localhost:8000/api/v1/browser/evaluate-js \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"script": "document.title"}'
```

### File Management

```bash
# List files
curl http://localhost:8000/api/v1/files/list \
  -H "Authorization: Bearer $TOKEN"

# Upload file
curl -X POST http://localhost:8000/api/v1/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@document.pdf"

# Download file
curl http://localhost:8000/api/v1/files/download/uploads/document.pdf \
  -H "Authorization: Bearer $TOKEN" -o document.pdf
```

## AI Providers

### Groq (Free — Recommended)

Groq provides free API access with fast inference. Sign up at [console.groq.com](https://console.groq.com).

```bash
# Set in .env
OPEN_AGENTS_AI_PROVIDER=groq
OPEN_AGENTS_AI_MODEL=llama-3.3-70b-versatile
OPEN_AGENTS_GROQ_API_KEY=gsk_...
```

Available free models: `llama-3.3-70b-versatile`, `llama-3.1-8b-instant`, `mixtral-8x7b-32768`, `gemma2-9b-it`

### OpenAI (Paid)

Set `OPEN_AGENTS_AI_PROVIDER=openai` and `OPEN_AGENTS_OPENAI_API_KEY=sk-...` in `.env`.

### Ollama (Local)

```bash
# Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Pull a model
ollama pull llama3

# Set in .env
OPEN_AGENTS_AI_PROVIDER=ollama
OPEN_AGENTS_AI_MODEL=llama3
OPEN_AGENTS_OLLAMA_BASE_URL=http://localhost:11434
```

## Deployment

### Hugging Face Spaces (Free — Recommended)

Deploy the backend to Hugging Face Spaces using the Docker SDK:

1. Create a free account at [huggingface.co](https://huggingface.co/join)
2. Create a new Space: Spaces → New Space → Select **Docker** SDK
3. Clone the Space repo and copy the project files:
   ```bash
   git clone https://huggingface.co/spaces/YOUR-USERNAME/open-agents
   # Copy Dockerfile and backend/ directory into the Space
   cp -r Dockerfile backend/ open-agents/
   cd open-agents
   git add . && git commit -m "Deploy Open Agents" && git push
   ```
4. Set secrets in the Space Settings:
   - `OPEN_AGENTS_SECRET_KEY` (generate with `openssl rand -hex 32`)
   - `OPEN_AGENTS_GROQ_API_KEY` (your Groq API key)
   - `OPEN_AGENTS_ENCRYPTION_KEY` (generate with `python -c "from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())"`)

Your backend will be live at `https://YOUR-USERNAME-open-agents.hf.space`

### Koyeb (Free)

1. Create a free account at [koyeb.com](https://www.koyeb.com)
2. Deploy from GitHub → select this repo
3. Set Dockerfile path to `Dockerfile`
4. Set environment variables (same as HuggingFace above)

### Render

1. Fork this repo
2. Connect to [Render](https://render.com)
3. Create "New Blueprint Instance" → select repo
4. Set environment variables

### Railway

1. Fork this repo
2. Connect to [Railway](https://railway.app)
3. Deploy from GitHub → set environment variables

### Fly.io

```bash
fly launch --dockerfile backend/Dockerfile
fly secrets set OPEN_AGENTS_GROQ_API_KEY=gsk_...
fly secrets set OPEN_AGENTS_SECRET_KEY=$(openssl rand -hex 32)
fly deploy
```

## Android App

The Android app in `android/` connects to the backend server. Built with Kotlin and Jetpack Compose.

### Features

- **Chat**: Natural language interface to control the browser agent
- **Remote Browser**: View screenshots of the automated browser
- **File Manager**: Browse and manage files in your workspace
- **Task Tracker**: Monitor running and completed tasks

### Pre-built Downloads

- **APK** (direct install): Download `OpenAgents-debug.apk` from Releases
- **AAB** (Play Store): Download `OpenAgents-release.aab` from Releases

### Build from Source

1. Open `android/` in Android Studio
2. Update `BASE_URL` in `app/build.gradle.kts` with your deployed backend URL
3. Build APK: `./gradlew assembleDebug`
4. Build AAB: `./gradlew bundleRelease`

## Project Structure

```
Open-agent/
├── backend/
│   ├── app/
│   │   ├── main.py              # FastAPI entry point
│   │   ├── config.py            # Settings
│   │   ├── api/routes/          # REST endpoints
│   │   │   ├── auth.py          # Authentication
│   │   │   ├── browser.py       # Browser control
│   │   │   ├── tasks.py         # Task execution
│   │   │   ├── files.py         # File management
│   │   │   └── agent.py         # AI agent
│   │   ├── services/
│   │   │   ├── browser_engine.py # Playwright automation
│   │   │   ├── ai_agent.py      # NL → actions
│   │   │   ├── task_executor.py # Task orchestration
│   │   │   ├── auth_service.py  # User auth
│   │   │   └── file_manager.py  # Workspace files
│   │   ├── models/              # SQLAlchemy models
│   │   ├── core/
│   │   │   ├── security.py      # JWT, encryption
│   │   │   └── workspace.py     # Isolated user dirs
│   │   └── db/database.py       # Async SQLite
│   ├── tests/
│   ├── Dockerfile
│   └── pyproject.toml
├── android/                     # Kotlin/Compose app
│   ├── app/src/main/java/com/openagents/app/
│   │   ├── MainActivity.kt
│   │   ├── ui/screens/          # Chat, Browser, Files, Tasks
│   │   ├── viewmodel/           # State management
│   │   ├── data/api/            # Retrofit API client
│   │   └── di/                  # Dependency injection
│   └── build.gradle.kts
├── docker-compose.yml
├── render.yaml                  # Render deployment
├── railway.json                 # Railway deployment
├── fly.toml                     # Fly.io deployment
└── .env.example
```

## Security

- JWT-based authentication
- Fernet-encrypted user data storage
- Isolated workspaces per user (path traversal protected)
- Passwords hashed with bcrypt
- CORS middleware configured

## License

MIT
