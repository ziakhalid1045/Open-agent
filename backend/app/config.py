from __future__ import annotations

import os
from pathlib import Path

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Open Agents"
    debug: bool = False

    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    # Security
    secret_key: str = os.getenv("SECRET_KEY", "change-me-in-production-use-openssl-rand-hex-32")
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440  # 24 hours

    # Database
    database_url: str = os.getenv("DATABASE_URL", "sqlite+aiosqlite:///./open_agents.db")

    # Workspace
    workspace_root: str = os.getenv(
        "WORKSPACE_ROOT", str(Path.home() / ".open_agents" / "workspaces")
    )

    # AI
    openai_api_key: str = os.getenv("OPENAI_API_KEY", "")
    ollama_base_url: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
    ai_provider: str = os.getenv("AI_PROVIDER", "openai")  # "openai" or "ollama"
    ai_model: str = os.getenv("AI_MODEL", "gpt-4o")

    # Browser
    headless: bool = True
    browser_timeout: int = 30000  # ms

    model_config = {"env_prefix": "OPEN_AGENTS_", "env_file": ".env", "extra": "ignore"}


settings = Settings()
