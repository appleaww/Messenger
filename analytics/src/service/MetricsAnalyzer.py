import logging
from typing import Dict, Any
from src.analytics.fetcher import MetricsFetcher


class MetricsAnalyzer:

    def __init__(self, fetcher: MetricsFetcher):
        self.fetcher = fetcher
        self.previous_session_count: int = 0
        self.previous_latency_count: int = 0
        self.is_first_run: bool = True
        self.logger = logging.getLogger(__name__)


    def _calculate_session_kpis(self, session_data: dict) -> Dict[str, Any]:
        kpis = {
            "sessions_per_minute": 0,
            "avg_session_duration_sec": 0.0,
            "max_session_duration_sec": 0.0,
        }

        if isinstance(session_data, dict) and "error" not in session_data:
            current_session_count = session_data["session_count"]

            if self.is_first_run:
                sessions_per_minute = 0
            else:
                sessions_per_minute = max(0, current_session_count - self.previous_session_count)

            self.previous_session_count = current_session_count

            kpis.update({
                "sessions_per_minute": int(sessions_per_minute),
                "avg_session_duration_sec": round(float(session_data["session_avg"]), 1),
                "max_session_duration_sec": round(float(session_data["session_max"]), 1),
            })
        else:
            if isinstance(session_data, dict) and "error" in session_data:
                self.logger.warning(f"Session metrics error: {session_data.get('error')}")

        return kpis


    def _calculate_latency_kpis(self, latency_data: dict) -> Dict[str, Any]:
        kpis = {
            "messages_per_minute": 0,
            "avg_message_latency_ms": 0.0,
            "max_message_latency_ms": 0.0,
            "min_message_latency_ms": 0.0,
        }

        if isinstance(latency_data, dict) and "error" not in latency_data:
            current_count = latency_data["count"]
            current_sum = latency_data["sum"]

            if self.is_first_run:
                messages_per_minute = 0
            else:
                messages_per_minute = max(0, current_count - self.previous_latency_count)

            self.previous_latency_count = current_count

            avg_latency_ms = (current_sum / current_count) if current_count > 0 else 0.0

            kpis.update({
                "messages_per_minute": int(messages_per_minute),
                "avg_message_latency_ms": round(avg_latency_ms, 1),
                "max_message_latency_ms": round(float(latency_data["latency_max"]), 1),
                "min_message_latency_ms": round(float(latency_data["latency_min"]), 1),
            })
        else:
            if isinstance(latency_data, dict) and "error" in latency_data:
                self.logger.warning(f"Latency metrics error: {latency_data.get('error')}")

        return kpis


    def _calculate_dau_mau_kpis(self, dau_mau_data: dict) -> Dict[str, Any]:
        kpis = {
            "timestamp": None,
            "dau": 0,
            "mau": 0,
        }

        if isinstance(dau_mau_data, dict) and "error" not in dau_mau_data:
            kpis.update({
                "timestamp": dau_mau_data.get("timestamp"),
                "dau": int(dau_mau_data.get("dau", 0)),
                "mau": int(dau_mau_data.get("mau", 0)),
            })
        else:
            if isinstance(dau_mau_data, dict) and "error" in dau_mau_data:
                self.logger.warning(f"DAU/MAU metrics error: {dau_mau_data.get('error')}")

        return kpis


    def _calculate_business_kpis(self, business_data: list[dict]) -> Dict[str, Any]:
        kpis = {
            "timestamp": None,
            "messages_sent": 0,
            "chats_created": 0,
            "users_registered": 0,
            "users_login": 0,
            "subscriptions_started": 0,
        }

        if not business_data:
            self.logger.warning("Business metrics data is empty")
            return kpis

        kpis["timestamp"] = str(business_data[0]["timestamp"])

        groups = self.group_metrics_by_name(business_data)

        mapping = {
            "messenger.messages.sent":        "messages_sent",
            "messenger.chats.created":        "chats_created",
            "messenger.users.registered":     "users_registered",
            "messenger.users.login":          "users_login",
            "messenger.subscriptions.started": "subscriptions_started",
        }

        for metric_name, target_key in mapping.items():
            value = groups.get(metric_name)
            if value is not None:
                kpis[target_key] = int(value)

        return kpis

    def group_metrics_by_name(self, metrics_data: list[dict]) -> dict[str, float]:
        groups: dict[str, float] = {}
        for row in metrics_data:
            metric_name = row.get("metric_name")
            value = row.get("value")
            if metric_name and value is not None and metric_name not in groups:
                groups[metric_name] = float(value)
        return groups

    def _calculate_technical_kpis(self, technical_data: list[dict]) -> Dict[str, Any]:
        kpis: Dict[str, Any] = {
            "timestamp": None,
            "memory_used_bytes": 0,
            "memory_committed_bytes": 0,
            "memory_limit_bytes": 0,
            "memory_usage_percent": 0.0,
            "memory_committed_percent": 0.0,
            "cpu_utilization_percent": 0.0,
            "thread_count": 0,
            "gc_duration_ms": 0.0,
            "kafka_messages_sent_per_minute": 0,
            "kafka_connection_count": 0,
            "db_connections_active": 0,
            "db_connection_wait_time_ms": 0.0,
            "db_connection_use_time_ms": 0.0,
            "http_request_duration_ms": 0.0,
        }

        if not technical_data:
            self.logger.warning("Technical metrics data is empty")
            return kpis

        kpis["timestamp"] = str(technical_data[0]["timestamp"])

        groups = self.group_metrics_by_name(technical_data)

        def get(name: str, default: float = 0.0) -> float:
            return groups.get(name, default)

        used = get('jvm.memory.used')
        committed = get('jvm.memory.committed')
        limit = get('jvm.memory.limit')

        kpis.update({
            "memory_used_bytes": int(used),
            "memory_committed_bytes": int(committed),
            "memory_limit_bytes": int(limit),
            "memory_usage_percent": round((used / limit * 100), 2) if limit > 0 else 0.0,
            "memory_committed_percent": round((committed / limit * 100), 2) if limit > 0 else 0.0,
        })

        kpis.update({
            "cpu_utilization_percent": round(get('jvm.cpu.recent_utilization') * 100, 2),
            "thread_count": int(get('jvm.thread.count')),
            "gc_duration_ms": round(get('jvm.gc.duration') * 1000, 2),
            "kafka_connection_count": int(get('kafka.producer.connection_count')),
            "db_connections_active": int(get('db.client.connections.usage')),
            "db_connection_wait_time_ms": round(get('db.client.connections.wait_time') * 1000, 2),
            "db_connection_use_time_ms": round(get('db.client.connections.use_time') * 1000, 2),
            "http_request_duration_ms": round(get('http.server.request.duration') * 1000, 2),
        })

        kpis["kafka_messages_sent_per_minute"] = int(get('kafka.producer.record_send_total'))

        return kpis


    def calculate_kpis(self) -> Dict[str, Any]:
        session_data = self.fetcher.get_latest_session_metrics()
        latency_data = self.fetcher.get_latest_latency_metrics()
        business_data = self.fetcher.get_latest_business_metrics()
        technical_data = self.fetcher.get_latest_technical_metrics()
        session_kpis = self._calculate_session_kpis(session_data)
        latency_kpis = self._calculate_latency_kpis(latency_data)
        business_kpis = self._calculate_business_kpis(business_data)
        technical_kpis = self._calculate_technical_kpis(technical_data)

        return {
            **session_kpis,
            **latency_kpis,
            **business_kpis,
            **technical_kpis,
        }

    def calculate_dau_mau_kpis(self) -> Dict[str, Any]:
        dau_mau_data = self.fetcher.get_dau_mau()
        return self._calculate_dau_mau_kpis(dau_mau_data)