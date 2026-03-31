# AI Review Automation

작성일: 2026-03-31

## 목적

이 문서는 Copilot PR 리뷰를 intake하고, 필요한 경우 수정 작업으로 연결하고, 안전한 조건에서만 자동 머지하는 흐름을 정의한다.

## 구성 요소

### 1. Review Intake

- 에이전트: `.github/agents/review-intake.agent.md`
- 워크플로: `.github/workflows/review-intake.yml`
- 스크립트: `scripts/pr_review_automation.py intake`

역할:

- Copilot 리뷰를 수집한다.
- actionable / advisory_low_confidence / manual_review로 분류한다.
- PR에 `Review Intake` 코멘트를 갱신한다.
- `ai-review-actionable`, `ai-review-manual` 라벨을 동기화한다.

### 2. Fix Review Comments

- 에이전트: `.github/agents/fix-review-comments.agent.md`
- 워크플로: `.github/workflows/request-ai-fix.yml`
- 스크립트: `scripts/pr_review_automation.py fix-prompt`

역할:

- 최신 `Review Intake` 코멘트에서 actionable task만 읽는다.
- Copilot 또는 다른 coding agent에게 넘길 수 있는 구조화된 수정 프롬프트를 만든다.
- `/ai-fix-review` 코멘트가 올라오면 PR에 `@copilot` 수정 요청 코멘트를 남긴다.
- GitHub가 bot-authored `@copilot` 코멘트를 반응 대상으로 보지 않으면, maintainer가 같은 프롬프트를 수동으로 다시 남기면 된다.

### 3. Safe Auto Merge

- 워크플로: `.github/workflows/safe-auto-merge.yml`
- 스크립트: `scripts/pr_review_automation.py merge-check`

역할:

- 최신 intake 결과와 체크 상태를 평가한다.
- `safe-auto-merge` 라벨이 있을 때만 자동 머지를 고려한다.
- 조건을 만족하지 못하면 `safe-auto-merge-blocked` 라벨과 상태 코멘트로 이유를 남긴다.

## Safe Auto Merge 조건

아래를 모두 만족해야 한다.

- PR이 draft가 아니다.
- `mergeStateStatus=CLEAN`
- required check가 모두 성공했다.
- 최신 head commit 기준 `Review Intake` 결과가 stale이 아니다.
- 최신 head commit 기준 Copilot review가 존재한다.
- `ai-review-actionable` 라벨이 없다.
- high-risk path가 없다.
- changed files가 low-risk allowlist 안에만 있다.
- 파일 수가 10개 이하이다.
- 총 diff 크기가 400 line 이하이다.
- `safe-auto-merge` 라벨이 수동으로 추가되어 있다.

### Follow-up 예외

아래 조건을 모두 만족하면 current head에 fresh Copilot review가 없어도 자동 머지를 허용할 수 있다.

- PR이 docs-only low-risk 변경이다.
- high-risk path가 없다.
- 최신 `Review Intake` 기준 actionable task가 없다.
- 이전 Copilot actionable comment는 stale thread로 남아 있다.
- 최신 head commit이 그 Copilot actionable 이후에 들어온 사람 커밋이다.

이 예외는 "직전 Copilot actionable을 사람이 문서 PR에서 바로 반영한 후, GitHub가 새 head에 re-review를 다시 생성하지 않는 경우"를 위한 좁은 안전장치다.

## High-Risk Path

다음 경로가 포함되면 자동 머지를 막는다.

- `.github/workflows/**`
- `.github/actions/**`
- `.github/CODEOWNERS`
- `.github/copilot-instructions.md`
- `.github/agents/**`
- `scripts/**`
- `.env*`
- `AI_TEAM/.env*`

## 중요한 GitHub 제한

- Copilot coding agent가 PR에 푸시한 뒤에는 GitHub Actions가 자동 실행되지 않을 수 있다.
- 이 경우 maintainer가 GitHub UI에서 workflow 실행을 승인해야 다시 체크가 돈다.
- 따라서 `Fix Review Comments -> re-review -> safe auto merge` 루프는 완전 무인이라기보다 `maintainer opt-in + 안전 게이트` 모델로 보는 것이 맞다.

## 권장 사용 흐름

1. PR 생성
2. Copilot review
3. `review-intake`가 task와 라벨 생성
4. maintainer가 `/ai-fix-review` 코멘트
5. Copilot 또는 coding agent가 수정 후 푸시
6. 필요하면 workflow 실행 승인
7. `review-intake` 재평가
8. low-risk PR이면 `safe-auto-merge` 라벨 추가
9. `safe-auto-merge`가 조건 충족 시 자동 머지
