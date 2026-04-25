import asyncio
import logging
from contextlib import asynccontextmanager
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from datetime import datetime, timedelta

from src.analytics.saver import MetricsSaver
from src.config.settings import settings

logger = logging.getLogger(__name__)
scheduler = AsyncIOScheduler(timezone="Europe/Moscow")


@asynccontextmanager
async def lifespan(app):
    delay_minutes = settings.scheduler.initial_delay_minutes

    saver = MetricsSaver()
    initial_delay = timedelta(minutes=delay_minutes)
    start_date = datetime.now() + initial_delay

    scheduler.add_job(
        saver.save_kpi_metrics,
        trigger="interval", #задаем тип расписания, в данном случае - интервальный
        seconds=60,
        start_date=start_date,
        id="save_kpi_metrics", #задаем уникальное имя задачи
        replace_existing=True, #если задача с таким именем уже есть - заменяем
        misfire_grace_time=30 #если задача опоздала не более чем на 30сек, даем возможность ей запуститься
    )

    scheduler.add_job(
        saver.save_dau_mau_metrics,
        trigger="interval",
        minutes=5,
        start_date=start_date,
        id="save_dau_mau_metrics",
        replace_existing=True,
        misfire_grace_time=60
    )

    scheduler.start()
    logger.info("Scheduler started")

    try:
        yield
    finally:
        scheduler.shutdown(wait=True)
        logger.info("Scheduler shutdown")

__all__ = ["lifespan"]