# MyStarNow Web

아이돌 그룹 중심 YouTube 허브를 가장 빠르게 검증하기 위한 웹 클라이언트입니다.  
현재 `youtube_favorit` 프로젝트에서 사용자 흐름을 확인할 때 가장 먼저 실행하는 프론트엔드입니다.

## 제공 화면

- `/`
  - 그룹 중심 최근 업로드 홈 피드
- `/groups`
  - 그룹 목록 검색 / 정렬 / 더보기
- `/groups/:groupSlug`
  - 그룹 상세
- `/members/:memberSlug`
  - 멤버 상세
- `/admin`
  - 그룹 / 멤버 / 채널 / 영상 운영 입력

## 기술 스택

- Vite
- React
- TypeScript
- React Router

## 개발 시 동작 방식

이 앱은 Vite 서버 안에 동적 프록시를 붙여서 백엔드로 요청을 전달합니다.

- 프록시 접두사: `/__mystarnow_api`
- 기본 백엔드 대상: `http://localhost:8080`
- 실제 대상 주소는 개발자 패널에서 바꿀 수 있습니다.

즉, 프론트에서 백엔드 URL을 직접 코드에 박지 않고 브라우저 로컬 저장소에 저장해 바꿔가며 테스트할 수 있습니다.

## 설치

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web
npm install
```

## 실행

먼저 백엔드가 떠 있는 것이 좋습니다.

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web
npm run dev
```

기본 주소:

- 웹: `http://127.0.0.1:5174`
- 관리자 화면: `http://127.0.0.1:5174/admin`

참고:

- 개발 포트는 `5174`로 고정되어 있습니다.
- 포트가 이미 사용 중이면 Vite가 다른 포트로 자동 이동하지 않고 에러를 냅니다.

## 함께 실행할 백엔드

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
docker-compose up -d
export YOUTUBE_API_KEY="YOUR_YOUTUBE_API_KEY"
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 관리자 화면 사용

- `/admin` 화면은 백엔드의 `/internal/operator` API를 호출합니다.
- 기본 인증 정보는 보통 `operator / operator`입니다.
- 샘플 데이터가 자동으로 들어오지 않기 때문에, 초기 로컬 실행에서는 이 관리자 화면으로 데이터를 넣는 흐름이 중요합니다.

## 개발자 패널

- 기본적으로 접힌 상태로 시작
- 백엔드 base URL을 브라우저 로컬 저장소에 저장
- connectivity check 제공
- 전체 refetch 제공

## 자주 쓰는 명령

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/web
npm run dev
npm run lint
npm run build
npm run preview
```

## 이 UI가 검증하는 범위

- 홈 최근 업로드 피드
- 그룹 목록
- 그룹 상세의 공식 채널 / 멤버 목록 / 최근 업로드
- 멤버 상세의 개인 채널 / 최근 업로드
- 운영자 입력 폼

## 주소 모음

- Web dev: `http://127.0.0.1:5174`
- Admin: `http://127.0.0.1:5174/admin`
- Backend: `http://localhost:8080`
- Backend health: `http://localhost:8080/actuator/health`

## 주의

- 백엔드가 내려가 있어도 앱 자체는 뜨지만, 연결 오류 UI를 보여줄 수 있습니다.
- 실제 YouTube 동기화 결과를 확인하려면 백엔드 실행 전에 `YOUTUBE_API_KEY`를 지정하는 것이 좋습니다.
- 현재 이 웹 앱이 `youtube_favorit` 프로젝트의 최신 그룹 중심 사용자 흐름을 가장 잘 반영합니다.
