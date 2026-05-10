FROM python:3.12-slim

# System deps for Playwright + Chromium
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget curl gnupg2 \
    libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 \
    libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 libgbm1 \
    libpango-1.0-0 libcairo2 libasound2 libxshmfence1 \
    fonts-liberation xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user (required by HuggingFace Spaces)
RUN useradd -m -u 1000 appuser

WORKDIR /app

# Install Python deps
COPY backend/pyproject.toml /app/backend/pyproject.toml
RUN pip install --no-cache-dir pip --upgrade && \
    pip install --no-cache-dir '/app/backend[dev]' && \
    pip install 'bcrypt==4.1.3'

# Install Playwright browsers
RUN playwright install chromium && playwright install-deps chromium

# Copy backend code
COPY backend/ /app/backend/

# Create workspace directory writable by appuser
RUN mkdir -p /app/workspaces /home/appuser/.open_agents/workspaces && \
    chown -R appuser:appuser /app /home/appuser

USER appuser

# HuggingFace Spaces uses port 7860 by default
ENV OPEN_AGENTS_HOST=0.0.0.0
ENV OPEN_AGENTS_PORT=7860
ENV OPEN_AGENTS_HEADLESS=true
ENV OPEN_AGENTS_WORKSPACE_ROOT=/app/workspaces
ENV OPEN_AGENTS_DATABASE_URL=sqlite+aiosqlite:////app/open_agents.db

EXPOSE 7860

CMD ["uvicorn", "backend.app.main:app", "--host", "0.0.0.0", "--port", "7860"]
