from __future__ import annotations

import uuid

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from backend.app.core.security import create_access_token, hash_password, verify_password
from backend.app.models.user import User


class AuthService:
    async def register(self, db: AsyncSession, email: str, username: str, password: str) -> dict:
        existing = await db.execute(select(User).where(User.email == email))
        if existing.scalar_one_or_none():
            raise ValueError("Email already registered")

        existing_username = await db.execute(select(User).where(User.username == username))
        if existing_username.scalar_one_or_none():
            raise ValueError("Username already taken")

        user = User(
            id=str(uuid.uuid4()),
            email=email,
            username=username,
            hashed_password=hash_password(password),
        )
        db.add(user)
        await db.commit()
        await db.refresh(user)

        token = create_access_token({"sub": user.id, "email": user.email})
        return {
            "user_id": user.id,
            "email": user.email,
            "username": user.username,
            "access_token": token,
            "token_type": "bearer",
        }

    async def login(self, db: AsyncSession, email: str, password: str) -> dict:
        result = await db.execute(select(User).where(User.email == email))
        user = result.scalar_one_or_none()

        if not user or not verify_password(password, user.hashed_password):
            raise ValueError("Invalid credentials")

        if not user.is_active:
            raise ValueError("Account is disabled")

        token = create_access_token({"sub": user.id, "email": user.email})
        return {
            "user_id": user.id,
            "email": user.email,
            "username": user.username,
            "access_token": token,
            "token_type": "bearer",
        }

    async def get_user(self, db: AsyncSession, user_id: str) -> User | None:
        return await db.get(User, user_id)

    async def get_user_by_email(self, db: AsyncSession, email: str) -> User | None:
        result = await db.execute(select(User).where(User.email == email))
        return result.scalar_one_or_none()


auth_service = AuthService()
