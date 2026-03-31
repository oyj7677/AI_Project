# AI Review Smoke Test

작성일: 2026-03-31

이 문서는 `review-intake`, `/ai-fix-review`, `safe-auto-merge` 흐름을
실제 저장소에서 검증하기 위한 저위험 smoke test 산출물이다.

## Purpose

- Copilot review 자동 실행 확인
- review-intake 코멘트 생성 확인
- `/ai-fix-review` 코멘트 트리거 확인
- safe-auto-merge 라벨 기반 판단 확인

## Notes

- 이 변경은 문서만 수정하는 low-risk PR 시나리오를 의도한다.
- high-risk 경로인 `.github/**`, `scripts/**`, 권한 설정은 포함하지 않는다.
- auto-merge는 opt-in 라벨이 있어야만 시도된다.
