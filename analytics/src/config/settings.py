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
class Settings(BaseSettings):
    clickhouse: ClickHouseSettings = ClickHouseSettings()

    model_config = SettingsConfigDict(
        env_file=".env",
        extra="ignore",
    )
settings = Settings()