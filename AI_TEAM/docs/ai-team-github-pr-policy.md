---
title: AI Team GitHub PR Policy
type: policy
status: active
created: 2026-03-30
updated: 2026-03-31
---

# AI Team GitHub PR Policy

## 목적

이 문서는 AI 팀이 GitHub 기반으로 브랜치, PR, 리뷰, 승인, 머지를 운영하는 기준을 정의한다.
현재 저장소의 기본 운영 모드는 `1인 개발 + AI reviewer 보조`이며, 핵심 목표는 `속도`보다 `검증 가능성`, `실수 방지`, `일관된 PR 기록`을 우선하는 것이다.

## 핵심 원칙

- `main` 변경은 PR을 통해서만 진행한다.
- 기계적 검증은 사람 판단보다 먼저 통과시킨다.
- 1인 개발에서는 사람 approval 수 대신 AI reviewer와 self-review 메모로 보완한다.
- 최종 머지는 저장소 owner가 AI 리뷰 결과와 리스크를 확인한 뒤 수행한다.
- PR은 문서 근거 없이 열지 않는다.

## 현재 운영 모드: Solo Developer

- 현재 저장소는 단일 GitHub 사용자 기준으로 운영한다.
- GitHub의 self-approval 제한 때문에 `required_approving_review_count=1`과 `require_code_owner_reviews=true`는 사용하지 않는다.
- 대신 머지 게이트를 `PR + validate 체크 + Copilot AI review + self-review 기록`으로 둔다.
- `.github/CODEOWNERS`는 현재 필수 승인 게이트가 아니라 ownership map으로 유지한다.

## 역할과 권한

| 역할 | 권장 권한 | 금지 권한 |
| --- | --- | --- |
| Solo Maintainer | 브랜치 작업, 커밋, 테스트, PR 작성, 최종 머지 | PR 없는 직접 푸시 |
| AI Reviewer (Copilot) | PR 리뷰, 회귀/위험/테스트 누락 지적, 머지 권고 | GitHub `required_approving_review_count`를 충족한 정식 human approval로 간주 |
| QA / Verifier | 테스트 결과 확인, 검증 의견 | 직접 `main` 푸시 |
| Security / Release Reviewer | 보안 검토, 위험도 평가 | 개발 브랜치 직접 수정 |
| Release Agent | PR 생성, 머지 준비, 병합 처리 | 구현 담당 |

## 권장 브랜치 전략

- 보호 브랜치: `main`
- 작업 브랜치: `codex/<task-name>`
- 서브 에이전트 분기: `codex/<task-name>/<role-or-worker>`

예시:

- `codex/ai-team-docs/executor-1`
- `codex/ai-team-docs/reviewer-check`
- `codex/github-policy/security-review`

원칙:

- 서브 에이전트마다 브랜치 또는 worktree를 분리한다.
- 한 PR에는 한 가지 주제만 담는다.
- 대형 작업은 여러 PR로 나눈다.

## PR 생성 기준

PR을 열기 전에 아래 조건을 만족해야 한다.

- 관련 PRD가 있다.
- 구조 변화가 있으면 관련 ADR이 있다.
- acceptance criteria가 정의되어 있다.
- 최소 테스트 또는 검증 근거가 있다.
- PR 설명에 변경 목적과 영향 범위가 적혀 있다.

## 권장 PR 흐름

### 1. Planning

- Planner가 PRD를 정리한다.
- Architect가 필요 시 ADR을 만든다.

### 2. Execution

- Executor 에이전트가 개별 브랜치 또는 worktree에서 구현한다.
- 구현 중 작은 판단은 결정 로그에 남긴다.

### 3. PR Opening

- 1인 개발에서는 작성자가 직접 PR을 열어도 된다.
- 다만 PR 템플릿의 `Validation`, `Risks`, `Review Notes`, `AI Review Handoff`를 반드시 채워 AI reviewer가 판단할 근거를 남긴다.

### 4. Review

- `solo-ai-review` ruleset이 Copilot 코드 리뷰를 자동 요청한다.
- `.github/agents/solo-reviewer.agent.md`는 더 깊은 후속 리뷰에 사용할 표준 reviewer profile이다.
- 작성자는 AI 리뷰가 지적한 blocking 이슈를 반영하거나, 왜 머지 가능한지 근거를 남긴다.

### 5. Merge Gate

- CI `validate` 통과
- PR 템플릿 필수 섹션 작성 완료
- 최신 푸시 기준 AI review 확인
- self-review 메모 또는 후속 조치 기록 완료
- 최종 머지 판단은 저장소 owner가 수행

### 6. Merge

- 머지는 저장소 owner 또는 Release Agent가 수행한다.
- low-risk 문서/설정 PR은 AI 리뷰 확인 후 바로 머지할 수 있다.

## GitHub 설정 권장안

### 1인 개발 기본값

- Branch protection 또는 ruleset 활성화
- Require a pull request before merging 유지
- Required status checks 활성화
- Required approving reviews = `0`
- Require review from code owners = `off`
- Enforce admins = `on`
- Copilot code review ruleset 활성화

### 팀 운영으로 확장할 때

- Required pull request reviews 활성화
- Dismiss stale approvals 활성화
- Require review from code owners 활성화
- Require approval for the most recent push 활성화
- Merge queue 사용 고려
- Force push 금지
- Direct push to `main` 금지

## GitHub MCP 사용 정책

GitHub MCP는 필수는 아니지만, 아래 작업을 자동화하려면 매우 유용하다.

- PR 생성
- 리뷰 코멘트 작성
- 승인 상태 확인
- 머지 실행

### 권장 권한 분리

- Solo Maintainer: GitHub MCP 읽기/쓰기 가능, 단 PR 경로 중심
- AI Reviewer: GitHub Copilot review 또는 read-only MCP
- QA / Security: read-only GitHub MCP
- Release Agent: 쓰기 가능한 GitHub MCP, 단 PR 관련 작업만 허용

이 방식이 좋은 이유:

- 머지 기록과 검증 근거가 PR에 남는다.
- AI reviewer는 안전하게 코멘트 중심으로 동작할 수 있다.
- 사람 approval을 억지로 요구하지 않아도 solo 흐름을 유지할 수 있다.

## AI Review Loop

- `review-intake`가 Copilot 리뷰를 actionable task로 변환한다.
- `fix-review-comments`는 actionable task만 수정 대상으로 삼는다.
- `safe-auto-merge`는 low-risk PR에 한해 opt-in 라벨 기반으로만 동작한다.
- docs-only low-risk follow-up PR에서는 직전 Copilot actionable을 사람이 반영한 경우 fresh re-review 없이도 예외적으로 자동 머지를 허용할 수 있다.
- `.github/workflows/**`, `scripts/**`, 권한/정책 파일 변경은 기본적으로 manual review로 남긴다.
- 이 흐름의 상세 조건은 [AI Review Automation](./ai-review-automation.md)에 정리한다.

## 중요한 운영 메모

실무적으로는 여러 에이전트가 같은 GitHub 계정이나 같은 토큰을 공유하면 GitHub 입장에서는 사실상 하나의 리뷰 정체성으로 취급된다.
따라서 solo 저장소에서는 approval count를 운영 게이트로 삼기보다, AI review 결과와 self-review 기록을 함께 남기는 편이 더 현실적이다.

## 권장 운영 토폴로지

### 1인 개발형

- Solo Maintainer 1명
- AI Reviewer 1명
- 필요 시 QA / Security reviewer

### 표준 운영형

- Executor 여러 명
- Reviewer 1명
- Security Reviewer 1명
- QA 1명
- Release Agent 1명
- Human Approver 1명

## 문서 연결 규칙

현재 문서는 `.md`로 통일되어 있다.
Obsidian에서는 `상대 경로 Markdown 링크`와 위키링크를 모두 사용할 수 있다.

예시:

- 블루프린트에서 정책 문서 연결:
  `[GitHub PR Policy](./ai-team-github-pr-policy.md)`
- 홈 문서에서 정책 문서 연결:
  `[GitHub PR Policy](../docs/ai-team-github-pr-policy.md)`

Obsidian 위키링크 예시:

- `[[ai-team-github-pr-policy]]`
- `[[branch-review-checklist]]`

## 관련 파일

- [Branch Review Checklist](./branch-review-checklist.md)
- [AI Review Automation](./ai-review-automation.md)
- [CODEOWNERS Draft](../.github/CODEOWNERS)
- [PR Template](../.github/pull_request_template.md)

## PR 설명 최소 템플릿

```md
## Summary
- 무엇을 바꿨는지

## Why
- 왜 필요한지

## Scope
- 포함 범위
- 제외 범위

## Validation
- 테스트
- 수동 확인

## Docs
- Related PRD:
- Related ADR:
- Related Decisions:

## AI Review Handoff
- Copilot reviewer가 집중해서 볼 리스크:
- 작성자가 이미 직접 확인한 항목:
- 머지 전 다시 확인할 항목:
```

## 한 줄 요약

1인 개발 저장소에서는 `PR`, `validate`, `Copilot AI review`, `self-review 기록`을 기본 게이트로 두고, 사람 approval 수는 강제하지 않는 구성이 가장 현실적이다.
