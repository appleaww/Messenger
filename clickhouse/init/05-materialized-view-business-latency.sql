CREATE MATERIALIZED VIEW IF NOT EXISTS mv_business_message_latency_metrics
TO business_message_latency_metrics
AS
SELECT
    timestamp,
    count,
    sum,
    latency_max,
    latency_min,
    bucket_count
FROM (
    SELECT
    toDateTime(
    toStartOfInterval(
    fromUnixTimestamp64Nano(JSONExtractUInt(dp, 'timeUnixNano')),
    INTERVAL 15 SECOND
    )
    ) AS timestamp,

    JSONExtractUInt(dp, 'count') AS count,
    JSONExtractFloat(dp, 'sum') AS sum,
    JSONExtractFloat(dp, 'max') AS latency_max,
    JSONExtractFloat(dp, 'min') AS latency_min,
    JSONExtract(dp, 'bucketCounts', 'Array(Float64)') AS bucket_count

    FROM kafka_business_metrics
    ARRAY JOIN JSONExtractArrayRaw(otlp_json, 'resourceMetrics') AS rm
    ARRAY JOIN JSONExtractArrayRaw(rm, 'scopeMetrics') AS sm
    ARRAY JOIN JSONExtractArrayRaw(sm, 'metrics') AS metric
    ARRAY JOIN JSONExtractArrayRaw(metric, 'histogram', 'dataPoints') AS dp
    WHERE JSONExtractString(metric, 'name') = 'messenger.messages.send.latency'
    );

SELECT 'MV mv_business_message_latency_metrics created successfully' AS status;