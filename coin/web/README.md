# Crypto Dashboard Web

`coin` 프로젝트의 브라우저용 대시보드입니다.  
종목 목록, 가격 차트, paper trading 포트폴리오와 주문 패널을 한 화면에서 확인할 수 있도록 구성돼 있습니다.

## 무엇을 볼 수 있나

- KRW 마켓 종목 목록
- 선택 종목 요약 카드
- 시간 범위별 차트
- 즐겨찾기 / 정렬
- 자동 갱신 주기 선택
- paper portfolio 요약
- 매수 / 매도 주문 티켓
- 포지션 표
- 주문 이력

## 기술 스택

- React
- TypeScript
- Vite
- Vitest
- ESLint

## 개발 시 함께 필요한 것

이 프론트엔드는 단독으로는 충분하지 않고 `coin/proxy`가 같이 실행되어야 합니다.

- 기본 개발 호출 경로: `/api`
- Vite 프록시 대상: `http://localhost:8787`

즉, 개발 환경에서는 먼저 `coin/proxy`를 띄운 뒤 이 앱을 실행해야 합니다.

## 실행 방법

### 1. 프록시 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy
cp .env.example .env
npm install
npm run dev
```

### 2. 웹 실행

다른 터미널에서:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web
npm install
npm run dev
```

- 기본 주소: `http://127.0.0.1:5173`
- 기본 프록시 대상: `http://localhost:8787`
- 개발 포트는 `5173`으로 고정되어 있습니다.
- 포트가 이미 사용 중이면 Vite가 다른 포트로 이동하지 않고 에러를 내므로, 잘못된 앱을 여는 혼란을 줄일 수 있습니다.

## 환경 변수

운영 배포처럼 Vite 프록시 없이 직접 API base를 주입해야 할 때는 아래 값을 사용합니다.

```bash
VITE_MARKET_API_BASE=https://your-proxy.example.com/api
```

현재 저장소에는 `.env.example`로 같은 예시가 들어 있습니다.

## 자주 쓰는 명령

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web
npm run dev
npm run lint
npm run test
npm run build
npm run preview
```

참고:

- `npm run dev:stack`은 현재 `npm run dev`와 동일합니다.
- 프록시를 자동으로 같이 띄우는 스크립트는 아직 없습니다.

## 구조 메모

- `src/hooks/useMarketDashboard.ts`: 시세 대시보드 상태
- `src/hooks/usePaperTrading.ts`: paper trading 상태와 주문 처리
- `src/services/marketDataGateway.ts`: 시장 데이터 계약
- `src/services/paperTradingGateway.ts`: paper trading 계약
- `src/lib/marketApi.ts`: 실제 API 호출

## 검증

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web
npm run lint
npm run test
npm run build
```

## 주의

- 브라우저가 거래소 API를 직접 호출하지 않도록 설계돼 있으므로, 운영 환경에서도 `coin/proxy`와 같은 책임의 백엔드가 필요합니다.
- 프론트는 `MarketDataGateway`와 `PaperTradingGateway` 계약에 맞춰 작성되어 있어, 백엔드 구현을 바꿔도 UI 변경을 최소화할 수 있습니다.
