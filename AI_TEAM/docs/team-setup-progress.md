# Team Setup Progress

작성일: 2026-03-30

## 목적

이 문서는 AI_TEAM 구성이 현재 어디까지 진행되었는지와, 남은 작업이 무엇인지 정리한다.

## 전체 상태

- 현재 상태: `기반 구조 정리 완료, GitHub 보호 규칙 적용 완료, 테스트 PR 검증 완료`
- 체감 진행률: `약 90%`

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

## 부분 완료 항목

### GitHub MCP 런타임 검증

- 설정 등록은 완료되었다.
- 다만 현재 세션에서 GitHub MCP 도구 호출 자체를 직접 사용해 PR 작업을 검증한 상태는 아니다.
- Codex CLI 설정상으로는 활성화되어 있으며, 다음 Codex 세션 또는 MCP 재로딩 이후 실제 사용 검증이 필요하다.

### 엄격 규칙 검증의 현재 한계

- `require_code_owner_reviews = true` 상태에서 `oyj7677`가 만든 PR은 같은 `oyj7677`가 승인할 수 없다.
- GitHub는 자기 PR self-approval을 허용하지 않음을 실제로 확인했다.
- 따라서 현재처럼 단일 GitHub 사용자만 있는 구성에서는 엄격 규칙 하에서 end-to-end 머지 검증이 완료되지 않는다.
- 이 검증을 끝까지 완료하려면 아래 중 하나가 추가로 필요하다.
  - 별도의 리뷰어 계정
  - 별도의 code owner 팀
  - GitHub App 또는 별도 bot identity

### CODEOWNERS 실사용화

- 저장소 루트 `.github/CODEOWNERS`에는 실제 사용자 `@oyj7677`가 반영되었다.
- `AI_TEAM/.github/CODEOWNERS`는 참고용 초안으로 남아 있다.

### PR / 리뷰 규칙 강제

- 문서와 템플릿은 준비되었다.
- GitHub 저장소의 branch protection / required review 설정은 실제 적용까지 완료되었다.
- 필요 시 이후 `required status checks`를 추가로 강화할 수 있다.

## 확인된 제약

- 이 머신에는 Docker가 설치되어 있지 않다.
- 따라서 로컬 Docker 기반 GitHub MCP 방식은 현재 경로에서 제외했다.
- 대신 remote GitHub MCP + Codex global MCP 등록 방식으로 전환했다.

## 남은 핵심 작업

1. `required status checks`를 branch protection에 연결할지 결정
2. 단일 사용자 환경에서 `require_code_owner_reviews=true`를 유지할지 결정
3. 팀 역할별 GitHub 계정 또는 승인 전략 확정
4. GitHub MCP를 사용한 실제 PR 작업도 별도로 검증
5. bot 또는 별도 리뷰어 identity를 이용한 엄격 규칙 최종 머지 검증

## 권장 다음 단계

### 바로 다음

- 별도 리뷰어 계정 또는 GitHub team 준비
- `CODEOWNERS`를 실제 다중 리뷰 구조에 맞게 확장

### 그 다음

- `Codex GitHub MCP`로도 PR 조회/리뷰/머지 동작 검증
- 리뷰용 봇 계정 또는 GitHub App 분리
- 엄격 규칙 하에서 PR 생성 -> code owner review -> 머지 재검증

### 이후

- Reviewer / QA / Release 역할별 운영 플레이북 문서화
- 실제 팀 운영 루틴에 맞게 자동화 범위 조정

## 관련 문서

- [AI Team Home](../00_HOME/AI_TEAM_HOME.md)
- [AI Team Blueprint](./ai-team-blueprint.md)
- [AI Team Skill Stack](./ai-team-skill-stack.md)
- [AI Team GitHub PR Policy](./ai-team-github-pr-policy.md)
- [GitHub Integration Setup](./github-integration-setup.md)
