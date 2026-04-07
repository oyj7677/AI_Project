# API Overview

`MyStarNow BFF API v1`는 Compose Multiplatform Android 클라이언트가 화면 단위로 바로 사용할 수 있도록 설계된 읽기 중심 BFF API다.

핵심 원칙:

- 모든 endpoint는 `meta`, `data`, `errors`의 동일한 top-level envelope를 사용한다.
- partial failure는 endpoint 전체 실패가 아니라 `section` 단위 상태로 표현한다.
- 클라이언트는 `data.<section>.status`만 보고도 UI를 렌더링할 수 있어야 한다.
- 현재 1차 운영 범위는 `YouTube`, `Instagram`이지만, schema는 향후 `X`, `CHZZK`, `SOOP` 확장을 견딜 수 있게 유지한다.
- upstream 플랫폼 응답 형식은 노출하지 않고, 정규화된 도메인 모델만 응답한다.

```json
{
  "openapi": "3.1.0",
  "info": {
    "title": "MyStarNow BFF API",
    "version": "v1",
    "description": "UI-optimized BFF API for the MyStarNow Android-first client"
  },
  "servers": [
    {
      "url": "https://api.mystarnow.com"
    }
  ]
}
```

# Endpoint Definitions

## GET /v1/home

```json
{
  "path": "/v1/home",
  "method": "GET",
  "operationId": "getHomeFeed",
  "summary": "Returns aggregated home feed optimized for section-based home rendering",
  "parameters": [
    {
      "name": "timezone",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "example": "Asia/Seoul"
      }
    },
    {
      "name": "locale",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "example": "ko-KR"
      }
    }
  ],
  "responses": {
    "200": {
      "description": "Home payload returned. One or more sections may be partial or failed."
    },
    "400": {
      "description": "Invalid timezone or malformed query"
    },
    "503": {
      "description": "Entire home payload unavailable"
    }
  }
}
```

## GET /v1/influencers

```json
{
  "path": "/v1/influencers",
  "method": "GET",
  "operationId": "getInfluencers",
  "summary": "Returns influencer list, filter metadata, and pagination state",
  "parameters": [
    {
      "name": "q",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "maxLength": 100
      }
    },
    {
      "name": "category",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string"
      }
    },
    {
      "name": "platform",
      "in": "query",
      "required": false,
      "schema": {
        "type": "array",
        "items": {
          "$ref": "#/components/schemas/Platform"
        }
      },
      "style": "form",
      "explode": true
    },
    {
      "name": "sort",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "enum": ["featured", "recent_activity", "name"],
        "default": "featured"
      }
    },
    {
      "name": "cursor",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string"
      }
    },
    {
      "name": "limit",
      "in": "query",
      "required": false,
      "schema": {
        "type": "integer",
        "minimum": 1,
        "maximum": 50,
        "default": 20
      }
    }
  ],
  "responses": {
    "200": {
      "description": "Influencer list payload returned. Results and filter sections may fail independently."
    },
    "400": {
      "description": "Invalid filter, cursor, or limit"
    },
    "503": {
      "description": "Entire list payload unavailable"
    }
  }
}
```

## GET /v1/influencers/{slug}

```json
{
  "path": "/v1/influencers/{slug}",
  "method": "GET",
  "operationId": "getInfluencerDetail",
  "summary": "Returns influencer detail sections optimized for the detail screen",
  "parameters": [
    {
      "name": "slug",
      "in": "path",
      "required": true,
      "schema": {
        "type": "string"
      }
    },
    {
      "name": "timezone",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "example": "Asia/Seoul"
      }
    },
    {
      "name": "activitiesLimit",
      "in": "query",
      "required": false,
      "schema": {
        "type": "integer",
        "minimum": 1,
        "maximum": 50,
        "default": 20
      }
    },
    {
      "name": "schedulesLimit",
      "in": "query",
      "required": false,
      "schema": {
        "type": "integer",
        "minimum": 1,
        "maximum": 20,
        "default": 10
      }
    }
  ],
  "responses": {
    "200": {
      "description": "Detail payload returned. Individual sections may be partial or failed."
    },
    "400": {
      "description": "Invalid slug format or malformed query"
    },
    "404": {
      "description": "Influencer not found"
    },
    "503": {
      "description": "Entire detail payload unavailable"
    }
  }
}
```

## GET /v1/meta/app-config

```json
{
  "path": "/v1/meta/app-config",
  "method": "GET",
  "operationId": "getAppConfig",
  "summary": "Returns client-facing runtime config, feature flags, and section visibility",
  "parameters": [
    {
      "name": "clientPlatform",
      "in": "query",
      "required": true,
      "schema": {
        "type": "string",
        "enum": ["android"]
      }
    },
    {
      "name": "clientVersion",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "example": "1.0.0"
      }
    },
    {
      "name": "locale",
      "in": "query",
      "required": false,
      "schema": {
        "type": "string",
        "example": "ko-KR"
      }
    }
  ],
  "responses": {
    "200": {
      "description": "App config payload returned"
    },
    "400": {
      "description": "Invalid client platform or malformed query"
    },
    "503": {
      "description": "App config unavailable"
    }
  }
}
```

# Request Parameters

## Common Rules

```json
{
  "requestRules": {
    "timezone": "Optional IANA timezone. Used to bucket schedule/date-sensitive sections.",
    "locale": "Optional BCP-47 locale. Used for localized labels and copy if supported.",
    "unknownQueryParams": "Ignored for backward compatibility.",
    "futurePlatforms": "Schema may include x, chzzk, soop enums before they are enabled in production."
  }
}
```

## Parameter Summary

```json
{
  "/v1/home": ["timezone", "locale"],
  "/v1/influencers": ["q", "category", "platform[]", "sort", "cursor", "limit"],
  "/v1/influencers/{slug}": ["slug", "timezone", "activitiesLimit", "schedulesLimit"],
  "/v1/meta/app-config": ["clientPlatform", "clientVersion", "locale"]
}
```

# Response Schema

## Common Envelope

```json
{
  "components": {
    "schemas": {
      "ApiResponse": {
        "type": "object",
        "required": ["meta", "data", "errors"],
        "properties": {
          "meta": {
            "$ref": "#/components/schemas/ResponseMeta"
          },
          "data": {
            "type": "object"
          },
          "errors": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/ApiError"
            }
          }
        }
      },
      "ResponseMeta": {
        "type": "object",
        "required": ["requestId", "apiVersion", "generatedAt", "partialFailure"],
        "properties": {
          "requestId": {
            "type": "string"
          },
          "apiVersion": {
            "type": "string",
            "example": "v1"
          },
          "generatedAt": {
            "type": "string",
            "format": "date-time"
          },
          "partialFailure": {
            "type": "boolean"
          }
        }
      },
      "SectionState": {
        "type": "object",
        "required": ["status", "freshness", "generatedAt", "data", "error"],
        "properties": {
          "status": {
            "type": "string",
            "enum": ["success", "partial", "failed", "empty"]
          },
          "freshness": {
            "type": "string",
            "enum": ["fresh", "stale", "manual", "unknown"]
          },
          "generatedAt": {
            "type": "string",
            "format": "date-time"
          },
          "staleAt": {
            "type": "string",
            "format": "date-time",
            "nullable": true
          },
          "data": {},
          "error": {
            "$ref": "#/components/schemas/SectionError"
          }
        }
      },
      "SectionError": {
        "type": ["object", "null"],
        "properties": {
          "code": {
            "type": "string"
          },
          "message": {
            "type": "string"
          },
          "retryable": {
            "type": "boolean"
          },
          "source": {
            "type": "string",
            "example": "youtube"
          }
        }
      },
      "ApiError": {
        "type": "object",
        "required": ["scope", "code", "message"],
        "properties": {
          "scope": {
            "type": "string",
            "enum": ["request", "section"]
          },
          "section": {
            "type": ["string", "null"]
          },
          "code": {
            "type": "string"
          },
          "message": {
            "type": "string"
          }
        }
      },
      "Platform": {
        "type": "string",
        "enum": ["youtube", "instagram", "x", "chzzk", "soop"]
      }
    }
  }
}
```

## GET /v1/home Response

```json
{
  "200": {
    "meta": {
      "requestId": "req_home_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": true
    },
    "data": {
      "liveNow": {
        "status": "success",
        "freshness": "fresh",
        "generatedAt": "2026-04-04T11:59:30Z",
        "staleAt": null,
        "data": {
          "items": [
            {
              "influencerId": "inf_haru",
              "slug": "haru",
              "name": "하루",
              "profileImageUrl": "https://cdn.mystarnow.com/haru.jpg",
              "platform": "youtube",
              "liveTitle": "오늘은 잡담 방송",
              "startedAt": "2026-04-04T11:40:00Z",
              "watchUrl": "https://youtube.com/watch?v=abc123"
            }
          ],
          "total": 1
        },
        "error": null
      },
      "latestUpdates": {
        "status": "partial",
        "freshness": "stale",
        "generatedAt": "2026-04-04T11:55:00Z",
        "staleAt": "2026-04-04T12:15:00Z",
        "data": {
          "items": [
            {
              "activityId": "act_1",
              "influencerId": "inf_haru",
              "platform": "youtube",
              "contentType": "video",
              "title": "어제 하이라이트",
              "publishedAt": "2026-04-03T10:00:00Z",
              "thumbnailUrl": "https://img.youtube.com/vi/abc/hqdefault.jpg",
              "externalUrl": "https://youtube.com/watch?v=abc"
            }
          ]
        },
        "error": {
          "code": "INSTAGRAM_UPSTREAM_UNAVAILABLE",
          "message": "Instagram latest updates are temporarily unavailable.",
          "retryable": true,
          "source": "instagram"
        }
      },
      "todaySchedules": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "timezone": "Asia/Seoul",
          "date": "2026-04-04",
          "items": []
        },
        "error": null
      },
      "featuredInfluencers": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "items": [
            {
              "influencerId": "inf_haru",
              "slug": "haru",
              "name": "하루",
              "summary": "게임, 토크 중심 스트리머",
              "categories": ["game", "talk"],
              "platforms": ["youtube", "instagram"],
              "liveStatus": "live"
            }
          ]
        },
        "error": null
      }
    },
    "errors": [
      {
        "scope": "section",
        "section": "latestUpdates",
        "code": "INSTAGRAM_UPSTREAM_UNAVAILABLE",
        "message": "Instagram latest updates are temporarily unavailable."
      }
    ]
  }
}
```

## GET /v1/influencers Response

```json
{
  "200": {
    "meta": {
      "requestId": "req_list_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": false
    },
    "data": {
      "results": {
        "status": "success",
        "freshness": "fresh",
        "generatedAt": "2026-04-04T12:00:00Z",
        "staleAt": null,
        "data": {
          "items": [
            {
              "influencerId": "inf_haru",
              "slug": "haru",
              "name": "하루",
              "summary": "게임, 토크 중심 스트리머",
              "profileImageUrl": "https://cdn.mystarnow.com/haru.jpg",
              "categories": ["game", "talk"],
              "platforms": ["youtube", "instagram"],
              "liveStatus": "live",
              "recentActivityAt": "2026-04-04T11:55:00Z",
              "badges": ["featured"]
            }
          ],
          "pageInfo": {
            "limit": 20,
            "nextCursor": "cursor_002",
            "hasNext": true
          },
          "appliedFilters": {
            "q": "하",
            "category": null,
            "platform": ["youtube"],
            "sort": "featured"
          }
        },
        "error": null
      },
      "filters": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "categories": [
            {
              "value": "game",
              "label": "게임",
              "count": 12
            }
          ],
          "platforms": [
            {
              "value": "youtube",
              "label": "YouTube",
              "enabled": true
            },
            {
              "value": "instagram",
              "label": "Instagram",
              "enabled": true
            },
            {
              "value": "x",
              "label": "X",
              "enabled": false
            }
          ],
          "sortOptions": [
            {
              "value": "featured",
              "label": "추천순"
            },
            {
              "value": "recent_activity",
              "label": "최근 활동순"
            },
            {
              "value": "name",
              "label": "이름순"
            }
          ]
        },
        "error": null
      }
    },
    "errors": []
  }
}
```

## GET /v1/influencers/{slug} Response

```json
{
  "200": {
    "meta": {
      "requestId": "req_detail_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": true
    },
    "data": {
      "profile": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "influencerId": "inf_haru",
          "slug": "haru",
          "name": "하루",
          "bio": "게임, 토크, 일상 소통 중심",
          "profileImageUrl": "https://cdn.mystarnow.com/haru.jpg",
          "categories": ["game", "talk"],
          "isFeatured": true
        },
        "error": null
      },
      "channels": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "items": [
            {
              "platform": "youtube",
              "handle": "@haru_live",
              "channelUrl": "https://youtube.com/@haru_live",
              "isOfficial": true,
              "isPrimary": true
            },
            {
              "platform": "instagram",
              "handle": "@haru.daily",
              "channelUrl": "https://instagram.com/haru.daily",
              "isOfficial": true,
              "isPrimary": false
            }
          ]
        },
        "error": null
      },
      "liveStatus": {
        "status": "success",
        "freshness": "fresh",
        "generatedAt": "2026-04-04T11:59:30Z",
        "staleAt": null,
        "data": {
          "isLive": true,
          "platform": "youtube",
          "liveTitle": "오늘은 잡담 방송",
          "startedAt": "2026-04-04T11:40:00Z",
          "watchUrl": "https://youtube.com/watch?v=abc123"
        },
        "error": null
      },
      "recentActivities": {
        "status": "partial",
        "freshness": "stale",
        "generatedAt": "2026-04-04T11:55:00Z",
        "staleAt": "2026-04-04T12:15:00Z",
        "data": {
          "items": [
            {
              "activityId": "act_1",
              "platform": "youtube",
              "contentType": "video",
              "title": "어제 하이라이트",
              "publishedAt": "2026-04-03T10:00:00Z",
              "externalUrl": "https://youtube.com/watch?v=abc"
            }
          ],
          "pageInfo": {
            "limit": 20,
            "nextCursor": null,
            "hasNext": false
          }
        },
        "error": {
          "code": "INSTAGRAM_ACTIVITY_MISSING",
          "message": "Instagram activity is currently managed manually and may be incomplete.",
          "retryable": false,
          "source": "instagram"
        }
      },
      "schedules": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "timezone": "Asia/Seoul",
          "items": [
            {
              "scheduleId": "sch_1",
              "title": "주말 개인 방송",
              "scheduledAt": "2026-04-05T11:00:00Z",
              "platform": "youtube",
              "note": "변동 가능"
            }
          ]
        },
        "error": null
      },
      "relatedTags": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "items": ["game", "talk"]
        },
        "error": null
      }
    },
    "errors": [
      {
        "scope": "section",
        "section": "recentActivities",
        "code": "INSTAGRAM_ACTIVITY_MISSING",
        "message": "Instagram activity is currently managed manually and may be incomplete."
      }
    ]
  }
}
```

## GET /v1/meta/app-config Response

```json
{
  "200": {
    "meta": {
      "requestId": "req_cfg_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": false
    },
    "data": {
      "runtime": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "minimumSupportedAppVersion": "1.0.0",
          "defaultPageSize": 20,
          "maxPageSize": 50,
          "supportedPlatforms": [
            {
              "platform": "youtube",
              "enabled": true,
              "supportMode": "auto"
            },
            {
              "platform": "instagram",
              "enabled": true,
              "supportMode": "limited"
            },
            {
              "platform": "x",
              "enabled": false,
              "supportMode": "disabled"
            },
            {
              "platform": "chzzk",
              "enabled": false,
              "supportMode": "disabled"
            },
            {
              "platform": "soop",
              "enabled": false,
              "supportMode": "disabled"
            }
          ]
        },
        "error": null
      },
      "featureFlags": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "showSchedules": true,
          "showRecentActivities": true,
          "enableLiveNow": true
        },
        "error": null
      },
      "sectionVisibility": {
        "status": "success",
        "freshness": "manual",
        "generatedAt": "2026-04-04T11:50:00Z",
        "staleAt": null,
        "data": {
          "home": {
            "liveNow": true,
            "latestUpdates": true,
            "todaySchedules": true,
            "featuredInfluencers": true
          },
          "detail": {
            "channels": true,
            "liveStatus": true,
            "recentActivities": true,
            "schedules": true,
            "relatedTags": true
          }
        },
        "error": null
      }
    },
    "errors": []
  }
}
```

# Error Responses

## Transport-level Errors

```json
{
  "400": {
    "meta": {
      "requestId": "req_bad_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": false
    },
    "data": null,
    "errors": [
      {
        "scope": "request",
        "section": null,
        "code": "INVALID_QUERY",
        "message": "The query parameter 'limit' must be between 1 and 50."
      }
    ]
  },
  "404": {
    "meta": {
      "requestId": "req_not_found_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": false
    },
    "data": null,
    "errors": [
      {
        "scope": "request",
        "section": null,
        "code": "INFLUENCER_NOT_FOUND",
        "message": "No influencer exists for slug 'haru-unknown'."
      }
    ]
  },
  "429": {
    "meta": {
      "requestId": "req_rate_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
      "partialFailure": false
    },
    "data": null,
    "errors": [
      {
        "scope": "request",
        "section": null,
        "code": "RATE_LIMITED",
        "message": "Too many requests. Please retry later."
      }
    ]
  },
  "503": {
    "meta": {
      "requestId": "req_unavailable_001",
      "apiVersion": "v1",
      "generatedAt": "2026-04-04T12:00:00Z",
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
}
```

## Partial Failure Rule

```json
{
  "rule": "If at least one core section can be returned, the endpoint should return HTTP 200 and encode section failures in data.<section>.status + errors[].",
  "exception": "Use 5xx only when the endpoint cannot produce a meaningful top-level payload."
}
```

# Design Notes

## Stable Response Shape

- Every endpoint uses the same top-level envelope: `meta`, `data`, `errors`.
- Every UI block is wrapped in a `SectionState`.
- `status` + `freshness` are always present so the client can render without schema branching.

## Frontend-friendly Shape

- Home and detail payloads are shaped by screen sections, not normalized storage internals.
- Filters are returned together with list results to reduce follow-up round trips.
- Pagination state is embedded under the section that owns the collection.

## Partial Failure Semantics

- Section failure never changes the section key; it only changes `status` and `error`.
- A failed section still returns `data` in the same position, usually as an empty object or empty array.
- The top-level `errors[]` mirrors section failures for analytics and logging simplicity.

## Future Platform Expansion

- Platform enum already includes `x`, `chzzk`, and `soop`.
- Actual production support is controlled by `/v1/meta/app-config.data.runtime.data.supportedPlatforms`.
- This keeps schema backward-compatible when new platforms are enabled later.

## Current Phase 1 Interpretation

- `youtube`: `supportMode=auto`
- `instagram`: `supportMode=limited`
- `x`, `chzzk`, `soop`: `supportMode=disabled`

# Open Questions

- Should `/v1/influencers` return `approximateTotal` for analytics or keep cursor-only pagination?
- Should detail activities support a dedicated `nextCursor` endpoint later, or stay capped for MVP?
- Should `supportedPlatforms` include disabled future platforms, or only enabled ones?
- How much Instagram content should be represented in `recentActivities` before manual-only data becomes misleading?
- Do we need a dedicated `warnings` top-level field, or is `errors[] + section.status` sufficient?
