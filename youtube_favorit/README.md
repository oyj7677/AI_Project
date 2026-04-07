# MyStarNow

아이돌 그룹의 공식 유튜브 채널과 멤버 개인 유튜브 채널을 한곳에서 탐색하기 위한 프로젝트입니다.  
현재 기준의 메인 개발 흐름은 `backend + web` 조합이며, `frontend`는 Android/KMP 클라이언트가 별도로 유지되고 있습니다.

## 현재 프로젝트 구성

```text
youtube_favorit/
├─ backend/   # Spring Boot BFF, 운영자 입력 API 포함
├─ web/       # 그룹 중심 웹 검증 화면
├─ frontend/  # Android/KMP 클라이언트
└─ docs/      # PRD, ADR, API/데이터/운영 문서
```

## 각 폴더가 하는 일

### `backend/`

- 그룹/멤버/채널/영상 중심 BFF
- 홈 피드, 그룹 목록, 그룹 상세, 멤버 상세 API
- 운영자 입력 API
- PostgreSQL, Redis, Flyway, Spring Security 포함

### `web/`

- 최신 제품 검증용 웹 클라이언트
- 그룹 홈, 그룹 목록, 그룹 상세, 멤버 상세, 관리자 화면 제공
- 개발자 패널에서 백엔드 주소를 바꿔가며 테스트 가능

### `frontend/`

- Android/Compose Multiplatform 기반 클라이언트
- shared 모듈에 API, repository, viewmodel, UI 코드가 들어 있음
- 다만 현재 네비게이션과 데이터 모델은 기존 인플루언서 중심 흐름이 남아 있어, 웹/백엔드의 최신 그룹 중심 흐름과 완전히 같지는 않습니다.

## 주요 기능

- 그룹 중심 최근 업로드 홈 피드
- 그룹 목록 검색 / 정렬 / 더보기
- 그룹 상세
- 멤버 상세
- 운영자 입력 화면
- YouTube 연동 기반 채널/영상 동기화 확장 여지

## 권장 로컬 실행 조합

가장 현실적인 로컬 검증 조합은 아래입니다.

1. `backend` 실행
2. `web` 실행
3. 필요하면 `frontend`는 별도로 Android에서 확인

## 사전 준비

- Java 21
- Docker / Docker Compose
- Node.js + npm
- Android Studio 또는 Android SDK/ADB (`frontend`를 실행할 경우)

## 빠른 시작

### 1. 백엔드 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
docker-compose up -d
export YOUTUBE_API_KEY="YOUR_YOUTUBE_API_KEY"
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2. 웹 실행

다른 터미널에서:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web
npm install
npm run dev
```

### 3. 안드로이드 앱 빌드

옵션:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend
./gradlew test
./gradlew :androidApp:assembleDebug
```

## 접속 주소

- 백엔드: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- 웹: `http://127.0.0.1:5174`
- 웹 관리자 화면: `http://127.0.0.1:5174/admin`

참고:

- 웹 개발 포트는 `5174`로 고정되어 있습니다.
- 포트가 이미 사용 중이면 자동으로 다른 포트로 이동하지 않고 실행이 실패합니다.

## 운영자 입력 관련 메모

- 웹 관리자 화면은 백엔드의 `/internal/operator` API를 사용합니다.
- 기본 보안 설정에서는 운영자 인증이 켜져 있습니다.
- 기본 계정은 `operator / operator`이며, 환경 변수로 변경할 수 있습니다.

## 현재 데이터 초기 상태

중요:

- `local`, `dev` 프로파일이 있어도 현재 설정 파일에서는 `app.sample-data.enabled=false`입니다.
- 즉, 로컬 실행 직후 자동 샘플 데이터가 들어오지 않습니다.
- 화면을 채우려면 관리자 입력 화면이나 운영자 API로 그룹/멤버/채널/영상을 먼저 넣어야 합니다.

## 자주 쓰는 검증 명령

### Backend

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
./gradlew test
./gradlew build
```

### Web

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web
npm run lint
npm run build
```

### Frontend

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend
./gradlew test
./gradlew :androidApp:assembleDebug
```

## 문서 위치

- 전체 문서 인덱스: `docs/README.md`
- 프론트 문서: `docs/front/README.md`
- 백엔드 문서: `docs/back/README.md`
- 운영 문서: `docs/ops/README.md`

## 참고

- 현재 제품 검증의 중심은 `web`입니다.
- `frontend`는 유지 중인 안드로이드 클라이언트이지만, 최신 그룹 중심 제품 구조에 완전히 맞춰진 상태는 아닙니다.
- 실제 YouTube 동기화 동작을 보려면 `YOUTUBE_API_KEY`가 필요합니다.
