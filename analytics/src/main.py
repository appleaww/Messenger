from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn

from src.clickhouse.client import clickhouse_client
from src.config.settings import settings

app = FastAPI(
    title="Analytics Service",
    version="0.1.0",
)


if __name__ == "__main__":
    uvicorn.run("src.main:app", host="0.0.0.0", port=8000, reload=True)