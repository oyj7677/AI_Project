---
id: ADR-MYSTARNOW-ARCHITECTURE
type: adr
status: proposed
date: 2026-04-04
owners: []
related_prd: PRD-MYSTARNOW
supersedes:
superseded_by:
---

# Context

`MyStarNow`는 YouTube, Instagram, X, CHZZK, SOOP 등 여러 플랫폼에 흩어진 인플루언서 소식을 한곳에서 모아 보여주는 Android 우선 서비스다.

초기 제품은 아래 조건을 동시에 만족해야 한다.

- Android를 우선 출시 플랫폼으로 삼는다.
- 이후 iOS 또는 Desktop으로 확장할 가능성을 열어둔다.
- 외부 플랫폼별 API 구조와 데이터 품질 차이를 앱 내부에 직접 노출하지 않는다.
- 공식 채널 링크, 라이브 상태, 최근 활동, 예정 방송을 한 화면 흐름으로 제공한다.
- MVP는 `Must Have + Nice to Have`까지 포함하며, 운영자가 일부 데이터를 수동으로 입력하는 방식도 허용한다.

또한 헤이딜러/PRND가 공개한 Android 아키텍처 원칙 중 아래 요소가 참고 가치가 높다.

- `MVVM + Clean Architecture`
- 멀티 모듈 구조
- Kotlin + Coroutines/Flow 중심 비동기 표준화
- 레이어별 모델 분리와 mapper 사용
- Android 프레임워크 의존을 presentation 로직에서 최소화

하지만 `MyStarNow`는 전통적인 Android 단일 앱이 아니라 Compose Multiplatform 기반 공용 코드 구조를 전제로 하므로, Android 전용 도구와 규칙은 그대로 복제할 수 없다.

# Decision

- 클라이언트 구조는 `Android-first Compose Multiplatform`으로 결정한다.
- 앱 아키텍처는 `MVVM + Clean Architecture`를 채택한다.
- 의존 방향은 `Presentation -> Domain <- Data`를 유지하고 Domain은 구현 세부사항을 모르게 한다.
- 공용 비즈니스 로직과 가능한 UI는 `frontend/shared/commonMain`에 두고, Android 진입점과 시스템 연동은 `frontend/androidApp` 및 `frontend/shared/androidMain`에 둔다.
- 외부 플랫폼 데이터는 앱이 직접 호출하지 않고, 정규화된 `MyStarNow API`를 제공하는 얇은 BFF/집계 백엔드를 둔다.
- 인플루언서, 채널, 라이브 상태, 최근 활동, 일정은 레이어별 모델을 따로 두고 mapper를 통해 변환한다.
- 비동기 처리와 상태 관리는 `Coroutines + Flow`를 기본으로 하며, 지속 상태는 `StateFlow`, 일회성 이벤트는 `SharedFlow` 계열로 관리한다.
- DI는 `shared` 공용 코드에 묶지 않고 constructor injection 중심으로 작성하며, Android 조립 계층에서만 Hilt 또는 동등한 도구를 사용한다.
- 프로젝트는 기능별/레이어별 멀티 모듈 구조를 지향한다.
- 운영 효율을 위해 디버그용 개발자 모드, feature flag override, analytics gateway, 오류 복구 화면 전략을 함께 채택한다.

# Alternatives Considered

## Option A: Android Native 단일 모듈 앱

- 장점: 초기 설정이 가장 단순하고 구현 속도가 빠르다.
- 장점: Compose Multiplatform이나 공용 계층 설계 부담이 적다.
- 단점: 기능이 늘수록 결합도가 커지고 구조 정리가 어려워질 수 있다.
- 단점: iOS/Desktop 확장 시 재사용 이점이 거의 없다.
- 단점: 플랫폼별 외부 데이터 처리 로직이 앱 내부로 스며들 가능성이 높다.

## Option B: Android-first Compose Multiplatform + BFF

- 장점: Android 우선 출시와 장기 확장성 사이의 균형이 좋다.
- 장점: 공용 도메인/데이터/상태 모델을 재사용할 수 있다.
- 장점: 외부 플랫폼별 차이를 서버에서 흡수해 앱 구조를 단순화할 수 있다.
- 장점: 헤이딜러식 계층 분리 원칙을 현대화해서 적용하기 좋다.
- 단점: 초기 설계 난이도가 단일 앱 구조보다 높다.
- 단점: 앱과 서버를 함께 설계해야 하므로 문서와 계약 관리가 더 중요하다.

## Option C: 앱이 외부 플랫폼 API를 직접 호출

- 장점: 초기 서버 개발량을 줄일 수 있다.
- 장점: 단순 조회 화면만 보면 빠르게 시작할 수 있다.
- 단점: 플랫폼별 인증, 응답 포맷, rate limit, 장애 처리를 앱이 직접 감당해야 한다.
- 단점: API 정책 변화가 앱 업데이트 이슈로 바로 연결된다.
- 단점: 플랫폼별 차이가 UI와 상태 코드에 섞여 장기 유지보수가 어려워진다.

## Option D: Google App Architecture 스타일로 Domain이 Data를 직접 참조

- 장점: 작은 프로젝트에서는 구현 진입 장벽이 낮다.
- 장점: 예제와 레퍼런스를 그대로 따라가기 쉽다.
- 단점: `MyStarNow`처럼 외부 원천 데이터가 다양하고 정규화가 중요한 서비스에서는 domain 안정성이 약해질 수 있다.
- 단점: 플랫폼별 API 계약이나 캐시 구조 변경이 domain/use case에 전파될 가능성이 있다.

# Consequences

- 긍정적 영향: 앱은 정규화된 단일 모델만 다루므로 플랫폼별 복잡도가 UI에 번지지 않는다.
- 긍정적 영향: Android 우선으로 빠르게 시작하면서도 iOS/Desktop 확장 여지를 남길 수 있다.
- 긍정적 영향: 기능 추가 시 홈, 목록, 상세, 즐겨찾기 등 기능별 모듈 확장이 쉬워진다.
- 긍정적 영향: 레이어별 모델 분리로 API 변경, 로컬 캐시 변경, UI 변경의 충격 범위를 줄일 수 있다.
- 부정적 영향: 초기 문서화와 설계 품질이 낮으면 오히려 과한 구조가 될 수 있다.
- 부정적 영향: Compose Multiplatform과 Android 셸 구조의 경계가 흐려지면 공용 코드가 Android 의존으로 오염될 수 있다.
- 부정적 영향: BFF와 수집 파이프라인 운영 비용이 추가된다.
- 운영상 주의점: 공용 코드에는 `android.*` 의존을 넣지 않는다.
- 운영상 주의점: 외부 플랫폼 장애는 마지막 정상 데이터와 수동 운영으로 흡수할 수 있어야 한다.
- 운영상 주의점: feature flag와 개발자 모드는 디버그 전용 접근 제어를 유지해야 한다.

# Rollout / Follow-up

- 1단계: PRD/TRD 기준으로 `frontend/shared/commonMain`, `frontend/androidApp`, `backend` 경계를 먼저 확정한다.
- 2단계: 홈, 목록, 상세, 즐겨찾기 화면의 공용 모델과 repository 계약을 정의한다.
- 3단계: `MyStarNow API`의 `/v1/home`, `/v1/influencers`, `/v1/influencers/{slug}` 계약을 우선 고정한다.
- 4단계: 라이브 상태, 최근 활동, 일정의 정규화 규칙을 어댑터 단위로 구현한다.
- 5단계: 개발자 모드, analytics gateway, feature flag override를 디버그 빌드 기준으로 도입한다.
- 검증 방법: TRD 기준 상태 흐름 검토, 화면 단위 구조 검토, API 계약 리뷰, Android MVP 수동 검증

# Links

- Related PRD: `../product/PRD-mystarnow.md`
- Related Decisions: `./TRD-mystarnow-android-cmp.md`
- Related PRs:
