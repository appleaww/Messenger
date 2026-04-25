from pydantic_settings import BaseSettings, SettingsConfigDict
from pathlib import Path
from dotenv import load_dotenv

env_path = Path(__file__).resolve().parent.parent.parent / ".env"
load_dotenv(dotenv_path=env_path, override=True)

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
    technical_metrics_max: int = 15
    business_metrics_max: int = 5

    model_config = SettingsConfigDict(
        env_prefix="FETCHER_",
        env_file=".env",
    )

class SchedulerSettings(BaseSettings):
    initial_delay_minutes: int = 5

    model_config = SettingsConfigDict(
        env_prefix="SCHEDULER_",
        env_file=".env",
        extra="ignore",
    )

class Settings(BaseSettings):
    clickhouse: ClickHouseSettings = ClickHouseSettings()
    fetcher: FetcherSettings = FetcherSettings()
    scheduler: SchedulerSettings = SchedulerSettings()

    model_config = SettingsConfigDict(
        env_file=".env",
        extra="ignore",
    )
settings = Settings()