---
id: ADR-MYSTARNOW-BFF-ARCHITECTURE
type: adr
status: proposed
date: 2026-04-04
owners: []
related_prd: PRD-MYSTARNOW-BFF-SERVER
supersedes:
superseded_by:
---

# Context

`MyStarNow`는 Android-first Compose Multiplatform 앱이지만, 실제 제품 경험은 외부 플랫폼 데이터를 얼마나 안정적으로 모아 화면용으로 제공하느냐에 크게 좌우된다.

프론트가 직접 각 플랫폼을 붙이는 방식은 아래 이유로 불리하다.

- 플랫폼별 인증, 응답 구조, rate limit, 장애 패턴이 다르다.
- 앱이 다뤄야 할 핵심 개념은 플랫폼 데이터가 아니라 `인플루언서 중심 읽기 모델`이다.
- 공식 링크, 소개, 카테고리, 예정 방송은 자동 수집보다 운영자 관리가 더 중요하다.
- Android 앱이 먼저 출시되더라도 추후 iOS/Desktop이 붙으면 동일한 서버 계약이 필요하다.

따라서 서버는 단순 프록시가 아니라 `정규화, 캐시, fallback, 운영 보정`을 수행하는 BFF 역할을 맡아야 한다.
또한 1차 범위는 수집 안정성을 위해 `YouTube`, `Instagram`으로 제한하고, X/CHZZK/SOOP은 후속 확장 대상으로 남긴다.

# Decision

- 서버 구조는 `Option B: 모듈형 단일 BFF + 플랫폼 어댑터 워커 + 수동 운영 데이터` 조합으로 결정한다.
- 서버 언어와 애플리케이션 프레임워크는 `Kotlin + Spring Boot`로 결정한다.
- 인바운드 API는 `Spring MVC` 기반 REST로 제공한다.
- 외부 플랫폼 호출은 `Spring WebClient`로 처리한다.
- 주기적 polling worker는 `Spring Scheduling`과 전용 task executor 정책으로 운영한다.
- 영속 저장소는 `PostgreSQL`, 마이그레이션은 `Flyway`를 사용한다.
- 짧은 TTL 캐시, stale 메타데이터, 향후 분산 락 후보 용도로 `Redis`를 사용한다.
- 운영 경로 인증과 보호는 `Spring Security`를 사용한다.
- 관측성은 `Spring Boot Actuator + Micrometer + Prometheus`를 기본으로 한다.
- 모듈 경계 검증과 문서화를 위해 `Spring Modulith` 도입을 권장한다.
- 앱은 외부 플랫폼 API를 직접 호출하지 않고 `MyStarNow API`만 호출한다.
- 서버는 `인플루언서`, `채널`, `라이브 상태`, `최근 활동`, `예정 방송` 중심 도메인 모델을 유지한다.
- 외부 플랫폼 수집은 플랫폼별 어댑터와 polling worker로 분리한다.
- 자동 수집만으로 부족한 데이터 품질은 운영자 수동 입력으로 보정한다.
- 1차 자동 수집은 YouTube 중심으로 두고, Instagram은 수동 등록 중심으로 운영한다.
- 읽기 API는 홈/목록/상세 화면 단위 응답으로 제공한다.
- 초기 배포는 마이크로서비스가 아니라 단일 코드베이스 기반 모듈형 서버로 구현한다.
- 장애 대응은 `마지막 정상 캐시 + stale 표시 + 수동 데이터 우선` 전략을 기본으로 한다.

# Alternatives Considered

## Option A: 앱이 외부 플랫폼 API를 직접 호출

- 장점: 서버 개발량을 줄일 수 있다.
- 장점: 아주 초기 프로토타입은 빠르게 만들 수 있다.
- 단점: 플랫폼별 인증과 예외 처리를 앱이 직접 감당해야 한다.
- 단점: 앱 구조가 플랫폼별 조건문과 장애 처리로 오염된다.
- 단점: iOS/Desktop 확장 시 중복 구현이 커진다.

## Option B: 모듈형 단일 BFF + 어댑터 워커 + 수동 운영 데이터

- 장점: 앱은 단일 API 계약만 다루면 된다.
- 장점: 플랫폼 차이와 장애를 서버에서 흡수할 수 있다.
- 장점: 운영자 수정과 자동 수집 데이터를 같은 읽기 모델로 합칠 수 있다.
- 장점: MVP 단계에서 가장 현실적인 복잡도와 확장성을 가진다.
- 단점: 서버와 운영 도구를 함께 설계해야 한다.
- 단점: 초기부터 데이터 모델과 캐시 전략을 신중히 잡아야 한다.

## Option C: 순수 프록시 서버

- 장점: 구현이 상대적으로 단순하다.
- 장점: 원본 응답을 거의 그대로 전달할 수 있다.
- 단점: 정규화 책임이 앱에 남는다.
- 단점: 홈/상세 조합 응답을 만들기 어렵다.
- 단점: 운영 보정 데이터와 자동 수집 데이터를 통합하기 어렵다.

## Option D: 처음부터 마이크로서비스 분리

- 장점: 이론상 서비스별 독립 배포가 가능하다.
- 장점: 팀이 매우 커질 경우 분리 운영 여지가 있다.
- 단점: 현재 제품 단계에서는 과도한 운영 복잡도다.
- 단점: 배포, 관측, 로컬 개발, 계약 관리 비용이 너무 크다.

# Consequences

- 긍정적 영향: 앱과 외부 플랫폼 사이에 안정적인 계약 계층이 생긴다.
- 긍정적 영향: 플랫폼별 장애와 수집 제약을 앱 업데이트 없이 서버에서 흡수할 수 있다.
- 긍정적 영향: 공식 링크와 일정 같은 운영성 정보를 함께 품질 관리할 수 있다.
- 긍정적 영향: Android 이후 다른 클라이언트가 추가돼도 같은 API를 재사용할 수 있다.
- 긍정적 영향: Spring Boot 표준 스택을 사용해 보안, 운영, 관측성을 빠르게 확보할 수 있다.
- 부정적 영향: 서버 운영과 데이터 품질 관리 책임이 새로 생긴다.
- 부정적 영향: 자동 수집과 수동 운영 사이의 source of truth 규칙을 명확히 정해야 한다.
- 부정적 영향: Redis, Flyway, 관측성 스택까지 포함하면 초기 인프라 구성이 조금 늘어난다.
- 운영상 주의점: stale 데이터는 숨기지 말고 응답 메타데이터로 드러내야 한다.
- 운영상 주의점: 플랫폼별 어댑터는 실패를 서로 전염시키지 않도록 격리해야 한다.
- 운영상 주의점: 운영 수정은 배치 완료를 기다리지 않고 읽기 모델에 빠르게 반영되어야 한다.

# Rollout / Follow-up

- 1단계: 서버 도메인 모델과 읽기 API 계약을 확정한다.
- 2단계: 인플루언서/채널/일정의 운영 데이터 저장 구조를 만든다.
- 3단계: 홈/목록/상세 BFF endpoint를 먼저 구현한다.
- 4단계: 플랫폼별 라이브 상태/활동 어댑터를 순차적으로 붙인다.
- 5단계: stale 처리, fallback, observability를 강화한다.
- 검증 방법: API 계약 리뷰, 플랫폼 장애 시 fallback 검증, 운영 수정 반영 검증, Android 앱 연동 검증

# Links

- Related PRD: `../product/PRD-mystarnow-bff-server.md`
- Related Decisions: `./TRD-mystarnow-bff-server.md`
- Related PRs:
- Related Front Docs:
  - `../../front/architecture/ADR-mystarnow-architecture.md`
  - `../../front/architecture/TRD-mystarnow-android-cmp.md`
