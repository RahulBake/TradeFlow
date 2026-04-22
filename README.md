# TradeFlow

Spring Boot backend for the FYNXT Java Developer Assignment. The app handles order booking, portfolio maintenance, and sector overlap analysis using Java 17, Spring Boot, JPA, and an in-memory H2 database.

## Endpoints

`POST /orders`

Request:
```json
{
  "traderId": "T001",
  "stock": "AAPL",
  "sector": "TECH",
  "quantity": 50,
  "side": "BUY"
}
```

`POST /orders/{id}/fill`

Marks a pending order as filled and applies the position update to the portfolio.

`POST /orders/{id}/cancel`

Marks a pending order as cancelled.

`GET /traders/{traderId}/portfolio`

Returns:
```json
{
  "traderId": "T001",
  "positions": {
    "AAPL": 150,
    "TSLA": 80
  },
  "sectorBreakdown": {
    "TECH": 230
  }
}
```

`GET /traders/{traderId}/portfolio/overlap`

Returns:
```json
{
  "overlaps": [
    { "basket": "TECH_HEAVY", "overlap": "60.00%" },
    { "basket": "FINANCE_HEAVY", "overlap": "0.00%" },
    { "basket": "BALANCED", "overlap": "40.00%" }
  ],
  "dominantBasket": "TECH_HEAVY",
  "riskFlag": "HIGH"
}
```

`POST /traders/{traderId}/portfolio`

Request:
```json
{
  "stock": "NVDA",
  "sector": "TECH",
  "quantity": 100
}
```

Adds holdings directly into the trader portfolio and returns the updated summary.

## Business Rules Implemented

- A trader cannot have more than 3 pending orders.
- SELL orders are rejected if the trader does not currently hold enough shares.
- Orders move from `PENDING` to either `FILLED` or `CANCELLED`.
- Filling a BUY order increases holdings; filling a SELL order decreases holdings.
- Sector overlap is calculated in pure Java with no DB access.
- Risk flags follow the assignment thresholds:
  - `HIGH` for any overlap `>= 60%`
  - `MEDIUM` for any overlap `>= 40%`
  - `LOW` otherwise

## Concurrency Notes

- `Order` and `Portfolio` use optimistic locking via `@Version`.
- Trader row locking serializes order placement for the same trader, which protects the `max 3 pending orders` rule under concurrent requests.
- Pessimistic row locks are used when mutating orders and portfolio positions to reduce race conditions during fills and manual portfolio updates.

## Database

- H2 in-memory database
- `schema.sql` defines all tables and constraints
- Foreign keys are enforced from orders and portfolio rows back to traders

## Running

Prerequisites:
- Java 17+
- Maven 3.9+

Run with Maven:
```bash
./mvnw spring-boot:run
```

Run tests:
```bash
./mvnw test
```

## Design Choices And Trade-offs

- Entities are not exposed directly in API responses; DTOs are used for cleaner contracts.
- Traders are auto-created on first order placement or direct portfolio add because the assignment does not define a separate trader-creation API.
- Portfolio overlap logic is kept in a pure Java utility so it can be tested independently from Spring and the database.
- Error handling maps validation, missing resources, and business-rule conflicts to separate HTTP statuses.

## What Was Intentionally Kept Simple

- Authentication and authorization are out of scope.
- The app uses H2 for simplicity instead of an external relational database.
- Error payloads are intentionally small and consistent: `{ "error": "..." }`.
