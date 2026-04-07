function normalizeLabelValue(value?: string | null) {
  return (value ?? '')
    .trim()
    .toLowerCase()
    .replace(/[\s-]+/g, '_')
}

export function formatPlatformLabel(platform?: string | null) {
  const normalized = normalizeLabelValue(platform)

  switch (normalized) {
    case 'youtube':
      return '유튜브'
    case 'instagram':
      return '인스타그램'
    case 'x':
      return 'X'
    case 'chzzk':
      return '치지직'
    case 'soop':
      return '숲'
    case 'manual':
      return '수동 입력'
    case 'unknown':
      return '알 수 없음'
    default:
      return platform?.trim() || '-'
  }
}

export function formatLiveStatusLabel(status?: string | null) {
  const normalized = normalizeLabelValue(status)

  switch (normalized) {
    case 'live':
    case 'live_now':
    case 'on_air':
    case 'is_live':
    case 'online':
      return '방송 중'
    case 'offline':
    case 'off_air':
      return '오프라인'
    case 'scheduled':
    case 'upcoming':
      return '예정됨'
    case 'ended':
      return '종료됨'
    case 'unknown':
    case '':
      return '알 수 없음'
    default:
      return status?.trim() || '알 수 없음'
  }
}

export function formatContentTypeLabel(contentType?: string | null) {
  const normalized = normalizeLabelValue(contentType)

  switch (normalized) {
    case 'video':
      return '영상'
    case 'short':
    case 'shorts':
      return '쇼츠'
    case 'live':
      return '라이브'
    case 'post':
      return '게시물'
    case 'reel':
    case 'reels':
      return '릴스'
    case 'story':
    case 'stories':
      return '스토리'
    case 'manual':
      return '수동 입력'
    case 'schedule':
      return '일정'
    default:
      return contentType?.trim() || '기타'
  }
}

export function formatBooleanLabel(value: boolean) {
  return value ? '예' : '아니오'
}

export function formatFeatureFlagLabel(value: boolean) {
  return value ? '켜짐' : '꺼짐'
}

export function formatMutationStatusLabel(status?: string | null) {
  const normalized = normalizeLabelValue(status)

  switch (normalized) {
    case 'created':
      return '생성 완료'
    case 'updated':
      return '수정 완료'
    case 'success':
    case 'ok':
      return '처리 완료'
    default:
      return status?.trim() || '처리 완료'
  }
}

export function formatChannelTypeLabel(channelType?: string | null) {
  const normalized = normalizeLabelValue(channelType)

  switch (normalized) {
    case 'group_official':
      return '그룹 공식'
    case 'member_personal':
      return '멤버 개인'
    case 'sub_unit':
      return '서브 유닛'
    case 'label':
      return '레이블'
    default:
      return channelType?.trim() || '알 수 없음'
  }
}

export function formatBadgeLabel(badge?: string | null) {
  const normalized = normalizeLabelValue(badge)

  switch (normalized) {
    case 'featured':
      return '추천'
    case 'official':
      return '공식'
    case 'group_official':
      return '그룹 공식'
    case 'member_personal':
      return '멤버 개인'
    case 'sub_unit':
      return '서브 유닛'
    case 'label':
      return '레이블'
    case 'member':
      return '멤버 채널'
    case 'pinned':
      return '고정'
    default:
      return badge?.trim() || '배지'
  }
}
