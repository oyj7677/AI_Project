---
title: AI Team Home
type: home
status: active
created: 2026-03-30
updated: 2026-03-30
---

# AI Team Home

## 목적

이 공간은 AI 팀의 PRD, ADR, 결정 로그, 운영 문서를 모아두는 문서 허브다.

## 폴더 구조

- `00_HOME`
- `10_PRD`
- `20_ADR`
- `30_DECISIONS`
- `40_TEMPLATES`
- `50_PROJECTS`
- `99_ARCHIVE`
- `docs`

## 시작 순서

1. 새 프로젝트나 기능이 생기면 `10_PRD`에서 PRD를 만든다.
2. 구조적 결정이 필요하면 `20_ADR`에서 ADR을 만든다.
3. 구현 중 생기는 작은 판단은 `30_DECISIONS`에 기록한다.
4. 운영 기준이나 팀 규칙은 `docs`에 남긴다.

## 기본 링크

- [팀 블루프린트](../docs/ai-team-blueprint.md)
- [스킬 구성](../docs/ai-team-skill-stack.md)
- [GitHub PR 정책](../docs/ai-team-github-pr-policy.md)
- [GitHub 연동 설정](../docs/github-integration-setup.md)
- [팀 구성 진행 현황](../docs/team-setup-progress.md)
- [브랜치/리뷰 체크리스트](../docs/branch-review-checklist.md)
- [PRD 템플릿](../40_TEMPLATES/PRD_TEMPLATE.md)
- [ADR 템플릿](../40_TEMPLATES/ADR_TEMPLATE.md)
- [결정 로그 템플릿](../40_TEMPLATES/DECISION_LOG_TEMPLATE.md)
- [CODEOWNERS 초안](../.github/CODEOWNERS)
- [PR 템플릿](../.github/pull_request_template.md)
- [실사용 MCP 설정](../.vscode/mcp.json)
- [MCP 설정 예시](../.vscode/mcp.json.example)
- [환경 변수 예시](../.env.example)
- [PR 자동화 예시 Workflow](../.github/workflows/pr-automation-example.yml)
- [gh wrapper](../scripts/ghx.sh)

## 주의

이 문서들은 Markdown 문법과 `.md` 확장자로 작성되어 Obsidian에서 그대로 사용할 수 있다.
