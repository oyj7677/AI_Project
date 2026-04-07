# MyStarNow Frontend

`MyStarNow`의 Android/Compose Multiplatform 클라이언트입니다.  
웹 검증면과 별도로 유지되는 앱 코드이며, `androidApp`과 `shared` 모듈로 나뉘어 있습니다.

## 현재 상태를 먼저 보면

- 안드로이드 앱은 실행 가능한 스캐폴드와 shared UI/데이터 계층을 포함합니다.
- `shared` 모듈에는 API 클라이언트, repository, viewmodel, Compose UI가 들어 있습니다.
- 다만 현재 앱 네비게이션과 화면 이름은 기존 인플루언서 중심 흐름이 남아 있어, `backend/web`의 최신 그룹 중심 흐름과 완전히 일치하지는 않습니다.

## 구조

```text
frontend/
├─ androidApp/          # Android 앱 엔트리
├─ shared/              # KMP shared 모듈
├─ gradle/
├─ build.gradle.kts
├─ settings.gradle.kts
├─ gradle.properties
├─ gradlew
└─ gradlew.bat
```

## 모듈 설명

### `androidApp/`

- AndroidManifest와 `MainActivity` 포함
- `MainActivity`에서 `MyStarNowApp` Compose 앱을 실행
- 실제 안드로이드 APK를 만드는 모듈

### `shared/`

- 공통 모델, repository, use case
- Ktor 기반 API 클라이언트
- mock API와 실제 API provider
- Compose UI 화면과 viewmodel
- 공통 테스트

## 필요한 준비물

- Java 21
- Android Studio 또는 Android SDK
- Android 에뮬레이터 또는 실제 기기

## 기본 명령

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend
./gradlew test
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

## 보통 이렇게 실행합니다

### 1. 테스트

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend
./gradlew test
```

### 2. 디버그 APK 빌드

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend
./gradlew :androidApp:assembleDebug
```

### 3. 에뮬레이터/기기에 설치

기기가 연결돼 있다면:

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/frontend
./gradlew :androidApp:installDebug
```

## Android Studio에서 열 때

1. `youtube_favorit/frontend` 폴더를 Android Studio로 엽니다.
2. Gradle sync를 완료합니다.
3. 에뮬레이터나 실제 기기를 선택합니다.
4. `androidApp` 실행 구성을 실행합니다.

## 코드 구조 힌트

- `androidApp/src/main/java/com/mystarnow/android/MainActivity.kt`
- `shared/src/commonMain/kotlin/com/mystarnow/shared/data`
- `shared/src/commonMain/kotlin/com/mystarnow/shared/presentation`
- `shared/src/commonMain/kotlin/com/mystarnow/shared/ui`

## 주의

- 현재 웹/백엔드가 그룹 중심으로 발전한 반면, 안드로이드 앱은 기존 인플루언서 중심 흐름이 일부 남아 있습니다.
- 그래서 최신 제품 검증은 우선 `youtube_favorit/web`에서 진행하고, 이 앱은 별도의 안드로이드 클라이언트 자산으로 보는 것이 정확합니다.
- API 연결 모드나 개발자 설정 관련 로직은 shared 모듈 내부 구현을 함께 확인하는 것이 좋습니다.
