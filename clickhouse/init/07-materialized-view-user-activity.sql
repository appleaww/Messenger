CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_activity_metrics
TO user_activity_metrics AS
SELECT
    timestamp,
    user_id,
    action_type
FROM (
    SELECT
    toDateTime(
    toStartOfInterval(
    fromUnixTimestamp64Nano(JSONExtractUInt(dp, 'timeUnixNano')),
    INTERVAL 15 SECOND
    )
    ) AS timestamp,

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
    ) AS action_type

    FROM kafka_business_metrics

    ARRAY JOIN JSONExtractArrayRaw(otlp_json, 'resourceMetrics') AS rm
    ARRAY JOIN JSONExtractArrayRaw(rm, 'scopeMetrics') AS sm
    ARRAY JOIN JSONExtractArrayRaw(sm, 'metrics') AS metric

    ARRAY JOIN arrayConcat(
    JSONExtractArrayRaw(metric, 'sum', 'dataPoints'),
    JSONExtractArrayRaw(metric, 'gauge', 'dataPoints')
    ) AS dp

    WHERE JSONExtractString(metric, 'name') = 'messenger.user.activity'
    AND action_type = 'session_started'
    );

SELECT 'MV mv_user_activity_metrics created successfully' AS status;