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
        kpis = {}

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
                self.logger.warning(f"Session metrics error: {session_data['error']}")

        return kpis

    def _calculate_latency_kpis(self, latency_data: dict) -> Dict[str, Any]:
        kpis = {}

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
                self.logger.warning(f"Latency metrics error: {latency_data['error']}")

        return kpis


    def _calculate_dau_mau_kpis(self, dau_mau_data: dict) -> Dict[str, Any]:
    kpis = {}

    if isinstance(dau_mau_data, dict) and "error" not in dau_mau_data:
        kpis.update({
            "timestamp": dau_mau_data.get("timestamp"),
            "dau": int(dau_mau_data.get("dau", 0)),
            "mau": int(dau_mau_data.get("mau", 0)),
        })
    else:
        if isinstance(dau_mau_data, dict) and "error" in dau_mau_data:
            self.logger.warning(f"DAU/MAU metrics error: {dau_mau_data.get('error')}")
        kpis.update({
            "timestamp": None,
            "dau": 0,
            "mau": 0
        })
        return kpis

    def calculate_kpis(self) -> Dict[str, Any]:
        session_data = self.fetcher.get_latest_session_metrics()
        latency_data = self.fetcher.get_latest_latency_metrics()
        dau_mau_data = self.fetcher.get_dau_mau()

        session_kpis = self._calculate_session_kpis(session_data)
        latency_kpis = self._calculate_latency_kpis(latency_data)
        dau_mau_kpis = self._calculate_dau_mau_kpis(dau_mau_data)

        return {
            **session_kpis,
            **latency_kpis,
            **dau_mau_kpis
        }