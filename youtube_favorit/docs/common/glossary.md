# Glossary

`MyStarNow` 프로젝트에서 반복해서 쓰는 핵심 용어를 정리한다.  
이 문서의 목적은 기획, 프론트, 백엔드, 운영 문서에서 같은 단어를 같은 의미로 쓰게 만드는 것이다.

# Core Product Terms

## MyStarNow

인플루언서의 여러 플랫폼 소식을 한곳에서 모아보는 서비스 이름.

## Influencer

서비스가 추적하는 사람 단위의 핵심 엔티티.  
앱과 서버는 플랫폼 계정 자체보다 `인플루언서`를 중심 개념으로 본다.

예:

- 하루
- 민도

## Channel

플랫폼별 실제 계정 또는 채널 단위 엔티티.  
한 인플루언서는 여러 채널을 가질 수 있다.

예:

- YouTube 채널
- Instagram 계정

## Platform

외부 데이터 소스 제공자.  
현재 문서에서 다루는 플랫폼:

- YouTube
- Instagram
- X
- CHZZK
- SOOP

# Domain Terms

## Official Channel

운영자가 공식으로 인정한 채널.  
자동 수집 결과와 별개로 운영자 검토를 통해 확정될 수 있다.

## Primary Channel

한 플랫폼 안에서 대표로 취급하는 채널.  
예를 들어 한 인플루언서가 YouTube 채널을 여러 개 가질 경우, 홈/목록에서 우선적으로 보여줄 대표 채널을 의미한다.

## Activity Item

플랫폼별 최근 활동을 앱에서 공통 형태로 표현한 정규화 엔티티.

포함 가능 예:

- YouTube 영상
- YouTube Shorts
- Instagram 게시물
- Instagram Reels
- 방송 공지

## Live Status

특정 채널 또는 인플루언서가 현재 라이브 중인지 표현하는 상태 모델.

대표 필드:

- `isLive`
- `platform`
- `liveTitle`
- `startedAt`
- `watchUrl`

## Schedule Item

예정된 방송 또는 운영자가 등록한 일정 항목.

## Category

인플루언서를 분류하기 위한 서비스 내부 태그 성격의 값.

예:

- game
- talk
- music

# API / Serving Terms

## BFF

Backend for Frontend.  
모바일 앱 화면에 맞는 응답을 제공하는 서버 계층으로, 외부 플랫폼 API를 직접 노출하지 않는다.

## Section

API 응답에서 화면 단위로 구분되는 블록.

예:

- `liveNow`
- `latestUpdates`
- `todaySchedules`
- `featuredInfluencers`
- `recentActivities`

## Partial Failure

응답 전체는 성공했지만 일부 섹션만 실패한 상태.  
HTTP 200을 유지하면서 section-level 상태로 표현한다.

## Degraded Response

fresh 데이터 대신 stale 또는 manual fallback 데이터를 사용한 응답 상태.

## Stable Response Shape

일부 데이터가 실패해도 응답 key 구조는 유지하는 원칙.  
즉, section이 사라지지 않고 `status`, `error`, `freshness` 값이 바뀐다.

# Data / Reliability Terms

## Normalized Domain Model

플랫폼별 원본 포맷을 내부 공통 모델로 바꾼 결과.  
클라이언트와 serving layer는 이 모델만 다뤄야 한다.

## Raw Source Data

외부 플랫폼에서 받아온 원본 payload.  
디버깅, 재처리, provenance 추적용으로 저장할 수 있다.

## Serving Projection

홈/목록/상세 조회를 빠르게 하기 위해 미리 만들어둔 읽기 최적화 데이터.

## Fresh

정책상 허용된 최신성 기준 안에 있는 데이터.

## Stale

최신성 기준은 넘었지만 아직 fallback으로 사용할 수 있는 데이터.

## Manual

운영자 입력 또는 운영자 보정이 반영된 데이터 상태.

## Unknown

fresh/stale/manual 여부를 신뢰성 있게 판단할 수 없는 상태.

## Fallback

원래 source가 실패했을 때 대신 사용하는 데이터 전략.

우선순위 기본:

```text
operator override -> last successful cache -> previous projection -> empty
```

## Provenance

현재 값이 어디서 왔는지 추적 가능한 정보.

예:

- YouTube API
- Instagram manual entry
- operator override

## Polling

외부 플랫폼을 주기적으로 조회해 데이터를 갱신하는 방식.

## Sync Metadata

플랫폼 수집 작업의 마지막 성공 시각, 마지막 실패, backoff 상태, cursor 등을 저장하는 운영 메타데이터.

# Ops Terms

## Operator

운영자.  
링크 수정, 일정 입력, 공식 채널 판별, fallback 데이터 입력을 담당한다.

## Override

자동 수집 결과보다 운영자 입력을 우선 적용하는 규칙.

## Support Mode

플랫폼의 현재 운영 상태.

예:

- `auto`
- `limited`
- `manual`
- `disabled`

# Current Phase Terms

## Phase 1 Active Platforms

현재 1차에서 실제로 활성인 플랫폼.

- YouTube
- Instagram

## Disabled Future Platforms

명세는 있지만 기본 비활성 상태인 플랫폼.

- X
- CHZZK
- SOOP

# Usage Rule

새 문서에서 새로운 용어를 만들기 전에 이 문서에 있는지 먼저 확인한다.  
없다면 먼저 여기에 정의를 추가한 뒤 문서에서 사용한다.
