# ThrottleAPI

A small Java 17 + Spring Boot rate limiting microservice built around a token bucket algorithm.

Although the repository is named **ThrottleAPI**, the packaged application itself is named **QuotaGuard** (`quota-guard`). The service exposes a simple HTTP API for:

- checking the active default rate limit policy,
- inspecting a bucket for a given key,
- attempting to acquire permits for that key.

The current implementation uses an **in memory token bucket**, which makes it a good fit for local development, demos, and single instance deployments.

## Features

- Token-bucket rate limiting
- Simple REST API for status and permit acquisition
- API-key authentication for application endpoints
- Spring Boot Actuator health/info endpoints
- Clean separation of API, service, domain, and infrastructure layers
- Dockerfile and Docker Compose support
- Unit and integration tests
- GitHub Actions CI workflow

## Tech Stack

- Java 17
- Spring Boot 3.2
- Maven
- Spring Security
- Spring Validation
- Spring Actuator

## Project Layout

```text
ThrottleAPI/
├── docs/
│   └── ARCHITECTURE.md
├── src/
│   ├── main/
│   │   ├── java/com/redstone/quotaguard/
│   │   │   ├── api/
│   │   │   ├── config/
│   │   │   ├── domain/
│   │   │   ├── infra/
│   │   │   ├── service/
│   │   │   └── QuotaGuardApplication.java
│   │   └── resources/application.yml
│   └── test/
│       └── java/com/redstone/quotaguard/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## How It Works

The service applies a default rate-limit policy to each bucket key:

- **capacity**: maximum number of tokens in the bucket
- **refillTokensPerSecond**: steady refill rate

When a client requests permits:

- the bucket is refilled based on elapsed time,
- the request is allowed if enough tokens are available,
- otherwise it is rejected and a suggested `retryAfterMs` is returned.

The current implementation is:

- **strongly consistent per key within one JVM process**,
- **not shared across multiple app instances**.

For horizontal scaling, the `RateLimiter` interface is designed so the in-memory implementation can be replaced by a distributed backend such as Redis.

## Default Configuration

`src/main/resources/application.yml` defines these defaults:

```yaml
quotaguard:
  limiter:
    mode: in-memory
  default-policy:
    capacity: 10
    refillTokensPerSecond: 1.0
  security:
    api-keys:
      - local-dev-key
```

This means:

- each new key starts with **10 tokens**,
- the bucket refills at **1 token per second**,
- application endpoints require the header `X-API-Key: local-dev-key` by default.

## API Endpoints

### Health

Unauthenticated health and info endpoints are exposed via Spring Boot Actuator:

- `GET /actuator/health`
- `GET /actuator/info`

### Application API

All `/api/v1/**` endpoints require `X-API-Key` when one or more API keys are configured.

#### Get default policy

```http
GET /api/v1/policy
X-API-Key: local-dev-key
```

Example response:

```json
{
  "name": "default",
  "capacity": 10,
  "refillTokensPerSecond": 1.0
}
```

#### Get bucket status for a key

```http
GET /api/v1/limits/{key}
X-API-Key: local-dev-key
```

Example response:

```json
{
  "tokens": 10.0,
  "capacity": 10
}
```

#### Acquire permits for a key

```http
POST /api/v1/limits/{key}:acquire
Content-Type: application/json
X-API-Key: local-dev-key
```

Request body:

```json
{
  "permits": 1
}
```

Allowed response example:

```json
{
  "allowed": true,
  "remainingTokens": 9.0,
  "retryAfterMs": 0
}
```

Rejected response example:

```json
{
  "allowed": false,
  "remainingTokens": 0.3,
  "retryAfterMs": 700
}
```

### Error Behavior

Common error cases include:

- missing or invalid API key → `401 {"error":"unauthorized"}`
- invalid request body (for example `permits < 1`) → `400`
- blank key or invalid policy inputs → `400`

If a request asks for more permits than the bucket capacity, the limiter rejects it without consuming tokens.

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.9+

### Run tests

```bash
mvn clean test
```

### Package the application

```bash
mvn -DskipTests clean package
```

### Start the app

```bash
java -jar target/quota-guard-1.0.0.jar
```

By default, the server starts on port `8080`.

## Local API Demo

```bash
export API=http://127.0.0.1:8080
export API_KEY=local-dev-key

curl -s "$API/actuator/health"

curl -s "$API/api/v1/policy" \
  -H "X-API-Key: $API_KEY"

curl -s "$API/api/v1/limits/user-1" \
  -H "X-API-Key: $API_KEY"

curl -s -X POST "$API/api/v1/limits/user-1:acquire" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"permits":1}'
```

## Configuration Overrides

Spring Boot environment-variable binding lets you override the defaults at runtime.

Examples:

```bash
export QUOTAGUARD_DEFAULT_POLICY_CAPACITY=20
export QUOTAGUARD_DEFAULT_POLICY_REFILL_TOKENS_PER_SECOND=2.5
export QUOTAGUARD_SECURITY_API_KEYS=local-dev-key,another-key
```

Then start the application normally:

```bash
java -jar target/quota-guard-1.0.0.jar
```

### Security Note

If the configured API key list is empty, the current security configuration allows all application endpoints without authentication.

## Docker

### Build and run with Docker

```bash
docker build -t throttleapi .
docker run -p 8080:8080 \
  -e QUOTAGUARD_SECURITY_API_KEYS=local-dev-key \
  throttleapi
```

### Run with Docker Compose

```bash
docker compose up --build
```

The provided Compose file exposes port `8080` and sets:

- `QUOTAGUARD_SECURITY_API_KEYS=local-dev-key`
- `QUOTAGUARD_DEFAULT_POLICY_CAPACITY=10`
- `QUOTAGUARD_DEFAULT_POLICY_REFILL_TOKENS_PER_SECOND=1.0`

## Testing

The repository includes:

- **unit tests** for the in-memory token-bucket limiter
- **integration tests** for the HTTP controller and API-key behavior

Run them with:

```bash
mvn test
```

## CI

GitHub Actions is configured to:

- check out the repository,
- install Temurin Java 17,
- run `mvn -B test`.

## Architecture Notes

The code follows a lightweight layered structure:

- **API layer**: controllers, DTOs, exception handling
- **Service layer**: use-case orchestration
- **Domain layer**: policy and limiter contract
- **Infrastructure layer**: in-memory token bucket implementation

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the included architecture note.

## Current Limitations

- only the in-memory limiter is implemented
- limits are not shared across multiple instances
- no persistence layer
- no per-key custom policy storage
- no Redis or distributed backend yet
- no built-in metrics dashboard beyond standard Actuator endpoints

## Suggested Next Steps

Good follow-on improvements for this project would be:

1. add a Redis-backed `RateLimiter` implementation,
2. support per-tenant or per-key policies,
3. expose richer metrics for throttling decisions,
4. add request tracing and structured logging,
5. publish an OpenAPI spec for the REST API.
