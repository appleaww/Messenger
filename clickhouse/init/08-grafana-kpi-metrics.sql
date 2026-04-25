CREATE TABLE IF NOT EXISTS grafana_kpi_metrics (
    timestamp DateTime64(0, 'Europe/Moscow') DEFAULT now64(0, 'Europe/Moscow'),

    sessions_per_minute           Int32      DEFAULT 0,
    messages_per_minute           Int32      DEFAULT 0,
    messages_sent                 Int32      DEFAULT 0,
    chats_created                 Int32      DEFAULT 0,
    users_registered              Int32      DEFAULT 0,
    users_login                   Int32      DEFAULT 0,
    subscriptions_started         Int32      DEFAULT 0,

    avg_session_duration_sec      Float32    DEFAULT 0,
    max_session_duration_sec      Float32    DEFAULT 0,
    avg_message_latency_ms        Float32    DEFAULT 0,
    max_message_latency_ms        Float32    DEFAULT 0,
    min_message_latency_ms        Float32    DEFAULT 0,

    memory_used_bytes             UInt64     DEFAULT 0,
    memory_committed_bytes        UInt64     DEFAULT 0,
    memory_limit_bytes            UInt64     DEFAULT 0,
    memory_usage_percent          Float32    DEFAULT 0,
    memory_committed_percent      Float32    DEFAULT 0,
    cpu_utilization_percent       Float32    DEFAULT 0,
    thread_count                  UInt32     DEFAULT 0,
    gc_duration_ms                Float32    DEFAULT 0,
    kafka_messages_sent_per_minute UInt32    DEFAULT 0,
    kafka_connection_count        UInt32     DEFAULT 0,
    db_connections_active         UInt32     DEFAULT 0,
    db_connection_wait_time_ms    Float32    DEFAULT 0,
    db_connection_use_time_ms     Float32    DEFAULT 0,
    http_request_duration_ms      Float32    DEFAULT 0
    )
    ENGINE = MergeTree()
    ORDER BY timestamp
    PARTITION BY toYYYYMM(timestamp)
    TTL toDate(timestamp) + INTERVAL 90 DAY
    SETTINGS index_granularity = 8192;

SELECT 'Table grafana_kpi_metrics created successfully' AS status;
