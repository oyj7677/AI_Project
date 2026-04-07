---
id: ADAPTER-SPEC-MYSTARNOW-IDOL-YOUTUBE-PLATFORMS
type: spec
status: draft
owner:
created: 2026-04-05
updated: 2026-04-05
related_prd: PRD-MYSTARNOW-IDOL-YOUTUBE-BFF-SERVER
related_adrs:
  - ADR-MYSTARNOW-IDOL-YOUTUBE-BFF-ARCHITECTURE
---

# Overview

이 문서는 `MyStarNow Idol YouTube BFF`의 외부 연동 경계를 정의한다. 1차 범위는 `YouTube only`이며, 핵심은 채널/영상 메타데이터 자동 수집과 운영자가 관리하는 그룹-멤버-채널 관계를 안전하게 결합하는 것이다.

원칙:

- 외부 API 원본 구조는 adapter 안에서 끝낸다.
- 내부에서는 `Group`, `Member`, `Channel`, `Video` 중심 정규화 모델만 사용한다.
- 관계 데이터는 adapter가 아니라 운영 입력이 source of truth다.
- adapter는 운영 override를 직접 판단하지 않고, normalized 저장 이후 merge 단계에서 적용한다.
- 수집 실패는 채널 단위로 격리한다.

# Scope

## Included

- YouTube 채널 메타데이터 수집
- YouTube 최신 영상 목록 수집
- raw payload 저장
- sync metadata 기록
- stale / retry / backoff 전략

## Excluded

- Instagram 등 타 플랫폼
- 실시간 라이브 전용 수집
- 댓글 / 좋아요 / 통계 지표
- 완전 자동 그룹-멤버-채널 관계 추론

# Active Platform

## YouTube

### Current Status

- Phase 1 active
- Preferred source type: official API

### What Data Is Collected

- 채널 제목
- 채널 설명
- 채널 이미지
- 업로드 플레이리스트 정보
- 최근 업로드 영상 목록
- 영상 제목
- 영상 설명
- 썸네일
- 게시 시각
- watch URL

### Source Type

- Official API: `YouTube Data API v3`
- Manual input: 그룹/멤버/채널 관계, 수동 영상 등록
- Scraping: 포함하지 않음

# Input To Adapter

adapter가 읽는 기본 입력은 운영자가 등록한 채널 레지스트리다.

채널 registry 기준 필수 값:

- `platform_code = youtube`
- `external_channel_id`
- `channel_url`
- `owner_type`
- `owner_group_id` 또는 `owner_member_id`
- `channel_type`

선택 값:

- `handle`
- `display_label`

설명:

- adapter는 "이 채널이 어떤 그룹 소속인지"를 판단하지 않는다.
- adapter는 단지 등록된 YouTube 채널에 대해 메타데이터와 영상을 가져온다.

# Sync Scopes

## `channel_profile`

용도:

- 채널 기본 메타데이터 동기화

수집 결과:

- 채널 제목
- 채널 설명
- 채널 이미지
- 업로드 playlist id
- 마지막 확인 시각

## `channel_videos`

용도:

- 최근 업로드 영상 목록 동기화

수집 결과:

- 최근 영상 메타데이터
- 삭제/비공개 여부 감지 가능 시 반영
- latest video 기준 serving state 갱신

# Official API Usage

## Step 1. Channel Metadata Fetch

사용 목적:

- 채널의 기본 메타정보와 uploads playlist id 확보

권장 endpoint 조합:

- `channels.list`

필요 part 예시:

- `snippet`
- `contentDetails`

정규화 대상:

- `snippet.title` -> `youtube_channels.display_label`
- `snippet.description` -> 채널 설명 raw 저장
- `snippet.thumbnails` -> 채널 대표 이미지 raw 저장
- `contentDetails.relatedPlaylists.uploads` -> 다음 단계 입력

## Step 2. Recent Video IDs Fetch

사용 목적:

- uploads playlist 기반 최근 영상 id 확보

권장 endpoint 조합:

- `playlistItems.list`

필요 필드 예시:

- `contentDetails.videoId`
- `contentDetails.videoPublishedAt`
- `snippet.title`
- `snippet.thumbnails`

정규화 대상:

- 외부 video id
- 게시 시각
- 기본 제목 / 썸네일

## Step 3. Video Detail Enrichment

사용 목적:

- 영상 상세 메타데이터 보강

권장 endpoint 조합:

- `videos.list`

필요 part 예시:

- `snippet`
- `contentDetails`
- 필요 시 `status`

정규화 대상:

- `title`
- `description`
- `thumbnailUrl`
- `publishedAt`
- `videoUrl`

# Normalization Rules

## Channel Normalization

저장 대상:

- `external_channel_id`
- `handle`
- `channel_url`
- `display_label`
- `last_seen_at`

merge 규칙:

- `channel_operator_metadata.override_display_label`가 있으면 자동 수집값보다 우선
- `override_handle`, `override_channel_url`가 있으면 우선

## Video Normalization

저장 대상:

- `external_video_id`
- `title`
- `description`
- `thumbnail_url`
- `published_at`
- `video_url`
- `source_type = youtube_imported`
- `freshness_status = fresh`

merge 규칙:

- `video_operator_metadata`가 있으면 override 적용
- 수동 등록 영상은 `source_type = manual`

## Content Display Rules

- 홈 피드는 `youtube_videos`를 그룹 기준으로 재정렬한다.
- 그룹 상세는 공식 채널과 멤버 개인 채널 영상을 합쳐서 정렬한다.
- 멤버 상세는 `owner_member_id` 기반 채널의 영상만 사용한다.

# Scheduling Strategy

## Channel Profile Polling

- 기본 주기: 6시간 ~ 24시간
- 목적:
  - 채널 제목 변경
  - 채널 이미지 변경
  - uploads playlist id 확인

## Channel Video Polling

- 기본 주기: 10분 ~ 30분
- 목적:
  - 최근 영상 반영
  - 홈/상세 피드 최신성 유지

## Dynamic Adjustment

- hot channel은 짧은 주기 허용
- inactive channel은 긴 주기 허용
- 연속 실패 시 backoff

# Retry / Backoff Strategy

## Retry Policy

- 채널 단위 retry
- 1회 sync 시 소수 횟수 재시도
- timeout, 5xx, 일시 네트워크 실패만 retry 대상

## Backoff Policy

- 연속 실패 수 증가에 따라 다음 스케줄 지연
- scope별로 backoff 관리
  - `channel_profile`
  - `channel_videos`

## Failure Recording

- `youtube_sync_metadata.last_status`
- `youtube_sync_metadata.last_error_code`
- `youtube_sync_metadata.last_error_message`
- `youtube_sync_metadata.consecutive_failures`
- `youtube_sync_metadata.backoff_until`

# Raw / Normalized / Serving Boundary

## Raw Layer

- `raw_source_records`
- YouTube API 원본 payload 저장
- 디버깅 / 회귀 분석 / 재정규화 용도

## Normalized Layer

- `youtube_channels`
- `youtube_videos`
- API 원본 구조 제거
- 내부 공통 모델 유지

## Serving Layer

- `group_serving_state`
- `member_serving_state`
- 홈 / 그룹 상세 / 멤버 상세 응답용 파생 상태

# Stale / Degraded Behavior

## When Profile Sync Fails

- 마지막 정상 채널 메타 유지
- channel section freshness를 `stale`로 전환 가능
- 전체 그룹 상세는 계속 응답

## When Video Sync Fails

- 마지막 정상 영상 목록 유지
- recentVideos section을 `partial` 또는 `failed`로 마킹
- 홈은 다른 그룹 영상이 있으면 계속 렌더링

## When No Data Exists Yet

- section status는 `empty`
- 운영자에게 수동 영상 등록 경로 제공

# Observability

필수 지표:

- sync success count
- sync failure count
- stale group count
- stale channel count
- endpoint latency
- operator manual video write count

필수 로그:

- channel id
- external channel id
- scope
- attempt time
- failure code
- retry/backoff 결정

# Operational Notes

- YouTube quota는 channel count 증가에 따라 빠르게 부담이 커질 수 있으므로, 채널 profile과 video sync를 분리해야 한다.
- 수집 자체보다 운영자가 관계를 잘못 연결하는 것이 더 큰 리스크일 수 있다.
- 따라서 adapter보다 registry 품질 관리가 더 중요하다.
- 수동 영상 등록은 자동 수집 전 공백을 메우는 fallback 수단으로 유지한다.

# Risks

- 잘못된 external channel id를 등록하면 sync가 전부 실패한다.
- 채널 유형 분류 오류가 있으면 홈/상세 UI가 잘못 보일 수 있다.
- YouTube API quota 또는 정책 변경에 대응해야 한다.
- channel_videos polling 간격이 너무 길면 최근 업로드 반영이 늦어진다.

# Open Questions

- Shorts를 별도 content type으로 강하게 노출할지
- live stream archive와 일반 영상을 같은 피드에서 어떻게 구분할지
- 삭제/비공개 영상의 soft hide 정책을 둘지
- uploads playlist 이외 추가 source가 필요한지

# Links

- Related PRD:
  - `../product/PRD-mystarnow-idol-youtube-bff-server.md`
- Related ADRs:
  - `../architecture/ADR-mystarnow-idol-youtube-bff-architecture.md`
- Related Decisions:
  - `../architecture/TRD-mystarnow-idol-youtube-bff-server.md`
