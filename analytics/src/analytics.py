from datetime import datetime, timedelta
from db import engine
from sqlalchemy import text
def get_metrics():
    with engine.connect() as conn:
        today = datetime.now().date()
        month_ago = today - timedelta(days=30)
        hour_ago = datetime.now() - timedelta(hours=1)

        dau = conn.execute(text("SELECT uniq(user_id) FROM business_metrics WHERE toDate(timestamp) = :today"), {'today': today}).scalar() or 0
        mau = conn.execute(text("SELECT uniq(user_id) FROM business_metrics WHERE timestamp >= :month_ago"), {'month_ago': month_ago}).scalar() or 0
        avg_session = conn.execute(text("SELECT avg(session_duration_ms) FROM business_metrics WHERE type = 'session_end'")).scalar() or 0.0
        avg_latency = conn.execute(text("SELECT avg(latency_ms) FROM technical_metrics WHERE type = 'message_sent' AND latency_ms IS NOT NULL")).scalar() or 0.0
        p95_latency = conn.execute(text("SELECT quantile(0.95)(latency_ms) FROM technical_metrics WHERE type = 'message_sent' AND latency_ms IS NOT NULL")).scalar() or 0.0
        avg_cpu = conn.execute(text("SELECT avg(cpu_usage) FROM technical_metrics WHERE type = 'system_metrics' AND timestamp >= :hour_ago"), {'hour_ago': hour_ago}).scalar() or 0.0
        avg_memory_mb = conn.execute(text("SELECT avg(memory_used_bytes) / (1024 * 1024) FROM technical_metrics WHERE type = 'system_metrics' AND timestamp >= :hour_ago"), {'hour_ago': hour_ago}).scalar() or 0.0

    return {
        'dau': dau,
        'mau': mau,
        'avg_session_duration_ms': avg_session,
        'avg_latency_ms': avg_latency,
        'p95_latency_ms': p95_latency,
        'avg_cpu': avg_cpu,
        'avg_memory_mb': avg_memory_mb
    }