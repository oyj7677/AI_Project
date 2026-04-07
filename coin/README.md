# Coin Project

업비트 시세를 기반으로 코인 시장을 살펴보고, paper trading 흐름까지 실험하는 독립 프로젝트입니다.  
현재는 실제 자동매매 엔진보다 "시장 대시보드 + 앱 전용 프록시 + 모의 매매 상태 관리"에 초점을 맞추고 있습니다.

## 현재 포함 범위

- KRW 마켓 코인 목록 조회
- 선택 종목 가격 요약
- `1H / 4H / 1D / 1W` 차트 조회
- 즐겨찾기와 정렬
- polling 기반 실시간 갱신
- paper portfolio 조회
- paper buy / sell / reset
- 주문 이력 및 포지션 확인

## 프로젝트 구성

```text
coin/
├─ README.md
├─ docs/      # PRD, ADR, TRD, 구현 상태 문서
├─ proxy/     # 업비트 + paper trading API 프록시
└─ web/       # React/Vite 기반 대시보드
```

## 어떤 식으로 실행하나

개발 환경에서는 `proxy`와 `web`을 함께 띄워야 합니다.

- `coin/proxy`가 업비트 시세와 paper trading 상태를 `/api` 계약으로 제공합니다.
- `coin/web`은 브라우저에서 직접 거래소를 때리지 않고, Vite 프록시를 통해 `proxy`를 호출합니다.

## 사전 준비

- Node.js
- npm

## 빠른 실행

### 1. API 프록시 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy
cp .env.example .env
npm install
npm run dev
```

- 기본 주소: `http://localhost:8787`
- 헬스 체크: `http://localhost:8787/health`

### 2. 웹 대시보드 실행

다른 터미널에서:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web
npm install
npm run dev
```

- 기본 주소: `http://127.0.0.1:5173`
- 이 앱은 개발 포트를 `5173`으로 고정합니다.
- `5173`이 이미 사용 중이면 자동으로 다른 포트로 바뀌지 않고 실행이 실패하므로, 먼저 점유 중인 프로세스를 정리해야 합니다.

## 추천 실행 순서

1. `coin/proxy`를 먼저 실행합니다.
2. `coin/web`을 실행합니다.
3. 브라우저에서 종목을 선택해 차트와 paper trading 패널이 정상 응답하는지 확인합니다.

## 주요 디렉터리 설명

- `docs/`: 요구사항, 구조 결정, 구현 현황
- `proxy/`: Node.js 표준 라이브러리 기반의 얇은 API 서버
- `web/`: React + TypeScript + Vite 대시보드

## 자주 쓰는 검증 명령

### Proxy

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy
npm test
```

### Web

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web
npm run lint
npm run test
npm run build
```

## 문서 위치

- 전체 문서 인덱스: `docs/README.md`
- 구현 현황: `docs/IMPLEMENTATION_STATUS.md`
- paper trading 설계: `docs/TRD-2026-04-02-paper-trading.md`

## 주의할 점

- 개발 중 `coin/web`은 `http://localhost:8787`의 프록시가 떠 있어야 정상 동작합니다.
- paper trading 상태는 파일로 저장되므로 단일 로컬 인스턴스에 적합합니다.
- 현재 주문 모델은 단순화된 모의 체결 모델이라 실제 거래소 체결 로직과는 다릅니다.
