# Adapter Overview

`MyStarNow BFF`의 플랫폼 어댑터 계층은 외부 플랫폼별 수집 차이를 흡수하고, 내부에서는 안정적인 정규화 도메인 모델만 다루도록 만드는 ingestion boundary다.

목표:

- 플랫폼별 API/응답 구조/정책 차이를 adapter layer 안에 격리한다.
- API serving layer는 adapter를 직접 호출하지 않고, 저장된 normalized domain data만 읽는다.
- 한 플랫폼이 실패해도 다른 플랫폼과 전체 홈/상세 응답은 유지된다.
- 자동 수집이 약한 플랫폼은 `partial automation + operator-managed fallback` 구조로 흡수한다.

현재 운영 상태 기준:

- `Phase 1 active`: YouTube, Instagram
- `Defined but disabled by default`: X, CHZZK, SOOP

공통 원칙:

- source/raw payload는 adapter boundary에서 끝낸다.
- normalized domain model은 `Influencer`, `Channel`, `LiveStatus`, `ActivityItem`, `ScheduleItem` 중심으로 고정한다.
- adapter는 domain serving schema를 직접 알지 않는다.
- operator override는 adapter 결과보다 높은 우선순위를 가질 수 있지만, override 적용은 adapter가 아니라 normalization/merge 단계에서 수행한다.

# Per-Platform Strategy

## YouTube

### Current Status

- Phase 1 active
- Preferred source type: official API

### What Data Is Collected

- 채널 기본 정보
- 채널 대표 이미지/설명
- 최근 업로드 영상
- 쇼츠 포함 최근 활동
- 라이브 방송 여부
- 라이브 제목 및 watch URL

### Source Type

- Official API: `YouTube Data API v3`
- Manual input: 운영자 보정 메타데이터
- Scraping: 기본 설계에 포함하지 않음

### Collection Method

- `channels.list`로 채널 메타데이터 수집
- `playlistItems.list` 또는 `search/list` + `videos.list` 조합으로 최근 영상 수집
- 라이브 상태는 API 응답 기반 조회 + polling 보완
- quota 절감을 위해 필요한 필드만 요청
- ETag/conditional request가 가능하면 활용

### Normalization Strategy

- YouTube resource shape는 저장하지 않고 `Channel`, `ActivityItem`, `LiveStatus`로 변환
- 영상 타입은 내부 `contentType` enum으로 정규화
  - `video`
  - `short`
  - `live_notice`
- 라이브 여부는 내부 `LiveStatus` snapshot으로만 노출

### Polling Strategy

- 채널 메타: 6~24시간 주기
- 최근 활동: 10~30분 주기
- 라이브 상태: 3~5분 주기
- hot channel은 shorter polling, inactive channel은 longer polling 허용

### Fallback Strategy

- 최근 정상 cache 유지
- stale 마킹 후 계속 serving
- 운영자 수동 링크/설명/대표 메타는 계속 노출

### Failure Isolation Strategy

- 채널 단위 sync metadata와 backoff 사용
- quota 초과, 4xx, 5xx를 분리 기록
- YouTube failure는 `latestUpdates` 또는 `liveNow` 섹션 일부 실패로만 전파

### Freshness Expectations

- live status: 3~5분 이내
- latest activity: 10~30분 이내
- profile/channel metadata: 수시간 단위 허용

### Operational Risks

- quota 관리 실패 시 전체 수집량 급감
- 라이브 상태는 실시간성이 아닌 polling 기반이라 약간의 지연 존재
- 채널 구조 변경, short/live 분류 규칙 변화 가능

## Instagram

### Current Status

- Phase 1 active
- Preferred source type: manual-first + limited official integration

### What Data Is Collected

- 프로필 링크
- 대표 핸들
- 소개/프로필 이미지
- 최근 대표 게시물 또는 릴스
- 운영자가 중요하다고 판단한 최근 소식

### Source Type

- Official API: 제한적 사용만 고려
- Manual input: 기본 전략
- Unofficial/scraping: 기본 설계에서는 비권장, 필요 시 별도 ADR 없이는 도입 금지

### Collection Method

- 운영자가 채널 링크와 대표 게시물 메타를 등록
- 가능한 범위에서 제한적 공식 API 또는 안전한 import 경로 사용
- 자동 수집은 “보조 수단”으로만 사용

### Normalization Strategy

- 게시물/릴스/공지성 업데이트를 모두 `ActivityItem`으로 정규화
- Instagram 특유의 media subtype은 raw/source에 남기고 normalized layer에는 최소 타입만 반영
- profile override 가능성을 염두에 두고 manual metadata merge

### Polling Strategy

- 기본은 polling보다 수동 갱신 중심
- 제한적 자동 수집이 있는 경우 30분~수시간 주기
- 긴급성보다 운영 안정성 우선

### Fallback Strategy

- operator-managed content를 primary fallback으로 사용
- auto source 실패 시 manual data 그대로 유지
- stale 상태는 내려주되 UI 콘텐츠 자체는 유지

### Failure Isolation Strategy

- Instagram 자동 수집 실패는 해당 section만 partial 처리
- manual data 존재 시 endpoint 전체 성공 유지
- adapter 오류와 manual missing 상태를 구분해서 기록

### Freshness Expectations

- profile/channel metadata: 수동 갱신 기준
- latest activity: 30분~수시간 또는 운영자 갱신 시점
- strict real-time은 목표 아님

### Operational Risks

- 공식 API 지원 범위가 use case에 충분하지 않을 수 있음
- manual workload가 늘 수 있음
- 자동 수집 신뢰도가 낮으면 잘못된 freshness 기대를 만들 수 있음

## X

### Current Status

- Future platform
- Disabled by default in Phase 1
- Preferred source type: official API only

### What Data Is Collected

- 계정 기본 정보
- 최근 포스트
- 방송 공지성 포스트
- 외부 방송 링크가 있는 포스트

### Source Type

- Official API: `X API`
- Manual input: 운영자 보조 등록
- Scraping: 기본 설계에서 비허용

### Collection Method

- 계정 메타와 최근 포스트를 공식 API로 조회
- 방송 공지성 포스트는 adapter에서 heuristic tagging 가능
- 비용/정책/권한이 확보되기 전까지는 disabled 상태 유지

### Normalization Strategy

- post/thread/reply 구조는 raw/source에 남기고, 앱에는 `ActivityItem`만 제공
- domain layer에는 `post`, `notice`, `link_update` 정도의 최소 타입만 허용

### Polling Strategy

- enabled 시 10~30분 주기 기본
- high-interest account만 short polling

### Fallback Strategy

- X 비활성 또는 실패 시 manual notice entry로 대체 가능
- app-config에서 disabled 상태를 명시

### Failure Isolation Strategy

- X adapter는 독립 scheduler와 독립 backoff 사용
- 실패가 다른 플랫폼 sync queue를 막지 않음

### Freshness Expectations

- enabled 시 latest activity 10~30분
- disabled 시 freshness 보장 없음

### Operational Risks

- 정책/요금/권한 구조 변화 가능성
- 사용량 대비 비용 효율이 낮을 수 있음
- 포스트 의미 해석이 과도하면 false positive 증가

## CHZZK

### Current Status

- Future platform
- Disabled by default in Phase 1
- Preferred source type: official API where coverage is sufficient

### What Data Is Collected

- 채널 기본 정보
- 라이브 방송 여부
- 방송 제목 및 진입 링크
- 최근 방송 관련 업데이트 또는 공지

### Source Type

- Official API: available, preferred
- Manual input: fallback
- Scraping: 기본 설계에서는 비권장

### Collection Method

- 공식 CHZZK API를 통해 인증 가능한 범위의 채널/라이브 데이터 조회
- 범위가 부족한 항목은 수동 입력 유지
- partner/public scope 차이가 있으면 capability flag로 제한

### Normalization Strategy

- CHZZK-specific category/state 값은 domain enum으로 매핑
- 라이브 스냅샷은 `LiveStatus`
- 최근 공지/업데이트는 `ActivityItem`

### Polling Strategy

- live status: 3~5분
- recent activity: 10~30분
- channel metadata: 수시간 단위

### Fallback Strategy

- 공식 API에서 수집 불가한 항목은 operator-managed metadata 사용
- link와 profile은 manual override 우선 가능

### Failure Isolation Strategy

- CHZZK adapter는 독립 worker, 독립 sync metadata 사용
- 인증 실패/권한 부족/업스트림 장애를 구분 기록

### Freshness Expectations

- enabled 시 YouTube와 유사한 live freshness 목표
- unsupported fields는 manual freshness로 표시

### Operational Risks

- 공식 API 범위가 public use case 전체를 커버하지 않을 수 있음
- 인증/권한 요구사항에 따라 운영 복잡도 증가 가능

## SOOP

### Current Status

- Future platform
- Disabled by default in Phase 1
- Preferred source type: official/partner API if access secured, otherwise manual-only

### What Data Is Collected

- 채널 링크 및 기본 프로필
- 라이브 여부
- 방송 제목/진입 링크
- 중요 방송 공지 또는 최근 대표 업데이트

### Source Type

- Official developer/partner APIs: 조건부
- Manual input: current default
- Scraping: 기본 설계에서 비허용

### Collection Method

- 공식 partner-capable API 또는 extension-compatible surface가 확보되면 adapter 구현
- access가 없으면 채널 메타와 공지성 데이터는 manual-only

### Normalization Strategy

- partner API가 있어도 domain에는 `Channel`, `LiveStatus`, `ActivityItem`만 노출
- 채팅/후원/extension 이벤트는 raw 기능으로만 보관 가능하며 앱 노출 모델에는 직접 포함하지 않음

### Polling Strategy

- official integration 확보 전: 없음
- official integration 확보 후: live 3~5분, updates 10~30분

### Fallback Strategy

- manual channel registration
- manual schedule / notice 등록
- app-config에서 disabled 또는 limited 상태 노출

### Failure Isolation Strategy

- SOOP adapter는 기본적으로 비활성
- 활성화되더라도 독립 executor와 sync metadata 사용

### Freshness Expectations

- current phase: manual freshness only
- future official integration: YouTube/CHZZK 수준 목표

### Operational Risks

- general public read API가 충분하지 않을 수 있음
- partner-only surface일 경우 접근성 제한
- manual 운영 의존도가 커질 수 있음

# Adapter Interface Design

## Common Adapter Responsibilities

- platform 인증/설정 관리
- raw source fetch
- source DTO parsing
- source validation
- normalized candidate record 생성
- sync result와 freshness metadata 반환

adapter가 하지 말아야 할 일:

- API response 조립
- 홈/목록/상세 section shaping
- operator override merge 최종 결정
- domain serving query 직접 수행

## Suggested Common Interface

```kotlin
interface PlatformAdapter {
    val platform: PlatformCode

    fun capabilities(): Set<AdapterCapability>

    suspend fun syncChannelProfile(command: SyncChannelProfileCommand): AdapterSyncResult<ChannelProfileSnapshot>

    suspend fun syncLiveStatus(command: SyncLiveStatusCommand): AdapterSyncResult<LiveStatusSnapshot>

    suspend fun syncRecentActivities(command: SyncRecentActivitiesCommand): AdapterSyncResult<List<ActivitySnapshot>>
}

enum class AdapterCapability {
    CHANNEL_PROFILE,
    LIVE_STATUS,
    RECENT_ACTIVITY,
    SCHEDULE_IMPORT
}

data class AdapterSyncResult<T>(
    val platform: PlatformCode,
    val status: AdapterResultStatus,
    val fetchedAt: Instant,
    val freshness: FreshnessHint,
    val rawRecords: List<RawSourceEnvelope>,
    val normalizedPayload: T?,
    val errors: List<AdapterError>,
    val nextSuggestedPollAt: Instant?
)

enum class AdapterResultStatus {
    SUCCESS,
    PARTIAL,
    FAILED,
    UNSUPPORTED
}
```

## Boundary Between Adapter and Domain Logic

- Adapter output:
  - source snapshot
  - normalized candidate records
  - sync metadata
- Domain normalization/merge layer:
  - operator override merge
  - deduplication
  - freshness/stale finalization
  - projection update

## Aggregator Integration

구성:

- `IngestionOrchestrator`
- `PlatformAdapterRegistry`
- `NormalizationService`
- `ServingProjectionUpdater`
- `SyncMetadataService`

흐름:

1. scheduler가 sync 대상 선정
2. registry가 platform adapter 선택
3. adapter가 raw + normalized candidate 반환
4. normalization service가 domain model로 확정
5. projection updater가 `live_status_cache`, `activity_items`, `influencer_serving_state` 갱신
6. sync metadata 저장

## Operator Override / Manual Correction Path

- operator metadata는 adapter를 통하지 않고 별도 관리 경로에서 저장
- serving projection 갱신 시 다음 우선순위 적용

```text
operator override > normalized adapter result > previous cached value > empty state
```

- 단, provenance는 항상 유지해야 한다.
  - 어떤 값이 manual인지
  - 어떤 값이 upstream-derived인지

# Data Flow

## End-to-End Flow

```text
Scheduler
  -> Sync Target Resolver
  -> Platform Adapter
      -> Raw Source Fetch
      -> Source DTO Parse
      -> Candidate Normalization
  -> Normalization / Merge Layer
      -> Operator Override Merge
      -> Freshness / Stale Evaluation
  -> Serving Storage Update
      -> live_status_cache
      -> activity_items
      -> influencer_serving_state
  -> Sync Metadata Update
  -> API Layer reads serving tables only
```

## Failure Flow

```text
Platform Adapter Failure
  -> raw error + sync metadata update
  -> no serving-table destructive overwrite
  -> stale threshold evaluation
  -> endpoint returns partial section failure if cached data still exists
```

## Why This Split Matters

- adapter failure와 API failure를 분리할 수 있다
- normalized model을 오래 유지할 수 있다
- 플랫폼이 추가돼도 serving layer는 변경이 작다

# Extension Strategy

## Adding a New Platform

단계:

1. `PlatformCode`와 capability 정의 추가
2. adapter 구현
3. source DTO와 raw storage mapping 추가
4. normalization mapping 규칙 추가
5. sync scheduling policy 추가
6. app-config enablement flag 추가

## Extension Rules

- 새 플랫폼은 기존 domain model을 우선 재사용한다.
- domain model에 바로 새 필드를 추가하지 않고, 먼저 raw/source와 adapter 내부에서 수용 가능성을 검토한다.
- truly cross-platform 개념일 때만 domain model 확장
- platform-specific extras는 raw/source 또는 internal extension field로 격리

## Rollout Model

- `disabled`
- `manual_only`
- `limited`
- `auto`

새 플랫폼은 항상 `disabled` 또는 `manual_only`부터 시작한다.

# Risks and Trade-offs

- 장점: 플랫폼 실패가 전체 시스템 실패로 번지지 않는다.
- 장점: adapter와 API serving이 분리돼 유지보수가 쉽다.
- 장점: upstream 포맷이 바뀌어도 domain model을 방어할 수 있다.
- 장점: operator-managed fallback 덕분에 자동 수집이 약한 플랫폼도 서비스 품질을 유지할 수 있다.

- 단점: adapter + normalization + projection 계층으로 인해 write path가 길어진다.
- 단점: manual fallback이 많을수록 운영 도구 품질이 중요해진다.
- 단점: 플랫폼별 freshness 기대치가 달라 사용자 경험 일관성이 흔들릴 수 있다.
- 단점: X/CHZZK/SOOP은 명세는 준비되더라도 실제 enable 시 별도 접근성 검증이 필요하다.

- 운영 리스크: stale 계산이 느슨하면 오래된 정보가 fresh처럼 보일 수 있다.
- 운영 리스크: scraping을 쉽게 허용하면 유지보수성과 정책 리스크가 급증한다.
- 운영 리스크: adapter가 domain model을 직접 알게 되면 platform-specific leakage가 재발할 수 있다.

## Recommended Default Policy

- YouTube: `auto`
- Instagram: `limited`
- X: `disabled`
- CHZZK: `disabled` or `limited`
- SOOP: `manual_only` or `disabled`
