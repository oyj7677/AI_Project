# AI Team Blueprint

작성일: 2026-03-30

## 목적

현재 워크스페이스에서 운영할 AI 프로젝트 팀의 권장 구성과 운영 원칙을 정리한다.
이 문서는 단순한 역할 나열보다, `권한 제한`, `검증 하네스`, `산출물 계약`을 중심으로 AI 팀을 설계하는 데 초점을 둔다.

## 핵심 결론

- `기획자, CTO, 아키텍처, 개발자, 리뷰어, QA` 구성은 좋은 출발점이다.
- 여기에 `Explorer/Researcher`, `Security/Release`, `Harness/Eval Owner`를 추가하는 것을 권장한다.
- AI 팀 운영에서는 "더 자율적이게" 만드는 것보다 "안전하게 일하게" 만드는 것이 먼저다.
- 특히 `Harness/Eval Owner`는 빠지기 쉬우나 실제로는 거의 필수 역할이다.

## 추천 팀 구성

| 역할 | 주 책임 | 주요 산출물 |
| --- | --- | --- |
| Planner / PM | 문제 정의, 범위, 우선순위, acceptance criteria 정리 | PRD, 작업 범위, 완료 조건 |
| CTO / Tech Lead | 기술 방향, 일정, 리스크, 투자 우선순위 결정 | 기술 결정, 리스크 판단, go/no-go |
| Architect | 모듈 경계, 의존성 규칙, ADR 설계 | ADR, 구조 규칙, 인터페이스 정의 |
| Explorer / Researcher | 코드베이스 맵핑, 외부 문서 조사, MCP 조회 | 시스템 맵, 조사 메모, 참고 자료 |
| Executor / Developer | 구현, 리팩터링, 테스트 보강 | 코드 변경, 테스트, 마이그레이션 |
| Reviewer | 버그, 회귀, 설계 위반, 테스트 누락 검토 | 리뷰 코멘트, 수정 요청 |
| QA / Verifier | 수용 기준 검증, e2e, 회귀 테스트, 시각 검증 | 검증 리포트, 테스트 증거 |
| Security / Release | 권한, 시크릿, 취약점, 배포 게이트 검토 | 보안 체크, 배포 승인 조건 |
| Harness / Eval Owner | 역할 프롬프트, 툴 권한, eval, trace, 게이트 설계 | 체크리스트, eval 세트, 운영 규칙 |

## 왜 추가 역할이 필요한가

- `Explorer / Researcher`
  코드 구현 전에 사실 관계를 빠르게 정리해 준다. 잘못된 컨텍스트에서 구현이 시작되는 것을 줄여 준다.
- `Security / Release`
  권한, 외부 API, 배포, 비용, 시크릿 같은 고위험 변경을 별도 게이트로 분리한다.
- `Harness / Eval Owner`
  누가 무엇을 할 수 있는지, 어떤 조건에서 멈춰야 하는지, 무엇으로 통과를 판단하는지 정의한다. AI 팀의 품질과 재현성을 좌우한다.

## 역할 분리 원칙

- 작성자와 리뷰어는 분리한다.
- 작성자와 QA는 분리한다.
- 가능하면 Planner와 Executor도 분리한다.
- 위험한 작업은 Security / Release 승인 없이 진행하지 않는다.
- 동일 컨텍스트가 자기 산출물을 최종 승인하지 않도록 한다.

핵심 규칙:

- `author != reviewer != QA`
- `spec before code`
- `read-only by default`

## 추천 Skill 구성

현재 컨텍스트 기준 추천 Skill은 아래와 같다.

### 기본 세트

- `plan`
  요구사항 정리, acceptance criteria, 실행 계획 수립
- `team`
  장시간 병렬 실행과 역할 분리를 갖춘 팀 운영
- `code-review`
  작성자와 분리된 리뷰 컨텍스트 운영
- `security-review`
  보안, 권한, 시크릿, 외부 연동 점검
- `trace`
  에이전트 흐름과 의사결정 추적
- `note`
  중간 결정, 제약, TODO 기록 유지

### 상황별 추천

- `visual-verdict`
  UI, 프론트, 스크린샷 기반 검증
- `configure-notifications`
  장시간 실행 알림
- `openai-docs`
  OpenAI 공식 문서 확인이 필요한 경우
- `ask-claude`
  외부 모델 관점 비교가 필요할 때
- `ask-gemini`
  대안적 관점이나 교차 검증이 필요할 때
- `worker`
  장기 팀 운영에서 작업자 규약 강화
- `hud`
  tmux 기반 팀 상태 가시화

### 시작 조합 추천

가장 안전한 시작 조합:

- `plan -> team -> code-review -> QA`

초기에는 `autopilot`류보다 위 조합이 더 예측 가능하고 통제하기 쉽다.

## 추천 MCP 구성

현재 세션에서는 MCP 리소스와 템플릿이 노출되어 있지 않았다.
따라서 지금은 Skill 중심으로 운영을 시작하고, 아래 MCP를 우선순위 순으로 붙이는 것이 좋다.

### 우선순위 1

- GitHub 또는 GitLab
  PR, diff, 리뷰, CI 상태 확인
- Linear 또는 Jira
  요구사항, 작업 상태, 우선순위 연동
- Notion 또는 Confluence
  PRD, ADR, 결정 로그 관리
- CI / Test 시스템
  빌드, 테스트, 커버리지, 아티팩트 확인

### 우선순위 2

- Sentry 또는 Datadog
  배포 후 회귀, 에러, 성능 이상 탐지
- Figma
  디자인 기준이 있는 경우 UI 검증 품질 향상
- Read-only DB / analytics
  데이터 계약 검증, 분석, 확인용

### 초기에 피할 MCP

- production write 권한
- secret manager write 권한
- human approval 없는 deploy
- 데이터 삭제나 비용 유발 작업을 바로 수행하는 툴

## 하네스 엔지니어링 관점의 운영 원칙

AI 팀의 성능은 모델 자체보다 하네스 설계에 더 크게 좌우된다.

### 1. Spec Before Code

- PRD 없이 구현 시작하지 않기
- acceptance criteria 없이 완료 판정하지 않기
- 구조 변경은 ADR 없이 진행하지 않기

### 2. Read-only By Default

- 대부분의 역할은 읽기 전용으로 시작
- 쓰기 권한은 Executor에만 부여
- 쓰기 권한도 파일 범위, 브랜치, 워크트리 단위로 제한

### 3. Small Tool Surface

- 같은 일을 하는 툴을 많이 주지 않기
- 권한이 큰 툴은 최소 인원에게만 부여
- 에이전트가 선택할 수 있는 경로를 줄여 혼란을 낮추기

### 4. Mechanical Enforcement Over Prompt

- 프롬프트만 믿지 않기
- lint, typecheck, test, policy check로 기계적으로 강제
- 배포 전 자동 게이트를 두기

### 5. Artifact Contract

각 역할의 출력물을 고정한다.

- Planner: PRD, acceptance criteria
- Architect: ADR, interface rules
- Explorer: 조사 메모, 근거 링크
- Executor: 코드 변경, 테스트
- Reviewer: 이슈 목록, 회귀 우려
- QA: 검증 결과, 실패 재현 절차
- Security: 승인 조건, 차단 사유
- Harness Owner: eval 결과, 체크리스트, trace

### 6. Eval Loop

- 회귀 테스트를 지속적으로 돌린다.
- 과거 실패 케이스를 eval 세트에 추가한다.
- 배포 후 incident replay를 축적한다.
- agent trace를 검토해 반복되는 실패 패턴을 찾는다.

### 7. Human Gate

아래 작업은 사람 승인 없이는 진행하지 않도록 한다.

- deploy
- database migration
- 외부 비용 발생 작업
- production 데이터 변경
- 권한 확대
- secret 관련 작업

## 권장 운영 흐름

### 1. Intake

- 문제 정의
- 성공 기준 정의
- 제약 조건 정리
- 관련 코드와 문서 조사

### 2. Planning

- PRD 작성
- acceptance criteria 작성
- ADR 필요 여부 판단
- 작업 분할

### 3. Execution

- 파일 범위와 책임을 나눈다.
- 작은 단위로 구현한다.
- 테스트를 함께 보강한다.

### 4. Review

- Reviewer가 버그, 회귀, 누락 테스트를 검토한다.
- Security / Release가 고위험 항목을 확인한다.

### 5. Verification

- QA가 acceptance criteria 기준으로 검증한다.
- UI는 시각 검증을 추가한다.
- 운영 지표가 있으면 observability까지 확인한다.

### 6. Release Gate

- 사람이 최종 승인한다.
- 배포 후 모니터링 기준을 확인한다.

## 최소 시작 구성

리소스가 제한된 경우, 아래 5역할부터 시작해도 된다.

- Planner
- Architect
- Executor
- Reviewer
- QA

하지만 가능한 빠르게 아래 두 역할은 추가하는 것이 좋다.

- Explorer / Researcher
- Harness / Eval Owner

## 추천 운영 체크리스트

- 요구사항 문서가 있는가
- acceptance criteria가 테스트 가능한가
- 누가 쓰기 권한을 가졌는가
- 리뷰어와 QA가 작성자와 분리되어 있는가
- 배포 전 자동 게이트가 있는가
- 실패 사례가 eval 세트로 축적되고 있는가
- 고위험 작업에 human gate가 있는가

## 현재 워크스페이스에 대한 제안

이 워크스페이스에서는 아래 순서로 시작하는 것을 권장한다.

1. `AI_TEAM/docs` 아래에 역할 정의와 운영 규칙을 먼저 문서화한다.
2. `plan` 기반으로 각 프로젝트의 acceptance criteria를 표준화한다.
3. `team` 기반으로 역할 분리된 실행 흐름을 만든다.
4. `code-review`, `security-review`, `QA`를 통과한 결과만 병합 후보로 올린다.
5. 반복 실패를 `trace`와 eval 형태로 축적한다.

## 한 줄 요약

AI 팀은 사람 조직도를 복제하는 방식보다, `역할 분리 + 권한 제한 + 검증 하네스 + 사람 승인 게이트`를 중심으로 설계할 때 가장 안정적으로 운영된다.

## 관련 문서

- [AI Team Skill Stack](./ai-team-skill-stack.md)
  추천 Skill 전체 구성, 역할별 매핑, 기본 운영 흐름
- [AI Team GitHub PR Policy](./ai-team-github-pr-policy.md)
  GitHub 브랜치, PR, 리뷰, 승인, 머지 운영 정책
- [Branch Review Checklist](./branch-review-checklist.md)
  브랜치 규칙, 리뷰 기준, 머지 전 점검 목록
