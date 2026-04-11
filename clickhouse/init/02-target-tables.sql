--основные таблицы в clickhouse
CREATE TABLE IF NOT EXISTS business_metrics(
    timestamp      DateTime64(3) DEFAULT now64(3),
    metric_name    String,
    metric_type    String,
    user_id        Nullable(String),
    action_type    Nullable(String),
    chat_id        Nullable(String),
    tier           Nullable(String),
    role           Nullable(String),
    value          Float64
    )
    ENGINE = ReplacingMergeTree()
    ORDER BY (timestamp, metric_name)
    PARTITION BY toYYYYMM(timestamp)--разбиваем все данные на партиции внутри clickhouse по времени
    TTL toDate(timestamp) + INTERVAL 60 DAY --срок хранения
    SETTINGS index_granularity = 8192;

SELECT 'Table business_metrics created successfully' AS status;

CREATE TABLE IF NOT EXISTS business_session_metrics( --метрика для сессий представлена гистограммой и содержит несколько значений
    timestamp     DateTime64(3) DEFAULT now64(3),
    session_count Float64,
    session_avg   Float64,
    session_p95   Float64,
    session_max   Float64
    )
    ENGINE = MergeTree()
    ORDER BY (timestamp)
    PARTITION BY toYYYYMM(timestamp)
    TTL toDate(timestamp) + INTERVAL 60 DAY
    SETTINGS index_granularity = 8192;

SELECT 'Table business_session_metrics created successfully' AS status;

CREATE TABLE IF NOT EXISTS technical_metrics(
    timestamp      DateTime64(3) DEFAULT now64(3),
    metric_name    String,
    metric_type    String,
    value          Float64 DEFAULT 0,
    attributes     Map(String, String) --другие атрибуты попадают сюда
    )
    ENGINE = ReplacingMergeTree()
    ORDER BY (timestamp, metric_name)
    PARTITION BY toYYYYMM(timestamp)
    TTL toDate(timestamp) + INTERVAL 30 DAY
    SETTINGS index_granularity = 8192;

SELECT 'Table technical_metrics created successfully' AS status;
