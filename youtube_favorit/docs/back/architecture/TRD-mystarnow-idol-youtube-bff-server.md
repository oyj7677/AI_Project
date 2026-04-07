---
id: TRD-MYSTARNOW-IDOL-YOUTUBE-BFF-SERVER
type: trd
status: draft
owner:
created: 2026-04-05
updated: 2026-04-05
related_prd: PRD-MYSTARNOW-IDOL-YOUTUBE-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-IDOL-YOUTUBE-BFF-ARCHITECTURE
---

# Summary

`MyStarNow Idol YouTube BFF`는 아이돌 그룹 중심 유튜브 허브를 위한 서버 기술 설계 문서다. 서버는 `그룹-멤버-채널-영상` 도메인을 기준으로 YouTube 공식 API 데이터를 수집하고, 운영자가 입력한 관계 데이터를 결합해 그룹 중심 읽기 API를 제공한다. 본 문서는 MVP 서버 구현 범위, 모듈 경계, 데이터 흐름, API 방향, 운영 전략을 정의한다.

# Technical Goals

- 클라이언트가 그룹 중심 화면을 바로 구성할 수 있는 BFF 응답을 제공한다.
- 그룹, 멤버, 채널, 영상 관계를 정규화된 내부 모델로 유지한다.
- YouTube 채널 메타데이터와 최신 영상 목록을 공식 API로 자동 수집한다.
- 그룹-멤버-채널 연결은 운영자가 안정적으로 관리할 수 있게 한다.
- 홈 피드와 그룹 상세 피드는 커서 기반 페이지네이션을 지원한다.
- YouTube 일부 채널 동기화 실패가 전체 홈/상세 API 장애로 이어지지 않도록 degraded mode를 지원한다.
- MVP는 Android/Web 검증 화면이 공통으로 사용할 수 있는 안정적 API 계약을 제공한다.

# Technical Stack

## Runtime / Build

- Kotlin
- Spring Boot
- Java 21
- Gradle Kotlin DSL

## API / Application

- Spring MVC
- Jackson Kotlin module
- Spring WebClient
- Spring Scheduling
- Spring Task Execution

## Persistence / Cache

- PostgreSQL
- Spring Data JPA
- Flyway
- Redis

## Security / Ops

- Spring Security
- Spring Boot Actuator
- Micrometer + Prometheus
- springdoc-openapi

## Testing / Local Dev

- JUnit 5
- Spring Boot Test
- MockK
- Testcontainers
- Docker Compose

## Modularization

- 모듈형 단일 서버
- Spring Modulith 권장

# Domain Strategy

## Core Domain

- `Group`
  - 그룹 메타데이터
  - 활동 상태
  - 대표 이미지
- `Member`
  - 멤버 메타데이터
  - 그룹 소속
  - 정렬 순서
- `Channel`
  - YouTube 채널
  - 소유 주체: 그룹 또는 멤버
  - 채널 유형
  - 공식 여부
- `Video`
  - 영상 메타데이터
  - 업로드 채널
  - 게시 시각
  - 썸네일
  - 외부 URL

## Relationship Rules

- 하나의 그룹은 여러 공식 채널을 가질 수 있다.
- 하나의 그룹은 여러 멤버를 가진다.
- 하나의 멤버는 0개 이상 개인 채널을 가질 수 있다.
- 하나의 채널은 정확히 하나의 소유 주체를 가진다.
  - 그룹 소유 또는 멤버 소유
- 채널 유형은 다음 중 하나다.
  - `GROUP_OFFICIAL`
  - `MEMBER_PERSONAL`
  - `SUB_UNIT`
  - `LABEL`

## Source of Truth

- 그룹/멤버/채널 관계: 운영 입력
- YouTube 채널 메타데이터: YouTube API
- YouTube 최신 영상 목록: YouTube API
- 대표 이미지, 소개, 관계 보정: 운영 입력 우선
- 수동 영상 등록: 자동 수집보다 우선 표시 가능

# Module Scope

## 1. Group Catalog Module

- 그룹 CRUD
- 그룹 메타데이터 조회
- 그룹 정렬/검색용 읽기 모델

## 2. Member Catalog Module

- 멤버 CRUD
- 그룹 소속 관리
- 멤버 정렬 순서 관리

## 3. Channel Registry Module

- YouTube 채널 등록
- 채널 소유 주체 연결
- 채널 유형 관리
- 공식 여부 / 대표 여부 관리

## 4. Video Aggregation Module

- 채널별 영상 메타데이터 저장
- 그룹 통합 피드 조합
- 멤버 개인 채널 영상 조합
- 수동 영상 등록 경로

## 5. Operator Module

- 그룹/멤버/채널/영상 입력 API
- 내부 인증
- 운영 보정 데이터 반영

## 6. Sync Module

- YouTube 채널 메타 동기화
- YouTube 최근 영상 동기화
- backoff / retry / stale 처리
- sync metadata 저장

## 7. Read API Module

- 홈 피드 API
- 그룹 목록 API
- 그룹 상세 API
- 멤버 상세 API
- 메타 설정 API

# API Strategy

## Principles

- 응답은 화면 조합 중심 구조를 가진다.
- envelope는 `meta`, `data`, `errors`를 유지한다.
- 섹션 단위 partial failure 구조를 유지한다.
- 피드 응답은 커서 기반 페이지네이션을 지원한다.
- 플랫폼은 1차에 `YouTube` 하나만 활성화한다.

## Suggested MVP Endpoints

### Public Read APIs

- `GET /v1/home`
  - 홈 최근 업로드 피드
  - `cursor`, `limit` 지원
- `GET /v1/groups`
  - 그룹 목록
  - 검색, 정렬, 페이지네이션
- `GET /v1/groups/{groupSlug}`
  - 그룹 헤더
  - 공식 채널
  - 멤버 리스트
  - 최근 업로드 피드
- `GET /v1/members/{memberSlug}`
  - 멤버 프로필
  - 연결 채널
  - 최근 업로드
- `GET /v1/meta/app-config`

### Internal Operator APIs

- `POST /internal/operator/groups`
- `PUT /internal/operator/groups/{groupSlug}`
- `POST /internal/operator/members`
- `PUT /internal/operator/members/{memberSlug}`
- `POST /internal/operator/channels`
- `PUT /internal/operator/channels/{channelId}`
- `POST /internal/operator/videos`
- `PUT /internal/operator/videos/{videoId}`

## Response Shape Requirements

- 홈 최근 업로드 아이템은 아래 필드를 포함해야 한다.
  - `videoId`
  - `title`
  - `thumbnailUrl`
  - `publishedAt`
  - `channelId`
  - `channelName`
  - `channelType`
  - `groupSlug`
  - `groupName`
  - `memberSlug`
  - `memberName`
  - `videoUrl`
- 그룹 상세는 공식 채널 섹션과 멤버 개인 채널 섹션을 분리해 반환해야 한다.
- 피드 pageInfo는 `limit`, `nextCursor`, `hasNext`를 제공해야 한다.

# Data Model Requirements

## Minimum New Tables

- `idol_groups`
  - `id`
  - `slug`
  - `display_name`
  - `normalized_name`
  - `description`
  - `cover_image_url`
  - `status`
  - `is_featured`
- `idol_members`
  - `id`
  - `group_id`
  - `slug`
  - `display_name`
  - `normalized_name`
  - `profile_image_url`
  - `sort_order`
  - `status`
- `youtube_channels`
  - `id`
  - `external_channel_id`
  - `handle`
  - `channel_url`
  - `display_label`
  - `channel_type`
  - `owner_type`
  - `owner_group_id`
  - `owner_member_id`
  - `is_official`
  - `is_primary`
  - `status`
- `youtube_videos`
  - `id`
  - `channel_id`
  - `external_video_id`
  - `title`
  - `description`
  - `thumbnail_url`
  - `published_at`
  - `video_url`
  - `source_type`
  - `freshness_status`
  - `stale_at`
- `youtube_sync_metadata`
  - 채널 메타 / 영상 목록 동기화 상태

## Suggested Supporting Tables

- `group_operator_metadata`
- `member_operator_metadata`
- `channel_operator_metadata`
- `video_operator_metadata`
- `group_serving_state`
- `member_serving_state`
- `raw_source_records`

## Constraints

- 그룹 slug unique
- 멤버 slug unique
- YouTube external channel id unique
- YouTube external video id unique
- 하나의 채널은 group 또는 member 중 하나에만 귀속
- 대표 채널은 owner + channel_type 기준 1개 제한 가능

# Functional Requirements

## FR-1 Group Management

- 운영자는 그룹을 생성/수정할 수 있어야 한다.
- 그룹은 홈/목록 정렬에 필요한 메타를 가져야 한다.

## FR-2 Member Management

- 운영자는 멤버를 생성/수정하고 그룹에 연결할 수 있어야 한다.
- 멤버 정렬 순서를 관리할 수 있어야 한다.

## FR-3 Channel Relationship Management

- 운영자는 채널을 그룹 공식 채널 또는 멤버 개인 채널로 분류할 수 있어야 한다.
- 채널의 소유 주체와 유형을 보정할 수 있어야 한다.

## FR-4 YouTube Channel Sync

- 서버는 YouTube API를 사용해 채널 메타데이터를 수집할 수 있어야 한다.
- 채널 썸네일, 제목, 설명 등은 운영 보정값이 없을 때 자동 수집값을 사용한다.

## FR-5 YouTube Video Sync

- 서버는 채널별 최신 영상 목록을 주기적으로 수집할 수 있어야 한다.
- 영상은 채널 기준이 아니라 그룹/멤버 읽기 모델로 재조합 가능해야 한다.

## FR-6 Home Feed

- 홈은 여러 그룹의 최근 유튜브 영상을 하나의 피드로 보여줘야 한다.
- 그룹/멤버/채널 유형 정보가 같이 보여야 한다.
- 커서 기반 더보기를 지원해야 한다.

## FR-7 Group Detail

- 그룹 상세는 그룹 헤더, 공식 채널, 멤버, 최근 업로드 피드를 제공해야 한다.
- 최근 업로드 피드는 공식 채널과 멤버 개인 채널 영상을 함께 포함해야 한다.

## FR-8 Member Detail

- 멤버 상세는 멤버 정보, 연결 채널, 최근 업로드 영상을 제공해야 한다.

## FR-9 Operator Video Path

- 운영자는 수동으로 영상을 등록할 수 있어야 한다.
- 수동 영상은 홈/상세 피드에 바로 반영되어야 한다.

## FR-10 Degraded Response

- 일부 채널 동기화 실패 시 마지막 정상 영상 데이터를 반환하고 stale/partial 상태를 포함해야 한다.

# Non-Functional Requirements

- 성능: 홈 피드 p95 500ms 이내, 그룹 상세 p95 700ms 이내
- 신선도: YouTube 최근 영상 동기화 목표 10~30분
- 가용성: 일부 채널 실패가 전체 그룹 화면 실패로 이어지지 않아야 함
- 운영성: 그룹 신규 온보딩 10분 이내 가능해야 함
- 확장성: 향후 타 플랫폼 확장을 위해 platform abstraction을 열어두되, MVP는 YouTube 전용으로 단순화
- 관측성: sync success/failure, stale ratio, API latency, operator write count를 측정

# Sync / Reliability Strategy

## Polling Scope

- `channel_profile`
- `channel_videos`

## Retry / Backoff

- 채널 단위 retry
- 연속 실패 시 exponential backoff
- 마지막 정상 데이터 유지

## Stale Behavior

- 최신 동기화 실패 시 기존 영상 목록 유지
- 섹션 freshness를 `stale`로 전환
- partial failure를 응답 메타에 노출

## Raw vs Normalized vs Serving

- Raw: YouTube API 원본 payload
- Normalized: 채널/영상 메타 엔티티
- Serving: 그룹/멤버/홈 읽기 모델

# UI / Interaction Requirements

- 서버는 홈 피드가 카드형 리스트로 바로 렌더링 가능한 응답을 제공해야 한다.
- 그룹 상세는 "공식 채널 섹션"과 "멤버 섹션"이 분리된 구조를 제공해야 한다.
- 홈과 그룹 상세 피드는 모두 더보기 기반 pageInfo를 가져야 한다.
- 운영자 입력 후 읽기 모델 반영은 배치 완료를 기다리지 않고 빠르게 이뤄져야 한다.

# Rollout Plan

## Phase 1

- 그룹/멤버/채널 운영 입력
- YouTube 채널 메타 자동 수집
- YouTube 최신 영상 자동 수집
- 홈 피드
- 그룹 목록 / 그룹 상세

## Phase 2

- 멤버 상세
- 즐겨찾기용 맞춤 조회
- 추천 그룹 / 최근 활동 그룹

## Phase 3

- 라이브 방송 구분
- 서브 유닛 / 레이블 채널 정교화
- 추가 플랫폼 확장

# Links

- Related PRD:
  - `../product/PRD-mystarnow-idol-youtube-bff-server.md`
- Related ADRs:
  - `./ADR-mystarnow-idol-youtube-bff-architecture.md`
- Related Front Docs:
  - `../../front/product/PRD-mystarnow-idol-youtube-hub.md`
