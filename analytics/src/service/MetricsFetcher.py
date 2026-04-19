from clickhouse_connect.driver.client import Client
import pandas as pd
from typing import Optional

from src.clickhouse.client import clickhouse_client
from src.settings import settings


class MetricsFetcher:

    def __init__(self, client: Client = clickhouse_client):
        self.client = client
        self.fetcher_settings = settings.fetcher

    def get_latest_session_metrics(self) -> dict:
        query = """
                SELECT timestamp, session_count, session_avg, session_max
                FROM business_session_metrics
                ORDER BY timestamp DESC
                    LIMIT 1 \
                """
        df = self.client.query_df(query)
        if df.empty:
            return {"error": "business_session_metrics is empty"}

        row = df.iloc[0]
        return {
            "timestamp": str(row["timestamp"]),
            "session_count": int(row["session_count"]),
            "session_avg": float(row["session_avg"]),
            "session_max": float(row["session_max"]),
        }

    def get_latest_latency_metrics(self) -> dict:
        query = """
                SELECT timestamp, count, sum, latency_max, latency_min, bucket_count
                FROM business_message_latency_metrics
                ORDER BY timestamp DESC
                    LIMIT 1 \
                """
        df = self.client.query_df(query)
        if df.empty:
            return {"error": "business_message_latency_metrics is empty"}

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
            minutes: Optional[int] = None,
            limit: int = 1000,
            metric_name_like: Optional[str] = None,
    ) -> list[dict]:

        minutes = minutes or self.fetcher_settings.business_metrics_minutes

        query = f"""
            SELECT 
                timestamp, metric_name, metric_type, user_id, action_type,
                chat_id, tier, role, value
            FROM business_metrics
            WHERE timestamp >= now() - INTERVAL {minutes} MINUTE
            {f"AND metric_name LIKE '%{metric_name_like}%'" if metric_name_like else ""}
            ORDER BY timestamp DESC
            LIMIT {limit}
        """

        df = self.client.query_df(query)
        if df.empty:
            return []

        records = df.to_dict(orient="records")

        for r in records:
            r["timestamp"] = str(r["timestamp"])
            r["value"] = float(r["value"])
            r["user_id"] = str(r["user_id"]) if r["user_id"] is not None else None
            r["chat_id"] = str(r["chat_id"]) if r["chat_id"] is not None else None

        return records

    def get_recent_technical_metrics(
            self,
            minutes: Optional[int] = None,
            max_metrics: Optional[int] = None,
    ) -> list[dict]:

        minutes = minutes or self.fetcher_settings.technical_metrics_minutes
        max_metrics = max_metrics or self.fetcher_settings.technical_metrics_max

        query = f"""
            SELECT
                metric_name,
                argMax(timestamp, timestamp)      AS timestamp,
                argMax(metric_type, timestamp)    AS metric_type,
                argMax(value, timestamp)          AS value,
                argMax(attributes, timestamp)     AS attributes
            FROM technical_metrics
            WHERE timestamp >= now() - INTERVAL {minutes} MINUTE
            GROUP BY metric_name
            ORDER BY timestamp DESC
            LIMIT {max_metrics}
        """

        df = self.client.query_df(query)
        if df.empty:
            return []

        records = df.to_dict(orient="records")

        for r in records:
            r["timestamp"] = str(r["timestamp"])
            r["value"] = float(r["value"])

        return records