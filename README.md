### Описание проекта 
****
....

В проекте(backend) использованы следующие технологии и инструменты: 

Spring Boot, Hibernate, REST, WebSocket, Lombok, Micrometer, MeterRegistry, PostgreSQL, ClickHouse, ClickHouse kafka engine, Apache Kafka,
OpenTelemetry Java Agent, grafana, JUnit, Mockito, AssertJ
****
### Архитектура
****
![](images/arch.png)
****
### Демонстрация работы
****
![](images/usage_example_2000.gif)
****
### Схема базы данных PostgreSQL
****
![](images/db-schema-demo.gif)
****
### Схема базы данных ClickHouse
****
![](images/click-house-schema.png)
****
### Дашборды в Grafana
****
![](images/grafana_business_metrics_image.png)
****
![](images/grafana_technical_metrics_image.png)
****
![](images/grafana_dau_mau_metrics_image.png)
****
### Запуск проекта
****
 Проект использует инструмент - just(https://just.systems) — современный и удобный runner команд.
 * Для macOS
```bash 
  brew install just
 ```
* Запуск
```bash 
  just run
 ```
* UI
```bash
  just ui
```

