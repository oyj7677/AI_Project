# GitHub Integration Setup

작성일: 2026-03-30

## 목적

이 문서는 AI_TEAM이 아래 경로를 모두 사용할 수 있도록 GitHub 연동 구조를 정리한다.

- Codex
- GitHub MCP
- `gh` CLI
- GitHub Actions 기반 자동화

목표 기능:

- PR 생성
- 리뷰 코멘트 작성
- 승인 상태 확인
- 머지

## 핵심 결론

- `gh`와 GitHub Actions는 GitHub 인증이 필요하다.
- GitHub MCP도 일반적으로 인증 토큰이 필요하다.
- 가장 안전한 구성은 `읽기 전용 토큰`과 `쓰기 가능 토큰`을 분리하는 것이다.
- 구현 에이전트, 리뷰 에이전트, 릴리즈 에이전트의 권한을 분리해야 한다.

## 권장 연동 구조

### 1. Codex

- 로컬 작업
- 코드 수정
- 로컬 리뷰
- 필요 시 GitHub connector 또는 GitHub MCP를 통해 PR 관련 작업 수행

### 2. GitHub MCP

- PR 생성
- PR 조회
- 리뷰 코멘트
- 승인 상태 조회
- 머지

권장 역할:

- Reviewer / QA / Security: 읽기 전용 또는 코멘트 중심
- Release Agent: 쓰기 가능한 권한

### 3. `gh` CLI

- 로컬 터미널에서 PR 생성
- 리뷰 코멘트
- PR 상태 확인
- 머지

### 4. GitHub Actions

- 자동 리뷰 코멘트
- 승인 상태 확인
- 조건 충족 시 자동 머지
- 수동 트리거 또는 정책 기반 자동화

## 인증 방식

### Codex

이 Codex 환경에서 GitHub connector가 이미 연결되어 있으면 별도 저장소 파일 없이 사용할 수 있다.
다만 별도 호스트 앱이나 MCP 서버를 붙일 때는 별도 인증이 필요할 수 있다.

### GitHub MCP

공식 GitHub MCP 서버 예시는 `GitHub Personal Access Token`을 입력받아 `Authorization: Bearer ...` 형태로 사용한다.
실무적으로는 PAT 또는 동등한 GitHub 인증 자격 증명이 필요하다고 보는 것이 맞다.

### `gh` CLI

로컬 환경에서는 보통 아래 둘 중 하나를 사용한다.

- `gh auth login --web`
- `GH_TOKEN` 또는 토큰 입력 기반 로그인

### GitHub Actions

같은 저장소 안에서 돌아가는 자동화는 보통 기본 제공 `GITHUB_TOKEN`으로 시작할 수 있다.
다만 PR 승인과 같은 동작은 별도 설정과 권한이 필요하다.

## 권장 토큰 전략

### 1. Review Token

용도:

- PR 읽기
- 코멘트
- 상태 확인

권장 사용처:

- Reviewer Agent
- QA Agent
- Security Agent
- 읽기 중심 MCP

### 2. Release Token

용도:

- PR 생성
- PR 업데이트
- 머지

권장 사용처:

- Release Agent
- Leader Agent
- 머지 자동화

### 3. `gh` Token

용도:

- 로컬 `gh` CLI 작업

권장 사용처:

- 사람 운영자
- 로컬 릴리즈 작업

## 최소 권한 원칙

- 읽기 전용 역할은 쓰기 토큰을 쓰지 않는다.
- 구현자는 직접 머지 권한을 가지지 않는다.
- 자동 머지 권한은 Release Agent 또는 GitHub Actions에만 준다.
- 토큰은 역할별로 나눈다.
- 토큰은 저장소에 커밋하지 않는다.

## 필요한 GitHub 설정

### Branch Protection / Rulesets

- `main` 직접 푸시 금지
- required status checks 활성화
- solo 모드면 required approving reviews = `0`
- solo 모드면 code owner review = `off`
- solo 모드면 Copilot code review ruleset 활성화
- 팀 모드로 확장할 때 required pull request reviews 활성화
- 팀 모드로 확장할 때 stale approvals 무효화 활성화
- 팀 모드로 확장할 때 latest push 재승인 요구 활성화 권장

### Actions 설정

- workflow permissions를 목적에 맞게 조정
- 필요한 경우 `GITHUB_TOKEN`에 write 권한 허용
- 필요한 경우 GitHub Actions가 PR을 승인할 수 있도록 설정

주의:

- GitHub 문서는 `can_approve_pull_request_reviews=true` 설정이 가능하다고 안내하지만, 동시에 보안 위험이 있다고 명시한다.
- 따라서 자동 승인은 제한된 저장소나 릴리즈 전용 경로에서만 쓰는 것이 안전하다.

## 로컬 설정 파일

이 저장소에는 아래 설정 파일을 추가해 두었다.

- [실사용 MCP 설정](../.vscode/mcp.json)
- [MCP 설정 예시](../.vscode/mcp.json.example)
- [환경 변수 예시](../.env.example)
- [GitHub Actions PR 자동화 예시](../.github/workflows/pr-automation-example.yml)
- [gitignore](../.gitignore)
- [gh wrapper](../scripts/ghx.sh)

## 권장 초기 적용 순서

1. `.env.example`을 참고해 실제 `.env` 또는 환경 변수 준비
2. `.vscode/mcp.json` 또는 Codex global MCP 설정에 `github-review`, `github-release`를 remote GitHub MCP로 연결
3. `GH_TOKEN`을 쓸 경우 `scripts/ghx.sh`로 로컬 `gh` 실행
4. GitHub 저장소에서 branch protection 설정
5. GitHub Actions workflow permissions 설정
6. 예시 workflow를 기반으로 PR 자동화 동작 검증

## 현재 설정 동작 방식

- `github-review` MCP 서버는 remote GitHub MCP와 `GITHUB_MCP_REVIEW_TOKEN`을 사용한다.
- `github-release` MCP 서버는 remote GitHub MCP와 `GITHUB_MCP_RELEASE_TOKEN`을 사용한다.
- 로컬 `gh`는 `GH_TOKEN`을 사용하려면 `scripts/ghx.sh`를 통해 실행한다.
- Codex global MCP 설정은 `~/.codex/config.toml`에 등록된다.

## 현재 확인 결과

- `gh auth status`는 `GH_TOKEN` 기준 로그인 상태를 확인했다.
- Codex에는 `github-review`, `github-release` MCP 서버가 global 설정으로 등록되었다.
- 이 머신에는 Docker가 없어서 로컬 Docker 기반 GitHub MCP 방식은 사용하지 않는다.

## 운영 권장안

### 가장 안전한 시작점

- `gh`는 사람 운영자가 사용
- GitHub MCP는 읽기 또는 리뷰 코멘트 중심
- 실제 머지는 사람 또는 Release Agent만 수행
- GitHub Actions는 상태 확인과 자동 코멘트부터 시작

### 그 다음 단계

- Release Agent에만 쓰기 가능한 GitHub MCP 부여
- GitHub Actions 자동 머지 도입
- 필요한 경우 bot approval 사용

## 관련 문서

- [AI Team GitHub PR Policy](./ai-team-github-pr-policy.md)
- [Branch Review Checklist](./branch-review-checklist.md)
- [Team Setup Progress](./team-setup-progress.md)
- [CODEOWNERS Draft](../.github/CODEOWNERS)
- [PR Template](../.github/pull_request_template.md)
