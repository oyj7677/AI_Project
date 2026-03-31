# Strict Rules Validation

작성일: 2026-03-31

이 문서는 `required status checks`, `require_code_owner_reviews`, `enforce_admins=true` 상태에서
PR 기반 변경이 어떻게 동작하는지 검증하기 위한 테스트 변경이다.

## Validation Scope

- `validate` 상태 체크 필수
- code owner review 필수
- 관리자 직접 푸시 우회 차단
