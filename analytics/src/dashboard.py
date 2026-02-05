import streamlit as st
import pandas as pd
from analytics import get_metrics
from db import engine
from sqlalchemy import text
import plotly.express as px
import time

st.title("Messenger Analytics Dashboard")


@st.fragment(run_every=10)
def update_metrics():
    metrics = get_metrics()
    if all(v == 0 or v == 0.0 for v in metrics.values()):
        st.warning("Нет данных для метрик. Генерируйте события в приложении!")
    st.metric("DAU", metrics['dau'])
    st.metric("MAU", metrics['mau'])
    st.metric("Avg Session (ms)", metrics['avg_session_duration_ms'])
    st.metric("Avg Latency (ms)", metrics['avg_latency_ms'])
    st.metric("P95 Latency (ms)", metrics['p95_latency_ms'])
    st.metric("Avg CPU", metrics['avg_cpu'])
    st.metric("Avg Memory (MB)", metrics['avg_memory_mb'])

update_metrics()


@st.fragment(run_every=10)
def update_latency_chart():
    with engine.connect() as conn:
        df = pd.read_sql(text("SELECT timestamp, latency_ms FROM technical_metrics WHERE type = 'message_sent' ORDER BY timestamp DESC LIMIT 1000"), conn)
    if df.empty:
        st.warning("Нет данных для графика latency. Отправьте сообщения в мессенджере!")
        return
    fig = px.line(df, x='timestamp', y='latency_ms', title='Latency over Time',
                  labels={'latency_ms': 'Latency (ms)', 'timestamp': 'Time'})
    fig.update_layout(xaxis_title='Timestamp', yaxis_title='Latency (ms)')
    st.plotly_chart(fig, use_container_width=True)

update_latency_chart()


@st.fragment(run_every=10)
def update_system_chart():
    with engine.connect() as conn:
        df_sys = pd.read_sql(text("SELECT timestamp, cpu_usage, memory_used_bytes FROM technical_metrics WHERE type = 'system_metrics' ORDER BY timestamp DESC LIMIT 1000"), conn)
    if not df_sys.empty:
        fig_sys = px.line(df_sys, x='timestamp', y=['cpu_usage', 'memory_used_bytes'], title='System Metrics')
        st.plotly_chart(fig_sys, use_container_width=True)
    else:
        st.info("Нет системных метрик.")

update_system_chart()