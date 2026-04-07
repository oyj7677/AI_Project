# MyStarNow Backend

아이돌 그룹 중심 YouTube 허브를 위한 Spring Boot BFF입니다.  
현재 웹 검증 화면과 운영자 입력 화면이 사용하는 메인 백엔드이며, 그룹/멤버/채널/영상 데이터를 조회하고 관리하는 역할을 맡습니다.

## 제공 기능

- 홈 피드 API
- 그룹 목록 / 그룹 상세 API
- 멤버 상세 API
- 앱 설정 API
- 운영자 입력 API
- PostgreSQL 기반 영속화
- Flyway 마이그레이션
- Redis 연동 준비
- Actuator / Prometheus / OpenAPI

## 공개 API

- `GET /v1/home`
- `GET /v1/groups`
- `GET /v1/groups/{groupSlug}`
- `GET /v1/members/{memberSlug}`
- `GET /v1/meta/app-config`

## 운영자 API

- `POST /internal/operator/groups`
- `PUT /internal/operator/groups/{groupSlug}`
- `POST /internal/operator/members`
- `PUT /internal/operator/members/{memberSlug}`
- `POST /internal/operator/channels`
- `PUT /internal/operator/channels/{channelId}`
- `POST /internal/operator/videos`
- `PUT /internal/operator/videos/{videoId}`

## 기술 스택

- Kotlin
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Redis
- Spring Security
- Spring Boot Actuator
- springdoc OpenAPI

## 실행에 필요한 것

- Java 21
- Docker / Docker Compose
- PostgreSQL 컨테이너
- Redis 컨테이너

## 로컬 실행 순서

### 1. 인프라 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
docker-compose up -d
```

기본 컨테이너:

- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

### 2. 애플리케이션 실행

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
export YOUTUBE_API_KEY="YOUR_YOUTUBE_API_KEY"
./gradlew bootRun --args='--spring.profiles.active=local'
```

`YOUTUBE_API_KEY`는 필수는 아니지만, 실제 YouTube 메타데이터/영상 동기화를 확인하려면 필요합니다.

## 주요 환경 변수

| 변수 | 설명 | 기본값 |
| --- | --- | --- |
| `SERVER_PORT` | 서버 포트 | `8080` |
| `DB_HOST` | PostgreSQL 호스트 | `localhost` |
| `DB_PORT` | PostgreSQL 포트 | `5432` |
| `DB_NAME` | DB 이름 | `mystarnow` |
| `DB_USERNAME` | DB 사용자 | `mystarnow` |
| `DB_PASSWORD` | DB 비밀번호 | `mystarnow` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `YOUTUBE_API_KEY` | YouTube Data API 키 | 빈 값 |
| `YOUTUBE_SYNC_ENABLED` | 실제 YouTube 동기화 사용 여부 | `false` |
| `YOUTUBE_MOCK_ENABLED` | YouTube mock 사용 여부 | `false` |
| `OPERATOR_AUTH_ENABLED` | 운영자 API 인증 사용 여부 | `true` |
| `OPERATOR_USERNAME` | 운영자 계정 | `operator` |
| `OPERATOR_PASSWORD` | 운영자 비밀번호 | `operator` |

## 프로파일 메모

- `local`
  - Redis health check 비활성화
  - 현재 설정상 `app.sample-data.enabled=false`
- `dev`
  - 현재 설정상 `app.sample-data.enabled=false`

중요:

- 로컬/개발 프로파일이 있다고 해서 샘플 데이터가 자동으로 들어오지 않습니다.
- 실행 직후 데이터가 비어 있다면 웹 관리자 화면이나 운영자 API로 직접 입력해야 합니다.

## 접속 주소

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## 운영자 인증

- 기본적으로 `/internal/operator/**`는 인증 대상입니다.
- 기본 계정은 `operator / operator`
- 웹 관리자 화면을 함께 쓸 때도 같은 계정을 사용하면 됩니다.

## 빌드 / 테스트

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/backend
./gradlew test
./gradlew build
```

참고:

- 테스트는 Testcontainers를 사용하므로 Docker 환경이 필요할 수 있습니다.
- 현재 Gradle 설정에는 Colima 환경을 고려한 Testcontainers 관련 환경 변수 기본값이 포함돼 있습니다.

## 코드 구조 힌트

- `src/main/kotlin/.../home`: 홈 피드 조회
- `src/main/kotlin/.../idol`: 그룹/멤버 중심 읽기 로직
- `src/main/kotlin/.../operator`: 운영자 쓰기 API
- `src/main/resources/db/migration`: Flyway 스키마 및 데이터 마이그레이션

## 참고

- 코드베이스 안에 과거 인플루언서 중심 흐름이 일부 남아 있을 수 있지만, 현재 제품 기준선은 그룹 중심 API입니다.
- 실제 최신 영상 반영을 보려면 YouTube API 키와 동기화 관련 설정이 필요합니다.
