# AI_TEAM

AI 팀 운영 문서와 협업 자산을 모아 둔 문서 허브입니다.  
애플리케이션 서버를 실행하는 프로젝트가 아니라, PRD/ADR/결정 로그/운영 정책/도구 설정을 관리하는 작업 공간입니다.

## 무엇을 위한 폴더인가

- 새 프로젝트를 시작할 때 PRD, ADR, 결정 로그를 일관된 형식으로 남기기 위한 기준 저장소
- Codex, GitHub MCP, `gh` CLI, GitHub Actions를 함께 쓰기 위한 운영 문서와 예시 자산 저장소
- 1인 또는 소규모 AI 협업 흐름을 재현 가능한 형태로 남기기 위한 템플릿 모음

## 구조

```text
AI_TEAM/
├─ 00_HOME/        # 팀 홈 문서
├─ 10_PRD/         # 제품 요구사항 문서
├─ 20_ADR/         # 구조적 의사결정 기록
├─ 30_DECISIONS/   # 구현 중 의사결정 로그
├─ 40_TEMPLATES/   # PRD/ADR/Decision 템플릿
├─ 50_PROJECTS/    # 프로젝트 인덱스용 공간
├─ 99_ARCHIVE/     # 보관 문서
├─ docs/           # 운영 가이드, 정책, 셋업 문서
├─ scripts/        # 보조 스크립트
└─ .vscode/        # MCP 연결 예시 설정
```

## 시작 방법

### 1. 문서 허브 읽기 시작

가장 먼저 아래 문서를 여는 것을 권장합니다.

- `00_HOME/AI_TEAM_HOME.md`
- `docs/ai-team-blueprint.md`
- `docs/github-integration-setup.md`
- `docs/team-setup-progress.md`

### 2. 환경 변수 준비

GitHub 연동을 같이 쓸 계획이면 예시 파일을 복사해 환경 변수를 준비합니다.

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/AI_TEAM
cp .env.example .env
```

주요 변수:

- `GITHUB_MCP_REVIEW_TOKEN`
- `GITHUB_MCP_RELEASE_TOKEN`
- `GH_TOKEN`

### 3. MCP / 에디터 설정 확인

- VS Code 예시 설정: `.vscode/mcp.json.example`
- 실제 로컬 설정: `.vscode/mcp.json`
- GitHub 연동 가이드: `docs/github-integration-setup.md`

## "실행" 개념은 어떻게 보면 되나

이 프로젝트는 서버를 띄우는 방식이 아니라 아래처럼 사용합니다.

1. 문서를 열어 팀 규칙과 작업 흐름을 확인합니다.
2. 새 작업이 생기면 템플릿을 복사해 PRD/ADR/결정 로그를 작성합니다.
3. GitHub 협업이 필요하면 `.env`, MCP 설정, GitHub 규칙을 맞춘 뒤 운영합니다.

## 자주 보는 파일

- 팀 홈: `00_HOME/AI_TEAM_HOME.md`
- PRD 템플릿: `40_TEMPLATES/PRD_TEMPLATE.md`
- ADR 템플릿: `40_TEMPLATES/ADR_TEMPLATE.md`
- 결정 로그 템플릿: `40_TEMPLATES/DECISION_LOG_TEMPLATE.md`
- 팀 블루프린트: `docs/ai-team-blueprint.md`
- GitHub 연동: `docs/github-integration-setup.md`
- 진행 현황: `docs/team-setup-progress.md`

## 보조 스크립트

`scripts/ghx.sh`는 `GH_TOKEN`을 로드한 뒤 `gh` CLI를 실행하는 래퍼입니다.

```bash
cd /Users/oyj/Desktop/workspace/AI_Project/AI_TEAM
bash ./scripts/ghx.sh auth status
```

주의:

- 이 스크립트는 현재 `ROOT="/Users/oyj/Desktop/workspace/AI_TEAM"` 경로를 기준으로 작성돼 있습니다.
- 현재 저장소 위치가 `/Users/oyj/Desktop/workspace/AI_Project/AI_TEAM`이므로, 그대로 사용할 경우 경로를 먼저 확인하거나 수정하는 것이 안전합니다.

## 추천 사용 시나리오

### 새 프로젝트 시작

1. `10_PRD/`에 요구사항 문서 작성
2. 구조 결정이 필요하면 `20_ADR/` 작성
3. 구현 중 판단은 `30_DECISIONS/`에 기록
4. 운영 규칙은 `docs/`에 정리

### 기존 저장소에 협업 규칙 이식

1. `docs/ai-team-blueprint.md`로 역할 분리 원칙 확인
2. `.github/` 자산과 PR 템플릿 검토
3. GitHub/MCP 인증 전략을 `docs/github-integration-setup.md` 기준으로 적용

## 검증 포인트

- 앱 빌드나 테스트를 돌리는 저장소가 아니라 문서/설정 중심이라 별도 서버 테스트는 없습니다.
- 대신 `.env.example`, `.vscode/mcp.json.example`, 템플릿 문서, 운영 가이드를 같이 읽으며 실제 운영 루틴에 맞게 연결하는 용도로 쓰면 됩니다.
