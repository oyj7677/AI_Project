# Coin Proxy

`coin/web`가 직접 거래소 응답 형식에 종속되지 않도록 해주는 얇은 API 프록시입니다.

## 목적

- 프론트는 앱 전용 API 계약만 사용
- Upbit 공개 시세 어댑터는 프록시 내부에 캡슐화
- 이후 다른 거래소나 백엔드 스택으로 바꿔도 프론트 변경 최소화

## 실행

```bash
cd coin/proxy
cp .env.example .env
npm run dev
```

## API

- `GET /health`
- `GET /api/markets?quote=KRW`
- `GET /api/markets/:market/chart?range=1H|4H|1D|1W`
- `GET /api/paper/portfolio`
- `GET /api/paper/orders`
- `POST /api/paper/orders`
- `POST /api/paper/reset`

## 환경 변수

- `PORT`
- `UPBIT_API_BASE`
- `ALLOWED_ORIGINS`
- `MARKETS_CACHE_TTL_MS`
- `CHART_CACHE_TTL_MS`
- `PAPER_STATE_FILE`
- `PAPER_INITIAL_CASH`
- `PAPER_FEE_RATE`

기본 상태 파일은 `./data/paper-trading.json`이며, 이 파일은 git에서 제외됩니다.
