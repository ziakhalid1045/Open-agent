from __future__ import annotations

from fastapi import APIRouter, Depends
from pydantic import BaseModel

from backend.app.api.deps import get_current_user
from backend.app.models.user import User
from backend.app.services.ai_agent import ai_agent
from backend.app.services.browser_engine import browser_engine

router = APIRouter(prefix="/agent", tags=["agent"])


class PlanRequest(BaseModel):
    command: str


class ChatRequest(BaseModel):
    message: str


@router.post("/plan")
async def plan_actions(req: PlanRequest, user: User = Depends(get_current_user)):
    """Plan browser actions from natural language without executing them."""
    page_ctx = await browser_engine.get_page_content(user.id)
    context = None
    if page_ctx.get("status") == "ok":
        context = {"url": page_ctx["url"], "title": page_ctx["title"]}
    steps = await ai_agent.plan_actions(user.id, req.command, context)
    return {"steps": steps}


@router.post("/chat")
async def chat(req: ChatRequest, user: User = Depends(get_current_user)):
    """Chat with the AI agent about browser tasks."""
    steps = await ai_agent.plan_actions(user.id, req.message)
    descriptions = [s.get("description", "") for s in steps if s.get("description")]
    return {
        "response": " → ".join(descriptions) if descriptions else "I'll help you with that.",
        "planned_steps": steps,
    }


@router.post("/clear-memory")
async def clear_memory(user: User = Depends(get_current_user)):
    """Clear the AI agent's conversation memory for this user."""
    ai_agent.clear_memory(user.id)
    return {"status": "ok", "message": "Memory cleared"}
