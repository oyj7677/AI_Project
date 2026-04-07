---
id: OPENAPI-MYSTARNOW-IDOL-YOUTUBE-BFF-V1
type: spec
status: draft
owner:
created: 2026-04-05
updated: 2026-04-05
related_prd: PRD-MYSTARNOW-IDOL-YOUTUBE-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-IDOL-YOUTUBE-BFF-ARCHITECTURE
---

# API Overview

`MyStarNow Idol YouTube BFF API v1`는 그룹 중심 YouTube 허브를 위한 읽기/운영 API 초안이다.

핵심 원칙:

- 모든 public 응답은 `meta`, `data`, `errors` envelope를 사용한다.
- partial failure는 endpoint 전체 실패가 아니라 `section` 단위 상태로 표현한다.
- 그룹 탐색 UI에 바로 쓰기 좋은 구조를 우선한다.
- 1차 범위는 `YouTube only`다.
- 운영자 입력 API는 internal-only 경로로 분리한다.

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "MyStarNow Idol YouTube BFF API",
    "version": "v1",
    "description": "Group-centric YouTube aggregation API for idol official/member channels"
  },
  "servers": [
    {
      "url": "https://api.mystarnow.com"
    }
  ]
}
```

# Shared Contract

## Envelope

```json
{
  "meta": {
    "requestId": "string",
    "apiVersion": "v1",
    "generatedAt": "2026-04-05T10:00:00Z",
    "partialFailure": false
  },
  "data": {},
  "errors": []
}
```

## ResponseMeta

```json
{
  "type": "object",
  "required": ["requestId", "apiVersion", "generatedAt", "partialFailure"],
  "properties": {
    "requestId": { "type": "string" },
    "apiVersion": { "type": "string", "example": "v1" },
    "generatedAt": { "type": "string", "format": "date-time" },
    "partialFailure": { "type": "boolean" }
  }
}
```

## ApiError

```json
{
  "type": "object",
  "required": ["scope", "section", "code", "message"],
  "properties": {
    "scope": { "type": "string", "example": "request" },
    "section": { "type": ["string", "null"] },
    "code": { "type": "string" },
    "message": { "type": "string" }
  }
}
```

## SectionState<T>

```json
{
  "type": "object",
  "required": ["status", "freshness", "generatedAt", "data"],
  "properties": {
    "status": {
      "type": "string",
      "enum": ["success", "partial", "failed", "empty"]
    },
    "freshness": {
      "type": "string",
      "enum": ["fresh", "stale", "manual", "unknown"]
    },
    "generatedAt": { "type": "string", "format": "date-time" },
    "staleAt": { "type": ["string", "null"], "format": "date-time" },
    "data": { "type": "object" },
    "error": {
      "type": ["object", "null"],
      "properties": {
        "code": { "type": "string" },
        "message": { "type": "string" },
        "retryable": { "type": "boolean" },
        "source": { "type": ["string", "null"] }
      }
    }
  }
}
```

## PageInfo

```json
{
  "type": "object",
  "required": ["limit", "nextCursor", "hasNext"],
  "properties": {
    "limit": { "type": "integer", "minimum": 1 },
    "nextCursor": { "type": ["string", "null"] },
    "hasNext": { "type": "boolean" }
  }
}
```

# Public Read APIs

## GET /v1/home

그룹 중심 최근 업로드 홈 피드

```json
{
  "path": "/v1/home",
  "method": "GET",
  "operationId": "getHome",
  "parameters": [
    {
      "name": "cursor",
      "in": "query",
      "required": false,
      "schema": { "type": "string" }
    },
    {
      "name": "limit",
      "in": "query",
      "required": false,
      "schema": { "type": "integer", "minimum": 1, "maximum": 20, "default": 8 }
    },
    {
      "name": "locale",
      "in": "query",
      "required": false,
      "schema": { "type": "string", "example": "ko-KR" }
    }
  ],
  "responses": {
    "200": { "description": "홈 피드 응답" },
    "400": { "description": "cursor 또는 limit 오류" },
    "503": { "description": "전체 홈 피드 생성 불가" }
  }
}
```

### Response Schema

```json
{
  "meta": {
    "requestId": "req-home-1",
    "apiVersion": "v1",
    "generatedAt": "2026-04-05T10:00:00Z",
    "partialFailure": false
  },
  "data": {
    "recentVideos": {
      "status": "success",
      "freshness": "fresh",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": [
          {
            "videoId": "uuid",
            "externalVideoId": "youtube-video-id",
            "title": "그룹 공식 채널 새 콘텐츠",
            "description": "영상 설명 요약",
            "thumbnailUrl": "https://...",
            "publishedAt": "2026-04-05T08:30:00Z",
            "videoUrl": "https://youtube.com/watch?v=...",
            "channel": {
              "channelId": "uuid",
              "externalChannelId": "UCxxxx",
              "channelName": "Group Official",
              "handle": "@group",
              "channelUrl": "https://youtube.com/@group",
              "channelType": "GROUP_OFFICIAL",
              "isOfficial": true
            },
            "group": {
              "groupId": "uuid",
              "groupSlug": "starwave",
              "groupName": "StarWave"
            },
            "member": null,
            "badges": ["group-official"]
          }
        ],
        "pageInfo": {
          "limit": 8,
          "nextCursor": "OA",
          "hasNext": true
        }
      }
    },
    "featuredGroups": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": [
          {
            "groupId": "uuid",
            "groupSlug": "starwave",
            "groupName": "StarWave",
            "coverImageUrl": "https://...",
            "officialChannelCount": 2,
            "memberPersonalChannelCount": 4,
            "latestVideoAt": "2026-04-05T08:30:00Z"
          }
        ]
      }
    }
  },
  "errors": []
}
```

## GET /v1/groups

그룹 목록

```json
{
  "path": "/v1/groups",
  "method": "GET",
  "operationId": "getGroups",
  "parameters": [
    {
      "name": "q",
      "in": "query",
      "required": false,
      "schema": { "type": "string", "maxLength": 100 }
    },
    {
      "name": "sort",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "enum": ["featured", "recent_video", "name"],
        "default": "featured"
      }
    },
    {
      "name": "cursor",
      "in": "query",
      "required": false,
      "schema": { "type": "string" }
    },
    {
      "name": "limit",
      "in": "query",
      "required": false,
      "schema": { "type": "integer", "minimum": 1, "maximum": 50, "default": 20 }
    }
  ],
  "responses": {
    "200": { "description": "그룹 목록 응답" },
    "400": { "description": "검색 파라미터 오류" },
    "503": { "description": "전체 목록 생성 불가" }
  }
}
```

### Response Schema

```json
{
  "meta": { "requestId": "req-groups-1", "apiVersion": "v1", "generatedAt": "2026-04-05T10:00:00Z", "partialFailure": false },
  "data": {
    "results": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": [
          {
            "groupId": "uuid",
            "groupSlug": "starwave",
            "groupName": "StarWave",
            "description": "한 줄 소개",
            "coverImageUrl": "https://...",
            "officialChannelCount": 2,
            "memberCount": 5,
            "memberPersonalChannelCount": 4,
            "latestVideoAt": "2026-04-05T08:30:00Z",
            "latestVideoThumbnailUrl": "https://...",
            "badges": ["featured"]
          }
        ],
        "pageInfo": {
          "limit": 20,
          "nextCursor": null,
          "hasNext": false
        },
        "appliedFilters": {
          "q": "star",
          "sort": "featured"
        }
      }
    },
    "filters": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "sortOptions": [
          { "value": "featured", "label": "추천순" },
          { "value": "recent_video", "label": "최근 업로드순" },
          { "value": "name", "label": "이름순" }
        ]
      }
    }
  },
  "errors": []
}
```

## GET /v1/groups/{groupSlug}

그룹 상세

```json
{
  "path": "/v1/groups/{groupSlug}",
  "method": "GET",
  "operationId": "getGroupDetail",
  "parameters": [
    {
      "name": "groupSlug",
      "in": "path",
      "required": true,
      "schema": { "type": "string" }
    },
    {
      "name": "videosCursor",
      "in": "query",
      "required": false,
      "schema": { "type": "string" }
    },
    {
      "name": "videosLimit",
      "in": "query",
      "required": false,
      "schema": { "type": "integer", "minimum": 1, "maximum": 20, "default": 8 }
    }
  ],
  "responses": {
    "200": { "description": "그룹 상세 응답" },
    "400": { "description": "query 오류" },
    "404": { "description": "그룹 없음" },
    "503": { "description": "전체 그룹 상세 생성 불가" }
  }
}
```

### Response Schema

```json
{
  "meta": { "requestId": "req-group-1", "apiVersion": "v1", "generatedAt": "2026-04-05T10:00:00Z", "partialFailure": true },
  "data": {
    "groupHeader": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "groupId": "uuid",
        "groupSlug": "starwave",
        "groupName": "StarWave",
        "description": "그룹 소개",
        "coverImageUrl": "https://...",
        "memberCount": 5,
        "officialChannelCount": 2,
        "memberPersonalChannelCount": 4,
        "latestVideoAt": "2026-04-05T08:30:00Z",
        "isFeatured": true
      }
    },
    "officialChannels": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": [
          {
            "channelId": "uuid",
            "externalChannelId": "UCxxxx",
            "channelName": "StarWave Official",
            "handle": "@starwave",
            "channelUrl": "https://youtube.com/@starwave",
            "channelType": "GROUP_OFFICIAL",
            "isOfficial": true,
            "latestVideoAt": "2026-04-05T08:30:00Z"
          }
        ]
      }
    },
    "members": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": [
          {
            "memberId": "uuid",
            "memberSlug": "harin",
            "memberName": "하린",
            "profileImageUrl": "https://...",
            "hasPersonalChannel": true,
            "personalChannelCount": 1,
            "latestVideoAt": "2026-04-05T08:00:00Z"
          }
        ]
      }
    },
    "recentVideos": {
      "status": "partial",
      "freshness": "stale",
      "generatedAt": "2026-04-05T10:00:00Z",
      "staleAt": "2026-04-05T09:40:00Z",
      "error": {
        "code": "YOUTUBE_SYNC_STALE",
        "message": "일부 채널의 최신 영상 동기화가 지연되었습니다.",
        "retryable": true,
        "source": "youtube"
      },
      "data": {
        "items": [],
        "pageInfo": {
          "limit": 8,
          "nextCursor": null,
          "hasNext": false
        }
      }
    },
    "detailMeta": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "supportedPlatforms": ["youtube"],
        "channelTypes": ["GROUP_OFFICIAL", "MEMBER_PERSONAL", "SUB_UNIT", "LABEL"]
      }
    }
  },
  "errors": [
    {
      "scope": "section",
      "section": "recentVideos",
      "code": "YOUTUBE_SYNC_STALE",
      "message": "일부 채널의 최신 영상 동기화가 지연되었습니다."
    }
  ]
}
```

## GET /v1/members/{memberSlug}

멤버 상세

```json
{
  "path": "/v1/members/{memberSlug}",
  "method": "GET",
  "operationId": "getMemberDetail",
  "parameters": [
    {
      "name": "memberSlug",
      "in": "path",
      "required": true,
      "schema": { "type": "string" }
    },
    {
      "name": "videosCursor",
      "in": "query",
      "required": false,
      "schema": { "type": "string" }
    },
    {
      "name": "videosLimit",
      "in": "query",
      "required": false,
      "schema": { "type": "integer", "minimum": 1, "maximum": 20, "default": 8 }
    }
  ],
  "responses": {
    "200": { "description": "멤버 상세 응답" },
    "404": { "description": "멤버 없음" }
  }
}
```

### Response Schema

```json
{
  "meta": { "requestId": "req-member-1", "apiVersion": "v1", "generatedAt": "2026-04-05T10:00:00Z", "partialFailure": false },
  "data": {
    "memberProfile": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "memberId": "uuid",
        "memberSlug": "harin",
        "memberName": "하린",
        "profileImageUrl": "https://...",
        "group": {
          "groupId": "uuid",
          "groupSlug": "starwave",
          "groupName": "StarWave"
        }
      }
    },
    "personalChannels": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": []
      }
    },
    "recentVideos": {
      "status": "success",
      "freshness": "fresh",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "items": [],
        "pageInfo": {
          "limit": 8,
          "nextCursor": null,
          "hasNext": false
        }
      }
    }
  },
  "errors": []
}
```

## GET /v1/meta/app-config

```json
{
  "path": "/v1/meta/app-config",
  "method": "GET",
  "operationId": "getAppConfig",
  "parameters": [
    {
      "name": "clientPlatform",
      "in": "query",
      "required": true,
      "schema": { "type": "string", "enum": ["android", "web"] }
    },
    {
      "name": "clientVersion",
      "in": "query",
      "required": false,
      "schema": { "type": "string" }
    }
  ],
  "responses": {
    "200": { "description": "런타임 설정 응답" },
    "400": { "description": "clientPlatform 오류" }
  }
}
```

### Response Schema

```json
{
  "meta": { "requestId": "req-config-1", "apiVersion": "v1", "generatedAt": "2026-04-05T10:00:00Z", "partialFailure": false },
  "data": {
    "runtime": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "minimumSupportedAppVersion": "1.0.0",
        "defaultPageSize": 8,
        "maxPageSize": 20,
        "supportedPlatforms": [
          {
            "platform": "youtube",
            "enabled": true,
            "supportMode": "auto"
          }
        ]
      }
    },
    "featureFlags": {
      "status": "success",
      "freshness": "manual",
      "generatedAt": "2026-04-05T10:00:00Z",
      "data": {
        "showMemberDetail": true,
        "showFeaturedGroups": true,
        "showVideoFeed": true
      }
    }
  },
  "errors": []
}
```

# Internal Operator APIs

## Authentication

```json
{
  "security": "HTTP Basic Auth",
  "required": true,
  "scope": "/internal/operator/**"
}
```

## POST /internal/operator/groups

```json
{
  "path": "/internal/operator/groups",
  "method": "POST",
  "summary": "신규 그룹 생성",
  "requestBody": {
    "required": true,
    "content": {
      "application/json": {
        "schema": {
          "type": "object",
          "required": ["slug", "displayName"],
          "properties": {
            "slug": { "type": "string" },
            "displayName": { "type": "string" },
            "description": { "type": "string" },
            "coverImageUrl": { "type": "string" },
            "featured": { "type": "boolean" },
            "note": { "type": "string" }
          }
        }
      }
    }
  },
  "responses": {
    "200": { "description": "생성 성공" },
    "400": { "description": "입력값 오류" },
    "409": { "description": "slug 중복" }
  }
}
```

## POST /internal/operator/members

```json
{
  "path": "/internal/operator/members",
  "method": "POST",
  "summary": "신규 멤버 생성",
  "requestBody": {
    "required": true,
    "content": {
      "application/json": {
        "schema": {
          "type": "object",
          "required": ["groupId", "slug", "displayName"],
          "properties": {
            "groupId": { "type": "string", "format": "uuid" },
            "slug": { "type": "string" },
            "displayName": { "type": "string" },
            "profileImageUrl": { "type": "string" },
            "sortOrder": { "type": "integer" },
            "note": { "type": "string" }
          }
        }
      }
    }
  }
}
```

## POST /internal/operator/channels

```json
{
  "path": "/internal/operator/channels",
  "method": "POST",
  "summary": "그룹 공식 또는 멤버 개인 채널 생성",
  "requestBody": {
    "required": true,
    "content": {
      "application/json": {
        "schema": {
          "type": "object",
          "required": ["platformCode", "externalChannelId", "channelUrl", "channelType", "ownerType"],
          "properties": {
            "platformCode": { "type": "string", "enum": ["youtube"] },
            "externalChannelId": { "type": "string" },
            "handle": { "type": "string" },
            "channelUrl": { "type": "string" },
            "displayLabel": { "type": "string" },
            "channelType": {
              "type": "string",
              "enum": ["GROUP_OFFICIAL", "MEMBER_PERSONAL", "SUB_UNIT", "LABEL"]
            },
            "ownerType": {
              "type": "string",
              "enum": ["GROUP", "MEMBER"]
            },
            "ownerGroupId": { "type": ["string", "null"], "format": "uuid" },
            "ownerMemberId": { "type": ["string", "null"], "format": "uuid" },
            "isOfficial": { "type": "boolean" },
            "isPrimary": { "type": "boolean" },
            "note": { "type": "string" }
          }
        }
      }
    }
  }
}
```

## POST /internal/operator/videos

```json
{
  "path": "/internal/operator/videos",
  "method": "POST",
  "summary": "수동 영상 등록",
  "requestBody": {
    "required": true,
    "content": {
      "application/json": {
        "schema": {
          "type": "object",
          "required": ["channelId", "title", "publishedAt", "videoUrl"],
          "properties": {
            "channelId": { "type": "string", "format": "uuid" },
            "externalVideoId": { "type": "string" },
            "title": { "type": "string" },
            "description": { "type": "string" },
            "thumbnailUrl": { "type": "string" },
            "publishedAt": { "type": "string", "format": "date-time" },
            "videoUrl": { "type": "string" },
            "pinned": { "type": "boolean" },
            "note": { "type": "string" }
          }
        }
      }
    }
  }
}
```

## Mutation Success Shape

```json
{
  "status": "created",
  "entityId": "uuid"
}
```

# Error Cases

## 400 Bad Request

```json
{
  "meta": {
    "requestId": "req-bad",
    "apiVersion": "v1",
    "generatedAt": "2026-04-05T10:00:00Z",
    "partialFailure": false
  },
  "data": null,
  "errors": [
    {
      "scope": "request",
      "section": null,
      "code": "INVALID_REQUEST",
      "message": "videosLimit must be between 1 and 20."
    }
  ]
}
```

## 404 Not Found

```json
{
  "meta": {
    "requestId": "req-not-found",
    "apiVersion": "v1",
    "generatedAt": "2026-04-05T10:00:00Z",
    "partialFailure": false
  },
  "data": null,
  "errors": [
    {
      "scope": "request",
      "section": null,
      "code": "RESOURCE_NOT_FOUND",
      "message": "No group exists for slug 'starwave'."
    }
  ]
}
```

## 503 Service Unavailable

```json
{
  "meta": {
    "requestId": "req-503",
    "apiVersion": "v1",
    "generatedAt": "2026-04-05T10:00:00Z",
    "partialFailure": false
  },
  "data": null,
  "errors": [
    {
      "scope": "request",
      "section": null,
      "code": "SERVICE_UNAVAILABLE",
      "message": "The requested resource is temporarily unavailable."
    }
  ]
}
```

# Notes

- 홈과 그룹 상세의 최근 업로드 피드는 같은 아이템 구조를 재사용한다.
- 1차에서는 `YouTube`만 `supportedPlatforms`에 enabled로 노출한다.
- 라이브 전용 섹션은 1차 계약에 포함하지 않는다.
- 운영자 입력 API는 CRUD 전체가 아니라 MVP 필수 생성/수정만 우선한다.

# Links

- Related PRD:
  - `../product/PRD-mystarnow-idol-youtube-bff-server.md`
- Related ADRs:
  - `../architecture/ADR-mystarnow-idol-youtube-bff-architecture.md`
- Related Decisions:
  - `../architecture/TRD-mystarnow-idol-youtube-bff-server.md`
