from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, UploadFile
from fastapi.responses import FileResponse

from backend.app.api.deps import get_current_user
from backend.app.models.user import User
from backend.app.services.file_manager import file_manager

router = APIRouter(prefix="/files", tags=["files"])


@router.get("/list")
async def list_files(subdir: str = "", user: User = Depends(get_current_user)):
    return {"files": file_manager.list_files(user.id, subdir)}


@router.post("/upload")
async def upload_file(file: UploadFile, user: User = Depends(get_current_user)):
    if not file.filename:
        raise HTTPException(status_code=400, detail="No filename provided")
    content = await file.read()
    result = await file_manager.save_upload(user.id, file.filename, content)
    return result


@router.get("/download/{file_path:path}")
async def download_file(file_path: str, user: User = Depends(get_current_user)):
    path = file_manager.get_file_path(user.id, file_path)
    if path is None:
        raise HTTPException(status_code=404, detail="File not found")
    return FileResponse(path, filename=path.name)


@router.delete("/{file_path:path}")
async def delete_file(file_path: str, user: User = Depends(get_current_user)):
    try:
        deleted = file_manager.delete_file(user.id, file_path)
        if not deleted:
            raise HTTPException(status_code=404, detail="File not found")
        return {"status": "deleted"}
    except PermissionError:
        raise HTTPException(status_code=403, detail="Access denied")


@router.get("/workspace")
async def workspace_info(user: User = Depends(get_current_user)):
    return file_manager.get_workspace_info(user.id)
