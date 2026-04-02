# Coin Project Phase 3 Paper Trading Plan

작성일: 2026-04-02

## 목적

이 문서는 `coin/web`에 paper trading 기반 주문/포지션 기능을 추가하기 위한 구현 계획을 정의한다.

## 범위

### 1. Paper Trading Account

- 초기 가상 현금 잔고
- 실현 손익
- 총 평가금액
- 포지션 목록

### 2. Order Entry

- 선택 종목 기준 시장가 매수
- 선택 종목 기준 시장가 매도
- 매수 시 KRW 금액 입력
- 매도 시 보유 수량 입력
- 주문 체결 즉시 paper portfolio 반영

### 3. Position / Order History

- 보유 포지션 목록
- 평균 단가
- 현재가 기준 평가손익
- 주문 이력 목록

### 4. Independent API Contract

- 프론트는 paper trading 앱 전용 API만 사용
- 프록시 내부에서만 상태 저장과 거래소 시세 의존성을 처리

## 제외 범위

- 실거래 주문
- 사용자 인증
- 멀티 계정
- 주문 취소/정정
- 지정가/조건부 주문
- 포트폴리오 장기 저장소 DB

## API 계약 초안

- `GET /api/paper/portfolio`
- `GET /api/paper/orders`
- `POST /api/paper/orders`
- `POST /api/paper/reset`

## 구현 원칙

- 프론트는 `PaperTradingGateway` 계약만 본다.
- 프록시는 얇은 상태 엔진 + API 계층으로 유지한다.
- 상태 저장은 파일 기반으로 시작하되 이후 DB로 교체 가능해야 한다.
- 체결 가격은 현재 market price를 사용한 즉시 체결로 단순화한다.

## 구현 단계

### Step 1. Docs / Contracts

- plan 문서 추가
- TRD / ADR / status 문서 갱신
- frontend / proxy 계약 타입 추가

### Step 2. Proxy Paper Engine

- paper account state 모델
- file persistence
- buy / sell engine
- portfolio / orders / reset endpoints
- 단위 테스트

### Step 3. Frontend Data Layer

- paper trading gateway
- account / positions / orders 훅
- selected market와 연결되는 order draft 계산

### Step 4. UI

- account summary 카드
- order ticket
- positions table
- order history list

### Step 5. Verification

- web test / build / lint
- proxy test
- proxy smoke call

## 완료 기준

- paper trading account가 생성되고 유지된다.
- 사용자가 매수/매도 주문을 넣을 수 있다.
- 포지션과 주문 이력이 화면에 반영된다.
- 프론트는 backend implementation 대신 app contract만 사용한다.
