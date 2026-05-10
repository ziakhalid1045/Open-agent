from __future__ import annotations

import json
import logging
import uuid
from datetime import datetime, timezone

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from backend.app.models.task import Task
from backend.app.services.ai_agent import ai_agent
from backend.app.services.browser_engine import browser_engine

logger = logging.getLogger(__name__)


class TaskExecutor:
    """Orchestrates AI planning and browser action execution."""

    async def create_task(self, db: AsyncSession, user_id: str, command: str) -> Task:
        task = Task(
            id=str(uuid.uuid4()),
            user_id=user_id,
            command=command,
            status="pending",
        )
        db.add(task)
        await db.commit()
        await db.refresh(task)
        return task

    async def execute_task(self, db: AsyncSession, task: Task) -> Task:
        await self._update_status(db, task.id, "running")

        try:
            page_ctx = await browser_engine.get_page_content(task.user_id)
            context = None
            if page_ctx.get("status") == "ok":
                context = {"url": page_ctx["url"], "title": page_ctx["title"]}

            steps = await ai_agent.plan_actions(task.user_id, task.command, context)
            await self._update_steps(db, task.id, steps)

            results = []
            for i, step in enumerate(steps):
                action = step.get("action", "")
                params = step.get("params", {})
                desc = step.get("description", "")

                logger.info("Executing step %d/%d: %s - %s", i + 1, len(steps), action, desc)
                result = await self._execute_action(task.user_id, action, params)
                results.append(
                    {"step": i + 1, "action": action, "description": desc, "result": result}
                )

                if result.get("status") == "error":
                    logger.warning("Step %d failed: %s", i + 1, result.get("error"))

                if action == "done":
                    break

            await self._update_result(db, task.id, "done", json.dumps(results))
        except Exception as e:
            logger.error("Task execution failed: %s", e)
            await self._update_result(db, task.id, "failed", None, str(e))

        task_result = await db.get(Task, task.id)
        return task_result

    async def _execute_action(self, user_id: str, action: str, params: dict) -> dict:
        action_map = {
            "navigate": lambda: browser_engine.navigate(user_id, params.get("url", "")),
            "click": lambda: browser_engine.click(user_id, params.get("selector", "")),
            "type": lambda: browser_engine.type_text(
                user_id, params.get("selector", ""), params.get("text", "")
            ),
            "screenshot": lambda: browser_engine.screenshot(
                user_id, params.get("full_page", False)
            ),
            "extract_text": lambda: browser_engine.extract_text(
                user_id, params.get("selector", "body")
            ),
            "extract_links": lambda: browser_engine.extract_links(user_id),
            "scroll": lambda: browser_engine.scroll(
                user_id, params.get("direction", "down"), params.get("amount", 500)
            ),
            "wait": lambda: browser_engine.wait_for_selector(
                user_id, params.get("selector", ""), params.get("timeout", 10000)
            ),
            "new_tab": lambda: browser_engine.new_tab(user_id, params.get("url", "about:blank")),
            "switch_tab": lambda: browser_engine.switch_tab(user_id, params.get("tab_id", "")),
            "close_tab": lambda: browser_engine.close_tab(user_id, params.get("tab_id", "")),
            "upload_file": lambda: browser_engine.upload_file(
                user_id, params.get("selector", ""), params.get("file_path", "")
            ),
            "evaluate_js": lambda: browser_engine.evaluate_js(user_id, params.get("script", "")),
            "go_back": lambda: browser_engine.go_back(user_id),
            "go_forward": lambda: browser_engine.go_forward(user_id),
            "done": lambda: self._done_action(params),
        }

        handler = action_map.get(action)
        if handler is None:
            return {"status": "error", "error": f"Unknown action: {action}"}
        return await handler()

    async def _done_action(self, params: dict) -> dict:
        return {"status": "ok", "summary": params.get("summary", "Task completed")}

    async def _update_status(self, db: AsyncSession, task_id: str, status: str) -> None:
        await db.execute(
            update(Task)
            .where(Task.id == task_id)
            .values(status=status, updated_at=datetime.now(timezone.utc))
        )
        await db.commit()

    async def _update_steps(self, db: AsyncSession, task_id: str, steps: list) -> None:
        await db.execute(
            update(Task)
            .where(Task.id == task_id)
            .values(steps=json.dumps(steps), updated_at=datetime.now(timezone.utc))
        )
        await db.commit()

    async def _update_result(
        self,
        db: AsyncSession,
        task_id: str,
        status: str,
        result: str | None,
        error: str | None = None,
    ) -> None:
        await db.execute(
            update(Task)
            .where(Task.id == task_id)
            .values(
                status=status,
                result=result,
                error=error,
                updated_at=datetime.now(timezone.utc),
            )
        )
        await db.commit()

    async def get_task(self, db: AsyncSession, task_id: str) -> Task | None:
        return await db.get(Task, task_id)

    async def list_tasks(
        self, db: AsyncSession, user_id: str, limit: int = 20, offset: int = 0
    ) -> list[Task]:
        result = await db.execute(
            select(Task)
            .where(Task.user_id == user_id)
            .order_by(Task.created_at.desc())
            .limit(limit)
            .offset(offset)
        )
        return list(result.scalars().all())


task_executor = TaskExecutor()
