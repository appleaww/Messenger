from clickhouse_connect import get_client
from clickhouse_connect.driver.client import Client
from src.config.settings import settings
from functools import lru_cache

@lru_cache(maxsize=1)
def get_clickhouse_client() -> Client:
    client = get_client(
        host=settings.clickhouse.host,
        port=settings.clickhouse.port,
        username=settings.clickhouse.username,
        password=settings.clickhouse.password,
        database=settings.clickhouse.database,
        send_receive_timeout=60,
        compress-True,
    )
    print("Connected to ClickHouse server")
    return client

clickhouse_client = get_clickhouse_client()