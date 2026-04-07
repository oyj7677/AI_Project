---
id: TRD-MYSTARNOW-BFF-SERVER
type: trd
status: draft
owner:
created: 2026-04-04
updated: 2026-04-04
related_prd: PRD-MYSTARNOW-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-BFF-ARCHITECTURE
---

# Summary

`MyStarNow BFF 서버`는 앱이 직접 외부 플랫폼과 통신하지 않도록 인플루언서 메타데이터, 공식 채널, 라이브 상태, 최근 활동, 예정 방송을 수집·정규화·캐시해 REST API로 제공하는 서버다. 본 문서는 MVP 서버 구현 범위와 기술 요구사항을 정의한다.

# Technical Goals

- 앱이 화면 중심 응답을 받을 수 있도록 BFF API 계약을 제공한다.
- 외부 플랫폼별 수집 로직을 어댑터 계층으로 분리한다.
- 운영성 데이터와 자동 수집 데이터를 결합해 단일 읽기 모델을 만든다.
- 플랫폼 일부 실패가 전체 API 장애로 이어지지 않도록 캐시와 fallback 전략을 적용한다.
- MVP는 Android `Version 1` 화면에 필요한 서버 기능을 모두 커버한다.
- 1차 서버 지원 플랫폼은 `YouTube`, `Instagram`으로 제한한다.
- YouTube는 공식 API 기반 자동 수집, Instagram은 수동 등록 중심 전략을 따른다.

# Technical Stack

## Runtime / Build

- Kotlin
- Spring Boot
- Java 21
- Gradle Kotlin DSL

## API / Application

- Spring MVC for inbound REST API
- Jackson Kotlin module for JSON serialization
- Spring WebClient for outbound platform HTTP calls
- Spring Scheduling for polling workers
- Spring Task Execution for adapter-specific executors

## Persistence / Cache

- PostgreSQL as primary relational store
- Spring Data JPA for repository layer
- Flyway for schema migration
- Redis for short-lived cache, stale markers, and future distributed lock support

## Security / Ops

- Spring Security for operator endpoints
- Spring Boot Actuator for health and management endpoints
- Micrometer + Prometheus for metrics collection
- springdoc-openapi for API specification and Swagger UI

## Testing / Local Dev

- JUnit 5
- Spring Boot Test
- MockK
- Testcontainers
- Docker Compose for local PostgreSQL and Redis

## Modularization

- Spring Modulith recommended for module boundary verification in the modular monolith structure

# Platform Strategy

## Phase 1 Supported Platforms

- YouTube
- Instagram

## YouTube Strategy

- 공식 `YouTube Data API v3`를 사용한다.
- 채널 정보, 영상 목록, 최근 업로드 정보는 자동 수집 대상으로 본다.
- 라이브 상태는 API 응답과 polling 기반 보완 전략을 함께 사용한다.
- quota 절감을 위해 필요한 필드만 조회하고 캐시를 적극 사용한다.

## Instagram Strategy

- 공식 API 활용은 제한적으로만 고려한다.
- 1차는 운영자 수동 등록을 기본으로 한다.
- 프로필 링크, 소개, 최근 대표 게시물, 중요 공지는 수동 입력 또는 제한적 자동 수집으로 처리한다.
- 자동 수집이 불안정한 데이터는 무리하게 실시간화하지 않는다.

# Functional Requirements

## FR-1 Influencer Catalog Management

- 요구사항 설명: 인플루언서 기본 정보와 공식 채널 정보를 저장하고 조회할 수 있어야 한다.
- 입력: 운영자 등록/수정 데이터
- 출력: 인플루언서 카탈로그 레코드
- 예외: 중복 slug 또는 잘못된 플랫폼 값은 저장 거부

## FR-2 Home Feed Aggregation

- 요구사항 설명: 홈 화면용 라이브 상태, 최신 업데이트, 오늘 예정 방송, 추천 인플루언서를 한 응답으로 제공해야 한다.
- 입력: 저장된 메타데이터, 라이브 캐시, 활동 캐시, 일정 데이터
- 출력: `/v1/home` 응답
- 예외: 일부 소스 실패 시 사용 가능한 섹션만 반환하고 섹션별 stale 정보 포함

## FR-3 Influencer Directory Query

- 요구사항 설명: 검색어, 카테고리, 플랫폼 필터로 인플루언서 목록을 조회할 수 있어야 한다.
- 입력: `q`, `category`, `platform`, `sort`
- 출력: 필터링된 인플루언서 리스트
- 예외: 유효하지 않은 필터는 무시하고 기본 정렬 사용

## FR-4 Influencer Detail Query

- 요구사항 설명: 특정 인플루언서의 프로필, 공식 채널, 라이브 상태, 최근 활동, 예정 방송을 한 번에 제공해야 한다.
- 입력: `slug`
- 출력: `/v1/influencers/{slug}` 응답
- 예외: 미존재 slug는 `404 not found`

## FR-5 Live Status Polling

- 요구사항 설명: 1차에서는 YouTube를 대상으로 라이브 상태를 주기적으로 조회하고 공통 모델로 저장해야 한다.
- 입력: YouTube 채널 정보
- 출력: 정규화된 라이브 상태 캐시
- 예외: 원천 조회 실패 시 마지막 정상 데이터 유지 및 stale 표시

## FR-6 Activity Harvesting

- 요구사항 설명: YouTube는 자동 수집, Instagram은 수동 또는 제한적 자동 수집으로 최근 활동 피드를 제공할 수 있어야 한다.
- 입력: 플랫폼 채널 정보 또는 운영자 입력 데이터
- 출력: 정규화된 활동 아이템 목록
- 예외: 플랫폼별 데이터가 없거나 실패해도 전체 피드는 유지

## FR-7 Schedule Management

- 요구사항 설명: 운영자가 예정 방송을 등록/수정할 수 있어야 하며 앱은 이를 조회할 수 있어야 한다.
- 입력: 일정 데이터
- 출력: 홈/상세 일정 응답
- 예외: 과거 일정은 조회 범위에서 자동 제외 가능

## FR-8 Category and Platform Metadata

- 요구사항 설명: 앱 필터 UI에 필요한 카테고리와 플랫폼 목록을 제공해야 한다.
- 입력: 저장된 메타데이터
- 출력: `/v1/categories`, `/v1/platforms`
- 예외: 값이 없으면 빈 배열 반환

## FR-9 App Config and Feature Flags

- 요구사항 설명: 앱이 사용 가능한 기능 및 서버 설정을 조회할 수 있어야 한다.
- 입력: 운영 설정값
- 출력: `/v1/meta/app-config`
- 예외: 설정 미존재 시 기본값 반환

## FR-10 Operator Correction Path

- 요구사항 설명: 잘못된 링크, 소개, 카테고리, 일정 정보를 운영자가 빠르게 수정할 수 있는 경로가 있어야 한다.
- 입력: 운영 수정 요청
- 출력: 변경 반영된 메타데이터
- 예외: 인증되지 않은 수정 요청은 거부

# Non-Functional Requirements

- 성능: `/v1/home` p95 500ms 이내, `/v1/influencers/{slug}` p95 700ms 이내
- 신선도: 라이브 상태는 3~5분 주기, 최근 활동은 10~30분 주기 갱신 목표
- 단계성: 1차 자동 수집은 YouTube 중심, Instagram은 수동 등록 중심으로 운영
- 가용성: 외부 플랫폼 일부 장애 시에도 API는 degraded mode로 응답해야 함
- 보안: 외부 플랫폼 키 및 운영자 인증 정보는 서버 환경변수 또는 비밀 저장소에서만 관리
- 관측성: 어댑터 실패율, stale 데이터 비율, API latency, fallback 발생 건수를 관측할 수 있어야 함
- 확장성: 초기에는 모듈형 단일 서버로 구현하되 플랫폼 어댑터는 독립적으로 확장 가능해야 함
- 운영성: 운영자가 링크/일정을 수동 수정하면 앱에 빠르게 반영되어야 함
- 표준화: 서버 기본 스택은 `Kotlin + Spring Boot + PostgreSQL + Flyway + Redis`를 기준으로 함

# Architecture Scope

- API Layer: 앱 전용 REST endpoint
- Service Layer: 홈 조합, 상세 조합, 필터 조회, 설정 조회
- Adapter Layer: YouTube adapter, Instagram limited adapter
- Storage Layer: 메타데이터, 일정, 캐시, stale 상태 저장
- Operator Layer: 수동 등록/수정 입력 경로
- Scheduler Layer: polling 및 refresh orchestration
- Module Boundary Layer: Spring Modulith-based application module verification

# Data Contracts

## Input Data

### Operator-managed Influencer

- 데이터 소스: 운영자 입력
- 필드: `id`, `name`, `slug`, `bio`, `profileImageUrl`, `categories[]`, `isFeatured`
- 갱신 주기: 즉시

### Channel Metadata

- 데이터 소스: 운영자 입력
- 필드: `influencerId`, `platform`, `handle`, `channelUrl`, `isOfficial`, `isPrimary`
- 갱신 주기: 즉시

### Live Source Payload

- 데이터 소스: 플랫폼 어댑터
- 필드: `platform`, `externalChannelId`, `isLive`, `title`, `startedAt`, `viewerCount`, `watchUrl`
- 갱신 주기: 3~5분

### Activity Source Payload

- 데이터 소스: 플랫폼 어댑터
- 필드: `platform`, `contentType`, `title`, `summary`, `thumbnailUrl`, `publishedAt`, `externalUrl`
- 갱신 주기: 10~30분

### Schedule Input

- 데이터 소스: 운영자 입력
- 필드: `influencerId`, `title`, `scheduledAt`, `platform`, `note`
- 갱신 주기: 즉시

## Output Data

### `/v1/home`

- UI 또는 API로 노출할 데이터: `liveNow[]`, `latestUpdates[]`, `todaySchedules[]`, `featuredInfluencers[]`, `generatedAt`
- 포맷: JSON

### `/v1/influencers`

- UI 또는 API로 노출할 데이터: `items[]`, `page`, `nextCursor`
- 포맷: JSON

### `/v1/influencers/{slug}`

- UI 또는 API로 노출할 데이터: `profile`, `channels[]`, `liveStatus`, `recentActivities[]`, `schedules[]`, `relatedTags[]`
- 포맷: JSON

### `/v1/meta/app-config`

- UI 또는 API로 노출할 데이터: `featureFlags`, `supportedPlatforms`, `minimumRefreshHints`
- 포맷: JSON

# UI / Interaction Requirements

- 서버는 앱 홈/목록/상세가 추가 조합 없이 바로 쓸 수 있는 응답 형태를 제공해야 한다.
- 서버는 섹션 단위 stale 정보를 포함해 앱이 degraded UI를 표현할 수 있게 해야 한다.
- 운영 입력 경로는 최소한 CRUD 가능해야 하며, 초기에는 간단한 내부 웹 또는 시트 동기화 방식도 허용한다.
- 운영 수정은 배치 완료를 기다리지 않고 읽기 모델에 빠르게 반영되어야 한다.

# API / Integration Requirements

- 사용할 API: `GET /v1/home`, `GET /v1/influencers`, `GET /v1/influencers/{slug}`, `GET /v1/categories`, `GET /v1/platforms`, `GET /v1/meta/app-config`
- 인증 방식: 앱 공개 조회 API는 무인증, 운영 경로는 내부 인증 필요
- rate limit 고려: 플랫폼별 polling 주기 분리, backoff, 캐시 선응답, 실패 소스 격리
- 실패 시 fallback: 마지막 정상 캐시 사용, stale 마킹, 수동 운영 데이터 우선
- outbound client: `Spring WebClient` 사용, full WebFlux 서버 전환은 기본안에 포함하지 않음
- 1차 수집 방식: YouTube는 공식 API 자동 수집, Instagram은 수동 등록 + 제한적 자동 수집

# State Management

- 영속 상태: 인플루언서 메타데이터, 채널 정보, 일정, 캐시된 라이브 상태, 캐시된 활동 데이터
- 파생 상태: 홈 섹션 조합 결과, 인플루언서 목록 필터 결과, 상세 조합 결과
- 작업 상태: polling job 상태, 마지막 성공 시각, 마지막 실패 원인, stale 여부
- 서버 상태와 클라이언트 상태 분리 원칙: 서버는 읽기 모델을 완성해 제공하고 앱은 화면 상태만 관리

# Observability

- 로그: 어댑터 요청/응답 실패, 정규화 실패, 운영 수정 반영, stale 전환 이벤트
- 메트릭: API latency, source success rate, cache hit rate, stale ratio, operator change lead time
- 에러 추적: 수집 파서 오류, 서버 예외, 스케줄러 실패
- 헬스체크: Spring Boot Actuator health/readiness/liveness endpoint 제공

# Test Strategy

- 단위 테스트: 어댑터 응답 정규화, 필터링 로직, 홈 조합 로직, stale 판정 로직
- 통합 테스트: 엔드포인트 응답, DB + 서비스 조합, polling 결과 반영
- 수동 검증: 플랫폼 일부 실패 시 fallback, 운영 수정 후 앱 반영, 홈/목록/상세 응답 확인

# Delivery Notes

- 구현 순서:
  1. 데이터 모델 및 DB 스키마 정의
  2. 인플루언서/채널/일정 CRUD
  3. YouTube 채널/영상 자동 수집
  4. 홈/목록/상세 API
  5. YouTube 라이브 상태 polling
  6. Instagram 수동 등록 흐름
  7. 최근 활동 수집/정규화
  8. app-config 및 feature flag
  9. 운영 입력 경로
  10. observability 및 actuator 정비
  11. OpenAPI 문서화
- feature flag 여부:
  - 플랫폼별 어댑터 on/off 필요
  - 일정/타임라인 섹션 노출 on/off 필요
- 배포 전 체크:
  - 최소 20명 인플루언서 샘플 데이터 적재
  - 모든 기본 endpoint 응답 검증
  - stale/fallback 동작 검증

# Open Questions

- Spring MVC + WebClient 조합으로 충분할지, 추후 full WebFlux 전환이 필요한지
- 저장소를 PostgreSQL 단일로 시작할지 Redis를 함께 둘지
- 운영 입력을 내부 웹으로 시작할지 시트 업로드로 시작할지
- YouTube polling 주기를 어떻게 가져갈지
- Instagram에서 어떤 항목까지 자동 수집으로 볼지

# Links

- Related PRD: `../product/PRD-mystarnow-bff-server.md`
- Related ADRs: `./ADR-mystarnow-bff-architecture.md`
- Related Issues:
- Related Front Docs:
  - `../../front/product/PRD-mystarnow.md`
  - `../../front/architecture/TRD-mystarnow-android-cmp.md`
