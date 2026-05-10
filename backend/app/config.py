from __future__ import annotations

from pathlib import Path

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Open Agents"
    debug: bool = False

    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    # Security
    secret_key: str = "change-me-in-production-use-openssl-rand-hex-32"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440

    # Database
    database_url: str = "sqlite+aiosqlite:///./open_agents.db"

    # Workspace
    workspace_root: str = str(Path.home() / ".open_agents" / "workspaces")

    # AI
    openai_api_key: str = ""
    ollama_base_url: str = "http://localhost:11434"
    ai_provider: str = "openai"
    ai_model: str = "gpt-4o"

    # Browser
    headless: bool = True
    browser_timeout: int = 30000

    # Encryption
    encryption_key: str = ""

    model_config = {
        "env_prefix": "OPEN_AGENTS_",
        "env_file": ".env",
        "extra": "ignore",
    }


settings = Settings()
