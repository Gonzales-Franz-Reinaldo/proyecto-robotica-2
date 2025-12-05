from pydantic import BaseModel
from typing import Optional


class Token(BaseModel):
    """Schema para respuesta de login exitoso"""
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class TokenPayload(BaseModel):
    """Schema para el payload del token"""
    sub: int  # user_id
    rol: str
    exp: Optional[int] = None


class RefreshTokenRequest(BaseModel):
    """Schema para solicitud de refresh token"""
    refresh_token: str