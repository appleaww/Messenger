from fastapi import FastAPI
import uvicorn
from .consumer import run_consumer
from .analytics import get_metrics
from prometheus_client import make_asgi_app, Gauge, start_http_server
from .models import MetricsResponse
app = FastAPI()

start_http_server(8001)

dau_gauge = Gauge('dau', 'Daily Active Users')


@app.on_event("startup")
async def startup():
    run_consumer()

@app.get("/metrics", response_model=MetricsResponse)
def fetch_metrics():
    metrics = get_metrics()
    dau_gauge.set(metrics['dau'])
    return metrics

app.mount("/prometheus", make_asgi_app())

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)