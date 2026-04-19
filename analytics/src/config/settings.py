from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional

class ClickHouseSettings(BaseSettings):
    host: str = "analytics_db"
    port: int = 8123
    username: str = "default"
    password: str = ""
    database: str = "default"

    model_config = SettingsConfigDict(
        env_prefix="CLICKHOUSE_",
        env_file=".env",
        extra="ignore",
    )

class FetcherSettings(BaseSettings):
    technical_metrics_minutes: int = 1
    technical_metrics_max: int = 30
    business_metrics_minutes: int = 1
    business_metrics_limit: int =

    model_config = SettingsConfigDict(
        env_prefix="FETCHER_",
        env_file=".env",
    )

class Settings(BaseSettings):
    clickhouse: ClickHouseSettings = ClickHouseSettings()
    fetcher: FetcherSettings = FetcherSettings()

    model_config = SettingsConfigDict(
        env_file=".env",
        extra="ignore",
    )
settings = Settings()