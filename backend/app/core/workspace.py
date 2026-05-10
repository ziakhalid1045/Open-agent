from __future__ import annotations

import shutil
from pathlib import Path

from backend.app.config import settings


class WorkspaceManager:
    """Manages isolated user workspaces for file storage and downloads."""

    def __init__(self) -> None:
        self.root = Path(settings.workspace_root)
        self.root.mkdir(parents=True, exist_ok=True)

    def get_workspace(self, user_id: str) -> Path:
        workspace = self.root / user_id
        workspace.mkdir(parents=True, exist_ok=True)
        (workspace / "downloads").mkdir(exist_ok=True)
        (workspace / "uploads").mkdir(exist_ok=True)
        (workspace / "screenshots").mkdir(exist_ok=True)
        return workspace

    def list_files(self, user_id: str, subdir: str = "") -> list[dict]:
        workspace = self.get_workspace(user_id)
        target = workspace / subdir if subdir else workspace
        if not target.exists() or not target.is_dir():
            return []
        if not str(target.resolve()).startswith(str(workspace.resolve())):
            raise PermissionError("Access denied: path traversal detected")
        result = []
        for entry in sorted(target.iterdir()):
            stat = entry.stat()
            result.append(
                {
                    "name": entry.name,
                    "path": str(entry.relative_to(workspace)),
                    "is_dir": entry.is_dir(),
                    "size": stat.st_size if entry.is_file() else 0,
                    "modified": stat.st_mtime,
                }
            )
        return result

    def delete_file(self, user_id: str, file_path: str) -> bool:
        workspace = self.get_workspace(user_id)
        target = workspace / file_path
        if not target.exists():
            return False
        if not str(target.resolve()).startswith(str(workspace.resolve())):
            raise PermissionError("Access denied: path traversal detected")
        if target.is_dir():
            shutil.rmtree(target)
        else:
            target.unlink()
        return True

    def get_file_path(self, user_id: str, file_path: str) -> Path | None:
        workspace = self.get_workspace(user_id)
        target = workspace / file_path
        if not str(target.resolve()).startswith(str(workspace.resolve())):
            raise PermissionError("Access denied: path traversal detected")
        if target.exists():
            return target
        return None

    def get_download_dir(self, user_id: str) -> Path:
        return self.get_workspace(user_id) / "downloads"

    def get_upload_dir(self, user_id: str) -> Path:
        return self.get_workspace(user_id) / "uploads"

    def get_screenshot_dir(self, user_id: str) -> Path:
        return self.get_workspace(user_id) / "screenshots"

    def get_workspace_size(self, user_id: str) -> int:
        workspace = self.get_workspace(user_id)
        total = 0
        for f in workspace.rglob("*"):
            if f.is_file():
                total += f.stat().st_size
        return total

    def cleanup_workspace(self, user_id: str) -> None:
        workspace = self.root / user_id
        if workspace.exists():
            shutil.rmtree(workspace)


workspace_manager = WorkspaceManager()
