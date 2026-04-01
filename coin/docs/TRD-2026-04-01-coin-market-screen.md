---
id: TRD-2026-04-01-COIN-MARKET-SCREEN
type: trd
status: draft
owner:
created: 2026-04-01
updated: 2026-04-01
related_prd: PRD-2026-04-01-COIN-MARKET-SCREEN
related_adrs:
  - ADR-2026-04-01-COIN-MARKET-SCREEN-ARCH
---

# Summary

`coin/web`에서 코인 종목 목록과 가격 그래프를 제공하는 화면의 기술 요구사항을 정의한다.

# Technical Goals

- KRW 마켓 종목 목록과 차트를 안정적으로 렌더링한다.
- Upbit 공개 시세 API 의존성을 UI 컴포넌트와 직접 결합하지 않는다.
- 검색, 종목 선택, 시간 범위 전환이 예측 가능하게 동작해야 한다.
- 이후 실시간 업데이트와 자동매매 기능으로 확장 가능한 구조를 유지한다.

# Functional Requirements

## FR-1 Market List Rendering

- 요구사항 설명: KRW 마켓 종목 목록을 화면에 보여야 한다.
- 입력: Upbit 마켓 목록과 ticker 요약 데이터
- 출력: 정렬 가능한 종목 목록
- 예외: 데이터가 없으면 empty state 표시

## FR-2 Market Search

- 요구사항 설명: 사용자가 종목명이나 심볼로 목록을 필터링할 수 있어야 한다.
- 입력: 검색어
- 출력: 필터링된 목록
- 예외: 검색 결과가 없으면 빈 상태 메시지 표시

## FR-3 Market Selection

- 요구사항 설명: 사용자가 종목을 선택하면 선택 상태와 차트가 바뀌어야 한다.
- 입력: 종목 클릭
- 출력: 선택 하이라이트, 차트 데이터 재조회
- 예외: 유효하지 않은 종목 코드는 무시

## FR-4 Chart Rendering

- 요구사항 설명: 선택 종목에 대한 가격 그래프가 렌더링되어야 한다.
- 입력: 차트 데이터 포인트
- 출력: SVG 기반 가격 라인 차트, 고점/저점, 마지막 가격
- 예외: 데이터 부족 시 placeholder 또는 오류 상태 표시

## FR-5 Time Range Switching

- 요구사항 설명: 사용자가 `1H`, `4H`, `1D`, `1W` 범위를 바꿀 수 있어야 한다.
- 입력: range 버튼
- 출력: 선택 범위 상태 변경, 새 캔들 데이터 렌더링
- 예외: 지원하지 않는 범위는 노출하지 않음

## FR-6 Market Sorting

- 요구사항 설명: 사용자가 종목 목록 정렬 기준을 바꿀 수 있어야 한다.
- 입력: 정렬 기준 선택
- 출력: 거래대금 / 등락률 / 이름 기준 정렬 목록
- 예외: 정렬 기준이 유효하지 않으면 기본값 `volume` 사용

## FR-7 Favorites Pinning

- 요구사항 설명: 사용자가 관심 종목을 목록 상단에 고정할 수 있어야 한다.
- 입력: 관심 종목 토글
- 출력: 관심 종목이 상단에 우선 표시
- 예외: 저장된 관심 종목 값이 손상되면 무시하고 기본 상태 사용

# Non-Functional Requirements

- 성능: 초기 데이터 로딩 3초 이내, 종목 전환 후 차트 갱신 1초 이내 목표
- 가용성: 외부 API 실패 시 사용자에게 오류 메시지와 재시도 경로 제공
- 보안: 브라우저 직접 외부 호출 대신 프록시 가능한 구조 우선
- 관측성: API 실패, 차트 실패, 빈 응답을 로그 가능하게 유지
- 확장성: WebSocket, 전략 패널, 주문 패널로 확장 가능해야 함
- 독립성: 프론트는 특정 백엔드 구현이나 거래소 응답 형식에 직접 종속되지 않아야 함

# Architecture Scope

- 클라이언트: `App.tsx`, `MarketList.tsx`, `PriceChart.tsx`, `MetricCard.tsx`
- 상태/데이터 훅: `useMarketDashboard.ts`
- 데이터 계약: `contracts/marketData.ts`
- 서비스 진입점: `services/marketDataGateway.ts`
- 외부 연동 어댑터: `lib/upbit.ts`
- 타입 정의: `types/market.ts`
- 서버/프록시: 개발 시 Vite 프록시, 운영 시 동일 역할의 얇은 프록시 필요

# Data Contracts

## Input Data

- 데이터 소스: Upbit 공개 시세 API
- 종목 목록 필드: `market`, `korean_name`, `english_name`
- ticker 필드: `trade_price`, `signed_change_rate`, `acc_trade_price_24h`, `high_price`, `low_price`
- 차트 필드: `timestamp`, `opening_price`, `high_price`, `low_price`, `trade_price`, `candle_acc_trade_volume`

## Output Data

- 리스트 필드: `market`, `koreanName`, `englishName`, `tradePrice`, `signedChangeRate`, `accTradePrice24h`
- 차트 포인트 필드: `timestamp`, `open`, `high`, `low`, `close`, `volume`
- 내부 상태: `selectedMarketCode`, `chartRange`, `markets`, `chartData`, `marketError`, `chartError`
- 클라이언트 부가 상태: `marketSort`, `favoriteCodes`

# UI / Interaction Requirements

- 데스크톱 기본 레이아웃은 종목 목록 + 차트 + 요약 카드 구성
- 모바일에서는 리스트와 차트가 좁은 폭에서도 읽기 쉬워야 함
- 기본 선택 종목은 KRW 거래량 상위 종목 또는 `KRW-BTC`
- 로딩 시 skeleton 또는 명확한 로딩 문구 표시
- 오류 시 차트 영역과 리스트 영역을 구분해서 실패 상태 표시

# API / Integration Requirements

- 마켓 목록 API: `/v1/market/all?isDetails=false`
- ticker API: `/v1/ticker?markets=...`
- 차트 API: range별 캔들 endpoint 사용
- 인증 필요 여부: 없음
- rate limit 대응: 종목 분할 요청, 프록시, 후속 캐시 전략
- fallback: API 실패 시 마지막 정상 데이터 유지 여부는 후속 결정

# State Management

- 서버 상태: 종목 목록, ticker, 차트 데이터
- 클라이언트 상태: 검색어, 선택 종목, 선택 기간
- 요청 경쟁 상태: chart refresh request id로 최신 응답만 반영

# Observability

- API 응답 실패 로그
- 종목 목록 로딩 실패 로그
- 차트 데이터 로딩 실패 로그
- 이후 필요 시 선택 종목 이벤트 추적

# Test Strategy

- 단위 테스트: `lib/upbit.ts` 정규화 함수
- 통합 테스트: 종목 선택 후 차트 갱신
- 수동 검증: 검색, 시간 범위 전환, 느린 네트워크, 오류 상태

# Delivery Notes

- 1차 구현: 목록 + 검색 + 단일 차트 + 범위 전환
- 2차 구현: 실시간 갱신은 `polling`으로 우선 도입하고, 정렬 개선, 관심 종목 고정, 차트 인터랙션 강화, 운영용 프록시 계약을 추가
- 3차 구현: WebSocket 또는 전략/주문 기능 연결

# Open Questions

- 운영 환경 프록시를 어디에 둘지
- 실시간 업데이트를 REST polling으로 계속 갈지 WebSocket으로 바꿀지
- 커스텀 SVG 차트를 유지할지 금융 차트 라이브러리로 교체할지
- 거래소를 업비트 단일로 유지할지

# Links

- Related PRD: `./PRD-2026-04-01-coin-market-screen.md`
- Related ADRs: `./ADR-2026-04-01-coin-market-screen-arch.md`
- Related Issues:
