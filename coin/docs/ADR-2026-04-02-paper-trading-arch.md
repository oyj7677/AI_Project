---
id: ADR-2026-04-02-PAPER-TRADING-ARCH
type: adr
status: proposed
date: 2026-04-02
owners: []
related_prd:
supersedes:
superseded_by:
---

# Context

시장 대시보드 다음 단계로 paper trading 주문/포지션 검증이 필요하다.
실제 주문 기능 이전에 UI 흐름, 상태 변화, 포지션 계산을 안전하게 검증할 수 있어야 한다.

# Decision

- paper trading 상태는 `coin/proxy`에서 관리한다.
- 프론트는 `PaperTradingGateway` 계약만 사용한다.
- 상태 저장은 파일 기반 JSON으로 시작한다.
- 체결 가격은 주문 시점의 현재 market price로 즉시 체결한다.

# Alternatives Considered

## Option A: 프론트 localStorage만 사용

- 장점: 구현이 가장 빠르다.
- 단점: 여러 화면/세션/향후 전략 엔진과 연결하기 어렵다.

## Option B: proxy 파일 저장

- 장점: 프론트/백 분리가 유지된다.
- 장점: 추후 DB로 교체하기 쉽다.
- 단점: 초기에는 단일 인스턴스 전제다.

# Consequences

- 긍정적 영향: 향후 실제 주문 API와 별개로 UI/상태 흐름을 빠르게 반복 검증할 수 있다.
- 긍정적 영향: 프론트는 거래소 주문 방식과 무관한 상태를 유지한다.
- 부정적 영향: 파일 기반 저장은 다중 인스턴스 환경에 적합하지 않다.

# Rollout / Follow-up

- 1단계: paper order / portfolio / history / reset 구현
- 2단계: 손익/수수료 모델 보강
- 3단계: 실제 주문 엔진 또는 전략 엔진 연결

# Links

- Related TRD: `./TRD-2026-04-02-paper-trading.md`
