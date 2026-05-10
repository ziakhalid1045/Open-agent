---
name: testing-open-agents-backend
description: Test the Open Agents FastAPI backend end-to-end. Use when verifying security fixes, API changes, config loading, or browser engine behavior.
---

# Testing Open Agents Backend

## Local Server Setup

1. Install dependencies:
   ```bash
   cd /home/ubuntu/Open-agent
   pip install -e "backend/.[dev]"
   playwright install chromium
   ```

2. Start the server:
   ```bash
   cd /home/ubuntu/Open-agent
   uvicorn backend.app.main:app --host 0.0.0.0 --port 8000
   ```
   - Pass config via env vars with `OPEN_AGENTS_` prefix (e.g. `OPEN_AGENTS_SECRET_KEY=mykey`)
   - Delete `open_agents.db` before starting if you want a fresh database

3. Register a test user:
   ```bash
   curl -s http://localhost:8000/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","username":"testuser","password":"Pass123!"}'
   ```
   Extract the `access_token` from the response for subsequent requests.

4. Use the token in all API calls:
   ```bash
   curl -s http://localhost:8000/api/v1/files/list \
     -H "Authorization: Bearer $TOKEN"
   ```

## API Endpoints

| Area | Key Endpoints |
|------|---------------|
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me` |
| Files | `GET /api/v1/files/list?subdir=`, `POST /api/v1/files/upload`, `DELETE /api/v1/files/{path}` |
| Browser | `POST /api/v1/browser/navigate`, `POST /api/v1/browser/screenshot`, `POST /api/v1/browser/upload-file` |
| Tasks | `POST /api/v1/tasks/execute`, `GET /api/v1/tasks/{id}` |
| Agent | `POST /api/v1/agent/chat`, `POST /api/v1/agent/plan` |

## Testing Security Fixes

- **Path traversal**: Test with `subdir=../../etc` or `file_path=../../etc/passwd`. Expect HTTP 403 or `{"status":"error","error":"Access denied: invalid file path"}`.
- **Config prefix**: Set `OPEN_AGENTS_<FIELD>=value` and verify via Python: `from backend.app.config import Settings; s = Settings(); print(s.<field>)`
- **Encryption key**: Set `OPEN_AGENTS_ENCRYPTION_KEY=<fernet-key>` and test encrypt/decrypt roundtrip via `backend.app.core.security.encrypt_data` / `decrypt_data`.

## Testing Browser Engine

- Browser endpoints require an active session. First call `POST /browser/navigate` with a URL to create one.
- Screenshot endpoint returns both a file path and base64 data.
- Upload-file endpoint validates the file path is within the user's workspace before passing to Playwright.

## Known Issues

- **bcrypt/passlib incompatibility**: `bcrypt>=4.2` removed `__about__` module that `passlib==1.7.4` depends on. Registration fails with "password cannot be longer than 72 bytes". Workaround: `pip install 'bcrypt==4.1.3'`. This may be fixed in future passlib releases.
- **Playwright browsers**: Must run `playwright install chromium` before starting the server. The blueprint handles this, but if browsers are missing the server will fail at startup with a clear error message.

## Lint & Type Checking

```bash
ruff check backend/ --config backend/pyproject.toml
```

## Devin Secrets Needed

No secrets required for local testing. For AI agent testing, `OPEN_AGENTS_OPENAI_API_KEY` would be needed.
