# Team Setup Progress

작성일: 2026-03-30

## 목적

이 문서는 AI_TEAM 구성이 현재 어디까지 진행되었는지와, 남은 작업이 무엇인지 정리한다.

## 전체 상태

- 현재 상태: `1인 개발용 GitHub 규칙 전환 완료, Copilot AI reviewer 적용 진행 중`
- 체감 진행률: `약 97%`

## 완료된 항목

### 문서 구조

- AI 팀 운영 문서 구조 생성
- Obsidian 친화적인 `.md` 문서 체계로 통일
- PRD / ADR / Decision 템플릿 생성
- 팀 홈 문서 생성

### 팀 운영 문서

- 팀 블루프린트 작성
- Skill 구성 문서 작성
- GitHub PR 운영 정책 작성
- 브랜치 / 리뷰 체크리스트 작성

### GitHub 협업 기본 자산

- `CODEOWNERS` 초안 생성
- `pull_request_template.md` 생성
- GitHub Actions PR 자동화 예시 생성
- 저장소 루트 `.github/CODEOWNERS` 적용
- 저장소 루트 `pull_request_template.md` 적용

### GitHub 인증 / 도구 연동

- `GH_TOKEN` 기반 `gh auth status` 확인 완료
- Codex global MCP에 `github-review` 등록 완료
- Codex global MCP에 `github-release` 등록 완료
- 두 MCP 모두 remote GitHub MCP URL과 bearer token env var 기준으로 등록 완료

### 실제 GitHub 저장소 검증

- 대상 저장소: `oyj7677/AI_Project`
- `main` 브랜치 초기 커밋 및 푸시 완료
- GitHub Actions workflow permissions를 `write`로 조정 완료
- GitHub Actions PR 승인 허용 활성화 완료
- `main` 브랜치 보호 규칙 적용 완료
- Required approving reviews = `1` 적용 완료
- 테스트 PR `#1` 생성 완료
- GitHub Actions 자동 승인 리뷰 확인 완료
- PR 상태 확인 완료
- PR `#1` 머지 완료
- `required status checks = validate` 강제 완료
- `require_code_owner_reviews = true` 적용 완료
- `enforce_admins = true` 적용 완료
- 엄격 규칙 검증용 PR `#2` 생성 완료
- PR `#2`는 승인 전 `BLOCKED` 상태임을 확인
- `validate` 상태 체크 성공 후에도 self-approval 불가로 인해 머지 불가 상태임을 확인
- 엄격 규칙 검증 결과를 바탕으로 1인 개발용 보호 규칙 재설계 완료
- `required_approving_review_count = 0` 재구성 완료
- `require_code_owner_reviews = false` 재구성 완료
- `solo-ai-review` Copilot ruleset 생성 완료
- `.github/copilot-instructions.md` 추가 완료
- `.github/agents/solo-reviewer.agent.md` 추가 완료

## 부분 완료 항목

### GitHub MCP 런타임 검증

- 설정 등록은 완료되었다.
- 현재 세션에서 GitHub MCP로 PR `#2` 조회를 직접 검증했다.
- 현재 세션에서 GitHub MCP로 PR `#2` `COMMENT` 리뷰 제출을 직접 검증했다.
- 현재 세션에서 GitHub MCP로 PR `#2` `APPROVE` 시도 시 GitHub API `422` 응답을 직접 확인했다.
- 즉, MCP 경로는 정상 동작하며 현재 blocker는 MCP 미동작이 아니라 self-approval 제한이다.
- 현재 세션에 노출된 GitHub MCP 액션에는 direct merge 호출이 없으므로, 최종 머지는 `gh` 또는 GitHub UI, 혹은 `enable_auto_merge` 경로로 검증해야 한다.

### 1인 개발 리뷰 모드

- 현재 `main` 브랜치는 PR 기반 흐름과 `validate` 체크를 유지한다.
- 사람 approval count와 code owner approval은 강제하지 않는다.
- 대신 Copilot AI review ruleset과 self-review 기록을 기본 리뷰 게이트로 사용한다.
- 최종 머지는 저장소 owner가 AI 리뷰 결과를 확인한 뒤 수행한다.

### CODEOWNERS 실사용화

- 저장소 루트 `.github/CODEOWNERS`에는 실제 사용자 `@oyj7677`가 반영되었다.
- `AI_TEAM/.github/CODEOWNERS`는 참고용 초안으로 남아 있다.
- 현재 solo 모드에서는 ownership map 용도로 유지한다.
- 이후 multi-reviewer 체제로 전환하면 collaborator 또는 organization team 기준으로 다시 확장한다.

### PR / 리뷰 규칙 강제

- 문서와 템플릿은 준비되었다.
- GitHub 저장소의 branch protection / required review 설정은 실제 적용까지 완료되었다.
- 필요 시 이후 `required status checks`를 추가로 강화할 수 있다.

## 확인된 제약

- 이 머신에는 Docker가 설치되어 있지 않다.
- 따라서 로컬 Docker 기반 GitHub MCP 방식은 현재 경로에서 제외했다.
- 대신 remote GitHub MCP + Codex global MCP 등록 방식으로 전환했다.

## 남은 핵심 작업

1. PR `#2`에서 Copilot AI review 재실행 확인
2. 새 solo-review 규칙 하에서 PR 머지 검증
3. 필요 시 auto-merge 또는 merge queue 정책 추가 검토
4. multi-reviewer 체제로 확장할 시 CODEOWNERS와 review 규칙 재강화

## 권장 다음 단계

### 바로 다음

- PR `#2`에서 Copilot AI review가 최신 커밋 기준으로 다시 붙는지 확인
- `validate` 통과 후 solo-review 기준으로 머지 검증
- 운영 문서에 최종 검증 결과 반영

### 그 다음

- `Codex GitHub MCP`로 PR 조회와 리뷰 쓰기 검증 결과를 운영 문서에 반영 유지
- 필요 시 auto-merge 전략 또는 release agent 경로 추가
- 팀 확장 시 required reviews / code owner review 재적용

### 이후

- Reviewer / QA / Release 역할별 운영 플레이북 문서화
- 실제 팀 운영 루틴에 맞게 자동화 범위 조정

## 관련 문서

- [AI Team Home](../00_HOME/AI_TEAM_HOME.md)
- [AI Team Blueprint](./ai-team-blueprint.md)
- [AI Team Skill Stack](./ai-team-skill-stack.md)
- [AI Team GitHub PR Policy](./ai-team-github-pr-policy.md)
- [GitHub Integration Setup](./github-integration-setup.md)
