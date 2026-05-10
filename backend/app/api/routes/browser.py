from __future__ import annotations

from fastapi import APIRouter, Depends
from pydantic import BaseModel

from backend.app.api.deps import get_current_user
from backend.app.models.user import User
from backend.app.services.browser_engine import browser_engine

router = APIRouter(prefix="/browser", tags=["browser"])


class NavigateRequest(BaseModel):
    url: str


class ClickRequest(BaseModel):
    selector: str


class TypeRequest(BaseModel):
    selector: str
    text: str


class ScreenshotRequest(BaseModel):
    full_page: bool = False


class ExtractTextRequest(BaseModel):
    selector: str = "body"


class ScrollRequest(BaseModel):
    direction: str = "down"
    amount: int = 500


class WaitRequest(BaseModel):
    selector: str
    timeout: int = 10000


class NewTabRequest(BaseModel):
    url: str = "about:blank"


class TabRequest(BaseModel):
    tab_id: str


class UploadFileRequest(BaseModel):
    selector: str
    file_path: str


class EvalJsRequest(BaseModel):
    script: str


@router.post("/navigate")
async def navigate(req: NavigateRequest, user: User = Depends(get_current_user)):
    return await browser_engine.navigate(user.id, req.url)


@router.post("/click")
async def click(req: ClickRequest, user: User = Depends(get_current_user)):
    return await browser_engine.click(user.id, req.selector)


@router.post("/type")
async def type_text(req: TypeRequest, user: User = Depends(get_current_user)):
    return await browser_engine.type_text(user.id, req.selector, req.text)


@router.post("/screenshot")
async def screenshot(req: ScreenshotRequest, user: User = Depends(get_current_user)):
    return await browser_engine.screenshot(user.id, req.full_page)


@router.post("/extract-text")
async def extract_text(req: ExtractTextRequest, user: User = Depends(get_current_user)):
    return await browser_engine.extract_text(user.id, req.selector)


@router.post("/extract-links")
async def extract_links(user: User = Depends(get_current_user)):
    return await browser_engine.extract_links(user.id)


@router.get("/content")
async def get_content(user: User = Depends(get_current_user)):
    return await browser_engine.get_page_content(user.id)


@router.post("/scroll")
async def scroll(req: ScrollRequest, user: User = Depends(get_current_user)):
    return await browser_engine.scroll(user.id, req.direction, req.amount)


@router.post("/wait")
async def wait_for(req: WaitRequest, user: User = Depends(get_current_user)):
    return await browser_engine.wait_for_selector(user.id, req.selector, req.timeout)


@router.post("/new-tab")
async def new_tab(req: NewTabRequest, user: User = Depends(get_current_user)):
    return await browser_engine.new_tab(user.id, req.url)


@router.post("/switch-tab")
async def switch_tab(req: TabRequest, user: User = Depends(get_current_user)):
    return await browser_engine.switch_tab(user.id, req.tab_id)


@router.post("/close-tab")
async def close_tab(req: TabRequest, user: User = Depends(get_current_user)):
    return await browser_engine.close_tab(user.id, req.tab_id)


@router.get("/tabs")
async def list_tabs(user: User = Depends(get_current_user)):
    return await browser_engine.list_tabs(user.id)


@router.post("/upload-file")
async def upload_file(req: UploadFileRequest, user: User = Depends(get_current_user)):
    return await browser_engine.upload_file(user.id, req.selector, req.file_path)


@router.post("/evaluate-js")
async def evaluate_js(req: EvalJsRequest, user: User = Depends(get_current_user)):
    return await browser_engine.evaluate_js(user.id, req.script)


@router.post("/back")
async def go_back(user: User = Depends(get_current_user)):
    return await browser_engine.go_back(user.id)


@router.post("/forward")
async def go_forward(user: User = Depends(get_current_user)):
    return await browser_engine.go_forward(user.id)


@router.post("/close-session")
async def close_session(user: User = Depends(get_current_user)):
    return await browser_engine.close_user_session(user.id)
