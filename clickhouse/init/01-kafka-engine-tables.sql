--создаем таблицы, которые читают данные из топиков, в каждой строке таблицы отдельный json файл в виде строки
CREATE TABLE IF NOT EXISTS kafka_business_metrics(otlp_json String) ENGINE = Kafka
SETTINGS
    kafka_broker_list = 'kafka:9092,kafka2:9092,kafka3:9092',
    kafka_topic_list = 'business-metrics',
    kafka_group_name = 'clickhouse-business-consumer-group',
    kafka_format = 'JSONEachRow',
    kafka_num_consumers = 3,
    kafka_max_block_size = 8192;

CREATE TABLE IF NOT EXISTS kafka_technical_metrics (otlp_json String) ENGINE = Kafka
SETTINGS
    kafka_broker_list = 'kafka:9092,kafka2:9092,kafka3:9092',
    kafka_topic_list = 'technical-metrics',
    kafka_group_name = 'clickhouse-technical-consumer-group',
    kafka_format = 'JSONEachRow',
    kafka_num_consumers = 3,
    kafka_max_block_size = 8192;

SELECT 'Kafka Engine tables created successfully' AS status;