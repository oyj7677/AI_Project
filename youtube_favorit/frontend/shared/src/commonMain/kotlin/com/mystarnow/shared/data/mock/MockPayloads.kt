package com.mystarnow.shared.data.mock

internal object MockPayloads {
    const val HOME = """
        {
          "meta": {
            "requestId": "req_home_mock",
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
                    "profileImageUrl": null,
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
                    "thumbnailUrl": null,
                    "externalUrl": "https://youtube.com/watch?v=abc",
                    "summary": "명장면 모음"
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
                    "profileImageUrl": null,
                    "categories": ["game", "talk"],
                    "platforms": ["youtube", "instagram"],
                    "liveStatus": "live",
                    "recentActivityAt": "2026-04-04T11:55:00Z",
                    "badges": ["featured"]
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
    """

    const val DETAIL = """
        {
          "meta": {
            "requestId": "req_detail_mock",
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
                "profileImageUrl": null,
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
                    "influencerId": "inf_haru",
                    "platform": "youtube",
                    "contentType": "video",
                    "title": "어제 하이라이트",
                    "publishedAt": "2026-04-03T10:00:00Z",
                    "thumbnailUrl": null,
                    "externalUrl": "https://youtube.com/watch?v=abc",
                    "summary": "명장면 모음"
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
            "detailMeta": {
              "status": "success",
              "freshness": "manual",
              "generatedAt": "2026-04-04T11:50:00Z",
              "staleAt": null,
              "data": {
                "relatedTags": ["game", "talk"],
                "supportedPlatforms": ["youtube", "instagram"]
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
    """

    const val CONFIG = """
        {
          "meta": {
            "requestId": "req_cfg_mock",
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
            }
          },
          "errors": []
        }
    """
}
