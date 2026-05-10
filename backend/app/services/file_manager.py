from __future__ import annotations

from pathlib import Path

import aiofiles

from backend.app.core.workspace import workspace_manager


class FileManager:
    """Manages file operations within user workspaces."""

    def list_files(self, user_id: str, subdir: str = "") -> list[dict]:
        return workspace_manager.list_files(user_id, subdir)

    def delete_file(self, user_id: str, file_path: str) -> bool:
        return workspace_manager.delete_file(user_id, file_path)

    def get_file_path(self, user_id: str, file_path: str) -> Path | None:
        return workspace_manager.get_file_path(user_id, file_path)

    async def save_upload(self, user_id: str, filename: str, content: bytes) -> dict:
        upload_dir = workspace_manager.get_upload_dir(user_id)
        target = upload_dir / filename
        # Prevent path traversal
        if not str(target.resolve()).startswith(str(upload_dir.resolve())):
            raise PermissionError("Invalid filename")
        async with aiofiles.open(target, "wb") as f:
            await f.write(content)
        stat = target.stat()
        return {
            "name": filename,
            "path": str(target.relative_to(workspace_manager.get_workspace(user_id))),
            "size": stat.st_size,
        }

    def get_workspace_info(self, user_id: str) -> dict:
        workspace = workspace_manager.get_workspace(user_id)
        total_size = workspace_manager.get_workspace_size(user_id)
        return {
            "workspace_path": str(workspace),
            "total_size_bytes": total_size,
            "total_size_mb": round(total_size / (1024 * 1024), 2),
        }


file_manager = FileManager()
