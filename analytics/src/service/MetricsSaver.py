import logging
import pandas as pd
from clickhouse_connect.driver.client import Client
from typing import Optional

from src.clickhouse.client import clickhouse_client
from src.analytics.fetcher import MetricsFetcher
from src.analytics.analyzer import MetricsAnalyzer


class MetricsSaver:
    def __init__(self, client: Client = clickhouse_client):
        self.client = client
        self.fetcher = MetricsFetcher(client=self.client)
        self.analyzer = MetricsAnalyzer(fetcher=self.fetcher)
        self.logger = logging.getLogger(__name__)

    def save_kpi_metrics(self) -> bool:
        try:
            kpis = self.analyzer.calculate_kpis()

            if not kpis:
                self.logger.warning("KPI data is empty, skipping save")
                return False
            df_data = {k: [v] for k, v in kpis.items() if k != "timestamp"}
            df = pd.DataFrame(df_data)

            self.client.insert_df("grafana_kpi_metrics", df)
            self.logger.info(f"KPI metrics saved to grafana_kpi_metrics")
            return True

        except Exception as e:
            self.logger.error(f"Failed to save KPI metrics to grafana_kpi_metrics: {e}", exc_info=True)
            return False

    def save_dau_mau_metrics(self) -> bool:
        try:
            dau_mau_data = self.analyzer.calculate_dau_mau_kpis()

            if not dau_mau_data or isinstance(dau_mau_data, dict) and "error" in dau_mau_data:
                self.logger.warning(f"DAU/MAU data issue: {dau_mau_data}")
                return False

            data = [{
                "timestamp": dau_mau_data.get("timestamp"),
                "dau": int(dau_mau_data.get("dau", 0)),
                "mau": int(dau_mau_data.get("mau", 0)),
            }]

            df = pd.DataFrame(data)
            self.client.insert_df("grafana_dau_mau_metrics", df)
            self.logger.info(f"DAU/MAU metrics saved to grafana_dau_mau_metrics")
            return True

        except Exception as e:
            self.logger.error(f"Failed to save DAU/MAU metrics to grafana_dau_mau_metrics: {e}", exc_info=True)
            return False