---
id: ROADMAP-MYSTARNOW-IDOL-YOUTUBE-DELIVERY
type: roadmap
status: draft
owner:
created: 2026-04-05
updated: 2026-04-05
related_prd: PRD-MYSTARNOW-IDOL-YOUTUBE-HUB
related_adrs:
  - ADR-MYSTARNOW-IDOL-YOUTUBE-BFF-ARCHITECTURE
---

# Summary

이 문서는 `MyStarNow`를 아이돌 그룹 중심 YouTube 허브로 전환하기 위한 실제 개발 계획서다. 현재 구현은 인플루언서 중심 구조이므로, 이번 작업은 기존 기능을 조금씩 수정하는 수준이 아니라 `백엔드 도메인 재설계 + 프론트 화면 재구성`을 전제로 한다. 계획의 핵심은 **백엔드 기준 모델과 API 계약을 먼저 확정하고**, 그 계약을 기준으로 Web과 Android를 병렬로 구현하는 것이다.

# Goal

- 그룹 중심 제품 구조로 백엔드와 프론트엔드를 다시 정렬한다.
- `그룹-멤버-채널-영상` 모델을 기준으로 서버를 새로 설계한다.
- Web을 가장 빠른 검증면으로 사용하고, Android는 그 계약을 재사용한다.
- 1차 제품은 `YouTube only` 범위에서 동작 가능해야 한다.
- 운영자가 그룹, 멤버, 채널, 영상을 입력할 수 있어야 한다.

# Planning Assumptions

- 1차 지원 플랫폼은 `YouTube` 하나다.
- 그룹/멤버/채널 관계는 운영 입력이 source of truth다.
- YouTube 채널 메타데이터와 최신 영상 목록은 공식 API 기반 자동 수집을 사용한다.
- Web은 제품 검증용 우선 클라이언트다.
- Android는 Web에서 검증된 API 계약을 재사용한다.
- 기존 구현은 참고 자산으로만 활용하고, 새 방향의 기준 구현은 새 도메인 기준으로 다시 만든다.

# Delivery Principles

- 백엔드 모델과 API 계약이 먼저 고정되어야 한다.
- 프론트는 백엔드 계약이 흔들리지 않는 범위에서 병렬 진행한다.
- 운영자 입력 경로는 MVP의 일부다.
- 자동 수집보다 관계 데이터 정확도를 우선한다.
- 작은 수직 slice를 끝까지 완성하면서 진행한다.

# Workstreams

## Stream A. Product / Contract

- 그룹 중심 화면 흐름 확정
- API 계약 확정
- 운영 입력 플로우 확정

## Stream B. Backend Core

- 새 도메인 모델
- DB 스키마
- 읽기 모델
- YouTube 어댑터
- 운영 입력 API

## Stream C. Web Frontend

- 그룹 목록
- 그룹 상세
- 멤버 상세
- 관리자 입력 화면

## Stream D. Android Frontend

- Web 검증 후 Android 화면 재구성
- 공통 DTO/상태 구조 재설계

## Stream E. QA / Reliability

- sync fallback
- partial failure
- stale 처리
- 통합 테스트

# Phase Plan

## Phase 0. Reset and Freeze

목표:

- 새 제품 방향을 기준선으로 고정한다.
- 기존 인플루언서 중심 구현을 새 기준 구현과 분리한다.

주요 작업:

- 새 PRD/TRD/ADR를 기준 문서로 채택
- 개발 기준 도메인 용어 통일
  - `Influencer` 대신 `Group`, `Member`, `Channel`, `Video`
- 기존 구현 중 재사용 가능한 것과 폐기할 것을 분류
- 새 API / DB / UI 기준 문서 작성 범위 확정

산출물:

- 기준 문서 세트
- 용어 정리
- 마이그레이션 범위 메모

종료 기준:

- 팀이 새 방향을 문서 기준으로 합의
- "무엇을 새로 만들고 무엇을 참고만 할지"가 명확함

## Phase 1. Contracts First

목표:

- 백엔드와 프론트엔드가 동시에 일할 수 있을 정도로 계약을 먼저 고정한다.

주요 작업:

- 새 방향 기준 OpenAPI 초안 작성
- 새 방향 기준 DB 스키마 초안 작성
- 운영자 입력 API 명세 작성
- YouTube adapter spec 작성
- 에러/partial/stale 응답 규칙 정리

산출물:

- `OPENAPI-mystarnow-idol-youtube-bff-v1.md`
- `DB-SCHEMA-mystarnow-idol-youtube-bff-server.md`
- `ADAPTER-SPEC-mystarnow-idol-youtube-platforms.md`

종료 기준:

- 프론트가 mock 없이도 어떤 JSON이 오는지 이해 가능
- 백엔드가 어떤 테이블과 endpoint를 구현해야 하는지 확정됨

의존성:

- 현재 작성된 PRD/TRD/ADR

## Phase 2. Backend Skeleton Rebuild

목표:

- 새 도메인 모델과 최소 실행 가능한 서버 구조를 만든다.

주요 작업:

- 모듈형 단일 BFF 프로젝트 구조 정리
- 새 엔티티/리포지토리 추가
  - `idol_groups`
  - `idol_members`
  - `youtube_channels`
  - `youtube_videos`
  - sync metadata / serving state
- Flyway migration 작성
- 기본 sample seed 추가
- 새 읽기 모델 projector 작성
- Actuator / Security / OpenAPI 설정

산출물:

- runnable backend skeleton
- local Docker Compose
- migration + sample data

종료 기준:

- 서버가 기동된다
- DB migration이 적용된다
- sample group/member/channel/video 데이터가 생성된다

의존성:

- Phase 1 DB schema / contract

## Phase 3. Operator-first Backend Slice

목표:

- 운영자가 새 도메인 데이터를 넣을 수 있어야 한다.

주요 작업:

- `POST /internal/operator/groups`
- `PUT /internal/operator/groups/{groupSlug}`
- `POST /internal/operator/members`
- `PUT /internal/operator/members/{memberSlug}`
- `POST /internal/operator/channels`
- `PUT /internal/operator/channels/{channelId}`
- `POST /internal/operator/videos`
- `PUT /internal/operator/videos/{videoId}`
- 읽기 모델 즉시 반영

산출물:

- operator write APIs
- operator integration tests

종료 기준:

- 운영자가 수동으로 그룹 1개, 멤버 2명, 채널 3개, 영상 5개를 등록 가능
- 홈/그룹 상세/멤버 상세 읽기 모델에 반영됨

의존성:

- Phase 2 backend skeleton

## Phase 4. YouTube Sync Slice

목표:

- 수동 등록한 채널에 대해 채널 메타데이터와 최신 영상 자동 수집을 붙인다.

주요 작업:

- YouTube channel metadata fetch
- latest video harvesting
- raw / normalized / serving 분리
- retry / backoff / stale 처리
- sync metrics / health 반영

산출물:

- YouTube adapter
- sync scheduler
- sync metadata tables / metrics

종료 기준:

- 수동 등록한 YouTube 채널의 최신 영상이 자동 반영됨
- 실패 시 stale / partial 응답이 보장됨

의존성:

- Phase 2 backend skeleton
- Phase 3 operator channel registration

## Phase 5. Public Read API Slice

목표:

- 제품 화면을 실제로 그릴 수 있는 그룹 중심 읽기 API를 완성한다.

주요 작업:

- `GET /v1/home`
- `GET /v1/groups`
- `GET /v1/groups/{groupSlug}`
- `GET /v1/members/{memberSlug}`
- `GET /v1/meta/app-config`
- 커서 기반 페이지네이션
- partial failure / stale envelope 적용

산출물:

- 공개 읽기 API
- API tests

종료 기준:

- Web이 실제 API로 홈/목록/상세를 렌더링 가능
- operator 입력 + YouTube sync 결과가 API에 반영됨

의존성:

- Phase 3
- Phase 4

## Phase 6. Web Frontend Rebuild

목표:

- 브라우저에서 제품을 검증할 수 있는 새 그룹 중심 UI를 만든다.

주요 작업:

- 정보 구조 재설계
  - 홈: 그룹 통합 피드
  - 그룹 목록
  - 그룹 상세
  - 멤버 상세
  - 관리자 입력
- API client / DTO 재작성
- section-level partial failure UI 반영
- 그룹/멤버/채널 타입 배지 설계
- 더보기 UX 구현

산출물:

- 새 `web/` 기준 UI
- 관리자 입력 화면

종료 기준:

- 브라우저에서 그룹 중심 시나리오를 끝까지 검증 가능
- 운영 입력 후 화면 반영 확인 가능

의존성:

- Phase 1 contract
- Phase 5 public API

## Phase 7. Android Frontend Rebuild

목표:

- Web 검증을 통과한 계약을 Android에 옮긴다.

주요 작업:

- shared DTO / state 재설계
- 그룹 목록/상세/멤버 상세 화면 구현
- 홈 피드 구현
- 즐겨찾기 재설계
- mock/live 모드 재정리

산출물:

- Android 그룹 중심 MVP

종료 기준:

- Android에서 그룹 탐색 시나리오가 동작
- Web과 같은 API 계약 사용

의존성:

- Phase 5 public API
- Phase 6 Web 검증

## Phase 8. Hardening and Launch Readiness

목표:

- 안정성과 운영성을 출시 수준으로 끌어올린다.

주요 작업:

- Testcontainers 기반 통합 테스트 강화
- sync failure / degraded mode 검증
- operator audit logging
- runbook / env-vars / deployment notes 정리
- seed / sample group onboarding script 정리

산출물:

- launch checklist
- runbook
- 운영 문서

종료 기준:

- 로컬/스테이징에서 전체 시나리오 재현 가능
- known blocker 없음

# Parallelization Strategy

## 먼저 순차적으로 해야 하는 것

1. 새 OpenAPI 초안
2. 새 DB schema 초안
3. operator API 범위 확정

이 3개는 백엔드와 프론트엔드 모두의 기준이므로 먼저 고정해야 한다.

## 그다음 병렬로 가능한 것

- 백엔드: DB + operator API
- 프론트엔드: Web 화면 구조와 mock DTO

## Web과 Android의 우선순위

- Web 먼저
- Android는 Web 검증 이후

이유:

- 브라우저가 검증 비용이 가장 낮다.
- 제품 방향이 바뀌었으므로 화면 구조를 빠르게 실험해야 한다.

# Suggested Milestones

## Milestone 1. 문서 기준선 확정

- PRD
- 서버 PRD
- TRD
- ADR
- OpenAPI
- DB schema

## Milestone 2. 운영자 입력 가능

- 그룹/멤버/채널/영상 수동 등록 가능
- 읽기 모델 반영 확인

## Milestone 3. 자동 수집 연결

- YouTube 채널 메타
- YouTube 최신 영상
- stale / degraded 반영

## Milestone 4. Web MVP

- 홈
- 그룹 목록
- 그룹 상세
- 멤버 상세
- 관리자 입력

## Milestone 5. Android MVP

- Web과 동일 계약 재사용
- Android 시나리오 완료

# Immediate Next Tasks

1. 새 방향 기준 OpenAPI 문서 작성
2. 새 방향 기준 DB schema 문서 작성
3. 그룹/멤버/채널/영상 operator API 명세 작성
4. YouTube adapter spec 작성
5. Web IA 와이어프레임 초안 작성

# Definition of Done

- 백엔드가 새 그룹 중심 도메인으로 동작한다.
- 운영자가 그룹, 멤버, 채널, 영상을 등록 가능하다.
- YouTube 자동 수집이 그룹 중심 읽기 모델에 반영된다.
- Web에서 그룹 중심 시나리오가 검증된다.
- Android에서 동일 계약으로 동작한다.
- 문서, 테스트, 운영 경로가 모두 정리된다.

# Risks

- 기존 인플루언서 중심 구현과 새 그룹 중심 구현이 혼재되면 복잡도가 높아질 수 있다.
- 관계 데이터 입력 가이드를 먼저 안 만들면 운영 오류가 많아질 수 있다.
- YouTube 자동 수집보다 관계 모델링이 늦어지면 제품 구조가 흔들린다.
- Web 검증을 건너뛰고 Android부터 가면 반복 비용이 커진다.

# Links

- Related Front PRD:
  - `./front/product/PRD-mystarnow-idol-youtube-hub.md`
- Related Back PRD:
  - `./back/product/PRD-mystarnow-idol-youtube-bff-server.md`
- Related TRD:
  - `./back/architecture/TRD-mystarnow-idol-youtube-bff-server.md`
- Related ADR:
  - `./back/architecture/ADR-mystarnow-idol-youtube-bff-architecture.md`
