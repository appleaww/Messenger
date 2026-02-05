from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class TechnicalEvent(BaseModel):
    type: str
    userId: Optional[str]
    latencyMs: Optional[int]
    throughput: Optional[float]
    cpuUsage: Optional[float]
    memoryUsedBytes: Optional[float]
    timestamp: datetime

class BusinessEvent(BaseModel):
    type: str
    userId: str
    sessionDurationMs: Optional[int]
    timestamp: datetime

class MetricsResponse(BaseModel):
    dau: int
    mau: int
    avg_session_duration_ms: float
    avg_latency_ms: float
    p95_latency_ms: float
    avg_cpu: float
    avg_memory_mb: float