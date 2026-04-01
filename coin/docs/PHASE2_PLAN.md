# Coin Project Phase 2 Plan

작성일: 2026-04-01

## 목적

이 문서는 `coin/web` 2차 구현 범위와 구현 순서를 정리한다.

## 이번 범위

### 1. 차트 인터랙션 강화

- 차트 hover 위치에 따라 값과 시각을 보여주는 툴팁
- 교차선 또는 현재 포인트 하이라이트
- 키보드 접근이 가능한 차트 탐색 최소 지원

### 2. 실시간 업데이트 방식 결정

- 2차에서는 `polling`을 채택한다.
- 이유:
  - 운영 복잡도가 WebSocket보다 낮다.
  - 초기 시장 모니터링 용도에는 충분하다.
  - 프론트와 백엔드 계약을 단순하게 유지할 수 있다.
- 구현 범위:
  - 자동 갱신 on/off
  - 갱신 주기 선택
  - 백그라운드 탭에서는 polling 완화 또는 일시 중지

### 3. 운영용 API 프록시 설계 및 스캐폴드

- 프론트는 Upbit 직접 호출이 아니라 앱 전용 API 계약을 사용한다.
- `coin/proxy`에 독립 실행 가능한 얇은 프록시를 둔다.
- 프록시 계약 예시:
  - `GET /health`
  - `GET /api/markets?quote=KRW`
  - `GET /api/markets/:market/chart?range=1D`
- Upbit 적응 계층은 프록시 내부에만 존재한다.

## 설계 원칙

- 프론트는 특정 백엔드 기술스택에 직접 종속되지 않는다.
- 프론트는 `MarketDataGateway` 계약과 앱 전용 DTO만 사용한다.
- 거래소 어댑터는 프록시 쪽으로 캡슐화한다.
- 차트 인터랙션은 현재 SVG 기반 구조를 유지하며 과도한 라이브러리는 도입하지 않는다.

## 구현 단계

### Step 1. 문서/계약 정리

- Phase 2 계획 문서 추가
- TRD / ADR에 polling 및 proxy 계약 반영

### Step 2. Proxy

- `coin/proxy` 스캐폴드
- Upbit 어댑터
- 앱 전용 응답 DTO
- 최소 CORS / health endpoint

### Step 3. Frontend Data Layer

- `coin/web`가 프록시 계약만 사용하도록 전환
- dev proxy를 Upbit 직결이 아니라 `coin/proxy` 기준으로 변경

### Step 4. Chart UX

- hover tooltip
- crosshair
- 키보드 이동

### Step 5. Live Updates

- polling interval 상태
- auto refresh controls
- visibility 기반 pause

### Step 6. Verification

- 프론트 test / build / lint
- 프록시 smoke run
- PR 생성 후 리뷰 반영

## 제외 범위

- 주문 실행
- 실거래 인증
- WebSocket 실시간 스트리밍
- 고급 보조지표
- 포트폴리오 / 포지션 패널

## 완료 기준

- `coin/web`가 Upbit 경로를 직접 참조하지 않는다.
- `coin/proxy`에서 앱 전용 API가 동작한다.
- 차트 hover/tooltip이 동작한다.
- polling on/off 및 주기 선택이 동작한다.
- test / build / lint 통과
