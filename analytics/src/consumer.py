import os
from confluent_kafka import Consumer
from threading import Thread
import json
from .db import engine
from .models import TechnicalEvent, BusinessEvent
from sqlalchemy import text
import logging
from datetime import datetime

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

def start_consumer():
    bootstrap_servers = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:19092,localhost:29092,localhost:39092')
    conf = {
        'bootstrap.servers': bootstrap_servers,
        'group.id': 'analytics-group',
        'auto.offset.reset': 'earliest'
    }
    consumer = Consumer(conf)
    consumer.subscribe(['technical-metrics', 'business-metrics'])

    while True:
        msg = consumer.poll(1.0)
        if msg is None or msg.error():
            continue

        try:
            event_data = json.loads(msg.value().decode('utf-8'))
            topic = msg.topic()

            with engine.connect() as conn:
                if topic == 'technical-metrics':

                    event = TechnicalEvent(**event_data)
                    conn.execute(text("""
                        INSERT INTO technical_metrics (type, user_id, latency_ms, throughput, cpu_usage, memory_used_bytes, timestamp)
                        VALUES (:type, :user_id, :latency_ms, :throughput, :cpu_usage, :memory_used_bytes, :timestamp)
                    """), {
                        'type': event.type,
                        'user_id': event.userId,
                        'latency_ms': event.latencyMs,
                        'throughput': event.throughput,
                        'cpu_usage': event.cpuUsage,
                        'memory_used_bytes': event.memoryUsedBytes,
                        'timestamp': event.timestamp
                    })

                elif topic == 'business-metrics':


                    event = BusinessEvent(**event_data)
                    conn.execute(text("""
                        INSERT INTO business_metrics (type, user_id, session_duration_ms, timestamp)
                        VALUES (:type, :user_id, :session_duration_ms, :timestamp)
                    """), {
                        'type': event.type,
                        'user_id': event.userId,
                        'session_duration_ms': event.sessionDurationMs,
                        'timestamp': event.timestamp
                    })
                logger.info(f"Processed event from {topic}: {event.type}")
        except Exception as e:
            logger.error(f"Error processing message: {e}")

def run_consumer():
    Thread(target=start_consumer, daemon=True).start()