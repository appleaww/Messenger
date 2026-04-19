CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_activity_metrics
TO user_activity_metrics AS
SELECT
    parseDateTimeBestEffortOrNull(
            JSONExtractString(event_json, 'timestamp')
    ) AS timestamp,

    JSONExtractString(event_json, 'userId')      AS user_id,

    JSONExtractString(event_json, 'actionType')  AS action_type

FROM kafka_user_activity_events;

SELECT 'MV mv_user_activity_events created successfully' AS status;