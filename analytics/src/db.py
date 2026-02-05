from sqlalchemy import create_engine, text
import os
import time
import logging

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

CLICKHOUSE_USER = os.getenv('CLICKHOUSE_USER')
CLICKHOUSE_PASSWORD = os.getenv('CLICKHOUSE_PASSWORD')
CLICKHOUSE_DB = os.getenv('CLICKHOUSE_DB')
CLICKHOUSE_HOST = os.getenv('CLICKHOUSE_HOST', 'analytics_db:8123')

if not all([CLICKHOUSE_USER, CLICKHOUSE_PASSWORD, CLICKHOUSE_DB]):
    raise ValueError("Missing ClickHouse credentials in env vars")


CLICKHOUSE_INIT_URL = f"clickhouse+http://{CLICKHOUSE_USER}:{CLICKHOUSE_PASSWORD}@{CLICKHOUSE_HOST}/default"


init_engine = None
max_retries = 30
for i in range(max_retries):
    try:
        init_engine = create_engine(CLICKHOUSE_INIT_URL)
        with init_engine.connect() as conn:
            logger.info("Connected to ClickHouse init (default DB)")
            conn.execute(text(f"CREATE DATABASE IF NOT EXISTS {CLICKHOUSE_DB}"))
            logger.info(f"Database '{CLICKHOUSE_DB}' created or exists")
        break
    except Exception as e:
        logger.error(f"Init connection attempt {i+1} failed: {e}")
        time.sleep(5)
    if i == max_retries - 1:
        raise ValueError("Failed to connect and create DB after max retries")


CLICKHOUSE_URL = f"clickhouse+http://{CLICKHOUSE_USER}:{CLICKHOUSE_PASSWORD}@{CLICKHOUSE_HOST}/{CLICKHOUSE_DB}"

engine = None
for i in range(max_retries):
    try:
        engine = create_engine(CLICKHOUSE_URL)
        with engine.connect() as conn:
            logger.info("ClickHouse connected successfully to target DB")
        break
    except Exception as e:
        logger.error(f"Target connection attempt {i+1} failed: {e}")
        time.sleep(5)
    if i == max_retries - 1:
        raise ValueError("Failed to connect to target DB after max retries")


with engine.connect() as conn:
    conn.execute(text("""
        CREATE TABLE IF NOT EXISTS technical_metrics (
            type String,
            user_id Nullable(String),
            latency_ms Nullable(Float64),
            throughput Nullable(Float64),
            cpu_usage Nullable(Float64),
            memory_used_bytes Nullable(Float64),
            timestamp DateTime
        ) ENGINE = MergeTree()
        ORDER BY timestamp
    """))
    conn.execute(text("""
        CREATE TABLE IF NOT EXISTS business_metrics (
            type String,
            user_id String,
            session_duration_ms Nullable(Float64),
            timestamp DateTime
        ) ENGINE = MergeTree()
        ORDER BY timestamp
    """))
    logger.info("Tables created successfully")