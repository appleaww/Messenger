set dotenv-load := true
set shell := ["bash", "-uc"]
FRONTEND_DIR := "frontend"

default:
    @just --list

#docker
[group('docker')]
run *services="":
    docker compose up -d {{services}}

[group('docker')]
run-build *services="":
    docker compose up -d --build {{services}}

[group('docker')]
down:
    docker compose down

[group('docker')]
stop:
    docker compose stop

[group('docker')]
restart service="":
    docker compose restart {{service}}

[group('docker')]
logs service="":
    docker compose logs -f {{service}}

[group('docker')]
ps:
    docker compose ps

[group('docker')]
rebuild:
    just down
    just run-build

[group('docker')]
clean:
    just down -v
    docker system prune -f --volumes --all

#java
[group('java')]
build:
    just mvn clean compile

[group('java')]
package:
    just mvn clean package -DskipTests

#frontend
[group('frontend')]
ui:
   cd {{FRONTEND_DIR}} && exec npm run dev

