---
id: DB-SCHEMA-MYSTARNOW-IDOL-YOUTUBE-BFF-SERVER
type: spec
status: draft
owner:
created: 2026-04-05
updated: 2026-04-05
related_prd: PRD-MYSTARNOW-IDOL-YOUTUBE-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-IDOL-YOUTUBE-BFF-ARCHITECTURE
---

# Overview

이 문서는 `MyStarNow Idol YouTube BFF`의 데이터베이스 초안이다. 목표는 아이돌 그룹, 멤버, YouTube 채널, 최신 영상 데이터를 그룹 중심 읽기 모델로 제공하는 것이다.

핵심 원칙:

- 관계 데이터는 운영 입력을 source of truth로 둔다.
- YouTube 채널/영상 메타는 자동 수집을 사용한다.
- 읽기 성능을 위해 serving state와 feed projection을 별도로 둘 수 있다.
- 운영자가 실제로 입력하는 필드와 자동 수집 필드를 명확히 구분한다.

# Data Layer Structure

1. 기준 / 참조 테이블
   - `platforms`
2. 코어 도메인 테이블
   - `idol_groups`
   - `idol_members`
   - `youtube_channels`
   - `youtube_videos`
3. 운영 보정 / 입력 테이블
   - `group_operator_metadata`
   - `member_operator_metadata`
   - `channel_operator_metadata`
   - `video_operator_metadata`
4. 수집 / 동기화 테이블
   - `youtube_sync_metadata`
   - `raw_source_records`
5. 읽기 최적화 / serving 테이블
   - `group_serving_state`
   - `member_serving_state`

# Source Of Truth Policy

## 운영자가 직접 관리하는 것

- 그룹-멤버 연결
- 채널 소유 주체
- 채널 유형
- 공식 여부
- 대표 여부

## YouTube 자동 수집이 담당하는 것

- 채널 제목
- 채널 설명
- 채널 프로필/배너 이미지
- 최신 영상 목록
- 영상 제목 / 설명 / 썸네일 / 게시 시각

## Merge Rule

- 운영자 override가 있으면 자동 수집값보다 우선한다.
- override가 없으면 자동 수집값을 사용한다.
- 읽기 모델은 항상 merged 결과를 사용한다.

# Entity Relationships

```text
platforms 1---N youtube_channels

idol_groups 1---N idol_members

idol_groups 1---N youtube_channels (owner_type = GROUP)
idol_members 1---N youtube_channels (owner_type = MEMBER)

youtube_channels 1---N youtube_videos

idol_groups 1---0..1 group_operator_metadata
idol_members 1---0..1 member_operator_metadata
youtube_channels 1---0..1 channel_operator_metadata
youtube_videos 1---0..1 video_operator_metadata

youtube_channels 1---N youtube_sync_metadata
youtube_channels 1---N raw_source_records

idol_groups 1---1 group_serving_state
idol_members 1---1 member_serving_state
```

# Operator Input Field Summary

## 그룹 입력 필드

필수:

- `slug`
- `display_name`

선택:

- `description`
- `cover_image_url`
- `is_featured`
- `status`
- `note`

## 멤버 입력 필드

필수:

- `group_id`
- `slug`
- `display_name`

선택:

- `profile_image_url`
- `sort_order`
- `status`
- `note`

## 채널 입력 필드

필수:

- `platform_code`
- `external_channel_id`
- `channel_url`
- `channel_type`
- `owner_type`
- `owner_group_id` 또는 `owner_member_id`

선택:

- `handle`
- `display_label`
- `is_official`
- `is_primary`
- `status`
- `note`

## 영상 입력 필드

필수:

- `channel_id`
- `title`
- `published_at`
- `video_url`

선택:

- `external_video_id`
- `description`
- `thumbnail_url`
- `pinned`
- `note`

# Reference Table

## `platforms`

용도:

- 지원 플랫폼 기준 테이블

주요 컬럼:

```text
platform_code        text PK
display_name         text not null
support_mode         text not null   -- auto | manual | disabled
is_enabled           boolean not null default false
created_at           timestamptz not null
updated_at           timestamptz not null
```

초기값:

- `youtube`

# Core Domain Tables

## `idol_groups`

용도:

- 그룹 기본 정보

주요 컬럼:

```text
id                   uuid PK
slug                 text not null
display_name         text not null
normalized_name      text not null
description          text null
cover_image_url      text null
status               text not null       -- active | inactive | hidden
is_featured          boolean not null default false
created_at           timestamptz not null
updated_at           timestamptz not null
deleted_at           timestamptz null
```

제약:

- PK: `id`
- UK: `slug`
- CHECK: `status in ('active','inactive','hidden')`

인덱스:

- `ux_idol_groups_slug (slug)`
- `ix_idol_groups_featured (status, is_featured desc, updated_at desc)`
- `ix_idol_groups_normalized_name (normalized_name)`

운영자 입력 여부:

- 대부분 직접 입력

## `idol_members`

용도:

- 그룹 소속 멤버 기본 정보

주요 컬럼:

```text
id                   uuid PK
group_id             uuid not null
slug                 text not null
display_name         text not null
normalized_name      text not null
profile_image_url    text null
sort_order           integer not null default 0
status               text not null       -- active | inactive | hidden
created_at           timestamptz not null
updated_at           timestamptz not null
deleted_at           timestamptz null
```

제약:

- PK: `id`
- FK: `group_id -> idol_groups.id`
- UK: `slug`
- CHECK: `status in ('active','inactive','hidden')`

인덱스:

- `ux_idol_members_slug (slug)`
- `ix_idol_members_group_sort (group_id, sort_order asc, updated_at desc)`
- `ix_idol_members_normalized_name (normalized_name)`

운영자 입력 여부:

- 대부분 직접 입력

설명:

- MVP에서는 멤버가 하나의 그룹에만 속한다고 가정한다.
- 추후 다중 소속이나 히스토리가 필요하면 `group_memberships` 분리 가능

## `youtube_channels`

용도:

- 그룹 공식 채널과 멤버 개인 채널 등록

주요 컬럼:

```text
id                   uuid PK
platform_code        text not null
external_channel_id  text not null
handle               text null
channel_url          text not null
display_label        text null
channel_type         text not null       -- GROUP_OFFICIAL | MEMBER_PERSONAL | SUB_UNIT | LABEL
owner_type           text not null       -- GROUP | MEMBER
owner_group_id       uuid null
owner_member_id      uuid null
is_official          boolean not null default false
is_primary           boolean not null default false
status               text not null       -- active | inactive | hidden
verified_at          timestamptz null
last_seen_at         timestamptz null
created_at           timestamptz not null
updated_at           timestamptz not null
deleted_at           timestamptz null
```

제약:

- PK: `id`
- FK: `platform_code -> platforms.platform_code`
- FK: `owner_group_id -> idol_groups.id` nullable
- FK: `owner_member_id -> idol_members.id` nullable
- UK: `(platform_code, external_channel_id)`
- CHECK: `channel_type in ('GROUP_OFFICIAL','MEMBER_PERSONAL','SUB_UNIT','LABEL')`
- CHECK: `owner_type in ('GROUP','MEMBER')`
- CHECK:
  - `owner_type = 'GROUP' -> owner_group_id is not null and owner_member_id is null`
  - `owner_type = 'MEMBER' -> owner_member_id is not null and owner_group_id is null`
- CHECK: `status in ('active','inactive','hidden')`

인덱스:

- `ux_youtube_channels_platform_external (platform_code, external_channel_id)`
- `ix_youtube_channels_owner_group (owner_group_id, channel_type, is_primary desc)`
- `ix_youtube_channels_owner_member (owner_member_id, channel_type, is_primary desc)`
- `ix_youtube_channels_handle (handle)`

운영자 입력 여부:

- 소유 주체, 채널 유형, 공식 여부는 직접 입력
- handle/channel_url/external_channel_id도 초기에 직접 입력

자동 수집 연계:

- `display_label`, `verified_at`, `last_seen_at`는 수집 결과로 업데이트 가능

## `youtube_videos`

용도:

- YouTube 영상 메타데이터 저장

주요 컬럼:

```text
id                   uuid PK
channel_id           uuid not null
external_video_id    text not null
title                text not null
description          text null
thumbnail_url        text null
published_at         timestamptz not null
video_url            text not null
source_type          text not null       -- youtube_imported | manual
freshness_status     text not null       -- fresh | stale | manual | unknown
stale_at             timestamptz null
is_pinned            boolean not null default false
last_successful_sync_at timestamptz null
created_at           timestamptz not null
updated_at           timestamptz not null
deleted_at           timestamptz null
```

제약:

- PK: `id`
- FK: `channel_id -> youtube_channels.id`
- UK: `(external_video_id)`
- CHECK: `source_type in ('youtube_imported','manual')`
- CHECK: `freshness_status in ('fresh','stale','manual','unknown')`

인덱스:

- `ux_youtube_videos_external_video_id (external_video_id)`
- `ix_youtube_videos_channel_published (channel_id, published_at desc)`
- `ix_youtube_videos_published (published_at desc)`

운영자 입력 여부:

- manual 영상일 때 직접 입력 가능

자동 수집 연계:

- imported 영상은 YouTube API가 source of truth

# Operator Metadata Tables

## `group_operator_metadata`

용도:

- 그룹 보정 정보

주요 컬럼:

```text
group_id             uuid PK
override_description text null
override_cover_image_url text null
override_status      text null
note                 text null
updated_by_operator  text null
created_at           timestamptz not null
updated_at           timestamptz not null
```

## `member_operator_metadata`

용도:

- 멤버 보정 정보

주요 컬럼:

```text
member_id            uuid PK
override_profile_image_url text null
override_sort_order  integer null
override_status      text null
note                 text null
updated_by_operator  text null
created_at           timestamptz not null
updated_at           timestamptz not null
```

## `channel_operator_metadata`

용도:

- 채널 보정 정보

주요 컬럼:

```text
channel_id           uuid PK
override_handle      text null
override_channel_url text null
override_display_label text null
override_channel_type text null
override_is_official boolean null
override_is_primary  boolean null
note                 text null
updated_by_operator  text null
created_at           timestamptz not null
updated_at           timestamptz not null
```

## `video_operator_metadata`

용도:

- 영상 보정 정보

주요 컬럼:

```text
video_id             uuid PK
override_title       text null
override_description text null
override_thumbnail_url text null
override_published_at timestamptz null
override_video_url   text null
override_is_pinned   boolean null
note                 text null
updated_by_operator  text null
created_at           timestamptz not null
updated_at           timestamptz not null
```

# Sync / Ingestion Tables

## `youtube_sync_metadata`

용도:

- 채널 메타 / 최신 영상 동기화 상태

주요 컬럼:

```text
id                   uuid PK
channel_id           uuid not null
resource_scope       text not null       -- channel_profile | channel_videos
sync_key             text not null
last_attempted_at    timestamptz null
last_succeeded_at    timestamptz null
last_status          text not null       -- idle | success | failed | partial
last_error_code      text null
last_error_message   text null
consecutive_failures integer not null default 0
next_scheduled_at    timestamptz null
backoff_until        timestamptz null
etag                 text null
created_at           timestamptz not null
updated_at           timestamptz not null
```

제약:

- UK: `(channel_id, resource_scope)`

인덱스:

- `ux_youtube_sync_channel_scope (channel_id, resource_scope)`
- `ix_youtube_sync_due (resource_scope, next_scheduled_at asc)`

## `raw_source_records`

용도:

- YouTube API 원본 payload 저장

주요 컬럼:

```text
id                   uuid PK
platform_code        text not null
resource_scope       text not null
channel_id           uuid null
external_object_id   text not null
http_status          integer null
request_trace_id     text null
payload              jsonb not null
fetched_at           timestamptz not null
normalized_at        timestamptz null
created_at           timestamptz not null
updated_at           timestamptz not null
```

# Serving / Read Model Tables

## `group_serving_state`

용도:

- 그룹 목록 / 홈용 파생 상태

주요 컬럼:

```text
group_id                uuid PK
official_channel_count  integer not null default 0
member_count            integer not null default 0
member_personal_channel_count integer not null default 0
latest_video_at         timestamptz null
latest_video_thumbnail_url text null
home_visibility         boolean not null default true
detail_visibility       boolean not null default true
last_projection_refresh_at timestamptz not null
```

## `member_serving_state`

용도:

- 멤버 상세 / 그룹 멤버 리스트용 파생 상태

주요 컬럼:

```text
member_id               uuid PK
personal_channel_count  integer not null default 0
latest_video_at         timestamptz null
detail_visibility       boolean not null default true
last_projection_refresh_at timestamptz not null
```

# Recommended Initial Operator Flow

1. 그룹 생성
   - `idol_groups`
   - 필요 시 `group_operator_metadata`
2. 멤버 생성
   - `idol_members`
   - 필요 시 `member_operator_metadata`
3. 채널 생성
   - `youtube_channels`
   - 필요 시 `channel_operator_metadata`
4. YouTube 자동 수집 시작
   - `youtube_sync_metadata`
   - `raw_source_records`
   - `youtube_videos`
5. 수동 영상 보정
   - `youtube_videos`
   - `video_operator_metadata`
6. 읽기 모델 재계산
   - `group_serving_state`
   - `member_serving_state`

# Notes

- MVP에서는 운영자의 입력 정확도가 제품 품질 핵심이다.
- `owner_type`와 `channel_type`은 가장 중요한 운영 필드다.
- `youtube_videos`는 imported와 manual을 같은 테이블에 두되 `source_type`으로 구분한다.
- 멤버 다중 소속, 활동 히스토리, 기간별 멤버십은 1차 범위에서 제외한다.

# Links

- Related PRD:
  - `../product/PRD-mystarnow-idol-youtube-bff-server.md`
- Related ADRs:
  - `../architecture/ADR-mystarnow-idol-youtube-bff-architecture.md`
- Related Decisions:
  - `../architecture/TRD-mystarnow-idol-youtube-bff-server.md`
