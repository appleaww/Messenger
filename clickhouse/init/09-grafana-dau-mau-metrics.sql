CREATE TABLE IF NOT EXISTS grafana_dau_mau_metrics(
    timestamp   DateTime64(0, 'Europe/Moscow') DEFAULT now64(0, 'Europe/Moscow'),
    dau         UInt32 DEFAULT 0,
    mau         UInt32 DEFAULT 0
    )
    ENGINE = MergeTree()
    ORDER BY (timestamp, date)
    PARTITION BY toYYYYMM(timestamp)
    TTL toDate(timestamp) + INTERVAL 1 YEAR
    SETTINGS index_granularity = 8192;

SELECT 'Table grafana_dau_mau_metrics created successfully' AS status;