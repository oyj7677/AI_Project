---
id: ADR-2026-04-01-COIN-MARKET-SCREEN-ARCH
type: adr
status: proposed
date: 2026-04-01
owners: []
related_prd: PRD-2026-04-01-COIN-MARKET-SCREEN
supersedes:
superseded_by:
---

# Context

`coin` 프로젝트는 이미 `coin/web` 아래에 React + Vite + TypeScript 기반 웹 앱 구조를 가지고 있다.
현재 구현 범위는 KRW 마켓 종목 목록, 검색, 선택 종목 그래프, Upbit 공개 시세 연동이다.

이 문서에서는 아래를 프로젝트 기본 방향으로 정리한다.

- 프론트엔드 빌드/런타임 구조
- 시세 데이터 연동 방식
- 차트 구현 방식
- 이후 자동매매 기능으로의 확장 방향

# Decision

- 프론트엔드 구조는 `Vite + React + TypeScript`를 유지한다.
- 시세 데이터는 Upbit 공개 API를 기준으로 한다.
- 개발 환경에서는 Vite 프록시 또는 동일 역할의 API 프록시를 사용한다.
- 차트는 외부 대형 라이브러리 대신 현재의 커스텀 SVG 라인 차트 구현을 유지하며, 필요 시 후속 단계에서 금융 차트 라이브러리로 교체한다.
- 프론트는 `MarketDataGateway` 계약에만 의존하고, Upbit 공개 시세 연동은 어댑터 구현으로 분리한다.
- UI 컴포넌트는 표시 책임에 가깝게 유지하고, 상태/데이터 훅이 게이트웨이 계층을 호출한다.

# Alternatives Considered

## Option A: 현재 Vite + React 구조 유지

- 장점: 이미 구현된 구조를 그대로 활용할 수 있다.
- 장점: 초기 속도가 가장 빠르다.
- 단점: 이후 서버 기능이 늘어나면 별도 API 계층이 필요할 수 있다.

## Option B: Next.js로 재구성

- 장점: SSR, API route, 확장성이 좋다.
- 장점: 장기적으로 단일 앱 운영이 쉬울 수 있다.
- 단점: 현재 구현을 다시 옮겨야 하므로 초기 속도가 떨어진다.

## Option C: 외부 차트 라이브러리 즉시 도입

- 장점: 금융 차트 표현력이 높다.
- 단점: 현재 MVP 범위에서는 과한 복잡도일 수 있다.

# Consequences

- 긍정적 영향: 지금 있는 `coin/web` 코드를 기반으로 빠르게 반복 개발할 수 있다.
- 긍정적 영향: 데이터 연동 구현을 바꾸더라도 프론트 UI를 크게 흔들지 않고 교체할 수 있다.
- 부정적 영향: 실시간 캔들, 보조지표, 주문 기능이 들어가면 구조 재정리가 필요할 수 있다.
- 부정적 영향: 현재 커스텀 SVG 차트는 고급 트레이딩 화면 요구를 모두 만족하지 못할 수 있다.

# Rollout / Follow-up

- 1단계: 현재 종목 목록 + 그래프 화면 안정화
- 2단계: 실시간 업데이트, 시간 범위 고도화, 차트 품질 개선
- 3단계: 전략/시그널/주문 기능과 연결
- 검증 방법: `npm run lint`, `npm run test`, `npm run build`, 수동 UI 검증

# Links

- Related PRD: `./PRD-2026-04-01-coin-market-screen.md`
- Related Decisions:
- Related PRs:
