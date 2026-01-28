# Architecture

QuotaGuard follows a lightweight Clean Architecture approach:

- **API layer** (`api/`): HTTP controllers + exception mapping
- **Service layer** (`service/`): application use-cases
- **Domain layer** (`domain/`): business rules (policy + rate limiter contract)
- **Infra layer** (`infra/`): implementation details (in-memory token bucket)

## Flow
`HTTP -> Controller -> AcquireService -> RateLimiter -> token bucket`

## Why this structure?
- Keeps business logic independent of Spring and transport
- Makes it easy to swap infra implementations (e.g., Redis) without touching API/use-case code

## Scaling notes
The current in-memory limiter is strongly consistent per key **within a single process**.
For multiple instances, implement a distributed limiter (e.g., Redis + Lua script) behind `RateLimiter`.
