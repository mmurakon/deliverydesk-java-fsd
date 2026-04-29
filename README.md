# Java FSD Project: DeliveryDesk

DeliveryDesk is a small full-stack Java project for tracking delivery work across a software team. It includes a Java 17 backend, REST-style API endpoints, an in-browser frontend, seeded data, and a tiny test harness.

## What It Shows

- Java backend with routing, request parsing, response helpers, and in-memory storage
- REST-style JSON API for projects, metrics, and status updates
- Responsive frontend with filtering, forms, optimistic UI updates, and dashboards
- No external dependencies, so it runs with only the JDK

## Run

```bash
./scripts/run.sh
```

Then open:

```text
http://localhost:8080
```

Use a different port:

```bash
PORT=9090 ./scripts/run.sh
```

## Test

```bash
./scripts/test.sh
```

## API

```text
GET    /api/projects
POST   /api/projects
PATCH  /api/projects/{id}/status
GET    /api/metrics
```

Example create payload:

```json
{
  "name": "Customer Portal",
  "owner": "Priya",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-05-15",
  "description": "Build account dashboard and billing workflows."
}
```

Example status update:

```json
{
  "status": "DONE"
}
```

## Project Layout

```text
src/main/java/com/codex/fsd  Java backend
src/test/java/com/codex/fsd  Lightweight tests
frontend                         Static UI served by Java
scripts                          Run and test scripts
```
