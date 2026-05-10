from __future__ import annotations

import json
import logging

import httpx

from backend.app.config import settings

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """You are Open Agents, an AI browser automation assistant. You convert natural language commands into structured browser actions.

Available actions:
- navigate: Go to a URL. Params: {"url": "..."}
- click: Click an element. Params: {"selector": "CSS selector"}
- type: Type text into a field. Params: {"selector": "CSS selector", "text": "..."}
- screenshot: Take a screenshot. Params: {"full_page": bool}
- extract_text: Extract text from page. Params: {"selector": "CSS selector"}
- extract_links: Extract all links from page. Params: {}
- scroll: Scroll the page. Params: {"direction": "up|down", "amount": int}
- wait: Wait for an element. Params: {"selector": "CSS selector", "timeout": int}
- new_tab: Open a new tab. Params: {"url": "..."}
- switch_tab: Switch to a tab. Params: {"tab_id": "..."}
- close_tab: Close a tab. Params: {"tab_id": "..."}
- upload_file: Upload a file. Params: {"selector": "CSS selector", "file_path": "..."}
- evaluate_js: Run JavaScript. Params: {"script": "..."}
- go_back: Go back in history. Params: {}
- go_forward: Go forward in history. Params: {}
- done: Task complete. Params: {"summary": "..."}

Respond ONLY with a JSON array of action steps. Each step: {"action": "...", "params": {...}, "description": "..."}

Example:
User: Search for "AI agents" on Google
Response:
[
  {"action": "navigate", "params": {"url": "https://www.google.com"}, "description": "Go to Google"},
  {"action": "type", "params": {"selector": "textarea[name='q']", "text": "AI agents"}, "description": "Type search query"},
  {"action": "click", "params": {"selector": "input[name='btnK']"}, "description": "Click search button"},
  {"action": "screenshot", "params": {"full_page": false}, "description": "Capture results"},
  {"action": "done", "params": {"summary": "Searched for AI agents on Google"}, "description": "Task complete"}
]
"""


class AIAgent:
    """Converts natural language into step-by-step browser actions."""

    def __init__(self) -> None:
        self._memory: dict[str, list[dict]] = {}

    def _get_memory(self, user_id: str) -> list[dict]:
        if user_id not in self._memory:
            self._memory[user_id] = []
        return self._memory[user_id]

    def _add_to_memory(self, user_id: str, role: str, content: str) -> None:
        mem = self._get_memory(user_id)
        mem.append({"role": role, "content": content})
        if len(mem) > 50:
            self._memory[user_id] = mem[-50:]

    async def plan_actions(
        self, user_id: str, command: str, page_context: dict | None = None
    ) -> list[dict]:
        context_msg = ""
        if page_context:
            context_msg = (
                f"\n\nCurrent page context:\n"
                f"- URL: {page_context.get('url', 'N/A')}\n"
                f"- Title: {page_context.get('title', 'N/A')}\n"
            )

        user_message = f"{command}{context_msg}"
        self._add_to_memory(user_id, "user", user_message)

        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            *self._get_memory(user_id),
        ]

        try:
            actions = await self._call_llm(messages)
            self._add_to_memory(user_id, "assistant", json.dumps(actions))
            return actions
        except Exception as e:
            logger.error("AI planning failed: %s", e)
            return [
                {
                    "action": "done",
                    "params": {"summary": f"Failed to plan actions: {e}"},
                    "description": "Error in planning",
                }
            ]

    async def _call_llm(self, messages: list[dict]) -> list[dict]:
        if settings.ai_provider == "ollama":
            return await self._call_ollama(messages)
        if settings.ai_provider == "groq":
            return await self._call_groq(messages)
        return await self._call_openai(messages)

    async def _call_openai(self, messages: list[dict]) -> list[dict]:
        if not settings.openai_api_key:
            return self._fallback_plan(messages)

        async with httpx.AsyncClient(timeout=60) as client:
            resp = await client.post(
                "https://api.openai.com/v1/chat/completions",
                headers={"Authorization": f"Bearer {settings.openai_api_key}"},
                json={
                    "model": settings.ai_model,
                    "messages": messages,
                    "temperature": 0.1,
                    "response_format": {"type": "json_object"},
                },
            )
            resp.raise_for_status()
            data = resp.json()
            content = data["choices"][0]["message"]["content"]
            parsed = json.loads(content)
            if isinstance(parsed, list):
                return parsed
            if isinstance(parsed, dict) and "steps" in parsed:
                return parsed["steps"]
            if isinstance(parsed, dict) and "actions" in parsed:
                return parsed["actions"]
            return [parsed]

    async def _call_groq(self, messages: list[dict]) -> list[dict]:
        if not settings.groq_api_key:
            return self._fallback_plan(messages)

        async with httpx.AsyncClient(timeout=60) as client:
            resp = await client.post(
                "https://api.groq.com/openai/v1/chat/completions",
                headers={"Authorization": f"Bearer {settings.groq_api_key}"},
                json={
                    "model": settings.ai_model,
                    "messages": messages,
                    "temperature": 0.1,
                    "response_format": {"type": "json_object"},
                },
            )
            resp.raise_for_status()
            data = resp.json()
            content = data["choices"][0]["message"]["content"]
            parsed = json.loads(content)
            if isinstance(parsed, list):
                return parsed
            if isinstance(parsed, dict) and "steps" in parsed:
                return parsed["steps"]
            if isinstance(parsed, dict) and "actions" in parsed:
                return parsed["actions"]
            return [parsed]

    async def _call_ollama(self, messages: list[dict]) -> list[dict]:
        try:
            async with httpx.AsyncClient(timeout=120) as client:
                resp = await client.post(
                    f"{settings.ollama_base_url}/api/chat",
                    json={
                        "model": settings.ai_model,
                        "messages": messages,
                        "stream": False,
                        "format": "json",
                    },
                )
                resp.raise_for_status()
                data = resp.json()
                content = data["message"]["content"]
                parsed = json.loads(content)
                if isinstance(parsed, list):
                    return parsed
                if isinstance(parsed, dict) and "steps" in parsed:
                    return parsed["steps"]
                return [parsed]
        except Exception as e:
            logger.warning("Ollama failed, using fallback: %s", e)
            return self._fallback_plan(messages)

    def _fallback_plan(self, messages: list[dict]) -> list[dict]:
        """Rule-based fallback when no AI provider is available."""
        last_msg = messages[-1]["content"].lower() if messages else ""

        if "search" in last_msg and "google" in last_msg:
            query = last_msg.split("search")[-1].strip().strip("\"'")
            return [
                {
                    "action": "navigate",
                    "params": {"url": "https://www.google.com"},
                    "description": "Go to Google",
                },
                {
                    "action": "type",
                    "params": {"selector": "textarea[name='q']", "text": query},
                    "description": f"Type: {query}",
                },
                {
                    "action": "click",
                    "params": {"selector": "input[name='btnK']"},
                    "description": "Search",
                },
                {
                    "action": "screenshot",
                    "params": {"full_page": False},
                    "description": "Capture results",
                },
                {
                    "action": "done",
                    "params": {"summary": f"Searched Google for: {query}"},
                    "description": "Done",
                },
            ]

        if "go to" in last_msg or "open" in last_msg or "navigate" in last_msg:
            words = last_msg.split()
            url = words[-1] if words[-1].startswith("http") else f"https://{words[-1]}"
            return [
                {"action": "navigate", "params": {"url": url}, "description": f"Navigate to {url}"},
                {
                    "action": "screenshot",
                    "params": {"full_page": False},
                    "description": "Capture page",
                },
                {
                    "action": "done",
                    "params": {"summary": f"Navigated to {url}"},
                    "description": "Done",
                },
            ]

        if "screenshot" in last_msg:
            return [
                {
                    "action": "screenshot",
                    "params": {"full_page": True},
                    "description": "Take screenshot",
                },
                {
                    "action": "done",
                    "params": {"summary": "Screenshot taken"},
                    "description": "Done",
                },
            ]

        return [
            {
                "action": "done",
                "params": {
                    "summary": "No AI provider configured. Set GROQ_API_KEY, OPENAI_API_KEY, or configure Ollama."
                },
                "description": "AI provider not available",
            }
        ]

    def clear_memory(self, user_id: str) -> None:
        self._memory.pop(user_id, None)


ai_agent = AIAgent()
