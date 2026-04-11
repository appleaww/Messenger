--основные таблицы в clickhouse
CREATE TABLE IF NOT EXISTS business_metrics(
    timestamp      DateTime64(3) DEFAULT now64(3),
    metric_name    String,
    metric_type    String COMMENT 'counter / timer / summary',
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
    TTL timestamp + INTERVAL 60 DAY --срок хранения
    SETTINGS index_granularity = 8192;

SELECT 'Table business_metrics created successfully' AS status;

CREATE TABLE IF NOT EXISTS technical_metrics(
    timestamp      DateTime64(3) DEFAULT now64(3),
    metric_name    String,
    metric_type    String,
    service_name   String DEFAULT 'messenger',
    value          Float64 DEFAULT 0,
    attributes     Map(String, String) --другие атрибуты попадают сюда
    )
    ENGINE = ReplacingMergeTree()
    ORDER BY (timestamp, metric_name)
    PARTITION BY toYYYYMM(timestamp)
    TTL timestamp + INTERVAL 30 DAY
    SETTINGS index_granularity = 8192;

SELECT 'Table technical_metrics created successfully' AS status;
