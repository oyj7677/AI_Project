# Strict Rules Validation

작성일: 2026-03-31

이 문서는 `required_status_checks`, `require_code_owner_reviews`, `enforce_admins` 상태에서
PR 기반 변경이 어떻게 동작하는지 검증하기 위한 테스트 변경이다.

## Validation Scope

- Required status check: `validate`
- Require review from Code Owners
- Enforce admins

## Verified Result

- 대상 PR: `#2`
- `validate` 체크 성공 확인
- PR 상태: `BLOCKED`
- 리뷰 결정 상태: `REVIEW_REQUIRED`

## GitHub MCP Runtime Validation

- 현재 Codex 세션에서 GitHub MCP로 PR 메타데이터 조회 성공
- 현재 Codex 세션에서 GitHub MCP로 branch protection 상태 조회 성공
- 현재 Codex 세션에서 GitHub MCP로 PR `#2`에 `COMMENT` 리뷰 제출 성공
- 현재 Codex 세션에서 GitHub MCP로 PR `#2` `APPROVE` 시도 시 GitHub API `422` 응답 확인
- 실패 메시지: `Review Can not approve your own pull request`

## Current Constraint

- 현재 인증 주체는 저장소 사용자 `oyj7677`이며, PR 작성자도 동일하다.
- 따라서 GitHub MCP 경로 자체는 정상 동작하지만, 동일 identity로는 required code owner approval을 충족할 수 없다.
- 개인 계정 저장소에서는 별도 collaborator 계정을 추가하는 것이 가장 바로 적용 가능한 다음 단계다.
- team 기반 code owner 구성을 쓰려면 저장소를 organization 소유로 옮기고 visible team을 사용해야 한다.

## Outcome And Policy Change

- 위 검증으로 엄격 규칙 자체는 정상 동작함을 확인했다.
- 다만 현재 저장소는 1인 개발 체제이므로, 2026-03-31에 `main` 보호 규칙을 solo-review 모드로 전환했다.
- 현재 기본 게이트는 `PR + validate + Copilot AI review + self-review 기록`이다.
