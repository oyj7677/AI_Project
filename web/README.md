# Crypto Dashboard Web

코인 자동매매 시스템 1차 구현용 웹 대시보드입니다.

## 현재 범위

- KRW 마켓 코인 목록 조회
- 선택 종목 가격 정보 조회
- 1시간 / 4시간 / 1일 / 1주 그래프 조회
- 종목 검색

## 실행 방법

```bash
cd web
npm install
npm run dev
```

기본 개발 서버는 Vite 프록시를 통해 Upbit 공개 시세 API를 `/api/upbit` 경로로 호출합니다.

운영 환경에서는 `.env` 또는 배포 환경 변수로 아래 값을 주입할 수 있습니다.

```bash
VITE_UPBIT_API_BASE=https://your-proxy.example.com/api/upbit
```

## 검증

```bash
npm run lint
npm run build
npm run test
```

## 주의

- Upbit 공개 시세는 브라우저 CORS 직결이 안정적이지 않으므로, 현재 구현은 Vite 개발 프록시에 의존합니다.
- 운영 배포 시에는 동일한 역할의 얇은 백엔드 프록시가 필요합니다.
