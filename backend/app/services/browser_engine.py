from __future__ import annotations

import asyncio
import base64
import logging
import uuid
from dataclasses import dataclass, field

from playwright.async_api import Browser, BrowserContext, Page, async_playwright

from backend.app.config import settings
from backend.app.core.workspace import workspace_manager

logger = logging.getLogger(__name__)


@dataclass
class TabInfo:
    id: str
    page: Page
    url: str = ""
    title: str = ""


@dataclass
class UserBrowser:
    user_id: str
    context: BrowserContext
    tabs: dict[str, TabInfo] = field(default_factory=dict)
    active_tab_id: str = ""


class BrowserEngine:
    """Headless Chromium engine with multi-tab support and DOM control."""

    def __init__(self) -> None:
        self._playwright = None
        self._browser: Browser | None = None
        self._user_browsers: dict[str, UserBrowser] = {}
        self._lock = asyncio.Lock()

    async def start(self) -> None:
        if self._browser is not None:
            return
        self._playwright = await async_playwright().start()
        self._browser = await self._playwright.chromium.launch(
            headless=settings.headless,
            args=["--no-sandbox", "--disable-dev-shm-usage"],
        )
        logger.info("Browser engine started")

    async def stop(self) -> None:
        if self._browser:
            await self._browser.close()
            self._browser = None
        if self._playwright:
            await self._playwright.stop()
            self._playwright = None
        self._user_browsers.clear()
        logger.info("Browser engine stopped")

    async def _get_user_browser(self, user_id: str) -> UserBrowser:
        if user_id not in self._user_browsers:
            async with self._lock:
                if user_id not in self._user_browsers:
                    if not self._browser:
                        await self.start()
                    workspace_manager.get_download_dir(user_id)
                    context = await self._browser.new_context(
                        accept_downloads=True,
                        viewport={"width": 1280, "height": 720},
                    )
                    page = await context.new_page()
                    tab_id = str(uuid.uuid4())
                    tab = TabInfo(id=tab_id, page=page, url="about:blank")
                    ub = UserBrowser(
                        user_id=user_id,
                        context=context,
                        tabs={tab_id: tab},
                        active_tab_id=tab_id,
                    )
                    self._user_browsers[user_id] = ub
        return self._user_browsers[user_id]

    def _active_page(self, ub: UserBrowser) -> Page:
        return ub.tabs[ub.active_tab_id].page

    async def navigate(self, user_id: str, url: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            resp = await page.goto(
                url, timeout=settings.browser_timeout, wait_until="domcontentloaded"
            )
            tab = ub.tabs[ub.active_tab_id]
            tab.url = page.url
            tab.title = await page.title()
            return {
                "status": "ok",
                "url": page.url,
                "title": tab.title,
                "http_status": resp.status if resp else None,
            }
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def click(self, user_id: str, selector: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            await page.click(selector, timeout=settings.browser_timeout)
            await page.wait_for_load_state("domcontentloaded")
            return {"status": "ok", "url": page.url}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def type_text(self, user_id: str, selector: str, text: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            await page.fill(selector, text, timeout=settings.browser_timeout)
            return {"status": "ok"}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def screenshot(self, user_id: str, full_page: bool = False) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            screenshot_dir = workspace_manager.get_screenshot_dir(user_id)
            path = screenshot_dir / f"{uuid.uuid4()}.png"
            await page.screenshot(path=str(path), full_page=full_page)
            raw = await page.screenshot(full_page=full_page)
            return {
                "status": "ok",
                "path": str(path),
                "base64": base64.b64encode(raw).decode(),
            }
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def get_page_content(self, user_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            content = await page.content()
            title = await page.title()
            url = page.url
            return {"status": "ok", "url": url, "title": title, "html": content}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def evaluate_js(self, user_id: str, script: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            result = await page.evaluate(script)
            return {"status": "ok", "result": result}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def extract_text(self, user_id: str, selector: str = "body") -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            text = await page.inner_text(selector, timeout=settings.browser_timeout)
            return {"status": "ok", "text": text}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def extract_links(self, user_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            links = await page.evaluate(
                """() => Array.from(document.querySelectorAll('a[href]')).map(a => ({
                    text: a.innerText.trim(),
                    href: a.href
                }))"""
            )
            return {"status": "ok", "links": links}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def new_tab(self, user_id: str, url: str = "about:blank") -> dict:
        ub = await self._get_user_browser(user_id)
        try:
            page = await ub.context.new_page()
            tab_id = str(uuid.uuid4())
            tab = TabInfo(id=tab_id, page=page, url=url)
            ub.tabs[tab_id] = tab
            ub.active_tab_id = tab_id
            if url != "about:blank":
                await page.goto(url, timeout=settings.browser_timeout)
                tab.url = page.url
                tab.title = await page.title()
            return {"status": "ok", "tab_id": tab_id, "url": url}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def switch_tab(self, user_id: str, tab_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        if tab_id not in ub.tabs:
            return {"status": "error", "error": "Tab not found"}
        ub.active_tab_id = tab_id
        tab = ub.tabs[tab_id]
        return {"status": "ok", "tab_id": tab_id, "url": tab.url, "title": tab.title}

    async def close_tab(self, user_id: str, tab_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        if tab_id not in ub.tabs:
            return {"status": "error", "error": "Tab not found"}
        if len(ub.tabs) <= 1:
            return {"status": "error", "error": "Cannot close the last tab"}
        tab = ub.tabs.pop(tab_id)
        await tab.page.close()
        if ub.active_tab_id == tab_id:
            ub.active_tab_id = next(iter(ub.tabs))
        return {"status": "ok"}

    async def list_tabs(self, user_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        tabs = []
        for tid, tab in ub.tabs.items():
            tabs.append(
                {
                    "id": tid,
                    "url": tab.url,
                    "title": tab.title,
                    "active": tid == ub.active_tab_id,
                }
            )
        return {"status": "ok", "tabs": tabs}

    async def upload_file(self, user_id: str, selector: str, file_path: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            await page.set_input_files(selector, file_path, timeout=settings.browser_timeout)
            return {"status": "ok"}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def wait_for_selector(self, user_id: str, selector: str, timeout: int = 10000) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            await page.wait_for_selector(selector, timeout=timeout)
            return {"status": "ok"}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def scroll(self, user_id: str, direction: str = "down", amount: int = 500) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            delta = amount if direction == "down" else -amount
            await page.mouse.wheel(0, delta)
            await asyncio.sleep(0.5)
            return {"status": "ok", "direction": direction, "amount": amount}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def go_back(self, user_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            await page.go_back(timeout=settings.browser_timeout)
            return {"status": "ok", "url": page.url}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def go_forward(self, user_id: str) -> dict:
        ub = await self._get_user_browser(user_id)
        page = self._active_page(ub)
        try:
            await page.go_forward(timeout=settings.browser_timeout)
            return {"status": "ok", "url": page.url}
        except Exception as e:
            return {"status": "error", "error": str(e)}

    async def close_user_session(self, user_id: str) -> dict:
        if user_id in self._user_browsers:
            ub = self._user_browsers.pop(user_id)
            await ub.context.close()
            return {"status": "ok"}
        return {"status": "ok", "message": "No active session"}


browser_engine = BrowserEngine()
