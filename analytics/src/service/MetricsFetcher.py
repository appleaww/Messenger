from clickhouse_connect.driver.client import Client
import pandas as pd
from datetime import datetime, timedelta
from typing import Optional, Dict, Any

from src.clickhouse.client import clickhouse_client

def get_latest_session_metrics(self) -> dict:
    query = """
            SELECT
                timestamp,
                session_count,
                session_avg,
                session_max
            FROM business_session_metrics
            ORDER BY timestamp DESC
                LIMIT 1 \
            """
    df = self.client.query_df(query)
    if df.empty:
        return {"Error": "business_session_metrics is empty"}

    row = df.iloc[0]
    return {
        "timestamp": str(row["timestamp"]),
        "session_count": int(row["session_count"]),
        "session_avg_seconds": float(row["session_avg"]),
        "session_max_seconds": float(row["session_max"]),
    }

def get_latest_latency_metrics(self) -> dict:
    query = """
            SELECT
                timestamp,
                count,
                sum,
                latency_max,
                latency_min,
                bucket_count
            FROM business_message_latency_metrics
            ORDER BY timestamp DESC
                LIMIT 1 \
            """
    df = self.client.query_df(query)
    if df.empty:
        return {"Error": "business_message_latency_metrics is empty"}

    row = df.iloc[0]
    return {
        "timestamp": str(row["timestamp"]),
        "count": int(row["count"]),
        "sum": float(row["sum"]),
        "latency_max": float(row["latency_max"]),
        "latency_min": float(row["latency_min"]),
        "bucket_count": row["bucket_count"].tolist() if hasattr(row["bucket_count"], "tolist") else row["bucket_count"],

    }

def get_latest_business_metrics(
        self,
        hours: int = 1,
        limit: int = 1000,
        metric_name_like: Optional[str] = None,
) -> dict:

    query = f"""
            SELECT 
                timestamp,
                metric_name,
                metric_type,
                user_id,
                action_type,
                chat_id,
                tier,
                role,
                value
            FROM business_metrics
            WHERE timestamp >= now() - INTERVAL {hours} HOUR
            {f"AND metric_name LIKE '%{metric_name_like}%'" if metric_name_like else ""}
            ORDER BY timestamp DESC
            LIMIT {limit}
        """

    df = self.client.query_df(query)

    if df.empty:
        return {"error": "Нет данных в business_metrics", "data": []}

    records = df.to_dict(orient="records")

    for r in records:
        r["timestamp"] = str(r["timestamp"])
        r["value"] = float(r["value"])
        r["user_id"] = str(r["user_id"]) if r["user_id"] is not None else None
        r["chat_id"] = str(r["chat_id"]) if r["chat_id"] is not None else None

    return {
        "data": records,
        "count": len(records),
        "period_hours": hours,
        "last_timestamp": records[0]["timestamp"] if records else None,
    }