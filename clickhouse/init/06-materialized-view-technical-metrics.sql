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

    multiIf(
    JSONHas(dp, 'asDouble'), JSONExtractFloat(dp, 'asDouble'),
    JSONHas(dp, 'asInt'),    toFloat64OrZero(JSONExtractString(dp, 'asInt')),
    JSONHas(dp, 'sum'),      JSONExtractFloat(dp, 'sum'),
    0
    ) AS value,

    mapFromArrays(
    arrayMap(x -> JSONExtractString(x, 'key'), JSONExtractArrayRaw(dp, 'attributes')),
    arrayMap(x -> coalesce(
    JSONExtractString(x, 'value', 'stringValue'),
    toString(JSONExtractInt(x, 'value', 'intValue')),
    toString(JSONExtractFloat(x, 'value', 'doubleValue')),
    toString(JSONExtractBool(x, 'value', 'boolValue')),
    'null'
    ), JSONExtractArrayRaw(dp, 'attributes'))
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

    WHERE JSONExtractString(metric, 'name') IN (
    'jvm.memory.used',
    'jvm.memory.limit',
    'jvm.memory.committed',
    'jvm.gc.duration',
    'jvm.cpu.recent_utilization',
    'jvm.thread.count',

    'http.server.request.duration',

    'db.client.connections.usage',
    'db.client.connections.wait_time',
    'db.client.connections.use_time',

    'kafka.producer.record_send_total',
    'kafka.producer.record_error_rate',
    'kafka.producer.requests_in_flight',
    'kafka.producer.outgoing_byte_rate',
    'kafka.producer.connection_count')
    );

SELECT 'MV mv_technical_metrics created successfully' AS status;