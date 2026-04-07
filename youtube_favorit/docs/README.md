# Docs Index

`MyStarNow` 문서는 `front`와 `back`을 기준 축으로 나누고, 각 영역 안에서 `product`, `architecture`, `api`, `data`, `reliability`, `integrations` 같은 목적별 폴더로 정리한다.

## Structure

```text
docs/
├─ README.md
├─ common/
├─ templates/
├─ front/
│  ├─ README.md
│  ├─ product/
│  ├─ architecture/
│  └─ api-contracts/
├─ back/
│  ├─ README.md
│  ├─ product/
│  ├─ architecture/
│  ├─ api/
│  ├─ data/
│  ├─ integrations/
│  └─ reliability/
└─ ops/
```

## Front

- Product docs:
  - `front/product/PRD-mystarnow.md`
  - `front/product/PRD-mystarnow-idol-youtube-hub.md`
- Architecture docs:
  - `front/architecture/ADR-mystarnow-architecture.md`
  - `front/architecture/TRD-mystarnow-android-cmp.md`

## Back

- Product docs:
  - `back/product/PRD-mystarnow-bff-server.md`
  - `back/product/PRD-mystarnow-idol-youtube-bff-server.md`
- Architecture docs:
  - `back/architecture/ADR-mystarnow-bff-architecture.md`
  - `back/architecture/TRD-mystarnow-bff-server.md`
  - `back/architecture/ADR-mystarnow-idol-youtube-bff-architecture.md`
  - `back/architecture/TRD-mystarnow-idol-youtube-bff-server.md`
- API docs:
  - `back/api/OPENAPI-mystarnow-bff-v1.md`
  - `back/api/OPENAPI-mystarnow-idol-youtube-bff-v1.md`
- Data docs:
  - `back/data/DB-SCHEMA-mystarnow-bff-server.md`
  - `back/data/DB-SCHEMA-mystarnow-idol-youtube-bff-server.md`
- Integration docs:
  - `back/integrations/ADAPTER-SPEC-mystarnow-bff-platforms.md`
  - `back/integrations/ADAPTER-SPEC-mystarnow-idol-youtube-platforms.md`
- Reliability docs:
  - `back/reliability/RELIABILITY-STRATEGY-mystarnow-bff-server.md`

## Cross-cutting Plans

- `ROADMAP-mystarnow-idol-youtube-delivery.md`

## Notes

- 새 문서는 기본적으로 `/Users/oyj/Desktop/workspace/AI_Project/youtube_favorit/docs` 아래에 추가한다.
- 가능하면 문서 타입과 주제를 파일명에 함께 넣는다.
- 문서 간 참조는 상대 경로를 사용한다.
- 네이밍 규칙은 `common/naming-conventions.md`를 따른다.
- front matter 규칙은 `common/front-matter-standard.md`를 따른다.
- 새 문서 초안은 `templates/` 아래 템플릿에서 시작한다.
