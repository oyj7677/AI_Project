# Team Setup Progress

작성일: 2026-03-30

## 목적

이 문서는 AI_TEAM 구성이 현재 어디까지 진행되었는지와, 남은 작업이 무엇인지 정리한다.

## 전체 상태

- 현재 상태: `기반 구조 정리 완료, GitHub 연동 1차 완료, 팀 운영 준비 단계`
- 체감 진행률: `약 70%`

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

### GitHub 인증 / 도구 연동

- `GH_TOKEN` 기반 `gh auth status` 확인 완료
- Codex global MCP에 `github-review` 등록 완료
- Codex global MCP에 `github-release` 등록 완료
- 두 MCP 모두 remote GitHub MCP URL과 bearer token env var 기준으로 등록 완료

## 부분 완료 항목

### GitHub MCP 런타임 검증

- 설정 등록은 완료되었다.
- 다만 현재 세션에서 GitHub MCP 도구 호출까지 직접 검증한 상태는 아니다.
- Codex CLI 설정상으로는 활성화되어 있으며, 다음 Codex 세션 또는 MCP 재로딩 이후 실제 사용 검증이 필요하다.

### CODEOWNERS 실사용화

- 파일은 생성되었지만 placeholder가 남아 있다.
- 실제 GitHub 사용자명 또는 팀명으로 교체해야 한다.

### PR / 리뷰 규칙 강제

- 문서와 템플릿은 준비되었다.
- GitHub 저장소의 branch protection / ruleset / required review 설정은 저장소 UI에서 실제 적용해야 한다.

## 확인된 제약

- 이 머신에는 Docker가 설치되어 있지 않다.
- 따라서 로컬 Docker 기반 GitHub MCP 방식은 현재 경로에서 제외했다.
- 대신 remote GitHub MCP + Codex global MCP 등록 방식으로 전환했다.

## 남은 핵심 작업

1. 실제 GitHub 저장소에 `CODEOWNERS` 반영
2. Branch protection / rulesets 적용
3. Required reviews / code owner reviews / status checks 활성화
4. GitHub Actions workflow permissions 설정
5. 실제 저장소 기준으로 PR 생성 -> 리뷰 코멘트 -> 상태 확인 -> 머지 흐름 검증
6. 팀 역할별 GitHub 계정 또는 승인 전략 확정

## 권장 다음 단계

### 바로 다음

- 실제 GitHub 사용자명과 팀명을 확정해 `CODEOWNERS` 채우기
- 저장소에서 branch protection 설정하기

### 그 다음

- 테스트용 브랜치로 PR 하나 생성
- `gh`, Codex GitHub MCP, Actions 예시를 각각 한 번씩 검증

### 이후

- Reviewer / QA / Release 역할별 운영 플레이북 문서화
- 실제 팀 운영 루틴에 맞게 자동화 범위 조정

## 관련 문서

- [AI Team Home](../00_HOME/AI_TEAM_HOME.md)
- [AI Team Blueprint](./ai-team-blueprint.md)
- [AI Team Skill Stack](./ai-team-skill-stack.md)
- [AI Team GitHub PR Policy](./ai-team-github-pr-policy.md)
- [GitHub Integration Setup](./github-integration-setup.md)
