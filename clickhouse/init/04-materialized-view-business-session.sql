CREATE MATERIALIZED VIEW IF NOT EXISTS mv_business_session_metrics
TO business_session_metrics AS
SELECT
    timestamp,
    session_count,
    round(session_avg / 1000.0, 3)                               AS session_avg,
    round(session_max / 1000.0, 3)                               AS session_max

FROM (
    SELECT
    toDateTime(
    toStartOfInterval(
    fromUnixTimestamp64Nano(JSONExtractUInt(dp, 'timeUnixNano')),
    INTERVAL 15 SECOND
    )
    ) AS timestamp,

    JSONExtractUInt(dp, 'count')                                 AS session_count,

    if(JSONExtractUInt(dp, 'count') > 0,
    JSONExtractFloat(dp, 'sum') / JSONExtractUInt(dp, 'count'),
    0)                                                        AS session_avg,

    JSONExtractFloat(dp, 'max')                                  AS session_max

    FROM kafka_business_metrics

    ARRAY JOIN JSONExtractArrayRaw(otlp_json, 'resourceMetrics')     AS rm
    ARRAY JOIN JSONExtractArrayRaw(rm, 'scopeMetrics')               AS sm
    ARRAY JOIN JSONExtractArrayRaw(sm, 'metrics')                    AS metric
    ARRAY JOIN JSONExtractArrayRaw(metric, 'histogram', 'dataPoints') AS dp

    WHERE JSONExtractString(metric, 'name') = 'messenger.sessions.duration'
    );

SELECT 'MV mv_business_session_metrics created successfully' AS status;

