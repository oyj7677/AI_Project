# AI_Project

여러 개의 독립 프로젝트를 한 워크스페이스에서 함께 관리하는 저장소입니다.  
각 디렉터리는 목적과 실행 방식이 다르며, 루트에서 한 번에 구동하는 모노레포가 아니라 프로젝트별로 직접 실행하는 구조입니다.

## 들어 있는 프로젝트

| 경로 | 무엇인지 | 주 실행 방식 |
| --- | --- | --- |
| `AI_TEAM/` | AI 팀 운영 문서, 템플릿, GitHub/MCP 설정 자산 | 서버 실행 없음, 문서/설정 중심 |
| `coin/` | 코인 시세 대시보드와 paper trading 실험용 프로젝트 | `coin/proxy` + `coin/web`를 함께 실행 |
| `habit/` | 습관 추적 대시보드 프로젝트 | `habit/backend` + `habit` 프론트를 함께 실행 |
| `youtube_favorit/` | MyStarNow 아이돌 YouTube 허브 프로젝트 | `backend` + `web` 중심, `frontend`는 Android/KMP 클라이언트 |

## 공통 전제

- 각 프로젝트는 독립적으로 의존성을 관리합니다.
- JavaScript 프로젝트는 `npm` 기준으로 문서화했습니다.
- JVM 프로젝트는 프로젝트별 요구 버전이 다릅니다.
- `habit/backend`는 Java 17+, `youtube_favorit/backend`와 `youtube_favorit/frontend`는 Java 21 기준입니다.
- 루트 `.github/`에는 저장소 공통 PR 정책과 자동화 워크플로가 들어 있습니다.

## 빠른 시작

### 1. 코인 대시보드 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy
cp .env.example .env
npm install
npm run dev
```

다른 터미널:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web
npm install
npm run dev
```

- 프론트 주소: `http://127.0.0.1:5173`
- 프록시 주소: `http://localhost:8787`

### 2. MyStarNow 웹 검증 환경 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
docker-compose up -d
export YOUTUBE_API_KEY="YOUR_KEY"
./gradlew bootRun --args='--spring.profiles.active=local'
```

다른 터미널:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web
npm install
npm run dev
```

- 백엔드 주소: `http://localhost:8080`
- 웹 주소: `http://127.0.0.1:5174`
- 관리자 화면: `http://127.0.0.1:5174/admin`

### 3. Habit 프로젝트 실행

먼저 백엔드:

```powershell
cd C:\Users\mediazen\Desktop\oyjProject\AI_Project\habit\backend
.\gradlew.bat bootRun
```

다른 터미널에서 프론트:

```powershell
cd C:\Users\mediazen\Desktop\oyjProject\AI_Project\habit
npm install
npm run dev
```

- 백엔드 주소: `http://localhost:8080`
- 헬스 체크: `http://localhost:8080/api/health`
- 프론트 주소: `http://127.0.0.1:5173`
- 참고: 기존 브라우저 `localStorage` 데이터가 있고 서버가 비어 있으면 첫 동기화 때 자동 이전됩니다.

### 4. AI_TEAM 문서 허브 사용

서버를 띄우는 프로젝트는 아닙니다. 아래 순서로 시작하면 됩니다.

1. `AI_TEAM/00_HOME/AI_TEAM_HOME.md`를 먼저 읽습니다.
2. GitHub/MCP 연동이 필요하면 `AI_TEAM/.env.example`을 참고해 `.env`를 준비합니다.
3. 템플릿이 필요하면 `AI_TEAM/40_TEMPLATES/`를 사용합니다.

## 추천 읽기 순서

- 전체 인덱스: 이 문서
- AI 운영 문서: `AI_TEAM/README.md`
- 코인 프로젝트: `coin/README.md`
- Habit 프로젝트: `habit/README.md`
- MyStarNow 프로젝트: `youtube_favorit/README.md`

## 검증 명령 예시

프로젝트별 README에 상세 명령을 정리해 두었습니다. 자주 쓰는 기본 명령은 아래와 같습니다.

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/coin/proxy && npm test
cd /Users/oyj/Desktop/workspace/AI_Project/coin/web && npm run lint && npm run test && npm run build
cd /Users/oyj/Desktop/workspace/AI_Project/habit && npm run test && npm run build
cd /Users/oyj/Desktop/workspace/AI_Project/habit/backend && ./gradlew test
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web && npm run lint && npm run build
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend && ./gradlew test
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend && ./gradlew test
```

## 참고

- `youtube_favorit/`는 현재 `backend + web` 조합이 최신 검증 경로입니다.
- `youtube_favorit/frontend`는 Android/KMP 클라이언트가 남아 있지만, 최신 그룹 중심 흐름과는 일부 차이가 있습니다.
- `coin/`은 거래소 API를 직접 브라우저에서 붙지 않고 `proxy`를 통해 앱 전용 계약으로 접근하도록 설계돼 있습니다.
- `habit/`은 이제 프론트 단일 앱이 아니라 `React + Spring Boot` 2계층 구조입니다.
- `habit/backend`는 H2 파일 DB를 사용하고, 개발 중 프론트는 Vite 프록시로 `/api`를 백엔드에 연결합니다.
- 웹 개발 포트는 프로젝트별 기본값이 다릅니다: `coin/web=5173`, `habit=5173`, `youtube_favorit/web=5174`.
- `coin/web`와 `habit`를 동시에 띄워야 하면 한쪽 Vite 포트를 변경해서 사용하세요.
