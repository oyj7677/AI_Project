# Branch Review Checklist

작성일: 2026-03-30

## 목적

이 문서는 브랜치 생성부터 PR 리뷰, 최종 머지 전까지 확인해야 할 기준을 정리한다.

## 브랜치 생성 체크리스트

- 브랜치명이 작업 목적을 드러내는가
- `main`에서 분기했는가
- 브랜치가 한 가지 주제만 담고 있는가
- 작업자가 명확한가

권장 규칙:

- `codex/<task-name>`
- `codex/<task-name>/<role-or-worker>`

예시:

- `codex/github-policy/executor-1`
- `codex/github-policy/reviewer-check`
- `codex/docs/qa-pass`

## PR 생성 전 체크리스트

- 관련 PRD가 있는가
- 구조 변경이 있으면 ADR이 있는가
- 관련 결정 로그가 연결되어 있는가
- 변경 범위가 PR 설명에 적혀 있는가
- 테스트 또는 검증 방법이 적혀 있는가
- 위험 요소와 롤백 가능성이 적혀 있는가

## 리뷰어 체크리스트

- 변경 의도가 PR 설명과 일치하는가
- 범위가 과도하게 넓지 않은가
- 회귀 가능성이 있는가
- 테스트가 충분한가
- 문서 변경이 필요한데 빠지지 않았는가
- 불필요한 리팩터링이 섞이지 않았는가

## 1인 개발 AI 리뷰 체크리스트

- Copilot AI review가 최신 푸시 기준으로 다시 실행되었는가
- AI 리뷰가 남긴 blocking 지적을 반영했는가
- 반영하지 않은 지적에는 PR 코멘트로 근거를 남겼는가
- 작성자가 스스로 확인한 항목이 `AI Review Handoff`에 적혀 있는가

## QA 체크리스트

- acceptance criteria를 충족하는가
- 재현 가능한 검증 절차가 있는가
- UI 변경이면 화면 검증이 되었는가
- 실패 케이스가 확인되었는가
- 배포 후 확인 포인트가 있는가

## 보안/릴리즈 체크리스트

- 권한 증가가 있는가
- 시크릿, 토큰, 환경 변수 노출 위험이 없는가
- 외부 API 비용 또는 rate limit 영향이 있는가
- 마이그레이션이나 파괴적 변경이 있는가
- 사람이 승인해야 할 항목이 구분되어 있는가

## 머지 전 체크리스트

- required checks가 모두 통과했는가
- Copilot AI review가 최신 푸시 기준으로 확인되었는가
- self-review 메모 또는 후속 조치 기록이 남아 있는가
- PR 설명과 문서 링크가 최신 상태인가
- 머지 후 후속 작업이 정리되어 있는가

## AI 팀 운영 체크리스트

- 리뷰 컨텍스트가 작성 컨텍스트와 분리되어 있는가, 또는 AI reviewer로 보완되었는가
- QA와 리뷰가 동일 컨텍스트에 잠기지 않았는가
- `main` 변경이 PR 없이 들어가지 않는가
- PR 생성 권한과 구현 권한의 기록이 PR에 남는가
- GitHub 연동 권한이 역할별로 제한되어 있는가

## 관련 문서

- [AI Team GitHub PR Policy](./ai-team-github-pr-policy.md)
- [AI Team Blueprint](./ai-team-blueprint.md)
- [AI Team Skill Stack](./ai-team-skill-stack.md)
