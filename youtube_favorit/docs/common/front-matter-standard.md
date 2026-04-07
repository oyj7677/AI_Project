# Front Matter Standard

`MyStarNow` 문서는 파일명 규칙과 별도로 front matter도 표준화한다.

목표:

- 문서 메타데이터를 일관되게 유지한다.
- 문서 간 연결을 쉽게 한다.
- 나중에 검색, 인덱싱, 자동화가 가능하게 한다.

# Core Rule

모든 문서는 가능하면 YAML front matter로 시작한다.

```yaml
---
id: <DOC-TYPE>-<SUBJECT>
type: <doc-type>
status: <status>
created: YYYY-MM-DD
updated: YYYY-MM-DD
---
```

기본 원칙:

- `id`는 파일명 규칙과 같은 naming policy를 따른다.
- `type`은 소문자 사용
- 날짜는 `YYYY-MM-DD`
- 문서 본문 내용이 수정되면 `updated`도 함께 갱신

# Global Field Rules

## Required Fields

모든 문서 공통 필수:

- `id`
- `type`
- `status`
- `created`
- `updated`

## Optional Fields

문서 종류에 따라 선택:

- `owner`
- `owners`
- `related_prd`
- `related_adrs`
- `related_decisions`
- `supersedes`
- `superseded_by`

## Allowed `type` Values

- `prd`
- `trd`
- `adr`
- `spec`
- `runbook`
- `roadmap`
- `glossary`

## Allowed `status` Values

기본 사용:

- `draft`
- `proposed`
- `active`
- `deprecated`
- `archived`

권장 기준:

- `draft`: 작성 중
- `proposed`: 제안 완료, 아직 확정 전
- `active`: 현재 기준 문서
- `deprecated`: 대체 문서가 있음
- `archived`: 기록 보관용

# Per-document Standard

## PRD Standard

권장 front matter:

```yaml
---
id: PRD-MYSTARNOW
type: prd
status: draft
owner:
created: 2026-04-04
updated: 2026-04-04
related_adrs:
  - ADR-MYSTARNOW-ARCHITECTURE
related_decisions: []
---
```

필드 설명:

- `owner`: 단일 책임자
- `related_adrs`: 이 PRD와 직접 연결된 아키텍처 결정
- `related_decisions`: 필요 시 후속 의사결정 목록

## TRD Standard

권장 front matter:

```yaml
---
id: TRD-MYSTARNOW-BFF-SERVER
type: trd
status: draft
owner:
created: 2026-04-04
updated: 2026-04-04
related_prd: PRD-MYSTARNOW-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-BFF-ARCHITECTURE
---
```

필드 설명:

- `related_prd`: 이 기술 설계가 따르는 제품 문서
- `related_adrs`: 기술 결정 문서

## ADR Standard

권장 front matter:

```yaml
---
id: ADR-MYSTARNOW-BFF-ARCHITECTURE
type: adr
status: proposed
date: 2026-04-04
owners: []
related_prd: PRD-MYSTARNOW-BFF-SERVER
supersedes:
superseded_by:
---
```

ADR은 예외적으로 `created/updated` 대신 `date`를 기본값으로 써도 된다.

ADR 추가 규칙:

- `status`는 보통 `proposed`, `active`, `deprecated` 중 하나
- 대체 결정이 생기면 `superseded_by`를 채운다

## SPEC / Strategy Standard

OpenAPI, DB schema, adapter, reliability 문서처럼 설계/명세 성격이 강한 문서는 `type: spec`을 권장한다.

권장 front matter:

```yaml
---
id: SPEC-MYSTARNOW-BFF-OPENAPI-V1
type: spec
status: draft
owner:
created: 2026-04-04
updated: 2026-04-04
related_prd: PRD-MYSTARNOW-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-BFF-ARCHITECTURE
---
```

적용 대상:

- OpenAPI
- DB schema
- adapter specification
- reliability strategy

# Title Rule

front matter 다음 첫 번째 헤더는 문서 목적이 바로 보이게 짧게 적는다.

예시:

- `# Summary`
- `# Context`
- `# API Overview`
- `# Domain Entities`

# Update Rule

문서를 수정할 때는 아래를 함께 맞춘다.

1. `updated` 날짜
2. 관련 문서 링크
3. superseded 관계
4. status 변화 여부

# Recommended Mapping For Current Project

- `PRD-mystarnow.md` -> `type: prd`
- `TRD-mystarnow-android-cmp.md` -> `type: trd`
- `ADR-mystarnow-architecture.md` -> `type: adr`
- `OPENAPI-mystarnow-bff-v1.md` -> `type: spec`
- `DB-SCHEMA-mystarnow-bff-server.md` -> `type: spec`
- `ADAPTER-SPEC-mystarnow-bff-platforms.md` -> `type: spec`
- `RELIABILITY-STRATEGY-mystarnow-bff-server.md` -> `type: spec`

# Short Checklist

새 문서를 만들 때 확인:

1. front matter가 있는가
2. `id`와 파일명이 같은 naming rule을 따르는가
3. `type`과 `status`가 적절한가
4. 관련 문서 연결 필드가 채워졌는가
5. 수정 시 `updated`를 갱신했는가
