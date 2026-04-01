# Coin Project Implementation Status

작성일: 2026-04-01
업데이트: 2026-04-01

## 목적

이 문서는 `coin` 프로젝트에서 현재까지 구현된 것과, 다음으로 진행할 작업을 한눈에 정리한다.

## 관련 문서

- [PRD - Coin Market Screen](./PRD-2026-04-01-coin-market-screen.md)
- [ADR - Coin Market Screen Architecture](./ADR-2026-04-01-coin-market-screen-arch.md)
- [TRD - Coin Market Screen](./TRD-2026-04-01-coin-market-screen.md)

## 현재 상태

- 현재 단계: `Phase 2 기능 구현 완료, PR 생성 준비`
- 현재 PR: `생성 예정`
- PR 상태: `구현/검증 완료`
- 체크 상태: `coin/web test / build / lint, coin/proxy test 통과`

## 현재까지 구현된 것

### 문서

- PRD 작성 완료
- ADR 작성 완료
- TRD 작성 완료
- 1차 계획서 작성 완료

### 프론트엔드 1차 구현

- React + TypeScript + Vite 기반 웹 앱 골격
- KRW 마켓 코인 목록 화면
- 종목 검색
- 선택 종목 요약 카드
- 시간 범위 전환이 가능한 가격 차트
- 로딩 / 오류 / 빈 상태 처리
- Upbit 시세 응답 정규화 계층

### 품질 보강

- 차트 요청 경쟁 상태 방지
- 차트 시간 표시를 `timestamp` 기준으로 보정
- Upbit 데이터 정규화 단위 테스트 추가
- 운영용 API base 주입 경로 문서화

### 2차 리팩터링

- `coin/web`를 정식 앱 루트로 확정
- 프론트가 `MarketDataGateway` 계약만 보도록 데이터 어댑터 분리
- 종목 정렬 기준 선택 추가
- 관심 종목 고정 기능 추가
- 접근성 보강

### Phase 2 구현 완료 항목

- 차트 hover 툴팁 및 crosshair
- 키보드 기반 차트 포인트 탐색
- polling 기반 실시간 업데이트 방식 결정 및 구현
- 자동 갱신 주기 선택
- 백그라운드 탭 polling 일시 중지
- 운영용 `coin/proxy` API 프록시 스캐폴드
- 프론트가 거래소 경로 대신 앱 전용 `/api` 계약만 사용하도록 전환

## 현재 구현 위치

- 문서: `coin/docs`
- 웹 앱: `coin/web`
- 프로젝트 안내: `coin/README.md`

## 현재 확인된 작업 트리 메모

- 로컬 작업 트리 기준으로 기존 `web/` 경로는 삭제 상태로 보이고, 현재 앱 구현은 `coin/web` 아래에 존재한다.
- 즉, 현재는 `coin` 폴더를 독립 프로젝트 루트처럼 정리하는 방향의 로컬 변경이 진행 중인 상태다.

## 다음 작업

### 바로 다음

1. Phase 2 PR 생성
2. Copilot 리뷰 확인 및 후속 리팩터링
3. PR 머지

### 2차 구현 후보

1. 종목 정렬 기준 토글
2. 차트 인터랙션 강화
3. 기본 관심 종목 고정
4. 실시간 업데이트 방식 결정
5. 운영용 API 프록시 설계

### 이후 기능

1. 포지션 / 주문 패널
2. 전략 신호 패널
3. 자동매매 실행 흐름
4. 로그 / 알림 / 관측성

## 현재 리스크

- `coin/web` 구조 확정용 re-PR이 아직 머지 전이다.
- Upbit 연동은 개발 프록시 기준이므로 운영 배포용 프록시 설계가 아직 필요하다.
- 차트는 1차 구현 수준이라 상호작용성과 금융 차트 표현은 이후 보강이 필요하다.

## 완료 판단 기준

- `coin/web` 기준 re-PR 머지
- 앱 경로 확정
- 로컬 실행 문서와 실제 디렉터리 구조 일치
- 2차 구현 우선순위 확정
