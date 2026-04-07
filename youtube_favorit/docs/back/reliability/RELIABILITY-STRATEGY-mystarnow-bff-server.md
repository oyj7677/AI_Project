# Failure Scenarios

`MyStarNow BFF`는 외부 플랫폼 장애를 정상 상태의 일부로 가정하고 설계한다. 장애 시 목표는 “정확한 실패”보다 “안정적인 부분 성공”이다.

## External API Failure Types

### 1. Upstream 5xx

- YouTube, X, CHZZK, SOOP 등의 서버 오류
- 영향:
  - 해당 플랫폼의 sync job 실패
  - 마지막 정상 cache가 있으면 serving 유지
  - 홈/상세의 관련 섹션만 partial 또는 stale 처리

### 2. Upstream 4xx

- 인증 실패
- 권한 부족
- quota 초과
- 잘못된 요청 파라미터
- 영향:
  - retry 정책을 보수적으로 적용
  - 동일 원인 반복 시 circuit-like backoff 강화
  - 플랫폼별 장애로 격리

### 3. Timeout / Slow Response

- upstream 응답 지연
- 일부 비공식/제한적 소스의 응답 불안정
- 영향:
  - adapter 단에서 bounded timeout 처리
  - partial data 유지
  - 전체 API blocking 금지

### 4. Unofficial Source Instability

- Instagram limited source
- 향후 scraping 또는 제한적 import source
- 영향:
  - auto source 신뢰도 낮음
  - manual fallback을 primary backup으로 사용

### 5. Data Shape Drift

- upstream JSON 구조 변경
- 필드 누락
- enum 변경
- 영향:
  - raw 저장 후 normalization 실패 기록
  - 기존 normalized cache 유지

### 6. Polling Backlog

- queue 적체
- 특정 플랫폼 worker 지연
- 영향:
  - freshness 저하
  - stale 비율 상승
  - endpoint는 degraded mode로 응답

### 7. Internal Projection Failure

- adapter는 성공했으나 normalized projection update 실패
- 영향:
  - raw source 저장
  - projection refresh retry
  - 기존 serving cache 유지

## Failure Isolation Principle

- 한 플랫폼 실패가 다른 플랫폼 sync를 막지 않는다.
- 한 resource scope 실패가 다른 scope를 막지 않는다.
- 한 섹션 실패가 endpoint 전체 실패로 확대되지 않는다.
- endpoint 전체 실패는 “의미 있는 top-level payload를 만들 수 없는 경우”에만 허용한다.

# Timeout Policy

기본 원칙:

- timeout은 명시적으로 설정한다.
- upstream 응답을 오래 기다리기보다 빠르게 실패하고 stale cache를 사용한다.
- timeout 값은 data type별 신선도 기대와 비용을 반영해 다르게 둔다.

## Recommended Timeout Defaults

### Upstream HTTP Timeout

```text
connect timeout: 1s
tls handshake timeout: 1s
read timeout: 2s
total request timeout: 3s
```

적용 대상:

- live status
- recent activity
- profile metadata

### Manual / Import Source Timeout

```text
import read timeout: 5s
total import timeout: 8s
```

적용 대상:

- operator-fed import
- sheet sync
- admin-driven enrichment

### Internal Projection Timeout

```text
projection update budget: 2s
db write batch timeout: 2s
cache update timeout: 500ms
```

## Timeout by Data Type

### Live Status

- total upstream timeout: 2s
- 이유:
  - latency sensitivity 높음
  - stale fallback 허용 가능

### Recent Activities

- total upstream timeout: 3s
- 이유:
  - 실시간성보다 completeness가 조금 더 중요

### Schedules

- total source timeout: 5s
- 이유:
  - 대부분 manual/import
  - stale tolerance 높음

### Metadata Validation

- total source timeout: 5s
- 이유:
  - user-facing urgency 낮음
  - batch성 특성 강함

# Retry Strategy

기본 원칙:

- retry는 bounded
- 동일 요청에 무한 재시도 금지
- retry는 idempotent read 성격 요청에만 적용
- 4xx 중 quota/auth 계열은 즉시 backoff 강화

## Recommended Retry Policy

### Live Status

```text
max attempts: 2
retry delay: 300ms jitter
retryable: timeout, connection reset, 502, 503, 504
non-retryable: 400, 401, 403, 404, validation parsing failure
```

### Recent Activities

```text
max attempts: 2
retry delay: 500ms jitter
retryable: timeout, 429, 502, 503, 504
non-retryable: auth errors, invalid query, permanent shape errors
```

### Metadata Validation

```text
max attempts: 3
retry delay: exponential backoff starting at 1s
```

## Backoff Rules

`platform_sync_metadata` 기준:

- 1회 실패: 다음 scheduled poll 유지
- 2회 연속 실패: 2x backoff
- 3회 연속 실패: 5x backoff
- 5회 이상 연속 실패: `degraded` platform mode + alert

예시:

```text
live poll base: 3m
after 2 fails: 6m
after 3 fails: 15m
after 5 fails: 30m or operator review
```

## Circuit-like Protection

정식 circuit breaker를 도입하지 않더라도 다음 규칙 적용:

- 동일 platform + scope 조합이 짧은 시간 안에 연속 실패하면 next poll을 뒤로 민다
- auth/quota failures는 즉시 aggressive backoff
- serving path에서는 adapter를 직접 호출하지 않는다

# Fallback Strategy

기본 원칙:

- empty보다 stale-but-usable을 우선한다
- manual data가 있으면 auto source 실패보다 우선해 유지한다
- fallback은 섹션/필드 단위로 다르게 적용한다

## Fallback Priority

```text
operator override
-> last successful normalized cache
-> previous denormalized serving state
-> empty state
```

## Section-specific Fallback

### Home `liveNow`

- 1순위: fresh `live_status_cache`
- 2순위: stale `live_status_cache` + stale 표시
- 3순위: empty state

### Home `latestUpdates`

- 1순위: fresh `activity_items`
- 2순위: stale `activity_items`
- 3순위: manual notice/activity entries
- 4순위: empty state

### Home `todaySchedules`

- 1순위: fresh/manual `schedule_items`
- 2순위: stale `schedule_items`
- 3순위: empty state

### Home `featuredInfluencers`

- 1순위: `influencer_serving_state` + `influencers`
- 2순위: previous projection snapshot
- 3순위: empty state

### Detail `profile` / `channels`

- 1순위: operator override merged profile/channel
- 2순위: last successful normalized metadata
- 3순위: partial field omission, not full object removal

### Detail `recentActivities`

- 1순위: fresh activity list
- 2순위: stale activity list
- 3순위: manual activity/notice entries
- 4순위: empty list with degraded marker

## Platform-specific Fallback Guidance

### YouTube

- stale normalized cache preferred
- live status may degrade to stale faster than profile/activity

### Instagram

- manual entries are primary fallback
- auto source failure should rarely create empty section if manual data exists

### X / CHZZK / SOOP

- disabled 플랫폼은 app-config로 hidden/disabled 처리
- future enable 후에도 failure 시 manual-only mode 전환 가능

# Degraded Response Rules

## Endpoint-level Rule

- top-level payload 일부라도 만들 수 있으면 HTTP `200`
- `meta.partialFailure=true`
- `errors[]`에 request-scope가 아닌 section-scope 오류 기록

## Section-level Rule

각 section은 아래 중 하나:

- `success`
- `partial`
- `failed`
- `empty`

## When to Mark `partial`

- 일부 source 실패 but usable data exists
- stale cache 사용 중
- manual fallback으로 대체됨

## When to Mark `failed`

- 해당 section에 usable data가 전혀 없음
- source도 없고 cache도 없음

## When to Mark `empty`

- 데이터가 정상적으로 없을 뿐 오류는 아님
- 예: 오늘 예정 방송 없음

## User-visible API Behavior

- stale 데이터 사용 시 `freshness=stale`
- manual fallback 사용 시 `freshness=manual`
- 오류 상세는 `data.<section>.error`와 top-level `errors[]` 둘 다 제공
- 클라이언트는 section key 존재를 신뢰할 수 있어야 함

## Total Response Failure

아래 경우만 5xx 허용:

- DB 또는 cache access 자체 실패
- projection layer가 전부 비어 있고 fallback도 없음
- config/read path 자체가 손상되어 의미 있는 payload 생성 불가

# Data Freshness Rules

## Freshness Labels

```text
fresh
stale
manual
unknown
```

## Recommended Polling Intervals

### Live Status

```text
YouTube: every 3 minutes
Instagram: no strict live polling in phase 1
X: every 5 minutes when enabled
CHZZK: every 3 minutes when enabled
SOOP: every 3 minutes when enabled
```

### Recent Activities

```text
YouTube: every 15 minutes
Instagram auto: every 30 to 60 minutes
X: every 15 minutes when enabled
CHZZK: every 15 to 30 minutes when enabled
SOOP: every 15 to 30 minutes when enabled
```

### Schedules

```text
manual/operator source refresh: on write
import/sync reconciliation: every 30 minutes
```

### Metadata Validation

```text
channel/profile validation: every 12 hours
featured influencer validation: every 24 hours
```

## Freshness Thresholds

### Live Status

```text
fresh: <= 5 minutes
stale: > 5 minutes and <= 30 minutes
failed/unknown: > 30 minutes with no valid fallback
```

### Recent Activities

```text
fresh: <= 30 minutes
stale: > 30 minutes and <= 24 hours
manual: operator-entered content regardless of age
```

### Schedules

```text
fresh: operator update within last 24 hours
stale: older than 24 hours but still upcoming
```

### Metadata

```text
fresh: validated within 24 hours
stale: older than 24 hours
```

## Projection Refresh Rules

- adapter success 후 projection 즉시 갱신
- projection 실패 시 기존 serving state 유지
- stale 판정은 read 시 계산하지 않고 background job 또는 write path에서 명시적으로 기록

# Monitoring / Metrics

## Core Metrics

### Upstream Health

- `adapter_requests_total{platform,scope,status}`
- `adapter_request_latency_ms{platform,scope}`
- `adapter_timeouts_total{platform,scope}`
- `adapter_retries_total{platform,scope}`
- `adapter_rate_limit_total{platform}`

### Freshness

- `serving_stale_sections_total{section,platform}`
- `serving_freshness_age_seconds{section,platform}`
- `live_status_age_seconds{platform}`
- `activity_age_seconds{platform}`

### Fallback / Degradation

- `fallback_used_total{section,platform,fallback_type}`
- `partial_response_total{endpoint}`
- `section_failed_total{endpoint,section}`
- `manual_override_served_total{entity_type}`

### Polling / Scheduler

- `sync_job_duration_ms{platform,scope}`
- `sync_job_backlog_count{platform,scope}`
- `sync_consecutive_failures{platform,scope}`
- `sync_next_scheduled_delay_seconds{platform,scope}`

### Serving Quality

- `home_response_latency_ms`
- `detail_response_latency_ms`
- `list_response_latency_ms`
- `cache_hit_ratio{table}`

## Logs

반드시 구조화 로그:

- requestId
- platform
- scope
- syncKey
- upstreamStatus
- retryCount
- fallbackType
- freshnessStatus
- projectionUpdated

## Alerting Recommendations

### High Severity

- 전체 endpoint 5xx 비율 급증
- DB/cache read failure
- home payload 생성 실패
- projection update 전반 실패

### Medium Severity

- 특정 플랫폼 live status stale ratio > 30%
- YouTube quota-related failures 지속 증가
- sync backlog 지속 증가
- fallback_used_total 급증

### Low Severity

- metadata stale 증가
- manual override 비율 증가
- 특정 platform partial failure 지속

## Incident Detection Guidance

- 단일 upstream 장애와 내부 장애를 구분해야 함
- “platform degraded but service healthy” 상태를 운영 대시보드에 명확히 표시
- app-config에서 platform enabled/disabled/supportMode와 실제 sync health를 함께 보여주는 운영 뷰 권장

# Risks and Trade-offs

- 신선도를 높일수록 비용과 quota 사용량이 증가한다.
- retry를 늘릴수록 성공 확률은 오르지만 backlog와 latency도 함께 증가한다.
- stale 허용 폭을 넓히면 UX는 안정적이지만 잘못된 최신성 기대를 줄 수 있다.
- manual fallback을 강화하면 안정성은 높아지지만 운영 비용이 증가한다.
- 플랫폼별 polling을 다르게 가져가면 효율은 좋아지지만 운영 복잡도는 커진다.

## Recommended Trade-off Position

- live status는 신선도 우선, 하지만 timeout 짧게
- recent activities는 안정성 우선, stale 허용
- schedules와 metadata는 manual/slow refresh 허용
- Instagram은 reliability-first로 manual-heavy 전략 유지
- X/CHZZK/SOOP은 실제 신뢰 가능한 source 확보 전까지 disabled 또는 manual-only

## Default Reliability Policy

```text
prefer stale over empty
prefer partial success over total failure
prefer bounded retry over aggressive retry
prefer platform isolation over global orchestration
prefer explicit degradation over silent data decay
```
