import type {
  ApiEnvelope,
  AppConfigPayload,
  ConnectivityState,
  GroupDetailPayload,
  GroupListPayload,
  HomePayload,
  MemberDetailPayload,
} from '../types/api'

export class ApiClientError extends Error {
  status?: number

  constructor(message: string, status?: number) {
    super(message)
    this.name = 'ApiClientError'
    this.status = status
  }
}

const proxyPrefix = '/__mystarnow_api'

export interface RequestOptions {
  signal?: AbortSignal
  headers?: Record<string, string>
}

export interface OperatorAuth {
  username: string
  password: string
}

function formatHealthStatus(status?: string) {
  switch (status?.trim().toUpperCase()) {
    case 'UP':
      return '정상'
    case 'DOWN':
      return '장애'
    default:
      return status?.trim() || '알 수 없음'
  }
}

async function parseJson<T>(response: Response): Promise<T> {
  const text = await response.text()
  if (!text) {
    throw new ApiClientError('백엔드가 비어 있는 응답을 반환했습니다.', response.status)
  }
  try {
    return JSON.parse(text) as T
  } catch {
    throw new ApiClientError('백엔드 응답이 올바른 JSON 형식이 아닙니다.', response.status)
  }
}

export async function requestEnvelope<T>(
  baseUrl: string,
  path: string,
  options: RequestOptions = {},
): Promise<ApiEnvelope<T>> {
  const response = await fetch(`${proxyPrefix}${path}`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
      'X-MyStarNow-Target': baseUrl,
      ...options.headers,
    },
    signal: options.signal,
  }).catch((error: unknown) => {
    throw new ApiClientError(
      error instanceof Error ? error.message : '백엔드에 연결하지 못했습니다.',
    )
  })

  const payload = await parseJson<ApiEnvelope<T>>(response)

  if (!response.ok) {
    throw new ApiClientError(
      payload.errors?.[0]?.message ??
        `백엔드 요청이 상태 코드 ${response.status}로 실패했습니다.`,
      response.status,
    )
  }

  return payload
}

function basicAuthHeader(auth: OperatorAuth) {
  return `Basic ${btoa(`${auth.username}:${auth.password}`)}`
}

export async function requestMutation<TResponse>(
  baseUrl: string,
  path: string,
  method: 'POST' | 'PUT',
  body: unknown,
  auth?: OperatorAuth,
) {
  const response = await fetch(`${proxyPrefix}${path}`, {
    method,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'X-MyStarNow-Target': baseUrl,
      ...(auth ? { Authorization: basicAuthHeader(auth) } : {}),
    },
    body: JSON.stringify(body),
  }).catch((error: unknown) => {
    throw new ApiClientError(
      error instanceof Error ? error.message : '백엔드에 연결하지 못했습니다.',
    )
  })

  const payload = await parseJson<TResponse>(response)

  if (!response.ok) {
    const maybeMessage =
      typeof payload === 'object' &&
      payload !== null &&
      'errors' in payload &&
      Array.isArray((payload as { errors?: unknown[] }).errors)
        ? (((payload as { errors: Array<{ message?: string }> }).errors[0]?.message) ??
            `백엔드 요청이 상태 코드 ${response.status}로 실패했습니다.`)
        : `백엔드 요청이 상태 코드 ${response.status}로 실패했습니다.`
    throw new ApiClientError(maybeMessage, response.status)
  }

  return payload
}

export interface HomeQuery {
  cursor?: string | null
  limit?: number
  contentType?: string | null
}

export interface GroupsQuery {
  q?: string
  sort?: string
  cursor?: string | null
  limit?: number
}

export interface GroupVideosQuery {
  videosCursor?: string | null
  videosLimit?: number
  contentType?: string | null
}

function buildSearch<T extends object>(query: T) {
  const search = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (
      (typeof value === 'string' && value.trim() !== '') ||
      typeof value === 'number'
    ) {
      search.set(key, String(value))
    }
  }
  return search
}

export async function fetchHome(
  baseUrl: string,
  query: HomeQuery = {},
  signal?: AbortSignal,
) {
  const search = buildSearch(query)
  return requestEnvelope<HomePayload>(
    baseUrl,
    `/v1/home${search.size > 0 ? `?${search.toString()}` : ''}`,
    { signal },
  )
}

export async function fetchGroups(
  baseUrl: string,
  query: GroupsQuery = {},
  signal?: AbortSignal,
) {
  const search = buildSearch(query)
  return requestEnvelope<GroupListPayload>(
    baseUrl,
    `/v1/groups${search.size > 0 ? `?${search.toString()}` : ''}`,
    { signal },
  )
}

export async function fetchGroupDetail(
  baseUrl: string,
  groupSlug: string,
  query: GroupVideosQuery = {},
  signal?: AbortSignal,
) {
  const search = buildSearch(query)
  return requestEnvelope<GroupDetailPayload>(
    baseUrl,
    `/v1/groups/${groupSlug}${search.size > 0 ? `?${search.toString()}` : ''}`,
    { signal },
  )
}

export async function fetchMemberDetail(
  baseUrl: string,
  memberSlug: string,
  query: GroupVideosQuery = {},
  signal?: AbortSignal,
) {
  const search = buildSearch(query)
  return requestEnvelope<MemberDetailPayload>(
    baseUrl,
    `/v1/members/${memberSlug}${search.size > 0 ? `?${search.toString()}` : ''}`,
    { signal },
  )
}

export async function fetchAppConfig(baseUrl: string, signal?: AbortSignal) {
  return requestEnvelope<AppConfigPayload>(
    baseUrl,
    '/v1/meta/app-config?clientPlatform=web',
    { signal },
  )
}

export interface OperatorMutationResponse {
  status: string
  entityId: string
}

export interface GroupOperatorCreateRequest {
  slug: string
  displayName: string
  description?: string
  coverImageUrl?: string
  featured?: boolean
  note?: string
}

export interface GroupOperatorUpdateRequest {
  displayName?: string
  description?: string
  coverImageUrl?: string
  featured?: boolean
  status?: string
  note?: string
}

export interface MemberOperatorCreateRequest {
  groupId: string
  slug: string
  displayName: string
  profileImageUrl?: string
  sortOrder?: number
  note?: string
}

export interface MemberOperatorUpdateRequest {
  displayName?: string
  profileImageUrl?: string
  sortOrder?: number
  status?: string
  note?: string
}

export interface ChannelOperatorCreateRequest {
  platformCode: string
  externalChannelId: string
  handle?: string
  channelUrl: string
  displayLabel?: string
  channelType: string
  ownerType: string
  ownerGroupId?: string
  ownerMemberId?: string
  isOfficial?: boolean
  isPrimary?: boolean
  note?: string
}

export interface ChannelOperatorUpdateRequest {
  handle?: string
  channelUrl?: string
  displayLabel?: string
  channelType?: string
  ownerType?: string
  ownerGroupId?: string
  ownerMemberId?: string
  isOfficial?: boolean
  isPrimary?: boolean
  status?: string
  note?: string
}

export interface VideoOperatorCreateRequest {
  channelId: string
  externalVideoId?: string
  title: string
  description?: string
  thumbnailUrl?: string
  publishedAt: string
  videoUrl: string
  contentType?: string
  pinned?: boolean
  note?: string
}

export interface VideoOperatorUpdateRequest {
  title?: string
  description?: string
  thumbnailUrl?: string
  publishedAt?: string
  videoUrl?: string
  contentType?: string
  pinned?: boolean
  note?: string
}

export async function createGroup(
  baseUrl: string,
  body: GroupOperatorCreateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    '/internal/operator/groups',
    'POST',
    body,
    auth,
  )
}

export async function updateGroup(
  baseUrl: string,
  groupSlug: string,
  body: GroupOperatorUpdateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    `/internal/operator/groups/${groupSlug}`,
    'PUT',
    body,
    auth,
  )
}

export async function createMember(
  baseUrl: string,
  body: MemberOperatorCreateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    '/internal/operator/members',
    'POST',
    body,
    auth,
  )
}

export async function updateMember(
  baseUrl: string,
  memberSlug: string,
  body: MemberOperatorUpdateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    `/internal/operator/members/${memberSlug}`,
    'PUT',
    body,
    auth,
  )
}

export async function createChannel(
  baseUrl: string,
  body: ChannelOperatorCreateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    '/internal/operator/channels',
    'POST',
    body,
    auth,
  )
}

export async function updateChannel(
  baseUrl: string,
  channelId: string,
  body: ChannelOperatorUpdateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    `/internal/operator/channels/${channelId}`,
    'PUT',
    body,
    auth,
  )
}

export async function createVideo(
  baseUrl: string,
  body: VideoOperatorCreateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    '/internal/operator/videos',
    'POST',
    body,
    auth,
  )
}

export async function updateVideo(
  baseUrl: string,
  videoId: string,
  body: VideoOperatorUpdateRequest,
  auth: OperatorAuth,
) {
  return requestMutation<OperatorMutationResponse>(
    baseUrl,
    `/internal/operator/videos/${videoId}`,
    'PUT',
    body,
    auth,
  )
}

export async function checkBackendConnectivity(baseUrl: string): Promise<ConnectivityState> {
  const startedAt = performance.now()

  try {
    const response = await fetch(`${proxyPrefix}/actuator/health`, {
      headers: {
        Accept: 'application/json',
        'X-MyStarNow-Target': baseUrl,
      },
    })
    const durationMs = Math.round(performance.now() - startedAt)

    if (!response.ok) {
      return {
        phase: 'offline',
        checkedAt: new Date().toISOString(),
        durationMs,
        statusCode: response.status,
        message: `헬스 체크가 상태 코드 ${response.status}로 실패했습니다.`,
      }
    }

    const payload = await parseJson<{ status?: string }>(response)
    return {
      phase: 'online',
      checkedAt: new Date().toISOString(),
      durationMs,
      statusCode: response.status,
      message: payload.status
        ? `백엔드 상태: ${formatHealthStatus(payload.status)}`
        : '백엔드에 연결되었습니다.',
    }
  } catch (error) {
    return {
      phase: 'offline',
      checkedAt: new Date().toISOString(),
      durationMs: Math.round(performance.now() - startedAt),
      message: error instanceof Error ? error.message : '연결 확인에 실패했습니다.',
    }
  }
}
