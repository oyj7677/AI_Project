---
id: PRD-MYSTARNOW-BFF-SERVER
type: prd
status: draft
owner:
created: 2026-04-04
updated: 2026-04-04
related_adrs:
  - ADR-MYSTARNOW-BFF-ARCHITECTURE
related_decisions: []
---

# Summary

`MyStarNow BFF 서버`는 1차 기준으로 YouTube와 Instagram의 인플루언서 데이터를 수집·정규화·캐시해 Android 앱이 안정적으로 사용할 수 있는 단일 API를 제공하는 백엔드 제품이다. X, CHZZK, SOOP은 후속 확장 대상으로 남긴다.

# Problem

- 인플루언서 데이터가 여러 플랫폼에 흩어져 있어 앱이 각 플랫폼을 직접 붙이면 구현과 운영 복잡도가 급격히 커진다.
- 플랫폼별 응답 형식, 인증, rate limit, 장애 패턴이 달라 앱에서 직접 처리하기 어렵다.
- 공식 계정 여부, 일정, 링크 수정 같은 운영성 데이터는 자동 수집만으로 해결되지 않는다.
- 앱이 원하는 것은 플랫폼별 원본 데이터가 아니라 `인플루언서 중심`으로 정리된 화면용 데이터다.

# Goals

- 앱이 단일 `MyStarNow API`만 호출하도록 만든다.
- 인플루언서, 채널, 라이브 상태, 최근 활동, 예정 방송을 공통 도메인 모델로 정규화한다.
- 1차 지원 플랫폼은 `YouTube`, `Instagram`으로 제한한다.
- YouTube는 공식 API 기반 자동 수집을 우선한다.
- Instagram은 수동 등록 중심에 제한적 자동 수집을 더하는 방식으로 시작한다.
- 외부 플랫폼 장애가 있더라도 마지막 정상 데이터와 수동 운영으로 핵심 화면을 유지한다.
- 운영자가 공식 링크, 일정, 소개 정보를 빠르게 수정할 수 있는 최소한의 운영 경로를 제공한다.
- MVP 기준으로 프론트 `Version 1`에 필요한 데이터와 API를 안정적으로 공급한다.

# Non-Goals

- 1차 범위에서 외부 파트너에게 공개하는 범용 Open API를 만들지 않는다.
- 1차 범위에서 완전 자동 크롤링/완전 자동 공식 계정 판별을 목표로 하지 않는다.
- 1차 범위에서 `X`, `CHZZK`, `SOOP` 연동은 포함하지 않는다.
- 1차 범위에서 사용자 로그인, 개인화 피드, 알림 발송 백엔드까지 포함하지 않는다.
- 1차 범위에서 대규모 관리자 CMS를 만들지 않는다.
- 1차 범위에서 실시간 채팅, 후원, 자체 미디어 스트리밍은 지원하지 않는다.

# Users / Scenarios

## Primary Users

- `MyStarNow` Android 앱
- 인플루언서 메타데이터를 관리하는 운영자

## Core Scenarios

- 앱 홈 화면이 `지금 라이브 중`, `최신 업데이트`, `오늘 예정 방송`을 한 번에 조회한다.
- 앱 목록 화면이 검색어, 카테고리, 플랫폼 필터 기준으로 인플루언서를 조회한다.
- 앱 상세 화면이 특정 인플루언서의 공식 채널, 최근 활동, 일정 데이터를 조회한다.
- 운영자가 잘못된 링크나 일정 정보를 수동으로 수정한다.
- 특정 플랫폼 수집이 실패해도 서버는 마지막 정상 데이터 또는 수동 데이터를 반환한다.

# Scope

## Included

- 인플루언서 기본 메타데이터 저장
- 플랫폼별 공식 채널 정보 저장
- 라이브 상태 polling 및 정규화
- 최근 활동 수집 및 정규화
- 예정 방송 데이터 저장
- 홈/목록/상세용 BFF API
- 카테고리/플랫폼 메타 API
- 수동 운영 입력 경로
- 캐시 및 fallback 전략
- 기본 관측성 로그와 운영 지표
- YouTube Data API v3 기반 채널/영상 정보 자동 수집
- YouTube 라이브 상태 polling 보완
- Instagram 수동 등록 + 제한적 자동 수집

## Excluded

- 사용자 계정 시스템
- 개인 맞춤 추천
- 푸시 알림 발송
- 외부에 개방된 파트너 API
- 대시보드형 풀 관리자 콘솔
- X, CHZZK, SOOP 연동

# Server Responsibilities

## BFF API

- 프론트가 화면 단위로 바로 사용할 수 있는 응답 제공
- 플랫폼별 차이를 숨기고 인플루언서 중심 구조로 응답

## Data Aggregation

- YouTube는 공식 API를 사용해 채널과 영상 정보를 자동 수집한다.
- YouTube 라이브 상태는 polling 기반 보완 전략을 사용한다.
- Instagram은 수동 등록을 기본으로 하고, 가능한 범위에서 일부 자동 수집만 허용한다.
- 공통 필드로 정규화하고 실패 시 마지막 정상 상태를 유지한다.

## Operator-managed Data

- 소개, 카테고리, 공식 링크, 일정 같은 운영성 데이터 관리
- 자동 수집으로 해결되지 않는 품질 문제 보정

# Success Metrics

- 홈 API 응답 p95 500ms 이내
- 상세 API 응답 p95 700ms 이내
- 라이브 상태 최신성 5분 이내 유지율 95% 이상
- 운영자 링크 수정 후 10분 이내 앱 반영
- 플랫폼 일부 장애 시에도 홈 핵심 섹션 응답 성공률 99% 이상

# Acceptance Criteria

- [ ] 서버가 `/v1/home`, `/v1/influencers`, `/v1/influencers/{slug}` API를 제공한다.
- [ ] 앱이 외부 플랫폼을 직접 호출하지 않고도 홈/목록/상세 화면을 구성할 수 있다.
- [ ] 인플루언서 메타데이터와 공식 채널 링크를 수동으로 관리할 수 있다.
- [ ] 라이브 상태와 최근 활동은 플랫폼별 수집 결과를 공통 모델로 정규화한다.
- [ ] 플랫폼 수집 실패 시 마지막 정상 데이터 또는 수동 데이터로 fallback 한다.
- [ ] 카테고리 및 플랫폼 필터용 메타 정보를 API로 제공한다.
- [ ] 1차 자동 수집 플랫폼은 YouTube로 제한한다.
- [ ] Instagram 데이터는 수동 등록 중심으로 운영할 수 있다.

# Risks

- 플랫폼별 인증 정책과 rate limit이 수집 안정성을 좌우할 수 있다.
- Instagram 자동 수집 범위는 기술적 제약이 클 수 있다.
- YouTube API quota 설계가 잘못되면 수집 안정성이 흔들릴 수 있다.
- 공식 계정 판별이 불명확하면 데이터 신뢰도가 떨어질 수 있다.
- 수동 운영 도구가 너무 약하면 데이터 최신성 유지가 어려울 수 있다.

# Open Questions

- 운영 입력 경로를 관리자 웹으로 만들지, 시트 업로드로 시작할지
- Instagram 자동 수집 범위를 어디까지 허용할지
- 캐시 저장소를 DB 중심으로 갈지 Redis를 추가할지
- YouTube 라이브 상태 polling 주기를 얼마로 가져갈지

# Links

- Related ADRs: `../architecture/ADR-mystarnow-bff-architecture.md`
- Related Decisions: `../architecture/TRD-mystarnow-bff-server.md`
- Related Issues:
- Related Front Docs:
  - `../../front/product/PRD-mystarnow.md`
  - `../../front/architecture/TRD-mystarnow-android-cmp.md`
