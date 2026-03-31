---
title: AI Team GitHub PR Policy
type: policy
status: active
created: 2026-03-30
updated: 2026-03-30
---

# AI Team GitHub PR Policy

## 목적

이 문서는 AI 팀이 GitHub 기반으로 브랜치, PR, 리뷰, 승인, 머지를 운영하는 기준을 정의한다.
핵심 목표는 `속도`보다 `권한 분리`, `검증 가능성`, `실수 방지`를 우선하는 것이다.

## 핵심 원칙

- 작성자와 리뷰어를 분리한다.
- 작성자와 머지 권한을 분리한다.
- QA와 보안 검토는 작성자 승인으로 대체하지 않는다.
- 최종 머지는 사람 또는 릴리즈 전용 역할만 수행한다.
- PR은 문서 근거 없이 열지 않는다.

## 역할과 권한

| 역할 | 권장 권한 | 금지 권한 |
| --- | --- | --- |
| Executor / Developer | 브랜치 작업, 커밋, 테스트 | 직접 머지 |
| Reviewer | PR 리뷰, 코멘트, 승인 의견 | 코드 작성 브랜치에 직접 푸시 |
| QA / Verifier | 테스트 결과 확인, 검증 의견 | 직접 머지 |
| Security / Release Reviewer | 보안 검토, 위험도 평가 | 개발 브랜치 직접 수정 |
| Release Agent | PR 생성, 머지 준비, 병합 처리 | 구현 담당 |
| Human Approver | 최종 승인, 예외 승인, 머지 판단 | 없음 |

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

- Executor가 직접 PR을 열게 할 수도 있지만, 하네스 관점에서는 `Release Agent` 또는 `Leader Agent`가 PR을 여는 편이 더 안전하다.
- 이유는 구현 권한과 외부 시스템 쓰기 권한을 분리할 수 있기 때문이다.

### 4. Review

- Reviewer가 버그, 회귀, 테스트 누락을 본다.
- Security 역할이 권한, 시크릿, 위험 변경을 본다.
- QA 역할이 acceptance criteria 충족 여부를 본다.

### 5. Merge Gate

- CI 통과
- required reviews 충족
- code owner review 충족
- 최신 푸시에 대한 재승인 조건 충족
- 사람 또는 릴리즈 역할 승인

### 6. Merge

- 머지는 사람 또는 Release Agent만 수행한다.

## GitHub 설정 권장안

### 필수

- Branch protection 또는 ruleset 활성화
- Required status checks 활성화
- Required pull request reviews 활성화
- Dismiss stale approvals 활성화
- Require review from code owners 활성화

### 권장

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

- Executor: GitHub MCP 없음 또는 읽기 제한
- Reviewer / QA / Security: read-only GitHub MCP
- Release Agent: 쓰기 가능한 GitHub MCP, 단 PR 관련 작업만 허용
- Human Approver: 최종 승인

이 방식이 좋은 이유:

- 구현자가 외부 시스템에 직접 쓰지 않아도 된다.
- 리뷰 에이전트는 안전하게 코멘트만 남길 수 있다.
- 머지 권한이 좁은 역할에만 집중된다.

## 중요한 운영 메모

실무적으로는 여러 에이전트가 같은 GitHub 계정이나 같은 토큰을 공유하면 GitHub 입장에서는 사실상 하나의 리뷰 정체성으로 취급된다.
따라서 "역할 수만큼 독립 승인"을 기대하기보다는, 에이전트 리뷰는 보조 신호로 보고 최종 승인 게이트는 사람 또는 분리된 승인 정체성으로 두는 것이 안전하다.

## 권장 운영 토폴로지

### 최소 운영형

- Executor 1명
- Reviewer 1명
- QA 1명
- Human Approver 1명

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
```

## 한 줄 요약

GitHub 기반 AI 팀 운영에서는 `작성`, `리뷰`, `승인`, `머지` 권한을 분리하고, GitHub MCP는 주로 `리뷰/PR/머지 자동화` 레이어에만 제한적으로 붙이는 것이 가장 안전하다.
