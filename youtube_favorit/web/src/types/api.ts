export type SectionStatus = 'success' | 'partial' | 'failed' | 'empty'
export type FreshnessStatus = 'fresh' | 'stale' | 'manual' | 'unknown'

export interface ResponseMeta {
  requestId: string
  apiVersion: string
  generatedAt: string
  partialFailure: boolean
}

export interface ApiError {
  scope: 'request' | 'section' | string
  section: string | null
  code: string
  message: string
}

export interface SectionError {
  code: string
  message: string
  retryable: boolean
  source?: string | null
}

export interface SectionState<T> {
  status: SectionStatus
  freshness: FreshnessStatus
  generatedAt: string
  staleAt?: string | null
  data: T
  error?: SectionError | null
}

export interface ApiEnvelope<T> {
  meta: ResponseMeta
  data: T | null
  errors: ApiError[]
}

export interface PageInfo {
  limit: number
  nextCursor?: string | null
  hasNext: boolean
}

export interface GroupRef {
  groupId: string
  groupSlug: string
  groupName: string
}

export interface MemberRef {
  memberId: string
  memberSlug: string
  memberName: string
}

export interface ChannelSummary {
  channelId: string
  externalChannelId: string
  channelName: string
  handle?: string | null
  channelUrl: string
  channelType: string
  isOfficial: boolean
}

export interface VideoFeedItem {
  videoId: string
  externalVideoId?: string | null
  title: string
  description?: string | null
  thumbnailUrl?: string | null
  publishedAt: string
  videoUrl: string
  contentType: string
  channel: ChannelSummary
  group: GroupRef
  member?: MemberRef | null
  badges: string[]
}

export interface HomeFeaturedGroupItem {
  groupId: string
  groupSlug: string
  groupName: string
  coverImageUrl?: string | null
  officialChannelCount: number
  memberPersonalChannelCount: number
  latestVideoAt?: string | null
}

export interface HomePayload {
  recentVideos: SectionState<{
    items: VideoFeedItem[]
    pageInfo: PageInfo
  }>
  featuredGroups: SectionState<{
    items: HomeFeaturedGroupItem[]
  }>
}

export interface GroupListItem {
  groupId: string
  groupSlug: string
  groupName: string
  description?: string | null
  coverImageUrl?: string | null
  officialChannelCount: number
  memberCount: number
  memberPersonalChannelCount: number
  latestVideoAt?: string | null
  latestVideoThumbnailUrl?: string | null
  badges: string[]
}

export interface GroupListPayload {
  results: SectionState<{
    items: GroupListItem[]
    pageInfo: PageInfo
    appliedFilters: {
      q?: string | null
      sort: string
    }
  }>
  filters: SectionState<{
    sortOptions: Array<{
      value: string
      label: string
    }>
  }>
}

export interface GroupHeaderData {
  groupId: string
  groupSlug: string
  groupName: string
  description?: string | null
  coverImageUrl?: string | null
  memberCount: number
  officialChannelCount: number
  memberPersonalChannelCount: number
  latestVideoAt?: string | null
  isFeatured: boolean
}

export interface GroupOfficialChannelItem {
  channelId: string
  externalChannelId: string
  channelName: string
  handle?: string | null
  channelUrl: string
  channelType: string
  isOfficial: boolean
  latestVideoAt?: string | null
}

export interface GroupMemberItem {
  memberId: string
  memberSlug: string
  memberName: string
  profileImageUrl?: string | null
  hasPersonalChannel: boolean
  personalChannelCount: number
  latestVideoAt?: string | null
}

export interface GroupDetailPayload {
  groupHeader: SectionState<GroupHeaderData>
  officialChannels: SectionState<{
    items: GroupOfficialChannelItem[]
  }>
  members: SectionState<{
    items: GroupMemberItem[]
  }>
  recentVideos: SectionState<{
    items: VideoFeedItem[]
    pageInfo: PageInfo
  }>
  detailMeta: SectionState<{
    supportedPlatforms: string[]
    channelTypes: string[]
  }>
}

export interface MemberDetailPayload {
  memberProfile: SectionState<{
    memberId: string
    memberSlug: string
    memberName: string
    profileImageUrl?: string | null
    group: GroupRef
  }>
  personalChannels: SectionState<{
    items: Array<{
      channelId: string
      externalChannelId: string
      channelName: string
      handle?: string | null
      channelUrl: string
      channelType: string
      isOfficial: boolean
      latestVideoAt?: string | null
    }>
  }>
  recentVideos: SectionState<{
    items: VideoFeedItem[]
    pageInfo: PageInfo
  }>
}

export interface SupportedPlatformItem {
  platform: string
  enabled: boolean
  supportMode: string
}

export interface AppConfigPayload {
  runtime: SectionState<{
    minimumSupportedAppVersion: string
    defaultPageSize: number
    maxPageSize: number
    supportedPlatforms: SupportedPlatformItem[]
  }>
  featureFlags: SectionState<{
    showSchedules: boolean
    showRecentActivities: boolean
    enableLiveNow: boolean
    showMemberDetail: boolean
    showFeaturedGroups: boolean
    showVideoFeed: boolean
  }>
}

export interface ConnectivityState {
  phase: 'idle' | 'checking' | 'online' | 'offline'
  checkedAt?: string
  durationMs?: number
  statusCode?: number
  message?: string
}
