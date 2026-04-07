---
id: TRD-MYSTARNOW-ANDROID-CMP
type: trd
status: draft
owner:
created: 2026-04-04
updated: 2026-04-04
related_prd: PRD-MYSTARNOW
related_adrs:
  - ADR-MYSTARNOW-ARCHITECTURE
---

# Summary

`MyStarNow`는 1차 기준으로 인플루언서의 YouTube와 Instagram 소식을 한곳에서 확인하는 Android 우선 애플리케이션이다. 본 문서는 Compose Multiplatform 기반 클라이언트와 인플루언서/플랫폼 데이터를 정규화하는 얇은 집계 백엔드의 기술 요구사항을 정의한다.

# Technical Goals

- Android를 첫 출시 플랫폼으로 하되, UI와 비즈니스 로직은 Compose Multiplatform 공용 코드로 최대한 공유한다.
- 인플루언서별 공식 채널, 라이브 상태, 최근 콘텐츠, 최근 SNS 소식, 예정 방송을 한 화면 흐름으로 안정적으로 제공한다.
- MVP 범위는 PRD의 `Must Have + Nice to Have`까지 포함하며, 이는 런칭 전략 기준 `Version 1`에 해당한다.
- 1차 지원 플랫폼은 `YouTube`, `Instagram`으로 제한한다.
- 플랫폼별 데이터 수집 방식이 달라도 앱은 정규화된 단일 도메인 모델만 다루도록 한다.
- 외부 플랫폼 API 변동이나 수집 실패가 앱 전체 장애로 번지지 않도록 백엔드에서 흡수한다.
- 이후 iOS/Desktop 확장과 알림 기능 추가를 고려해 모듈 분리와 상태 구조를 설계한다.

# Architecture Decision Snapshot

- 클라이언트는 Compose Multiplatform + Kotlin 공용 로직 구조를 사용한다.
- Android 앱은 첫 배포 타깃이며, 공용 UI는 `commonMain`, Android 진입점은 `androidMain`에 둔다.
- 서버는 인플루언서 메타데이터, 채널 링크, 라이브 상태, 최근 활동, 예정 방송을 정규화해 제공하는 BFF 성격의 API를 맡는다.
- 앱은 외부 플랫폼을 직접 호출하지 않고 `MyStarNow API`만 호출한다.
- 즐겨찾기와 마지막 조회 데이터는 기기 로컬에 저장하고, 서버는 공개 메타데이터만 관리한다.
- 플랫폼별 완전 자동화보다 `수동 등록 + 부분 자동 수집`을 우선한다.
- 1차는 YouTube 자동 수집, Instagram 수동 등록 중심 전략을 따른다.

# Technical Stack

## Client

- Kotlin 100%
- Compose Multiplatform
- Android target as primary runtime
- MVVM + Clean Architecture
- Ktor Client for HTTP
- kotlinx.serialization for API parsing
- Kotlin Coroutines / Flow for async and state streams
- StateFlow for persistent UI state
- SharedFlow-based one-shot event delivery
- ViewModel-based presentation state
- `androidApp` only DI composition root with Hilt or equivalent Android wiring
- `shared` modules remain DI-framework-agnostic with constructor injection

## Local Storage

- SQLite 계열 로컬 캐시 레이어
- Android 전용 설정 저장소는 DataStore 또는 동일 역할의 래퍼 사용
- 공용 코드에서는 저장 인터페이스만 의존하고 플랫폼 구현은 분리

## Server

- 얇은 REST API + 배치 수집 작업 + 운영자 입력 경로
- 구현체는 `Kotlin + Spring Boot`를 기준으로 한다
- 인바운드 API는 `Spring MVC`, 외부 플랫폼 호출은 `Spring WebClient`를 사용한다
- 저장소는 `PostgreSQL + Flyway`, 캐시는 `Redis`를 기본안으로 둔다
- 수집 작업은 플랫폼 어댑터 단위로 분리

# HeyDealer-inspired Android Architecture

직접 제공된 ChatGPT 공유 링크는 로그인 화면으로만 확인되어 본문을 직접 열람할 수 없었다. 대신 헤이딜러/PRND가 공개한 Android 아키텍처 자료를 기준으로 `MyStarNow`에 적용할 원칙을 정리한다.

## Adopted Principles

- 아키텍처 기본형은 헤이딜러와 동일하게 `MVVM + Clean Architecture`를 사용한다.
- 의존 방향은 `Presentation -> Domain <- Data`로 유지하고 Domain은 Data 구현을 알지 않는다.
- 기능 단위 멀티 모듈 구조를 사용해 홈, 목록, 상세, 즐겨찾기, 라이브 기능을 느슨하게 분리한다.
- 새 코드는 Kotlin만 사용하고 비동기 처리 표준은 Coroutines/Flow로 통일한다.
- UI 상태는 `StateFlow`, 일회성 이벤트는 `SharedFlow` 기반으로 관리한다.
- ViewModel 또는 shared presentation 로직에는 Android framework 의존을 넣지 않는다.
- 모델은 레이어별로 분리하고 mapper를 통해 변환한다.

## Compose Multiplatform Adaptation

- 헤이딜러의 `Hilt 100%` 원칙은 `Android shell only DI`로 변형 적용한다.
- `shared/commonMain`은 constructor injection과 interface 조합으로 작성하고, Android 런처 계층만 Hilt 또는 동등한 Android DI로 조립한다.
- 헤이딜러의 멀티 모듈 전략은 `shared` 내부 feature/core 모듈과 `androidApp` shell 분리로 재해석한다.
- 공용 ViewModel 또는 presenter는 `commonMain`에 두고, Android lifecycle 연결부와 `SavedStateHandle` 성격의 래퍼만 `androidMain`에서 다룬다.
- 공통 코드에서 `android.*` import를 금지해 이후 iOS/Desktop 확장을 막지 않도록 한다.

## Operational Patterns Borrowed

- 디버그 빌드 전용 개발자 모드 화면을 두어 API base URL, feature flag, mock source, raw payload 확인을 지원한다.
- 원격 설정과 로컬 override를 함께 두어 기능 롤아웃과 QA 점검을 안전하게 진행한다.
- Analytics SDK를 화면 코드에 직접 붙이지 않고 gateway/interface 뒤로 숨긴다.
- 앱 장애 시 치명적 예외를 바로 종료하기보다 사용자 친화적 오류 화면과 재시도 경로를 우선 제공한다.

# Functional Requirements

## FR-1 Home Feed Rendering

- 요구사항 설명: 홈 화면에서 현재 라이브 중인 인플루언서, 최신 업데이트, 오늘 예정 방송을 한 번에 보여야 한다.
- 입력: `/v1/home` 응답
- 출력: `liveNow`, `latestUpdates`, `todaySchedules`, `featuredInfluencers`
- 예외: 일부 섹션 로딩 실패 시 실패한 섹션만 오류 상태로 표시하고 다른 섹션은 계속 노출

## FR-2 Influencer Directory

- 요구사항 설명: 사용자가 인플루언서를 검색하고 목록으로 탐색할 수 있어야 한다.
- 입력: 검색어, 카테고리 필터, 플랫폼 필터
- 출력: 필터링된 인플루언서 리스트
- 예외: 결과가 없으면 빈 상태 메시지와 필터 초기화 액션 표시

## FR-3 Influencer Detail

- 요구사항 설명: 인플루언서 상세 화면에서 프로필, 공식 채널 링크, 라이브 상태, 최근 활동, 예정 방송을 볼 수 있어야 한다.
- 입력: 인플루언서 `slug` 또는 `id`
- 출력: 정규화된 상세 화면 데이터
- 예외: 존재하지 않는 식별자면 `not found` 상태 표시

## FR-4 Official Channel Hub

- 요구사항 설명: 플랫폼별 공식 채널을 버튼 또는 카드 형태로 제공해야 한다.
- 입력: 채널 목록
- 출력: 플랫폼 아이콘, 핸들, 공식 여부, 외부 링크
- 예외: 특정 플랫폼 계정이 없으면 해당 플랫폼은 숨기거나 `미등록` 상태 표시

## FR-5 Live Status

- 요구사항 설명: 현재 라이브 상태를 홈, 목록, 상세에서 일관되게 보여야 한다.
- 입력: 라이브 상태 데이터
- 출력: `LIVE NOW`, 플랫폼명, 시작 시간, 방송 제목, 외부 열기 액션
- 예외: 상태 갱신이 실패하면 마지막 정상 상태와 `갱신 지연` 표시를 함께 노출

## FR-6 Unified Activity Feed

- 요구사항 설명: 유튜브 업로드, 인스타그램 게시물, X 포스트, 방송 공지를 시간순 피드로 보여야 한다.
- 입력: 플랫폼별 정규화 콘텐츠 목록
- 출력: 최근 활동 리스트 및 상세 타임라인
- 예외: 특정 플랫폼 항목만 비어 있어도 전체 피드는 유지

## FR-7 Scheduled Broadcast Section

- 요구사항 설명: 예정 방송을 홈과 상세에서 확인할 수 있어야 한다.
- 입력: 일정 데이터
- 출력: 날짜, 시간, 플랫폼, 제목, 메모
- 예외: 일정이 없으면 `예정된 방송 없음` 상태 표시

## FR-8 Favorites

- 요구사항 설명: 사용자가 인플루언서를 즐겨찾기에 추가하고 홈/즐겨찾기 화면에서 모아볼 수 있어야 한다.
- 입력: 즐겨찾기 토글 액션
- 출력: 로컬 저장된 즐겨찾기 목록
- 예외: 저장 실패 시 토스트 또는 스낵바로 실패 알림

## FR-9 Category Filter

- 요구사항 설명: 사용자는 인플루언서를 카테고리로 필터링할 수 있어야 한다.
- 입력: 카테고리 선택
- 출력: 해당 카테고리 기준 리스트 갱신
- 예외: 서버에 없는 카테고리는 무시하고 기본 목록 유지

## FR-10 Platform Filter

- 요구사항 설명: 사용자는 특정 플랫폼을 사용하는 인플루언서만 볼 수 있어야 한다.
- 입력: 플랫폼 선택
- 출력: 플랫폼 기준 필터링 목록
- 예외: 플랫폼 필터와 검색어가 충돌해 결과가 없으면 빈 상태 표시

## FR-11 Recent Activity Timeline

- 요구사항 설명: 상세 화면에 최근 활동을 시간순 타임라인으로 제공해야 한다.
- 입력: 최근 활동 목록
- 출력: 플랫폼 배지, 게시 시간, 콘텐츠 타입, 요약 텍스트
- 예외: 타임라인 데이터가 비면 요약 카드만 표시

## FR-12 Correction Request

- 요구사항 설명: 잘못된 링크 또는 정보 변경 요청을 위한 외부 제보 경로를 제공해야 한다.
- 입력: 인플루언서 상세 화면에서 제보 액션
- 출력: 외부 폼 또는 이메일 링크 열기
- 예외: 외부 링크 설정이 없으면 버튼을 숨김

# Non-Functional Requirements

- 성능: Android 기준 첫 진입 후 홈 데이터 표시 2.5초 이내 목표, 캐시 존재 시 1초 이내 1차 렌더링
- 응답성: 화면 내 필터 변경은 300ms 이내 반영 목표
- 데이터 신선도: 홈과 라이브 상태는 foreground 진입 시 자동 새로고침, 백그라운드 복귀 후 5분 이상 경과 시 재조회
- 가용성: 특정 플랫폼 소스가 실패해도 앱의 나머지 기능은 동작해야 함
- 오프라인 대응: 마지막 정상 데이터 캐시와 즐겨찾기는 네트워크 없이 읽을 수 있어야 함
- 보안: 외부 플랫폼 키나 비공개 토큰은 앱에 넣지 않고 서버에서만 관리
- 개인정보: MVP는 로그인 없이 공개 메타데이터만 다루며 개인식별정보 저장을 최소화
- 관측성: API 실패율, 캐시 적중률, 빈 상태 비율, 화면 로드 시간 측정 가능해야 함
- 확장성: iOS 타깃 추가 시 presentation/domain/data 공용 레이어 재사용 가능해야 함

# Architecture Scope

## Client

- `androidApp`: Android 런처, 딥링크, 시스템 UI, 추후 알림 진입점
- `shared/commonMain`: 도메인 모델, 유스케이스, 공용 UI, 네비게이션 모델, 상태 모델
- `shared/androidMain`: Android 특화 구현

## Presentation Layers

- `HomeScreen`
- `InfluencerListScreen`
- `InfluencerDetailScreen`
- `FavoritesScreen`
- `LiveNowScreen`

## Domain Layers

- `GetHomeFeed`
- `SearchInfluencers`
- `GetInfluencerDetail`
- `ToggleFavorite`
- `ObserveFavorites`
- `GetFeatureFlags`
- `TrackAnalyticsEvent`

## Data Layers

- `InfluencerRepository`
- `HomeRepository`
- `ActivityRepository`
- `FavoritesRepository`
- `ScheduleRepository`
- `FeatureFlagRepository`
- `AnalyticsRepository`
- `DiagnosticsRepository`

## Server

- `catalog service`: 인플루언서, 채널, 카테고리 메타데이터 제공
- `feed aggregator`: 라이브/콘텐츠/일정 정규화
- `polling workers`: 플랫폼별 상태 수집
- `operator input`: 수동 등록 또는 수정 경로

## External Integration

- YouTube source adapter
- Instagram source adapter
- X source adapter
- CHZZK source adapter
- SOOP source adapter

## Storage

- 서버 저장소: 인플루언서 메타데이터, 채널 정보, 최신 상태 캐시, 일정 데이터
- 앱 로컬 저장소: 즐겨찾기, 마지막 조회 데이터, 최근 검색어
- 디버그 저장소: 개발자 모드 override 값, mock endpoint, feature flag override

# Module Layout Proposal

```text
MyStarNow/
├─ frontend/
│  ├─ androidApp/
│  │  ├─ app/
│  │  ├─ di/
│  │  ├─ navigation/
│  │  └─ debug/
│  ├─ shared/
│  │  ├─ commonMain/
│  │  │  ├─ core/designsystem/
│  │  │  ├─ core/analytics/
│  │  │  ├─ core/featureflag/
│  │  │  ├─ core/diagnostics/
│  │  │  ├─ domain/
│  │  │  │  ├─ model/
│  │  │  │  ├─ repository/
│  │  │  │  └─ usecase/
│  │  │  ├─ data/
│  │  │  │  ├─ repository/
│  │  │  │  ├─ remote/
│  │  │  │  └─ local/
│  │  │  └─ feature/
│  │  │     ├─ home/
│  │  │     ├─ influencerlist/
│  │  │     ├─ influencerdetail/
│  │  │     ├─ favorites/
│  │  │     └─ livenow/
│  │  └─ androidMain/
│  │     ├─ lifecycle/
│  │     ├─ deeplink/
│  │     └─ platform/
└─ backend/
   ├─ api/
   ├─ workers/
   ├─ adapters/
   └─ storage/
```

# Layering Rules

## Dependency Rules

- `Presentation`은 `Domain`을 의존한다.
- `Data`는 `Domain`을 의존한다.
- `Domain`은 `Presentation`, `Data`, `Remote`, `Local`을 모른다.
- `Remote`와 `Local`은 `Data` 구현 세부사항으로만 존재한다.

## Model Mapping Rules

- Domain model은 비즈니스 의미를 기준으로 정의한다.
- Presentation model은 화면 렌더링과 문자열/상태 표현에 맞게 정의한다.
- Data entity는 repository 구현 경계에서만 사용한다.
- Remote response와 Local entity는 각각 별도 모델로 두고 mapper를 통해 Data entity 또는 Domain model로 변환한다.
- 같은 개념이라도 레이어마다 다른 클래스를 허용하며 직접 재사용하지 않는다.

## Example Mapping

- Domain: `Influencer`, `Channel`, `LiveStatus`, `ActivityItem`, `ScheduleItem`
- Presentation: `InfluencerUiModel`, `LiveBadgeModel`, `ActivityTimelineItemModel`
- Data: `InfluencerEntity`, `ChannelEntity`
- Remote: `InfluencerResponse`, `LiveStatusResponse`, `ActivityResponse`
- Local: `InfluencerCache`, `FavoriteLocal`, `HomeFeedCache`

# Data Contracts

## Input Data

### Source: Operator-managed Influencer Catalog

- 필드: `id`, `name`, `slug`, `bio`, `profileImageUrl`, `categories[]`, `isFeatured`
- 갱신 주기: 수동 등록/수정 시 즉시 반영

### Source: Platform Channel Metadata

- 필드: `platform`, `handle`, `channelUrl`, `isOfficial`, `isPrimary`
- 갱신 주기: 수동 또는 주기 점검

### Source: Live Status Adapter

- 필드: `platform`, `isLive`, `title`, `startedAt`, `viewerCount`, `watchUrl`
- 갱신 주기: 3~5분 polling

### Source: Activity Adapter

- 필드: `platform`, `contentType`, `title`, `summary`, `thumbnailUrl`, `publishedAt`, `externalUrl`
- 갱신 주기: 10~30분 polling 또는 수동 반영

### Source: Schedule Input

- 필드: `title`, `scheduledAt`, `platform`, `note`
- 갱신 주기: 운영자 입력 시 즉시 반영

## Output Data

### `/v1/home`

- `liveNow[]`
- `latestUpdates[]`
- `todaySchedules[]`
- `featuredInfluencers[]`
- `generatedAt`

### `/v1/influencers`

- `items[]`
- 필드: `id`, `name`, `slug`, `profileImageUrl`, `categories[]`, `platforms[]`, `liveStatus`
- 쿼리: `q`, `category`, `platform`, `sort`

### `/v1/influencers/{slug}`

- `profile`
- `channels[]`
- `liveStatus`
- `recentActivities[]`
- `schedules[]`
- `relatedTags[]`

### Local App Output

- 홈 UI 상태
- 목록 UI 상태
- 상세 UI 상태
- 즐겨찾기 UI 상태
- 마지막 동기화 시각
- feature flag snapshot

# UI / Interaction Requirements

- Android 기본 레이아웃은 하단 탭 또는 단순 상단 네비게이션 기반으로 홈, 탐색, 라이브, 즐겨찾기를 제공한다.
- 홈에서는 `지금 라이브 중` 섹션이 최상단에 위치해야 한다.
- 목록 화면은 검색창 + 카테고리 필터 칩 + 플랫폼 필터 칩을 제공한다.
- 상세 화면은 `프로필 -> 공식 채널 -> 라이브 상태 -> 최근 소식 -> 예정 방송` 순서로 구성한다.
- 최근 활동 타임라인은 플랫폼 아이콘과 시간 정보를 우선 노출한다.
- 즐겨찾기 토글은 목록 카드와 상세 상단 양쪽에서 접근 가능해야 한다.
- 로딩 상태는 skeleton 또는 placeholder 카드로 표시한다.
- 오류 상태는 전체 화면 오류보다 섹션 단위 오류를 우선 사용한다.
- 외부 플랫폼 링크는 앱 외부 브라우저 또는 해당 앱 deep link로 연다.

# API / Integration Requirements

## API Strategy

- 앱은 `MyStarNow API` 한 곳만 호출한다.
- 서버는 각 플랫폼 어댑터를 통해 원천 데이터를 수집하고 내부 모델로 정규화한다.
- 플랫폼별 응답 차이는 서버에서 처리하고 앱에 노출하지 않는다.

## Endpoint Proposal

- `GET /v1/home`
- `GET /v1/influencers`
- `GET /v1/influencers/{slug}`
- `GET /v1/live`
- `GET /v1/categories`
- `GET /v1/platforms`
- `GET /v1/meta/app-config`

## Authentication

- MVP 사용자 인증 없음
- 서버-플랫폼 간 인증 또는 API 키는 서버 환경변수로 관리

## Rate Limit / Freshness

- 플랫폼별 polling 간격을 개별 설정
- 실패한 플랫폼은 exponential backoff 적용
- 홈 API는 서버 캐시를 통해 짧은 응답 시간을 유지

## Fallback

- 플랫폼 자동 수집이 불가능한 항목은 운영자 수동 데이터로 대체
- 라이브 상태 조회 실패 시 마지막 정상값 + 갱신 지연 표시
- 예정 방송은 전면 수동 등록을 허용

# State Management

## State Split

- 서버 상태: 홈 피드, 인플루언서 목록, 상세, 라이브 상태, 일정, 카테고리
- 클라이언트 상태: 검색어, 선택 필터, 정렬, 즐겨찾기, 마지막 탭, 외부 링크 열기 이벤트

## Presentation Model

- 각 화면은 `UiState` 단일 상태 객체를 가진다.
- `Loading`, `Content`, `Empty`, `Error`를 명시적인 sealed model로 관리한다.
- 화면 상태 보유는 `StateFlow`를 기본으로 한다.
- 일회성 UI 이벤트는 `SharedFlow` 또는 동등 개념의 event stream으로 분리한다.
- 즐겨찾기 변경은 optimistic update 후 로컬 저장 결과로 확정한다.

## ViewModel Rules

- shared presentation 계층에는 `android.*` import를 두지 않는다.
- Android 전용 navigation arg, saved state, permission 처리 연결부는 `androidMain` 또는 `androidApp` shell에 둔다.
- 이벤트 이름은 `행위-대상-결과` 의미가 드러나도록 유지한다.

## Refresh Rules

- 홈: 화면 진입 시 조회, pull-to-refresh 지원
- 상세: 진입 시 조회, 5분 이상 지난 캐시가 있으면 백그라운드 새로고침
- 목록: 검색/필터 변경 시 debounce 후 재조회 또는 로컬 필터

# Observability

- 앱 로그: 화면 진입, API 실패, 외부 링크 열기 실패, 캐시 읽기 실패
- 서버 로그: 플랫폼 어댑터 실패, 정규화 실패, 빈 응답, 수동 데이터 fallback 발생
- 메트릭: 홈 API latency, adapter success rate, live status freshness, favorite count
- 에러 추적: 앱 크래시, 서버 예외, 어댑터 파싱 실패
- analytics 이벤트 수집은 `AnalyticsGateway`를 통해서만 호출하고 SDK 직접 호출을 금지한다.
- 디버그 빌드에서는 개발자 모드에서 현재 endpoint, flag, 마지막 API 실패 원인을 확인할 수 있어야 한다.
- 치명적 오류는 가능하면 오류 복구 화면과 함께 기록하고, 즉시 앱 종료는 최후 fallback으로만 사용한다.

# Test Strategy

## Unit Tests

- 응답 모델 -> 도메인 모델 정규화 테스트
- 라이브 상태 병합 규칙 테스트
- 필터 및 검색 조건 적용 테스트
- 즐겨찾기 저장/복원 테스트

## Integration Tests

- 홈 API 응답을 UI state로 변환하는 흐름 테스트
- 인플루언서 목록 검색/필터 조합 테스트
- 상세 화면 데이터 로딩 및 섹션 표시 테스트
- 캐시 데이터 우선 렌더링 후 background refresh 테스트

## UI Tests

- 홈에서 라이브 카드 노출 확인
- 목록에서 카테고리 및 플랫폼 필터 동작 확인
- 상세에서 공식 채널 버튼 클릭 동작 확인
- 즐겨찾기 토글 상태 유지 확인

## Manual Verification

- 느린 네트워크 환경에서 홈/상세 표시 확인
- 일부 플랫폼 소스 실패 시 degraded UI 확인
- 외부 링크 deep link / browser fallback 확인
- 앱 재시작 후 즐겨찾기와 마지막 캐시 유지 확인

# Delivery Notes

## Implementation Order

1. 서버 메타데이터 모델과 홈/상세 응답 계약 정의
2. Compose Multiplatform 프로젝트 골격 및 공용 모델 구성
3. 홈 화면 + 인플루언서 목록 + 상세 화면 기본 렌더링
4. 즐겨찾기 로컬 저장
5. 카테고리/플랫폼 필터
6. 예정 방송 섹션
7. 최근 활동 타임라인
8. 운영용 수동 데이터 입력 경로

## Release Scope

- Release 1은 PRD의 `Must Have + Nice to Have` 전부 포함
- 대상 플랫폼은 Android
- 지원 소스는 YouTube, Instagram, X, CHZZK, SOOP
- 데이터 수집은 수동 등록 + 부분 자동 집계 혼합 방식

## Feature Flags

- 필요 시 플랫폼별 어댑터를 개별 비활성화 가능해야 함
- 일정 섹션과 타임라인 섹션은 서버 설정으로 숨길 수 있어야 함
- 디버그 빌드에서는 개발자 모드에서 feature flag override를 허용해야 함
- 원격 설정값과 로컬 override 우선순위를 명확히 유지해야 함

## Pre-release Checklist

- 최소 20명 이상의 인플루언서 데이터 등록
- 각 플랫폼별 샘플 데이터 정상 노출 확인
- 빈 상태/오류 상태/오프라인 캐시 확인
- 주요 Android 디바이스 화면 크기 검증

# Open Questions

- 서버 구현을 같은 Kotlin 생태계로 통일할지
- Instagram, X, CHZZK, SOOP 데이터 수집을 어떤 조합으로 자동화할지
- 초기 운영 도구를 관리자 화면으로 만들지, 시트 업로드로 시작할지
- 로컬 캐시 DB를 어떤 라이브러리로 확정할지
- 향후 알림 기능을 FCM 중심으로 확장할지

# Links

- Related PRD: `../product/PRD-mystarnow.md`
- Related ADRs: `./ADR-mystarnow-architecture.md`
- Related Issues:
- External References:
  - PRND Medium - 프로젝트 구조: https://medium.com/prnd/%ED%97%A4%EC%9D%B4%EB%94%9C%EB%9F%AC-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C%ED%8C%80%EC%9D%80-%EC%96%B4%EB%96%BB%EA%B2%8C-%EC%9D%BC%ED%95%98%EB%82%98%EC%9A%94-3-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EA%B5%AC%EC%A1%B0-1928c9ca5e6
  - PRND Android Style Guide - Architecture: https://github.com/PRNDcompany/android-style-guide/blob/main/Architecture.md
  - PRND 더팀스 - 개발자 모드: https://www.theteams.kr/teams/8261/post/73199
  - PRND 더팀스 - Analytics SDK 관리: https://www.theteams.kr/teams/8261/post/73200
  - PRND 더팀스 - 오류 처리 UX: https://www.theteams.kr/teams/8261/post/73202
