# Environment Variables

`MyStarNow BFF` 서버 실행과 운영에 필요한 환경 변수 기준 문서다.  
대상 시스템은 `Kotlin + Spring Boot + PostgreSQL + Flyway + Redis` 기반 백엔드다.

# Rules

- 민감 정보는 코드나 문서에 실제 값으로 넣지 않는다.
- `.env` 또는 배포 환경의 secret store를 사용한다.
- 변수명은 대문자 snake case를 사용한다.
- 기본값이 있는 변수와 필수 변수를 구분한다.

# Runtime Basics

## Required

```text
SPRING_PROFILES_ACTIVE
SERVER_PORT
APP_BASE_URL
APP_TIMEZONE
```

설명:

- `SPRING_PROFILES_ACTIVE`
  - 예: `local`, `dev`, `staging`, `prod`
- `SERVER_PORT`
  - 예: `8080`
- `APP_BASE_URL`
  - 예: `https://api.mystarnow.com`
- `APP_TIMEZONE`
  - 예: `Asia/Seoul`

## Recommended Defaults

```text
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080
APP_TIMEZONE=Asia/Seoul
```

# Database

## Required

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

## Optional

```text
DB_SCHEMA
DB_POOL_MAX_SIZE
DB_POOL_MIN_IDLE
DB_POOL_CONNECTION_TIMEOUT_MS
```

## Example Mapping

```text
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mystarnow
DB_USERNAME=mystarnow
DB_PASSWORD=*****
DB_SCHEMA=public
DB_POOL_MAX_SIZE=10
DB_POOL_MIN_IDLE=2
DB_POOL_CONNECTION_TIMEOUT_MS=3000
```

# Redis

## Required For Standard Deployment

```text
REDIS_HOST
REDIS_PORT
```

## Optional

```text
REDIS_PASSWORD
REDIS_DATABASE
REDIS_SSL_ENABLED
REDIS_TIMEOUT_MS
```

## Example Mapping

```text
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_SSL_ENABLED=false
REDIS_TIMEOUT_MS=1000
```

# Security / Operator Access

## Required

```text
OPERATOR_AUTH_ENABLED
OPERATOR_USERNAME
OPERATOR_PASSWORD
```

## Optional

```text
ALLOWED_OPERATOR_IPS
ADMIN_BASIC_AUTH_REALM
```

설명:

- 초기 단계에서는 간단한 internal auth 또는 basic auth 기반 운영 경로 보호를 가정
- 운영 환경에서는 secret manager 사용 권장

# Platform Integrations

## YouTube

### Required In Phase 1

```text
YOUTUBE_API_KEY
YOUTUBE_API_BASE_URL
YOUTUBE_API_TIMEOUT_MS
```

### Optional

```text
YOUTUBE_MAX_RESULTS
YOUTUBE_CHANNEL_METADATA_POLL_MINUTES
YOUTUBE_ACTIVITY_POLL_MINUTES
YOUTUBE_LIVE_POLL_MINUTES
YOUTUBE_ENABLE_ETAG
```

### Recommended Defaults

```text
YOUTUBE_API_BASE_URL=https://www.googleapis.com/youtube/v3
YOUTUBE_API_TIMEOUT_MS=3000
YOUTUBE_MAX_RESULTS=20
YOUTUBE_CHANNEL_METADATA_POLL_MINUTES=720
YOUTUBE_ACTIVITY_POLL_MINUTES=15
YOUTUBE_LIVE_POLL_MINUTES=3
YOUTUBE_ENABLE_ETAG=true
```

## Instagram

### Optional In Phase 1

```text
INSTAGRAM_ENABLED
INSTAGRAM_API_BASE_URL
INSTAGRAM_ACCESS_TOKEN
INSTAGRAM_TIMEOUT_MS
```

### Recommended Defaults

```text
INSTAGRAM_ENABLED=false
INSTAGRAM_API_BASE_URL=https://graph.facebook.com
INSTAGRAM_TIMEOUT_MS=3000
```

설명:

- 현재 1차 전략은 `manual-first`
- 자동 수집을 켤 때만 token/endpoint가 실제로 필요

## X

### Future / Disabled By Default

```text
X_ENABLED
X_API_BASE_URL
X_BEARER_TOKEN
X_TIMEOUT_MS
```

### Recommended Defaults

```text
X_ENABLED=false
X_API_BASE_URL=https://api.x.com/2
X_TIMEOUT_MS=3000
```

## CHZZK

### Future / Disabled By Default

```text
CHZZK_ENABLED
CHZZK_API_BASE_URL
CHZZK_CLIENT_ID
CHZZK_CLIENT_SECRET
CHZZK_TIMEOUT_MS
```

## SOOP

### Future / Disabled Or Manual-only

```text
SOOP_ENABLED
SOOP_API_BASE_URL
SOOP_CLIENT_ID
SOOP_CLIENT_SECRET
SOOP_TIMEOUT_MS
```

# Polling / Scheduling

## Required

```text
SCHEDULER_ENABLED
SYNC_BATCH_SIZE
SYNC_WORKER_THREADS
```

## Optional

```text
LIVE_STATUS_POLL_MINUTES
ACTIVITY_POLL_MINUTES
SCHEDULE_RECONCILE_MINUTES
METADATA_VALIDATION_HOURS
SYNC_BACKOFF_BASE_MINUTES
SYNC_BACKOFF_MAX_MINUTES
```

## Recommended Defaults

```text
SCHEDULER_ENABLED=true
SYNC_BATCH_SIZE=50
SYNC_WORKER_THREADS=4
LIVE_STATUS_POLL_MINUTES=3
ACTIVITY_POLL_MINUTES=15
SCHEDULE_RECONCILE_MINUTES=30
METADATA_VALIDATION_HOURS=12
SYNC_BACKOFF_BASE_MINUTES=3
SYNC_BACKOFF_MAX_MINUTES=30
```

# Reliability / Fallback

## Optional

```text
UPSTREAM_CONNECT_TIMEOUT_MS
UPSTREAM_READ_TIMEOUT_MS
UPSTREAM_TOTAL_TIMEOUT_MS
LIVE_STATUS_STALE_MINUTES
ACTIVITY_STALE_MINUTES
SCHEDULE_STALE_HOURS
ENABLE_STALE_FALLBACK
ENABLE_MANUAL_FALLBACK
```

## Recommended Defaults

```text
UPSTREAM_CONNECT_TIMEOUT_MS=1000
UPSTREAM_READ_TIMEOUT_MS=2000
UPSTREAM_TOTAL_TIMEOUT_MS=3000
LIVE_STATUS_STALE_MINUTES=5
ACTIVITY_STALE_MINUTES=30
SCHEDULE_STALE_HOURS=24
ENABLE_STALE_FALLBACK=true
ENABLE_MANUAL_FALLBACK=true
```

# Observability

## Required

```text
ACTUATOR_ENABLED
METRICS_ENABLED
LOG_LEVEL_ROOT
LOG_LEVEL_APP
```

## Optional

```text
PROMETHEUS_ENABLED
TRACING_ENABLED
SENTRY_DSN
HEALTH_DETAILS_ENABLED
```

## Recommended Defaults

```text
ACTUATOR_ENABLED=true
METRICS_ENABLED=true
PROMETHEUS_ENABLED=true
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=INFO
HEALTH_DETAILS_ENABLED=false
```

# Feature Flags

## Optional

```text
FEATURE_SHOW_SCHEDULES
FEATURE_SHOW_RECENT_ACTIVITIES
FEATURE_ENABLE_LIVE_NOW
FEATURE_PLATFORM_YOUTUBE
FEATURE_PLATFORM_INSTAGRAM
FEATURE_PLATFORM_X
FEATURE_PLATFORM_CHZZK
FEATURE_PLATFORM_SOOP
```

## Recommended Defaults

```text
FEATURE_SHOW_SCHEDULES=true
FEATURE_SHOW_RECENT_ACTIVITIES=true
FEATURE_ENABLE_LIVE_NOW=true
FEATURE_PLATFORM_YOUTUBE=true
FEATURE_PLATFORM_INSTAGRAM=true
FEATURE_PLATFORM_X=false
FEATURE_PLATFORM_CHZZK=false
FEATURE_PLATFORM_SOOP=false
```

# Environment Profiles

## local

- YouTube만 연결해도 됨
- Instagram 자동 수집은 기본 off
- Redis/Postgres는 Docker Compose 권장

## dev

- 실제 API key 사용 가능
- operator auth는 간단하게 유지
- metrics on

## staging

- prod와 유사한 polling
- alert/metrics 반드시 on
- feature flag 검증

## prod

- secret store 사용
- actuator 보호
- operator auth 강화
- platform enablement를 config로 제어

# Minimal Local Example

```text
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080
APP_BASE_URL=http://localhost:8080
APP_TIMEZONE=Asia/Seoul

DB_HOST=localhost
DB_PORT=5432
DB_NAME=mystarnow
DB_USERNAME=mystarnow
DB_PASSWORD=localpass

REDIS_HOST=localhost
REDIS_PORT=6379

YOUTUBE_API_KEY=*****
YOUTUBE_API_BASE_URL=https://www.googleapis.com/youtube/v3

INSTAGRAM_ENABLED=false
X_ENABLED=false
CHZZK_ENABLED=false
SOOP_ENABLED=false
```

# Notes

- 새 플랫폼이 활성화되면 이 문서에 관련 환경 변수를 먼저 추가한다.
- 로컬 실행 가이드는 이후 별도 `ops/run-local.md`로 분리할 수 있다.
- secret 값은 이 문서에 실값으로 남기지 않는다.
