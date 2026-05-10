from __future__ import annotations

from base64 import b64decode, b64encode
from datetime import datetime, timedelta, timezone

from cryptography.fernet import Fernet
from jose import jwt
from passlib.context import CryptContext

from backend.app.config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

_fernet_key: bytes | None = None


def _get_fernet() -> Fernet:
    global _fernet_key
    if _fernet_key is None:
        if settings.encryption_key:
            _fernet_key = settings.encryption_key.encode()
        else:
            _fernet_key = Fernet.generate_key()
    return Fernet(_fernet_key)


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)


def create_access_token(data: dict, expires_delta: timedelta | None = None) -> str:
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + (
        expires_delta or timedelta(minutes=settings.access_token_expire_minutes)
    )
    to_encode["exp"] = expire
    return jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)


def decode_access_token(token: str) -> dict:
    return jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])


def encrypt_data(data: str) -> str:
    return b64encode(_get_fernet().encrypt(data.encode())).decode()


def decrypt_data(encrypted: str) -> str:
    return _get_fernet().decrypt(b64decode(encrypted.encode())).decode()
