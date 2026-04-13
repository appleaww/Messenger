CREATE MATERIALIZED VIEW IF NOT EXISTS mv_technical_metrics
TO technical_metrics AS
SELECT
    timestamp,
    metric_name,
    metric_type,
    value,
    attributes
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
    JSONHas(metric, 'sum'),       'counter',
    JSONHas(metric, 'gauge'),     'gauge',
    JSONHas(metric, 'histogram'), 'histogram',
    'other'
    ) AS metric_type,

    if(JSONHas(metric, 'sum'),
    JSONExtractFloat(dp, 'asDouble'),
    if(JSONHas(metric, 'gauge'),
    JSONExtractFloat(dp, 'asDouble'),
    JSONExtractFloat(dp, 'sum')
    )
    ) AS value,

    mapFromArrays(
    JSONExtractArrayRaw(JSONExtractRaw(dp, 'attributes'), 'key'),
    arrayMap(
    x -> coalesce(
    JSONExtractString(x, 'stringValue'),
    toString(JSONExtractInt(x, 'intValue')),
    toString(JSONExtractFloat(x, 'doubleValue')),
    toString(JSONExtractBool(x, 'boolValue')),
    'null'
    ),
    JSONExtractArrayRaw(JSONExtractRaw(dp, 'attributes'), 'value')
    )
    ) AS attributes

    FROM kafka_technical_metrics

    ARRAY JOIN JSONExtractArrayRaw(otlp_json, 'resourceMetrics') AS rm
    ARRAY JOIN JSONExtractArrayRaw(rm, 'scopeMetrics') AS sm
    ARRAY JOIN JSONExtractArrayRaw(sm, 'metrics') AS metric

    ARRAY JOIN arrayConcat(
    JSONExtractArrayRaw(metric, 'sum', 'dataPoints'),
    JSONExtractArrayRaw(metric, 'gauge', 'dataPoints'),
    JSONExtractArrayRaw(metric, 'histogram', 'dataPoints')
    ) AS dp
    );

SELECT 'MV mv_technical_metrics created successfully' AS status;
