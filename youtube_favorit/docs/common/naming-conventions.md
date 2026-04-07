# Document Naming Conventions

`MyStarNow` 문서는 찾기 쉽고, 역할이 바로 보이고, 경로만 봐도 문서 성격을 알 수 있게 이름을 정한다.

# Core Rule

파일명은 아래 형식을 기본으로 한다.

```text
<DOC-TYPE>-<subject>.md
```

문서 front matter의 `id`도 같은 규칙을 따른다.

```text
<DOC-TYPE>-<SUBJECT>
```

예시:

```text
id: PRD-MYSTARNOW
id: TRD-MYSTARNOW-BFF-SERVER
id: ADR-MYSTARNOW-ARCHITECTURE
```

예시:

```text
PRD-mystarnow.md
TRD-mystarnow-bff-server.md
ADR-mystarnow-bff-architecture.md
OPENAPI-mystarnow-bff-v1.md
DB-SCHEMA-mystarnow-bff-server.md
ADAPTER-SPEC-mystarnow-bff-platforms.md
RELIABILITY-STRATEGY-mystarnow-bff-server.md
```

# Allowed Prefixes

문서 타입 prefix는 아래만 사용한다.

- `PRD`
- `TRD`
- `ADR`
- `OPENAPI`
- `DB-SCHEMA`
- `ADAPTER-SPEC`
- `RELIABILITY-STRATEGY`
- `RUNBOOK`
- `ROADMAP`
- `GLOSSARY`

새 타입이 필요하면 먼저 `docs/common`에 규칙을 추가한 뒤 사용한다.

# Subject Rule

subject는 문서가 다루는 실제 대상을 짧고 명확하게 적는다.

원칙:

- 소문자 kebab-case 사용
- 공백 사용 금지
- 불필요한 날짜 넣지 않기
- 프로젝트명 반복 최소화
- 버전이 필요한 경우만 suffix 추가

좋은 예:

- `mystarnow`
- `mystarnow-bff-server`
- `mystarnow-android-cmp`
- `mystarnow-bff-v1`
- `mystarnow-bff-platforms`

피해야 할 예:

- `final-doc.md`
- `new-api-spec.md`
- `2026-04-04-prd.md`
- `mystarnow-document-final-v2-really-final.md`

# Date Rule

파일명에 날짜는 기본적으로 넣지 않는다.

이유:

- 현재 문서는 살아 있는 설계 문서라서 경로가 자주 바뀌면 안 된다.
- 날짜는 front matter에 넣는 것이 더 적절하다.

날짜를 파일명에 넣는 경우:

- ADR/decision log를 날짜 순으로 강하게 관리해야 하는 별도 정책이 생겼을 때만 허용

# Version Rule

버전은 아래 경우에만 파일명에 넣는다.

- OpenAPI 명세처럼 명시적인 버전 계약이 있을 때
- schema나 migration 정책처럼 병행 버전이 존재할 때

예시:

- `OPENAPI-mystarnow-bff-v1.md`
- `OPENAPI-mystarnow-bff-v2.md`

# Folder Responsibility Rule

문서 의미는 파일명보다 폴더가 먼저 설명해야 한다.

예시:

- `front/product/PRD-mystarnow.md`
- `back/architecture/TRD-mystarnow-bff-server.md`
- `back/api/OPENAPI-mystarnow-bff-v1.md`

즉, 파일명에 `front-`, `back-`, `product-`, `architecture-`를 중복으로 넣지 않는다.

# README Rule

README는 예외적으로 고정 이름을 사용한다.

허용:

- `docs/README.md`
- `docs/front/README.md`
- `docs/back/README.md`
- `docs/common/README.md`
- `docs/ops/README.md`

# Internal Linking Rule

문서 안의 링크는 현재 파일 기준 상대 경로를 사용한다.

예시:

- front architecture 문서에서 PRD 참조:
  - `../product/PRD-mystarnow.md`
- back architecture 문서에서 API 문서 참조:
  - `../api/OPENAPI-mystarnow-bff-v1.md`

# Recommended Names For Current Project

## Front

- `PRD-mystarnow.md`
- `TRD-mystarnow-android-cmp.md`
- `ADR-mystarnow-architecture.md`

## Back

- `PRD-mystarnow-bff-server.md`
- `TRD-mystarnow-bff-server.md`
- `ADR-mystarnow-bff-architecture.md`
- `OPENAPI-mystarnow-bff-v1.md`
- `DB-SCHEMA-mystarnow-bff-server.md`
- `ADAPTER-SPEC-mystarnow-bff-platforms.md`
- `RELIABILITY-STRATEGY-mystarnow-bff-server.md`

# Short Checklist

새 문서를 만들기 전 아래만 확인한다.

1. 이 문서는 어느 폴더에 들어가야 하는가
2. prefix가 기존 허용 목록 안에 있는가
3. subject가 짧고 정확한가
4. 날짜를 파일명에 넣지 않아도 되는가
5. 버전이 정말 필요한가

# Related Standards

- front matter 규칙: `./front-matter-standard.md`
- 문서 템플릿: `../templates/README.md`
