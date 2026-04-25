from clickhouse_connect.driver.client import Client
from typing import Optional

from src.clickhouse.client import clickhouse_client
from src.config.settings import settings


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
            max_business_metrics: Optional[int] = None,
    ) -> list[dict]:
        max_business_metrics = max_business_metrics or self.fetcher_settings.business_metrics_max

        query = f"""
            SELECT 
                timestamp, metric_name, tier, value
            FROM business_metrics
            ORDER BY timestamp DESC
            LIMIT {max_business_metrics}
        """

        df = self.client.query_df(query)
        if df.empty:
            return []

        records = df.to_dict(orient="records")

        for r in records:
            r["timestamp"] = str(r["timestamp"])
            r["value"] = float(r["value"])

        return records

    def get_latest_technical_metrics(
            self,
            max_technical_metrics: Optional[int] = None,
    ) -> list[dict]:
        max_technical_metrics = max_technical_metrics or self.fetcher_settings.technical_metrics_max

        query = f"""
            SELECT
                timestamp,
                metric_name,
                metric_type,
                value,
                attributes
            FROM technical_metrics
            ORDER BY timestamp DESC
            LIMIT {max_technical_metrics}
        """

        df = self.client.query_df(query)
        if df.empty:
            return []

        records = df.to_dict(orient="records")

        for r in records:
            r["timestamp"] = str(r["timestamp"])
            r["value"] = float(r["value"])

        return records

    def get_dau_mau(self) -> dict:
        query = """
            SELECT
                now() AS timestamp,
            count(DISTINCT if(timestamp >= now() - INTERVAL 1 DAY, user_id, NULL)) AS dau,
            count(DISTINCT user_id) AS mau
            FROM user_activity_metrics
            WHERE timestamp >= now() - INTERVAL 30 DAY
                """

        df = self.client.query_df(query)
        if df.empty:
            return {"error": "user_activity_metrics is empty"}

        row = df.iloc[0]
        return {
            "timestamp": str(row["timestamp"]),
            "dau": int(row["dau"]),
            "mau": int(row["mau"]),
        }