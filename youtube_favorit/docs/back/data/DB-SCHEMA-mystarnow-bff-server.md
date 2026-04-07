# Domain Entities

`MyStarNow BFF`의 데이터 모델은 크게 네 층으로 나눈다.

1. 정규화된 코어 도메인
   - `influencers`
   - `channels`
   - `categories`
   - `influencer_categories`
   - `schedule_items`

2. 읽기 최적화 / serving cache
   - `live_status_cache`
   - `activity_items`
   - `influencer_serving_state`

3. 수집 운영 상태
   - `platform_sync_metadata`

4. 원천 데이터 / 운영 보정
   - `raw_source_records`
   - `influencer_operator_metadata`
   - `channel_operator_metadata`

보조 기준 테이블:

- `platforms`
- `activity_types`

이 구조의 의도는 다음과 같다.

- 앱이 사용하는 핵심 개념은 `인플루언서`, `채널`, `활동`, `라이브 상태`, `일정`이다.
- 외부 플랫폼별 응답 구조는 `raw_source_records`에 격리한다.
- 읽기 API는 `activity_items`, `live_status_cache`, `influencer_serving_state`를 우선 사용해 빠르게 응답한다.
- 운영자가 직접 수정한 정보는 코어 테이블과 분리된 override 테이블에 둔다.

# Entity Relationships

```text
platforms 1---N channels
platforms 1---N platform_sync_metadata
platforms 1---N raw_source_records

influencers 1---N channels
influencers 1---N schedule_items
influencers 1---N activity_items
influencers 1---1 influencer_serving_state
influencers 1---0..1 influencer_operator_metadata

categories 1---N influencer_categories
influencers 1---N influencer_categories

channels 1---0..1 live_status_cache
channels 1---N activity_items
channels 1---0..1 channel_operator_metadata
channels 1---N platform_sync_metadata

raw_source_records 1---0..N activity_items
raw_source_records 1---0..1 live_status_cache
raw_source_records 1---0..N platform_sync_metadata
```

관계 해석:

- 한 인플루언서는 여러 플랫폼 채널을 가질 수 있다.
- 한 채널은 하나의 플랫폼에만 속한다.
- 라이브 상태는 채널 단위로 추적하되, 홈/목록 용으로 인플루언서 단위 serving state에 일부 반영한다.
- 활동 아이템은 인플루언서 단위 조회가 많으므로 `influencer_id`와 `channel_id`를 모두 가진다.
- 운영 보정 데이터는 코어 엔티티와 분리해 source of truth 충돌을 줄인다.

# Storage Model Proposal

## 1. Core Normalized Tables

### `platforms`

용도:
- 지원 플랫폼의 기준 테이블

주요 컬럼:

```text
platform_code        text PK
display_name         text not null
support_mode         text not null  -- auto | limited | manual | disabled
is_enabled           boolean not null default false
created_at           timestamptz not null
updated_at           timestamptz not null
```

제약:

- PK: `platform_code`
- CHECK: `support_mode in ('auto','limited','manual','disabled')`

인덱스:

- PK만으로 충분

### `influencers`

용도:
- 앱과 서버가 공유하는 인플루언서 중심 정규화 엔티티

주요 컬럼:

```text
id                      uuid PK
slug                    text not null
display_name            text not null
normalized_name         text not null
bio                     text null
profile_image_url       text null
status                  text not null        -- active | inactive | hidden
is_featured             boolean not null default false
default_timezone        text null
latest_activity_at      timestamptz null     -- denormalized helper
current_live_platform   text null            -- denormalized helper
is_live_now             boolean not null default false
created_at              timestamptz not null
updated_at              timestamptz not null
deleted_at              timestamptz null
```

제약:

- PK: `id`
- UK: `slug`
- CHECK: `status in ('active','inactive','hidden')`
- FK: `current_live_platform -> platforms.platform_code` nullable

인덱스:

- UNIQUE INDEX `ux_influencers_slug (slug)`
- INDEX `ix_influencers_status_featured (status, is_featured desc, updated_at desc)`
- INDEX `ix_influencers_live_latest (is_live_now desc, latest_activity_at desc nulls last)`
- INDEX `ix_influencers_normalized_name (normalized_name)`

설명:

- `latest_activity_at`, `current_live_platform`, `is_live_now`는 순수 정규화 관점에서는 파생값이지만, 목록/홈 조회 성능 때문에 의도적으로 허용한다.

### `categories`

용도:
- 목록 필터와 태그 표현을 위한 카테고리 사전

주요 컬럼:

```text
category_code        text PK
display_name         text not null
sort_order           integer not null default 0
is_enabled           boolean not null default true
created_at           timestamptz not null
updated_at           timestamptz not null
```

### `influencer_categories`

용도:
- 인플루언서와 카테고리의 다대다 매핑

주요 컬럼:

```text
influencer_id        uuid not null
category_code        text not null
created_at           timestamptz not null
```

제약:

- PK: `(influencer_id, category_code)`
- FK: `influencer_id -> influencers.id`
- FK: `category_code -> categories.category_code`

인덱스:

- INDEX `ix_influencer_categories_category (category_code, influencer_id)`

### `channels`

용도:
- 인플루언서의 플랫폼별 공식/비공식 채널 메타데이터

주요 컬럼:

```text
id                      uuid PK
influencer_id           uuid not null
platform_code           text not null
external_channel_id     text not null
handle                  text null
channel_url             text not null
display_label           text null
is_official             boolean not null default false
is_primary              boolean not null default false
status                  text not null          -- active | inactive | hidden
verified_at             timestamptz null
last_seen_at            timestamptz null
created_at              timestamptz not null
updated_at              timestamptz not null
deleted_at              timestamptz null
```

제약:

- PK: `id`
- FK: `influencer_id -> influencers.id`
- FK: `platform_code -> platforms.platform_code`
- UK: `(platform_code, external_channel_id)`
- CHECK: `status in ('active','inactive','hidden')`

인덱스:

- UNIQUE INDEX `ux_channels_platform_external (platform_code, external_channel_id)`
- INDEX `ix_channels_influencer_platform (influencer_id, platform_code, is_primary desc)`
- INDEX `ix_channels_platform_handle (platform_code, handle)`
- PARTIAL UNIQUE INDEX `ux_channels_primary_per_platform (influencer_id, platform_code) WHERE is_primary = true AND deleted_at IS NULL`

설명:

- 공식/대표 채널 판별은 채널 메타와 운영 보정이 함께 결정한다.

### `schedule_items`

용도:
- 예정 방송, 공지 기반 일정, 운영자 수동 등록 일정

주요 컬럼:

```text
id                      uuid PK
influencer_id           uuid not null
channel_id              uuid null
platform_code           text null
source_type             text not null      -- manual | imported | derived
status                  text not null      -- scheduled | cancelled | completed
title                   text not null
note                    text null
scheduled_at            timestamptz not null
ends_at                 timestamptz null
source_reference        text null
created_by_operator     text null
updated_by_operator     text null
created_at              timestamptz not null
updated_at              timestamptz not null
```

제약:

- PK: `id`
- FK: `influencer_id -> influencers.id`
- FK: `channel_id -> channels.id` nullable
- FK: `platform_code -> platforms.platform_code` nullable
- CHECK: `source_type in ('manual','imported','derived')`
- CHECK: `status in ('scheduled','cancelled','completed')`

인덱스:

- INDEX `ix_schedule_items_influencer_time (influencer_id, scheduled_at desc)`
- INDEX `ix_schedule_items_today (status, scheduled_at asc)`
- INDEX `ix_schedule_items_platform_time (platform_code, scheduled_at desc)`

## 2. Serving / Cache Tables

### `live_status_cache`

용도:
- 채널별 최신 라이브 상태 snapshot
- 홈 `liveNow`와 상세 `liveStatus`의 즉시 응답 소스

주요 컬럼:

```text
channel_id               uuid PK
influencer_id            uuid not null
platform_code            text not null
is_live                  boolean not null
live_title               text null
watch_url                text null
viewer_count             integer null
started_at               timestamptz null
snapshot_at              timestamptz not null
stale_at                 timestamptz null
freshness_status         text not null      -- fresh | stale | manual | unknown
last_successful_sync_at  timestamptz null
last_attempted_sync_at   timestamptz null
source_record_id         uuid null
error_code               text null
error_message            text null
updated_at               timestamptz not null
```

제약:

- PK: `channel_id`
- FK: `channel_id -> channels.id`
- FK: `influencer_id -> influencers.id`
- FK: `platform_code -> platforms.platform_code`
- FK: `source_record_id -> raw_source_records.id` nullable
- CHECK: `freshness_status in ('fresh','stale','manual','unknown')`

인덱스:

- INDEX `ix_live_status_cache_live (is_live desc, snapshot_at desc)`
- INDEX `ix_live_status_cache_influencer (influencer_id, snapshot_at desc)`
- INDEX `ix_live_status_cache_platform (platform_code, is_live desc, snapshot_at desc)`
- INDEX `ix_live_status_cache_stale (freshness_status, stale_at)`

설명:

- 채널 단위 1행 유지가 기본이다.
- 이전 snapshot 이력이 필요하면 raw/source 테이블에서 복원한다.

### `activity_items`

용도:
- 정규화된 최근 활동 피드
- 홈 `latestUpdates`, 상세 `recentActivities`, 목록 `recent_activity` 정렬 소스

주요 컬럼:

```text
id                      uuid PK
influencer_id           uuid not null
channel_id              uuid null
platform_code           text not null
source_activity_id      text null
content_type            text not null      -- video | short | post | reel | live_notice | clip | notice
title                   text null
summary                 text null
thumbnail_url           text null
external_url            text not null
published_at            timestamptz not null
is_pinned               boolean not null default false
freshness_status        text not null      -- fresh | stale | manual | unknown
source_record_id        uuid null
last_successful_sync_at timestamptz null
created_at              timestamptz not null
updated_at              timestamptz not null
```

제약:

- PK: `id`
- FK: `influencer_id -> influencers.id`
- FK: `channel_id -> channels.id` nullable
- FK: `platform_code -> platforms.platform_code`
- FK: `source_record_id -> raw_source_records.id` nullable
- CHECK: `freshness_status in ('fresh','stale','manual','unknown')`

권장 unique:

- PARTIAL UNIQUE INDEX `ux_activity_items_source (platform_code, source_activity_id) WHERE source_activity_id IS NOT NULL`

인덱스:

- INDEX `ix_activity_items_home (published_at desc)`
- INDEX `ix_activity_items_influencer_time (influencer_id, published_at desc)`
- INDEX `ix_activity_items_platform_time (platform_code, published_at desc)`
- INDEX `ix_activity_items_pinned_time (is_pinned desc, published_at desc)`

설명:

- `published_at` 중심 정렬이 많으므로 time-first 인덱스를 강하게 둔다.
- `content_type`는 앱 노출 모델에 맞춘 정규화 값만 저장하고, 플랫폼 원본 타입은 raw/source에 남긴다.

### `influencer_serving_state`

용도:
- 홈/목록/상세의 빠른 조합을 위한 read-optimized denormalized projection
- 파생 필드와 cache성 요약값을 모은다

주요 컬럼:

```text
influencer_id                uuid PK
is_live_now                  boolean not null default false
live_platform_code           text null
live_started_at              timestamptz null
latest_activity_at           timestamptz null
latest_schedule_at           timestamptz null
supported_platforms_cache    jsonb not null default '[]'
featured_rank                integer null
home_visibility              boolean not null default true
detail_visibility            boolean not null default true
last_projection_refresh_at   timestamptz not null
```

제약:

- PK: `influencer_id`
- FK: `influencer_id -> influencers.id`
- FK: `live_platform_code -> platforms.platform_code` nullable

인덱스:

- INDEX `ix_serving_state_home (home_visibility, is_live_now desc, featured_rank asc nulls last, latest_activity_at desc nulls last)`
- INDEX `ix_serving_state_live (is_live_now desc, live_started_at desc)`

설명:

- 이 테이블은 엄격한 source of truth가 아니라 read model이다.
- write 복잡도는 늘지만 홈/목록 응답을 단순화한다.

## 3. Sync / Ingestion Tables

### `platform_sync_metadata`

용도:
- polling/수집 상태 추적
- freshness, provenance, backoff, cursor 관리

주요 컬럼:

```text
id                      uuid PK
platform_code           text not null
resource_scope          text not null      -- channel_profile | channel_live | channel_activity | influencer_profile
channel_id              uuid null
influencer_id           uuid null
sync_key                text not null
last_attempted_at       timestamptz null
last_succeeded_at       timestamptz null
last_status             text not null      -- idle | success | failed | partial
last_error_code         text null
last_error_message      text null
consecutive_failures    integer not null default 0
next_scheduled_at       timestamptz null
backoff_until           timestamptz null
cursor_token            text null
etag                    text null
source_quota_bucket     text null
updated_at              timestamptz not null
created_at              timestamptz not null
```

제약:

- PK: `id`
- FK: `platform_code -> platforms.platform_code`
- FK: `channel_id -> channels.id` nullable
- FK: `influencer_id -> influencers.id` nullable
- CHECK: `resource_scope in ('channel_profile','channel_live','channel_activity','influencer_profile')`
- CHECK: `last_status in ('idle','success','failed','partial')`

권장 unique:

- UNIQUE INDEX `ux_sync_metadata_sync_key (platform_code, resource_scope, sync_key)`

인덱스:

- INDEX `ix_sync_metadata_schedule (next_scheduled_at asc, backoff_until asc nulls first)`
- INDEX `ix_sync_metadata_failures (last_status, consecutive_failures desc)`
- INDEX `ix_sync_metadata_channel_scope (channel_id, resource_scope)`

설명:

- `sync_key`는 실제 대상 식별자다.
  - 예: `youtube:channel:UC123...`
  - 예: `instagram:channel:1784...`

## 4. Raw / Provenance Tables

### `raw_source_records`

용도:
- 외부 플랫폼 원천 payload 저장
- 정규화 버그 분석, 재처리, 감사용 provenance

주요 컬럼:

```text
id                      uuid PK
platform_code           text not null
resource_scope          text not null
external_object_id      text not null
channel_id              uuid null
influencer_id           uuid null
http_status             integer null
payload_checksum        text not null
payload_json            jsonb not null
fetched_at              timestamptz not null
normalized_at           timestamptz null
request_trace_id        text null
created_at              timestamptz not null
```

제약:

- PK: `id`
- FK: `platform_code -> platforms.platform_code`
- FK: `channel_id -> channels.id` nullable
- FK: `influencer_id -> influencers.id` nullable

인덱스:

- INDEX `ix_raw_source_lookup (platform_code, resource_scope, external_object_id, fetched_at desc)`
- INDEX `ix_raw_source_channel (channel_id, fetched_at desc)`
- INDEX `ix_raw_source_influencer (influencer_id, fetched_at desc)`
- INDEX `ix_raw_source_normalized (normalized_at asc nulls first)`

설명:

- 기본적으로 GIN 인덱스는 두지 않는다.
- 원본 JSON 검색이 자주 필요해질 때만 제한적으로 추가한다.

## 5. Operator-managed Tables

### `influencer_operator_metadata`

용도:
- 운영자 보정 정보
- 자동 수집 결과를 덮어쓰거나 보완하는 수동 메타데이터

주요 컬럼:

```text
influencer_id            uuid PK
bio_override             text null
profile_image_override   text null
display_priority         integer null
review_status            text not null      -- unreviewed | reviewed | needs_attention
operator_notes           text null
last_reviewed_at         timestamptz null
updated_at               timestamptz not null
```

제약:

- PK: `influencer_id`
- FK: `influencer_id -> influencers.id`
- CHECK: `review_status in ('unreviewed','reviewed','needs_attention')`

### `channel_operator_metadata`

용도:
- 채널 공식 여부, 대표 채널 여부, 링크 보정 등 운영자 보정

주요 컬럼:

```text
channel_id               uuid PK
is_official_override     boolean null
is_primary_override      boolean null
handle_override          text null
channel_url_override     text null
display_label_override   text null
review_status            text not null
operator_notes           text null
last_reviewed_at         timestamptz null
updated_at               timestamptz not null
```

제약:

- PK: `channel_id`
- FK: `channel_id -> channels.id`

# Key Fields and Constraints

## Stable Identifiers

- 모든 핵심 테이블 PK는 `uuid`
- 외부 플랫폼 식별자는 자연키로 쓰지 않고 `external_*` 컬럼으로 분리
- 앱 식별자는 `slug`, 내부 관계는 `id`

## Uniqueness

- `influencers.slug` unique
- `channels (platform_code, external_channel_id)` unique
- `activity_items (platform_code, source_activity_id)` partial unique
- `platform_sync_metadata (platform_code, resource_scope, sync_key)` unique
- `influencer_categories (influencer_id, category_code)` composite PK

## Foreign Keys

- `channels.influencer_id -> influencers.id`
- `schedule_items.influencer_id -> influencers.id`
- `activity_items.influencer_id -> influencers.id`
- `activity_items.channel_id -> channels.id`
- `live_status_cache.channel_id -> channels.id`
- `platform_sync_metadata.channel_id -> channels.id`
- `raw_source_records.channel_id -> channels.id`

## Freshness / Stale Tracking

핵심 컬럼:

- `snapshot_at`
- `stale_at`
- `freshness_status`
- `last_attempted_sync_at`
- `last_successful_sync_at`
- `last_projection_refresh_at`

원칙:

- serving 테이블은 반드시 freshness를 스스로 설명해야 한다.
- stale 여부는 응답 조립 시 계산하지 않고, 테이블에 저장해 빠르게 읽는다.

# Indexing / Query Notes

## Home Feed

### `liveNow`

주요 테이블:

- `live_status_cache`
- `channels`
- `influencer_serving_state`
- `influencers`

핵심 인덱스:

- `ix_live_status_cache_live`
- `ix_serving_state_live`

### `latestUpdates`

주요 테이블:

- `activity_items`
- `influencers`

핵심 인덱스:

- `ix_activity_items_home`

### `todaySchedules`

주요 테이블:

- `schedule_items`

핵심 인덱스:

- `ix_schedule_items_today`

### `featuredInfluencers`

주요 테이블:

- `influencers`
- `influencer_serving_state`

핵심 인덱스:

- `ix_influencers_status_featured`
- `ix_serving_state_home`

## Influencer List

쿼리 패턴:

- `q`
- `category`
- `platform`
- `sort`
- cursor pagination

권장 방식:

- 기본 목록은 `influencers` + `influencer_serving_state`
- `category` 필터는 `influencer_categories`
- `platform` 필터는 `channels`
- `recent_activity` 정렬은 `influencers.latest_activity_at` 또는 `influencer_serving_state.latest_activity_at`

핵심 인덱스:

- `ix_influencers_normalized_name`
- `ix_influencer_categories_category`
- `ix_channels_influencer_platform`
- `ix_influencers_live_latest`

## Influencer Detail

주요 테이블:

- `influencers`
- `channels`
- `live_status_cache`
- `activity_items`
- `schedule_items`
- `influencer_operator_metadata`
- `channel_operator_metadata`

핵심 인덱스:

- `ux_influencers_slug`
- `ix_channels_influencer_platform`
- `ix_live_status_cache_influencer`
- `ix_activity_items_influencer_time`
- `ix_schedule_items_influencer_time`

## Sync / Polling

주요 테이블:

- `platform_sync_metadata`
- `raw_source_records`

핵심 인덱스:

- `ix_sync_metadata_schedule`
- `ix_sync_metadata_failures`
- `ix_raw_source_normalized`

# Normalization Decisions

## Where We Normalize

- `influencers`는 앱이 이해하는 인플루언서 개념만 담는다.
- `channels`는 플랫폼별 채널 메타를 정규화한다.
- `activity_items.content_type`는 앱 기준 타입으로 통일한다.
- `schedule_items`는 플랫폼 공지/운영 입력을 동일 모델로 흡수한다.

정규화 이유:

- 앱과 API가 upstream 세부사항에 종속되지 않게 하기 위해
- 플랫폼 추가 시 API 구조를 유지하기 위해

## Where We Intentionally Denormalize

- `influencers.latest_activity_at`
- `influencers.is_live_now`
- `influencers.current_live_platform`
- `influencer_serving_state.*`

의도:

- 홈/목록 응답은 read-heavy이므로 join 수와 계산량을 줄인다.
- polling 기반 시스템에서는 write 비용보다 read latency가 중요하다.

## Raw vs Normalized Separation

- `raw_source_records.payload_json`에는 원천 응답을 그대로 저장한다.
- `activity_items`, `live_status_cache`에는 앱이 쓰는 정규화 결과만 저장한다.
- 정규화 실패나 규칙 변경 시 raw 데이터를 기준으로 재처리할 수 있다.

## Operator-managed Separation

- 운영자 override는 코어 엔티티에 직접 섞지 않는다.
- 이유:
  - 원천 데이터와 수동 수정 이력을 분리
  - override 해제/재검토를 쉽게 하기 위함

# Risks and Trade-offs

- 장점: 앱에 필요한 도메인 모델을 안정적으로 유지할 수 있다.
- 장점: 홈/목록/상세 조회가 빠르고 예측 가능해진다.
- 장점: raw/source 분리 덕분에 어댑터 버그 분석과 재처리가 쉬워진다.
- 단점: `influencer_serving_state`와 `influencers`의 denormalized 컬럼 때문에 write 경로가 복잡해진다.
- 단점: 운영자 override와 원천 데이터의 우선순위 규칙을 애플리케이션 레벨에서 명확히 관리해야 한다.
- 단점: 모든 플랫폼을 하나의 스키마로 수용하려면 `resource_scope`, `content_type` 같은 분류 체계가 중요해진다.
- 운영 리스크: stale 계산 로직이 일관되지 않으면 API 응답과 DB 상태가 어긋날 수 있다.
- 운영 리스크: YouTube quota 또는 Instagram 제한 때문에 `platform_sync_metadata` 기반 backoff 설계가 필수다.
- 확장 메모: X, CHZZK, SOOP 추가 시 기존 코어 스키마는 유지하고 `platforms`, `channels`, `platform_sync_metadata`, `raw_source_records`만 우선 확장하면 된다.
