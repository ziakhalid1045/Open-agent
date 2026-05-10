from __future__ import annotations

from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession

from backend.app.api.deps import get_current_user
from backend.app.db.database import async_session, get_db
from backend.app.models.user import User
from backend.app.services.task_executor import task_executor

router = APIRouter(prefix="/tasks", tags=["tasks"])


class TaskRequest(BaseModel):
    command: str


class TaskResponse(BaseModel):
    task_id: str
    command: str
    status: str
    result: str | None = None
    steps: str | None = None
    error: str | None = None
    created_at: str
    updated_at: str


def _task_to_response(task) -> TaskResponse:
    return TaskResponse(
        task_id=task.id,
        command=task.command,
        status=task.status,
        result=task.result,
        steps=task.steps,
        error=task.error,
        created_at=task.created_at.isoformat(),
        updated_at=task.updated_at.isoformat(),
    )


async def _run_task_background(task_id: str, user_id: str) -> None:
    async with async_session() as db:
        task = await task_executor.get_task(db, task_id)
        if task:
            await task_executor.execute_task(db, task)


@router.post("/execute", response_model=TaskResponse)
async def execute_task(
    req: TaskRequest,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    """Execute a task synchronously (waits for completion)."""
    task = await task_executor.create_task(db, user.id, req.command)
    task = await task_executor.execute_task(db, task)
    return _task_to_response(task)


@router.post("/submit", response_model=TaskResponse)
async def submit_task(
    req: TaskRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    """Submit a task for background execution."""
    task = await task_executor.create_task(db, user.id, req.command)
    background_tasks.add_task(_run_task_background, task.id, user.id)
    return _task_to_response(task)


@router.get("/{task_id}", response_model=TaskResponse)
async def get_task(
    task_id: str,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    task = await task_executor.get_task(db, task_id)
    if not task or task.user_id != user.id:
        raise HTTPException(status_code=404, detail="Task not found")
    return _task_to_response(task)


@router.get("/", response_model=list[TaskResponse])
async def list_tasks(
    limit: int = 20,
    offset: int = 0,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    tasks = await task_executor.list_tasks(db, user.id, limit, offset)
    return [_task_to_response(t) for t in tasks]
