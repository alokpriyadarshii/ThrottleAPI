# ThrottleAPI

set -euo pipefail

## 1) Go to project folder (adjust if you're already there)
cd "ThrottleAPI"

## 2) Run tests
mvn -U clean test

## 3) Build (fat jar)
mvn -U -DskipTests clean package

## 4) Start server in background
API_HOST=127.0.0.1
PORT=8080
API="http://${API_HOST}:${PORT}"
PID="$(lsof -ti tcp:${PORT} 2>/dev/null || true)"; [ -n "$PID" ] && kill -9 $PID || true
JAR="$(ls -1 target/*.jar | grep -v '\.original$' | head -n 1)"
java -jar "$JAR" --server.port="$PORT" >/tmp/quota-guard.log 2>&1 & APP_PID=$!
trap 'kill "$APP_PID" 2>/dev/null || true' EXIT INT TERM

## 5) Wait until server is ready
until curl -sf "$API/actuator/health" >/dev/null; do sleep 0.2; done

## 6) Demo API calls
export API_KEY="local-dev-key"

echo "== health =="; curl -sS "$API/actuator/health" | python3 -m json.tool

echo "== policy =="; curl -sS "$API/api/v1/policy" \
  -H "X-API-Key: ${API_KEY}" \
| python3 -m json.tool

echo "== limits (user-1) =="; curl -sS "$API/api/v1/limits/user-1" \
  -H "X-API-Key: ${API_KEY}" \
| python3 -m json.tool

echo "== acquire 1 =="; curl -sS -X POST "$API/api/v1/limits/user-1:acquire" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ${API_KEY}" \
  -d '{"permits":1}' \
| python3 -m json.tool

echo "== acquire 20 =="; curl -sS -X POST "$API/api/v1/limits/user-1:acquire" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: ${API_KEY}" \
  -d '{"permits":20}' \
| python3 -m json.tool

echo "Done. Server will stop now. Logs: /tmp/quota-guard.log"

