# AI Team Skill Stack

작성일: 2026-03-30

## 목적

이 문서는 AI_TEAM에서 사용할 추천 Skill 구성을 한곳에 모아 둔 운영 기준서다.
여기서 말하는 "추가"는 현재 세션에서 사용 가능한 Skill을 기준으로, 팀 운영 문서와 역할 매핑에 반영한 것을 뜻한다.

참고:

- 현재 추천 Skill들은 이미 이 Codex 환경에서 사용 가능한 상태다.
- 따라서 별도 설치보다는, 팀이 일관되게 사용할 수 있도록 `역할별 사용 기준`과 `기본 조합`을 먼저 고정한다.

## 전체 추천 Skill 목록

### 핵심 운영 Skill

| Skill | 용도 | 기본 사용 시점 |
| --- | --- | --- |
| `plan` | 요구사항 정리, acceptance criteria, 실행 계획 수립 | 구현 시작 전 |
| `team` | 역할 분리된 장시간 병렬 실행 | 구현 단계 |
| `code-review` | 작성자와 분리된 리뷰 | 구현 후 |
| `security-review` | 보안, 권한, 시크릿, 위험 작업 점검 | 병합 전 |
| `trace` | 에이전트 흐름과 병목 추적 | 문제 분석, 회고 |
| `note` | 중간 의사결정, TODO, 가정 정리 | 전 단계 |

### 확장 Skill

| Skill | 용도 | 사용 조건 |
| --- | --- | --- |
| `visual-verdict` | UI, 프론트, 시각 비교 검증 | 화면 결과물이 있을 때 |
| `configure-notifications` | 장시간 작업 알림 구성 | 병렬 작업, 장시간 팀 실행 |
| `openai-docs` | OpenAI 공식 문서 확인 | OpenAI API/제품 관련 설계 시 |
| `ask-claude` | 외부 모델 관점 비교 | 설계 검토, 교차 검증 |
| `ask-gemini` | 대안적 관점 비교 | 설계 검토, 교차 검증 |
| `worker` | 장기 팀 운영에서 작업자 규약 강화 | `team` 운영 시 |
| `hud` | tmux 기반 팀 상태 시각화 | `team` 운영 시 |

## 기본 활성 조합

### 1. 최소 안전 조합

- `plan`
- `team`
- `code-review`
- `note`

추천 상황:

- 작은 기능 추가
- 내부 도구 제작
- 초기 프로토타입

### 2. 표준 제품 개발 조합

- `plan`
- `team`
- `code-review`
- `security-review`
- `trace`
- `note`

추천 상황:

- 실제 운영 가능성을 염두에 둔 기능 개발
- 여러 역할이 동시에 참여하는 작업

### 3. 프론트엔드 / UI 검증 조합

- `plan`
- `team`
- `code-review`
- `visual-verdict`
- `trace`
- `note`

추천 상황:

- 디자인 구현
- 랜딩 페이지
- 관리자 화면

### 4. 장시간 병렬 팀 운영 조합

- `plan`
- `team`
- `worker`
- `hud`
- `configure-notifications`
- `trace`
- `note`

추천 상황:

- tmux 기반 팀 실행
- 여러 작업자가 동시에 다른 책임을 맡는 경우

### 5. 외부 문서 / 모델 교차 검증 조합

- `plan`
- `openai-docs`
- `ask-claude`
- `ask-gemini`
- `note`

추천 상황:

- 기술 선택 검토
- 정책성 판단
- 외부 API 설계 확인

## 역할별 Skill 매핑

| 역할 | 기본 Skill | 보조 Skill |
| --- | --- | --- |
| Planner / PM | `plan`, `note` | `trace` |
| CTO / Tech Lead | `plan`, `trace` | `openai-docs`, `ask-claude`, `ask-gemini` |
| Architect | `plan`, `trace`, `note` | `openai-docs`, `ask-claude`, `ask-gemini` |
| Explorer / Researcher | `note`, `trace` | `openai-docs`, `ask-claude`, `ask-gemini` |
| Executor / Developer | `team`, `note` | `worker`, `hud` |
| Reviewer | `code-review`, `trace` | `note` |
| QA / Verifier | `trace`, `note` | `visual-verdict` |
| Security / Release | `security-review`, `trace` | `note` |
| Harness / Eval Owner | `trace`, `note`, `plan` | `configure-notifications`, `hud` |

## 권장 기본 운영 흐름

### 단계 1. 문제 정의

- `plan`
- `note`

출력:

- 요구사항
- acceptance criteria
- 범위와 제외 범위

### 단계 2. 구현 준비

- `plan`
- `trace`

출력:

- 구현 순서
- 리스크 메모
- 병렬 처리 가능 영역

### 단계 3. 구현

- `team`
- `note`
- 필요 시 `worker`, `hud`

출력:

- 코드 변경
- 테스트
- 작업 로그

### 단계 4. 리뷰

- `code-review`
- `security-review`
- `trace`

출력:

- 버그/리스크 피드백
- 보안 승인 조건

### 단계 5. 검증

- `trace`
- 필요 시 `visual-verdict`

출력:

- 검증 결과
- 시각 비교 결과
- 회귀 여부

## 팀 운영 규칙

### 항상 켜둘 Skill

- `note`
  결정, 가정, TODO가 빠르게 증발하지 않게 유지
- `trace`
  병목과 실패 패턴을 누적 관찰

### 구현 전에 먼저 쓸 Skill

- `plan`

이유:

- acceptance criteria 없이 구현을 시작하면 역할이 많아질수록 혼선이 커진다.

### 구현 중 핵심 Skill

- `team`
- `worker`
- `hud`

이유:

- 역할을 분리해도 운영 규약이 없으면 서로 충돌하거나 중복 작업이 생긴다.

### 병합 전에 꼭 도는 Skill

- `code-review`
- `security-review`

이유:

- 작성자 관점과 승인 관점을 분리해야 한다.

## 우선 적용 권장안

현재 AI_TEAM에는 아래 순서로 적용하는 것을 권장한다.

1. 기본 활성 조합을 `표준 제품 개발 조합`으로 둔다.
2. UI 작업이 포함되면 `visual-verdict`를 추가한다.
3. 장시간 병렬 실행이 필요해지면 `worker`, `hud`, `configure-notifications`를 활성화한다.
4. OpenAI 관련 기능을 만들면 `openai-docs`를 필수 확인 경로로 둔다.
5. 중요한 설계 결정은 `ask-claude` 또는 `ask-gemini`로 교차 검증한다.

## 바로 사용할 기본 세트

초기 기본 세트:

- `plan`
- `team`
- `code-review`
- `security-review`
- `trace`
- `note`

조건부 추가 세트:

- `visual-verdict`
- `configure-notifications`
- `openai-docs`
- `ask-claude`
- `ask-gemini`
- `worker`
- `hud`

## 한 줄 요약

AI_TEAM의 추천 Skill 구성은 `plan -> team -> review -> verify` 흐름을 중심으로 하고, `trace`와 `note`를 항상 유지하며, UI/장시간 병렬/외부 문서 검증이 필요한 경우 확장 Skill을 얹는 방식이 가장 안정적이다.
