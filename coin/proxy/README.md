# Coin Proxy

`coin/web`가 거래소 응답 형식에 직접 의존하지 않도록 중간에서 앱 전용 계약을 제공하는 얇은 Node.js 프록시입니다.  
업비트 시세 조회와 paper trading 상태 저장을 한곳에서 담당합니다.

## 역할

- 업비트 공개 시세를 앱 전용 JSON 형태로 전달
- 차트/마켓 목록 응답 캐시
- paper trading 포트폴리오와 주문 이력 관리
- 로컬 파일 기반 상태 영속화
- CORS 제어

## 기술적 특징

- 외부 프레임워크 없이 Node.js 표준 라이브러리 기반으로 동작
- 개발 모드에서는 `node --watch`로 서버 재시작
- 별도 DB 없이 파일로 paper state 저장

## 실행 방법

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy
cp .env.example .env
npm install
npm run dev
```

참고:

- 현재 `package.json`에는 외부 의존성이 없어서 `npm install`은 사실상 필수는 아닙니다.
- 그래도 일반적인 실행 절차에 맞춰 문서에서는 포함했습니다.

기본 주소:

- 서버: `http://localhost:8787`
- 헬스 체크: `http://localhost:8787/health`

## API 목록

- `GET /health`
- `GET /api/markets?quote=KRW`
- `GET /api/markets/:market/chart?range=1H|4H|1D|1W`
- `GET /api/paper/portfolio`
- `GET /api/paper/orders`
- `POST /api/paper/orders`
- `POST /api/paper/reset`

## 주문 요청 예시

### 매수

```bash
curl -X POST http://localhost:8787/api/paper/orders \
  -H 'Content-Type: application/json' \
  -d '{"market":"KRW-BTC","side":"buy","amountKrw":1000000}'
```

### 매도

```bash
curl -X POST http://localhost:8787/api/paper/orders \
  -H 'Content-Type: application/json' \
  -d '{"market":"KRW-BTC","side":"sell","quantity":0.005}'
```

### 초기화

```bash
curl -X POST http://localhost:8787/api/paper/reset
```

## 환경 변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `PORT` | 서버 포트 | `8787` |
| `UPBIT_API_BASE` | 업비트 API 베이스 URL | `https://api.upbit.com` |
| `ALLOWED_ORIGINS` | CORS 허용 origin 목록 | `*` |
| `MARKETS_CACHE_TTL_MS` | 마켓 목록 캐시 시간 | `5000` |
| `CHART_CACHE_TTL_MS` | 차트 캐시 시간 | `3000` |
| `PAPER_STATE_FILE` | paper trading 상태 파일 경로 | `./data/paper-trading.json` |
| `PAPER_INITIAL_CASH` | 초기 현금 | `10000000` |
| `PAPER_FEE_RATE` | 수수료율 | `0.0005` |

## 상태 저장

- 기본 상태 파일은 `./data/paper-trading.json`
- 이 파일은 git에서 제외됩니다.
- 로컬 단일 인스턴스 개발 환경에 적합한 방식입니다.

## 테스트

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy
npm test
```

## 함께 보는 문서

- 상위 프로젝트 안내: `../README.md`
- 웹 대시보드 안내: `../web/README.md`
- 구현/설계 문서: `../docs/README.md`

## 주의

- paper trading은 단순화된 모의 주문 로직입니다.
- 멀티 인스턴스 운영, 영구 보존, 계정 분리 같은 요구사항이 생기면 파일 저장 대신 DB나 별도 상태 저장소로 옮겨야 합니다.
