CREATE MATERIALIZED VIEW IF NOT EXISTS mv_business_metrics
TO business_metrics AS
SELECT
    timestamp,
    metric_name,
    metric_type,
    user_id,
    action_type,
    chat_id,
    tier,
    role,
    value
FROM (
    SELECT
    toDateTime(
    toStartOfInterval(
    fromUnixTimestamp64Nano(JSONExtractUInt(dp, 'timeUnixNano')),
    INTERVAL 15 SECOND
    )
    ) AS timestamp,

    JSONExtractString(metric, 'name') AS metric_name,

    multiIf(
    JSONHas(metric, 'sum'),   'counter',
    JSONHas(metric, 'gauge'), 'gauge',
    'other'
    ) AS metric_type,

    JSONExtractString(
    JSONExtractRaw(
    arrayFirst(x -> JSONExtractString(x, 'key') = 'userId',
    JSONExtractArrayRaw(dp, 'attributes')
    ), 'value'
    ), 'stringValue'
    ) AS user_id,

    JSONExtractString(
    JSONExtractRaw(
    arrayFirst(x -> JSONExtractString(x, 'key') = 'action_type',
    JSONExtractArrayRaw(dp, 'attributes')
    ), 'value'
    ), 'stringValue'
    ) AS action_type,

    JSONExtractString(
    JSONExtractRaw(
    arrayFirst(x -> JSONExtractString(x, 'key') = 'chatId',
    JSONExtractArrayRaw(dp, 'attributes')
    ), 'value'
    ), 'stringValue'
    ) AS chat_id,

    JSONExtractString(
    JSONExtractRaw(
    arrayFirst(x -> JSONExtractString(x, 'key') = 'tier',
    JSONExtractArrayRaw(dp, 'attributes')
    ), 'value'
    ), 'stringValue'
    ) AS tier,

    JSONExtractString(
    JSONExtractRaw(
    arrayFirst(x -> JSONExtractString(x, 'key') = 'role',
    JSONExtractArrayRaw(dp, 'attributes')
    ), 'value'
    ), 'stringValue'
    ) AS role,

    JSONExtractFloat(dp, 'asDouble') AS value

    FROM kafka_business_metrics

    ARRAY JOIN JSONExtractArrayRaw(otlp_json, 'resourceMetrics') AS rm
    ARRAY JOIN JSONExtractArrayRaw(rm, 'scopeMetrics') AS sm
    ARRAY JOIN JSONExtractArrayRaw(sm, 'metrics') AS metric

    ARRAY JOIN arrayConcat(
    JSONExtractArrayRaw(metric, 'sum', 'dataPoints'),
    JSONExtractArrayRaw(metric, 'gauge', 'dataPoints')
    ) AS dp
    )
WHERE metric_name LIKE 'messenger.%'
  AND NOT endsWith(metric_name, '.max')
  AND NOT like(metric_name, '%session%');

SELECT 'MV mv_business_metrics created successfully' AS status;



