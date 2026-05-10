FROM python:3.12-slim

# System deps for Playwright
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget curl gnupg2 \
    libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 \
    libxkbcommon0 libxcomposite1 libxdamage1 libxrandr2 libgbm1 \
    libpango-1.0-0 libcairo2 libasound2 libxshmfence1 \
    fonts-liberation xdg-utils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Install Python deps
COPY pyproject.toml .
RUN pip install --no-cache-dir pip --upgrade && \
    pip install --no-cache-dir . && \
    pip install 'bcrypt==4.1.3'

# Install Playwright browsers
RUN playwright install chromium && playwright install-deps chromium

# Copy app into the expected package path
COPY . /app/backend/
RUN touch /app/backend/__init__.py

EXPOSE 8000

CMD ["uvicorn", "backend.app.main:app", "--host", "0.0.0.0", "--port", "8000"]
