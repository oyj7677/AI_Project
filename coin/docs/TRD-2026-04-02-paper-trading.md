---
id: TRD-2026-04-02-PAPER-TRADING
type: trd
status: draft
owner:
created: 2026-04-02
updated: 2026-04-02
related_prd:
related_adrs:
  - ADR-2026-04-02-PAPER-TRADING-ARCH
---

# Summary

`coin/web`와 `coin/proxy`를 사용해 paper trading 주문/포지션 기능을 제공하는 기술 요구사항을 정의한다.

# Technical Goals

- 실제 거래소 주문 없이도 주문/포지션 흐름을 검증할 수 있어야 한다.
- 프론트는 backend implementation 대신 app contract만 사용해야 한다.
- 상태는 프로세스 재시작 이후에도 유지될 수 있어야 한다.

# Functional Requirements

## FR-1 Portfolio Snapshot

- paper account 현금 잔고를 조회할 수 있어야 한다.
- 보유 포지션 목록을 조회할 수 있어야 한다.
- 실현 손익을 조회할 수 있어야 한다.

## FR-2 Buy Order

- 사용자는 선택 종목에 대해 KRW 금액 기준 시장가 매수 주문을 넣을 수 있어야 한다.
- 체결 시 포지션과 현금 잔고가 즉시 반영되어야 한다.

## FR-3 Sell Order

- 사용자는 보유 종목에 대해 수량 기준 시장가 매도 주문을 넣을 수 있어야 한다.
- 체결 시 포지션 수량과 실현 손익이 즉시 반영되어야 한다.

## FR-4 Order History

- 최근 paper order 이력을 볼 수 있어야 한다.
- 주문 구분, 체결가, 수량, 금액, 시간 정보가 포함되어야 한다.

## FR-5 Reset

- paper portfolio를 초기 상태로 리셋할 수 있어야 한다.

# Non-Functional Requirements

- 프론트는 `PaperTradingGateway` 계약만 의존한다.
- 프록시는 파일 기반 persistence로 시작한다.
- 주문 체결은 현재 시세 기준 즉시 체결로 단순화한다.

# API Contracts

- `GET /api/paper/portfolio`
- `GET /api/paper/orders`
- `POST /api/paper/orders`
- `POST /api/paper/reset`

# Test Strategy

- proxy 단위 테스트: 주문 엔진
- proxy smoke 테스트: health / portfolio / order endpoints
- frontend build/lint/test
