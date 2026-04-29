# BiteFlow: Java + React Food Ordering App

BiteFlow is a full-stack food ordering app built with a Java 17 backend and a React frontend. Customers can browse menu items, build a cart, place delivery orders, and watch orders move through kitchen statuses.

## Features

- Java HTTP API using only the JDK
- React frontend served by the Java backend
- Menu browsing by category
- Cart quantity controls and checkout form
- Order queue with status updates
- Restaurant metrics for menu count, open orders, delivered orders, and revenue
- Lightweight Java test harness

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
GET    /api/menu
GET    /api/orders
POST   /api/orders
PATCH  /api/orders/{id}/status
GET    /api/metrics
```

Example order payload:

```json
{
  "customerName": "Priya",
  "address": "120 Spring Street",
  "items": "1:2,3:1",
  "notes": "No onions, call on arrival"
}
```

`items` uses `menuItemId:quantity` pairs so the Java app can stay dependency-free without a JSON library.

Example status update:

```json
{
  "status": "PREPARING"
}
```

Supported statuses:

```text
RECEIVED
PREPARING
OUT_FOR_DELIVERY
DELIVERED
```

## Project Layout

```text
src/main/java/com/codex/fsd  Java backend and domain logic
src/test/java/com/codex/fsd  Lightweight tests
frontend                         React UI served by Java
scripts                          Run and test scripts
```

The React UI uses React 18 from a CDN in `frontend/index.html`, while the API and static file server are implemented in Java.
