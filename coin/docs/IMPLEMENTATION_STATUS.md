# Coin Project Implementation Status

작성일: 2026-04-01
업데이트: 2026-04-02

## 목적

이 문서는 `coin` 프로젝트에서 현재까지 구현된 것과, 다음으로 진행할 작업을 한눈에 정리한다.

## 관련 문서

- [PRD - Coin Market Screen](./PRD-2026-04-01-coin-market-screen.md)
- [ADR - Coin Market Screen Architecture](./ADR-2026-04-01-coin-market-screen-arch.md)
- [TRD - Coin Market Screen](./TRD-2026-04-01-coin-market-screen.md)

## 현재 상태

- 현재 단계: `Phase 3 paper trading 구현 완료`
- 현재 PR: `#8`
  링크: `https://github.com/oyj7677/AI_Project/pull/8`
- PR 상태: `MERGED`
- 체크 상태: `coin/web test / build / lint, coin/proxy test 통과`

## 현재까지 구현된 것

### 문서

- PRD 작성 완료
- ADR 작성 완료
- TRD 작성 완료
- 1차 계획서 작성 완료
- 2차 계획서 작성 완료
- 3차 paper trading 계획서 작성 완료

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

### Phase 3 구현 완료 항목

- paper portfolio 상태 모델
- paper buy / sell engine
- paper order history
- paper reset
- 주문 티켓 / 포지션 / 주문 이력 UI
- paper trading TRD 작성 완료
- paper trading ADR 작성 완료
- file persistence 기반 상태 저장 완료
- 프론트가 `PaperTradingGateway` 계약만 사용하도록 분리 완료

## 현재 구현 위치

- 문서: `coin/docs`
- 웹 앱: `coin/web`
- 프로젝트 안내: `coin/README.md`

## 다음 작업

### 바로 다음

1. 전략 신호 패널 설계
2. paper trading과 시그널 연결
3. 포지션 관리 규칙 정의

### 단기 후보

1. 전략 시그널 패널
2. PnL/수수료 모델 세분화
3. paper portfolio persistence 개선
4. websocket market feed 적용 검토
5. 운영 배포 프록시 하드닝

### 이후 기능

1. 자동매매 실행 흐름
2. 로그 / 알림 / 관측성
3. 실제 주문 엔진 연결
4. 전략 백테스트

## 현재 리스크

- paper state는 파일 기반 저장이라 단일 인스턴스 환경에 적합하다.
- 현재 주문 모델은 시장가 즉시 체결 단순화 모델이라 실거래와 차이가 있다.
- 운영 배포용 프록시는 스캐폴드 수준이며 보안/캐시 설정 보강이 필요하다.

## 완료 판단 기준

- phase 3 PR `#8` 머지
- paper buy/sell/reset flow 검증
- frontend / proxy 계약 분리 유지
- 다음 기능 우선순위 확정
