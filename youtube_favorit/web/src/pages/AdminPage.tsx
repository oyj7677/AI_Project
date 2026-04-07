import { useEffect, useState } from 'react'
import {
  createChannel,
  createGroup,
  createMember,
  createVideo,
  fetchGroups,
  fetchMemberDetail,
  updateChannel,
  updateGroup,
  updateMember,
  updateVideo,
  type ChannelOperatorCreateRequest,
  type ChannelOperatorUpdateRequest,
  type GroupOperatorCreateRequest,
  type GroupOperatorUpdateRequest,
  type MemberOperatorCreateRequest,
  type MemberOperatorUpdateRequest,
  type OperatorMutationResponse,
  type VideoOperatorCreateRequest,
  type VideoOperatorUpdateRequest,
} from '../api/client'
import { useDeveloperSettings } from '../app/useDeveloperSettings'
import type { GroupListItem } from '../types/api'
import {
  formatBooleanLabel,
  formatChannelTypeLabel,
  formatMutationStatusLabel,
} from '../utils/display'

type MutationState =
  | { phase: 'idle' }
  | { phase: 'loading' }
  | { phase: 'success'; response: OperatorMutationResponse }
  | { phase: 'error'; message: string }

interface SearchableGroupFieldProps {
  label: string
  placeholder: string
  query: string
  setQuery: (value: string) => void
  selectedGroupId: string
  setSelectedGroupId: (value: string) => void
  groups: GroupListItem[]
}

function defaultMutationState(): MutationState {
  return { phase: 'idle' }
}

export function AdminPage() {
  const {
    backendUrl,
    operatorUsername,
    operatorPassword,
    draftOperatorUsername,
    draftOperatorPassword,
    setDraftOperatorUsername,
    setDraftOperatorPassword,
    saveOperatorCredentials,
    refetchVersion,
  } = useDeveloperSettings()

  const [availableGroups, setAvailableGroups] = useState<GroupListItem[]>([])

  const [groupCreate, setGroupCreate] = useState<GroupOperatorCreateRequest>({
    slug: '',
    displayName: '',
    description: '',
    coverImageUrl: '',
    featured: true,
    note: '',
  })
  const [groupSlug, setGroupSlug] = useState('')
  const [groupUpdate, setGroupUpdate] = useState<GroupOperatorUpdateRequest>({})
  const [groupCreateState, setGroupCreateState] = useState<MutationState>(defaultMutationState)
  const [groupUpdateState, setGroupUpdateState] = useState<MutationState>(defaultMutationState)
  const [quickGroupName, setQuickGroupName] = useState('')
  const [quickGroupYouTubeUrl, setQuickGroupYouTubeUrl] = useState('')
  const [quickGroupState, setQuickGroupState] = useState<MutationState>(defaultMutationState)

  const [memberCreate, setMemberCreate] = useState<MemberOperatorCreateRequest>({
    groupId: '',
    slug: '',
    displayName: '',
    profileImageUrl: '',
    sortOrder: 0,
    note: '',
  })
  const [memberSlug, setMemberSlug] = useState('')
  const [memberUpdate, setMemberUpdate] = useState<MemberOperatorUpdateRequest>({})
  const [memberCreateState, setMemberCreateState] = useState<MutationState>(defaultMutationState)
  const [memberUpdateState, setMemberUpdateState] = useState<MutationState>(defaultMutationState)
  const [quickMemberGroupId, setQuickMemberGroupId] = useState('')
  const [quickMemberGroupQuery, setQuickMemberGroupQuery] = useState('')
  const [quickMemberName, setQuickMemberName] = useState('')
  const [quickMemberYouTubeUrl, setQuickMemberYouTubeUrl] = useState('')
  const [quickMemberState, setQuickMemberState] = useState<MutationState>(defaultMutationState)
  const [memberCreateGroupQuery, setMemberCreateGroupQuery] = useState('')

  const [channelCreate, setChannelCreate] = useState<ChannelOperatorCreateRequest>({
    platformCode: 'youtube',
    externalChannelId: '',
    handle: '',
    channelUrl: '',
    displayLabel: '',
    channelType: 'GROUP_OFFICIAL',
    ownerType: 'GROUP',
    ownerGroupId: '',
    ownerMemberId: '',
    isOfficial: true,
    isPrimary: true,
    note: '',
  })
  const [channelId, setChannelId] = useState('')
  const [channelUpdate, setChannelUpdate] = useState<ChannelOperatorUpdateRequest>({})
  const [channelCreateState, setChannelCreateState] = useState<MutationState>(defaultMutationState)
  const [channelUpdateState, setChannelUpdateState] = useState<MutationState>(defaultMutationState)

  const [videoCreate, setVideoCreate] = useState<VideoOperatorCreateRequest>({
    channelId: '',
    externalVideoId: '',
    title: '',
    description: '',
    thumbnailUrl: '',
    publishedAt: '',
    videoUrl: '',
    contentType: 'video',
    pinned: false,
    note: '',
  })
  const [videoId, setVideoId] = useState('')
  const [videoUpdate, setVideoUpdate] = useState<VideoOperatorUpdateRequest>({})
  const [videoCreateState, setVideoCreateState] = useState<MutationState>(defaultMutationState)
  const [videoUpdateState, setVideoUpdateState] = useState<MutationState>(defaultMutationState)

  const auth = {
    username: operatorUsername,
    password: operatorPassword,
  }

  useEffect(() => {
    const controller = new AbortController()
    fetchGroups(
      backendUrl,
      {
        limit: 50,
        sort: 'name',
      },
      controller.signal,
    )
      .then((response) => {
        setAvailableGroups(response.data?.results.data.items ?? [])
      })
      .catch(() => {
        setAvailableGroups([])
      })

    return () => controller.abort()
  }, [backendUrl, refetchVersion])

  async function ensureGroup(groupName: string) {
    const normalizedName = normalizeNameKey(groupName)
    const targetSlug = slugifyName(groupName)

    const found = await fetchGroups(backendUrl, {
      q: groupName.trim(),
      limit: 20,
    })
    const existing = found.data?.results.data.items.find(
      (item) =>
        normalizeNameKey(item.groupName) === normalizedName || item.groupSlug === targetSlug,
    )

    if (existing) {
      return { id: existing.groupId, slug: existing.groupSlug }
    }

    try {
      const created = await createGroup(
        backendUrl,
        {
          slug: targetSlug,
          displayName: groupName.trim(),
          featured: true,
        },
        auth,
      )
      return { id: created.entityId, slug: targetSlug }
    } catch (error) {
      const retry = await fetchGroups(backendUrl, {
        q: groupName.trim(),
        limit: 20,
      })
      const fallback = retry.data?.results.data.items.find(
        (item) =>
          normalizeNameKey(item.groupName) === normalizedName || item.groupSlug === targetSlug,
      )
      if (fallback) {
        return { id: fallback.groupId, slug: fallback.groupSlug }
      }
      throw error
    }
  }

  async function ensureMember(
    group: { id: string; slug: string },
    memberName: string,
  ) {
    const memberSlugCandidate = `${group.slug}-${slugifyName(memberName)}`
    try {
      const detail = await fetchMemberDetail(backendUrl, memberSlugCandidate)
      const data = detail.data?.memberProfile.data
      if (data) {
        return { id: data.memberId, slug: data.memberSlug }
      }
    } catch {
      // create path below
    }

    try {
      const created = await createMember(
        backendUrl,
        {
          groupId: group.id,
          slug: memberSlugCandidate,
          displayName: memberName.trim(),
          sortOrder: 0,
        },
        auth,
      )
      return { id: created.entityId, slug: memberSlugCandidate }
    } catch (error) {
      const detail = await fetchMemberDetail(backendUrl, memberSlugCandidate).catch(() => null)
      const data = detail?.data?.memberProfile.data
      if (data) {
        return { id: data.memberId, slug: data.memberSlug }
      }
      throw error
    }
  }

  async function submitQuickGroup() {
    setQuickGroupState({ phase: 'loading' })
    try {
      const group = await ensureGroup(quickGroupName)
      const youtube = parseYouTubeChannelInput(quickGroupYouTubeUrl)
      const response = await createChannel(
        backendUrl,
        {
          platformCode: 'youtube',
          externalChannelId: youtube.externalChannelId,
          handle: youtube.handle,
          channelUrl: youtube.channelUrl,
          displayLabel: `${quickGroupName.trim()} Official`,
          channelType: 'GROUP_OFFICIAL',
          ownerType: 'GROUP',
          ownerGroupId: group.id,
          isOfficial: true,
          isPrimary: true,
        },
        auth,
      )
      setQuickGroupState({ phase: 'success', response })
      setGroupSlug(group.slug)
      setMemberCreate((current) => ({ ...current, groupId: group.id }))
      setMemberCreateGroupQuery(groupOptionLabel({ groupId: group.id, groupSlug: group.slug, groupName: quickGroupName.trim(), description: null, coverImageUrl: null, officialChannelCount: 0, memberCount: 0, memberPersonalChannelCount: 0, latestVideoAt: null, latestVideoThumbnailUrl: null, badges: [] }))
      setQuickMemberGroupId(group.id)
      setQuickMemberGroupQuery(groupOptionLabel({ groupId: group.id, groupSlug: group.slug, groupName: quickGroupName.trim(), description: null, coverImageUrl: null, officialChannelCount: 0, memberCount: 0, memberPersonalChannelCount: 0, latestVideoAt: null, latestVideoThumbnailUrl: null, badges: [] }))
      setChannelId(response.entityId)
      setVideoCreate((current) => ({ ...current, channelId: response.entityId }))
      const refreshedGroups = await fetchGroups(backendUrl, { limit: 50, sort: 'name' })
      setAvailableGroups(refreshedGroups.data?.results.data.items ?? [])
    } catch (error) {
      setQuickGroupState({
        phase: 'error',
        message: error instanceof Error ? error.message : '간편 그룹 추가에 실패했습니다.',
      })
    }
  }

  async function submitQuickMember() {
    setQuickMemberState({ phase: 'loading' })
    try {
      const selectedGroup = availableGroups.find((group) => group.groupId === quickMemberGroupId)
      if (!selectedGroup) {
        throw new Error('먼저 등록된 그룹을 선택해주세요.')
      }
      const group = { id: selectedGroup.groupId, slug: selectedGroup.groupSlug }
      const member = await ensureMember(group, quickMemberName)
      const youtube = parseYouTubeChannelInput(quickMemberYouTubeUrl)
      const response = await createChannel(
        backendUrl,
        {
          platformCode: 'youtube',
          externalChannelId: youtube.externalChannelId,
          handle: youtube.handle,
          channelUrl: youtube.channelUrl,
          displayLabel: `${quickMemberName.trim()} 채널`,
          channelType: 'MEMBER_PERSONAL',
          ownerType: 'MEMBER',
          ownerMemberId: member.id,
          isOfficial: true,
          isPrimary: true,
        },
        auth,
      )
      setQuickMemberState({ phase: 'success', response })
      setMemberSlug(member.slug)
      setChannelId(response.entityId)
      setVideoCreate((current) => ({ ...current, channelId: response.entityId }))
    } catch (error) {
      setQuickMemberState({
        phase: 'error',
        message: error instanceof Error ? error.message : '간편 멤버 추가에 실패했습니다.',
      })
    }
  }

  async function submitCreateGroup() {
    setGroupCreateState({ phase: 'loading' })
    try {
      const response = await createGroup(backendUrl, normalizeGroupCreate(groupCreate), auth)
      setGroupCreateState({ phase: 'success', response })
      setGroupSlug(groupCreate.slug.trim())
      setChannelCreate((current) => ({ ...current, ownerGroupId: response.entityId }))
      setMemberCreate((current) => ({ ...current, groupId: response.entityId }))
    } catch (error) {
      setGroupCreateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '그룹을 생성하지 못했습니다.',
      })
    }
  }

  async function submitUpdateGroup() {
    setGroupUpdateState({ phase: 'loading' })
    try {
      const response = await updateGroup(backendUrl, groupSlug, groupUpdate, auth)
      setGroupUpdateState({ phase: 'success', response })
    } catch (error) {
      setGroupUpdateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '그룹을 수정하지 못했습니다.',
      })
    }
  }

  async function submitCreateMember() {
    setMemberCreateState({ phase: 'loading' })
    try {
      const response = await createMember(backendUrl, normalizeMemberCreate(memberCreate), auth)
      setMemberCreateState({ phase: 'success', response })
      setMemberSlug(memberCreate.slug.trim())
      setChannelCreate((current) => ({ ...current, ownerMemberId: response.entityId }))
    } catch (error) {
      setMemberCreateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '멤버를 생성하지 못했습니다.',
      })
    }
  }

  async function submitUpdateMember() {
    setMemberUpdateState({ phase: 'loading' })
    try {
      const response = await updateMember(backendUrl, memberSlug, memberUpdate, auth)
      setMemberUpdateState({ phase: 'success', response })
    } catch (error) {
      setMemberUpdateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '멤버를 수정하지 못했습니다.',
      })
    }
  }

  async function submitCreateChannel() {
    setChannelCreateState({ phase: 'loading' })
    try {
      const response = await createChannel(backendUrl, normalizeChannelCreate(channelCreate), auth)
      setChannelCreateState({ phase: 'success', response })
      setChannelId(response.entityId)
      setVideoCreate((current) => ({ ...current, channelId: response.entityId }))
    } catch (error) {
      setChannelCreateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '채널을 생성하지 못했습니다.',
      })
    }
  }

  async function submitUpdateChannel() {
    setChannelUpdateState({ phase: 'loading' })
    try {
      const response = await updateChannel(backendUrl, channelId, channelUpdate, auth)
      setChannelUpdateState({ phase: 'success', response })
    } catch (error) {
      setChannelUpdateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '채널을 수정하지 못했습니다.',
      })
    }
  }

  async function submitCreateVideo() {
    setVideoCreateState({ phase: 'loading' })
    try {
      const response = await createVideo(backendUrl, normalizeVideoCreate(videoCreate), auth)
      setVideoCreateState({ phase: 'success', response })
      setVideoId(response.entityId)
    } catch (error) {
      setVideoCreateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '영상을 생성하지 못했습니다.',
      })
    }
  }

  async function submitUpdateVideo() {
    setVideoUpdateState({ phase: 'loading' })
    try {
      const response = await updateVideo(backendUrl, videoId, videoUpdate, auth)
      setVideoUpdateState({ phase: 'success', response })
    } catch (error) {
      setVideoUpdateState({
        phase: 'error',
        message: error instanceof Error ? error.message : '영상을 수정하지 못했습니다.',
      })
    }
  }

  return (
    <div className="page-grid">
      <section className="page-hero compact">
        <p className="eyebrow">운영자</p>
        <h1>그룹 허브 관리자</h1>
        <p>그룹, 멤버, 채널, 영상을 직접 입력하고 새 제품 방향을 빠르게 검증할 수 있습니다.</p>
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>운영자 인증</h2>
            <p>빠른 테스트를 위해 이 브라우저에 로컬 저장됩니다.</p>
          </div>
        </header>
        <div className="form-grid">
          <label className="field">
            <span>사용자 이름</span>
            <input value={draftOperatorUsername} onChange={(event) => setDraftOperatorUsername(event.target.value)} />
          </label>
          <label className="field">
            <span>비밀번호</span>
            <input type="password" value={draftOperatorPassword} onChange={(event) => setDraftOperatorPassword(event.target.value)} />
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={saveOperatorCredentials}>인증 정보 저장</button>
        </div>
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>간편 그룹 추가</h2>
            <p>그룹 이름과 유튜브 주소만 넣으면 그룹과 공식 채널을 함께 생성합니다.</p>
          </div>
        </header>
        <div className="form-grid">
          <label className="field">
            <span>그룹 이름</span>
            <input value={quickGroupName} onChange={(event) => setQuickGroupName(event.target.value)} />
          </label>
          <label className="field">
            <span>그룹 공식 유튜브 주소</span>
            <input value={quickGroupYouTubeUrl} onChange={(event) => setQuickGroupYouTubeUrl(event.target.value)} placeholder="https://www.youtube.com/@group" />
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={submitQuickGroup} disabled={!quickGroupName.trim() || !quickGroupYouTubeUrl.trim() || quickGroupState.phase === 'loading'}>
            {quickGroupState.phase === 'loading' ? '추가 중...' : '간편 그룹 추가'}
          </button>
        </div>
        <MutationResult state={quickGroupState} />
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>간편 멤버 추가</h2>
            <p>등록된 그룹을 선택하고, 멤버 이름과 유튜브 주소만 넣으면 멤버와 개인 채널을 함께 생성합니다.</p>
          </div>
        </header>
        <div className="form-grid">
          <SearchableGroupField
            label="그룹 검색"
            placeholder="그룹 이름을 입력하면 목록이 보입니다"
            query={quickMemberGroupQuery}
            setQuery={setQuickMemberGroupQuery}
            selectedGroupId={quickMemberGroupId}
            setSelectedGroupId={setQuickMemberGroupId}
            groups={availableGroups}
          />
          <label className="field">
            <span>멤버 이름</span>
            <input value={quickMemberName} onChange={(event) => setQuickMemberName(event.target.value)} />
          </label>
          <label className="field">
            <span>멤버 유튜브 주소</span>
            <input value={quickMemberYouTubeUrl} onChange={(event) => setQuickMemberYouTubeUrl(event.target.value)} placeholder="https://www.youtube.com/@member" />
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={submitQuickMember} disabled={!quickMemberGroupId.trim() || !quickMemberName.trim() || !quickMemberYouTubeUrl.trim() || quickMemberState.phase === 'loading'}>
            {quickMemberState.phase === 'loading' ? '추가 중...' : '간편 멤버 추가'}
          </button>
        </div>
        <MutationResult state={quickMemberState} />
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>그룹 생성 / 수정</h2>
            <p>POST `/internal/operator/groups` · PUT `/internal/operator/groups/{'{groupSlug}'}`</p>
          </div>
        </header>
        <div className="form-grid">
          <label className="field">
            <span>그룹 slug</span>
            <input value={groupCreate.slug} onChange={(event) => setGroupCreate((current) => ({ ...current, slug: event.target.value }))} />
          </label>
          <label className="field">
            <span>그룹 이름</span>
            <input value={groupCreate.displayName} onChange={(event) => setGroupCreate((current) => ({ ...current, displayName: event.target.value }))} />
          </label>
          <label className="field">
            <span>설명</span>
            <textarea rows={4} value={groupCreate.description ?? ''} onChange={(event) => setGroupCreate((current) => ({ ...current, description: event.target.value }))} />
          </label>
          <label className="field">
            <span>커버 이미지 주소</span>
            <input value={groupCreate.coverImageUrl ?? ''} onChange={(event) => setGroupCreate((current) => ({ ...current, coverImageUrl: event.target.value }))} />
          </label>
          <label className="field">
            <span>추천 그룹</span>
            <select value={String(groupCreate.featured ?? true)} onChange={(event) => setGroupCreate((current) => ({ ...current, featured: event.target.value === 'true' }))}>
              <option value="true">{formatBooleanLabel(true)}</option>
              <option value="false">{formatBooleanLabel(false)}</option>
            </select>
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={submitCreateGroup} disabled={!groupCreate.slug.trim() || !groupCreate.displayName.trim() || groupCreateState.phase === 'loading'}>
            {groupCreateState.phase === 'loading' ? '생성 중...' : '그룹 생성'}
          </button>
        </div>
        <MutationResult state={groupCreateState} />

        <div className="form-grid">
          <label className="field">
            <span>수정할 그룹 slug</span>
            <input value={groupSlug} onChange={(event) => setGroupSlug(event.target.value)} />
          </label>
          <label className="field">
            <span>수정 이름</span>
            <input value={groupUpdate.displayName ?? ''} onChange={(event) => setGroupUpdate((current) => ({ ...current, displayName: event.target.value || undefined }))} />
          </label>
          <label className="field">
            <span>수정 설명</span>
            <textarea rows={4} value={groupUpdate.description ?? ''} onChange={(event) => setGroupUpdate((current) => ({ ...current, description: event.target.value || undefined }))} />
          </label>
        </div>
        <div className="panel-actions">
          <button className="secondary-button" onClick={submitUpdateGroup} disabled={!groupSlug.trim() || groupUpdateState.phase === 'loading'}>
            {groupUpdateState.phase === 'loading' ? '수정 중...' : '그룹 수정'}
          </button>
        </div>
        <MutationResult state={groupUpdateState} />
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>멤버 생성 / 수정</h2>
            <p>POST `/internal/operator/members` · PUT `/internal/operator/members/{'{memberSlug}'}`</p>
          </div>
        </header>
        <div className="form-grid">
          <SearchableGroupField
            label="그룹 검색"
            placeholder="그룹 이름을 입력하면 목록이 보입니다"
            query={memberCreateGroupQuery}
            setQuery={setMemberCreateGroupQuery}
            selectedGroupId={memberCreate.groupId}
            setSelectedGroupId={(value) => setMemberCreate((current) => ({ ...current, groupId: value }))}
            groups={availableGroups}
          />
          <label className="field">
            <span>멤버 slug</span>
            <input value={memberCreate.slug} onChange={(event) => setMemberCreate((current) => ({ ...current, slug: event.target.value }))} />
          </label>
          <label className="field">
            <span>멤버 이름</span>
            <input value={memberCreate.displayName} onChange={(event) => setMemberCreate((current) => ({ ...current, displayName: event.target.value }))} />
          </label>
          <label className="field">
            <span>프로필 이미지 주소</span>
            <input value={memberCreate.profileImageUrl ?? ''} onChange={(event) => setMemberCreate((current) => ({ ...current, profileImageUrl: event.target.value }))} />
          </label>
          <label className="field">
            <span>정렬 순서</span>
            <input value={memberCreate.sortOrder ?? 0} onChange={(event) => setMemberCreate((current) => ({ ...current, sortOrder: Number(event.target.value) || 0 }))} />
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={submitCreateMember} disabled={!memberCreate.groupId.trim() || !memberCreate.slug.trim() || !memberCreate.displayName.trim() || memberCreateState.phase === 'loading'}>
            {memberCreateState.phase === 'loading' ? '생성 중...' : '멤버 생성'}
          </button>
        </div>
        <MutationResult state={memberCreateState} />

        <div className="form-grid">
          <label className="field">
            <span>수정할 멤버 slug</span>
            <input value={memberSlug} onChange={(event) => setMemberSlug(event.target.value)} />
          </label>
          <label className="field">
            <span>수정 이름</span>
            <input value={memberUpdate.displayName ?? ''} onChange={(event) => setMemberUpdate((current) => ({ ...current, displayName: event.target.value || undefined }))} />
          </label>
          <label className="field">
            <span>프로필 이미지 주소</span>
            <input value={memberUpdate.profileImageUrl ?? ''} onChange={(event) => setMemberUpdate((current) => ({ ...current, profileImageUrl: event.target.value || undefined }))} />
          </label>
        </div>
        <div className="panel-actions">
          <button className="secondary-button" onClick={submitUpdateMember} disabled={!memberSlug.trim() || memberUpdateState.phase === 'loading'}>
            {memberUpdateState.phase === 'loading' ? '수정 중...' : '멤버 수정'}
          </button>
        </div>
        <MutationResult state={memberUpdateState} />
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>채널 생성 / 수정</h2>
            <p>POST `/internal/operator/channels` · PUT `/internal/operator/channels/{'{channelId}'}`</p>
          </div>
        </header>
        <div className="form-grid">
          <label className="field">
            <span>채널 외부 ID</span>
            <input value={channelCreate.externalChannelId} onChange={(event) => setChannelCreate((current) => ({ ...current, externalChannelId: event.target.value }))} />
          </label>
          <label className="field">
            <span>핸들</span>
            <input value={channelCreate.handle ?? ''} onChange={(event) => setChannelCreate((current) => ({ ...current, handle: event.target.value }))} />
          </label>
          <label className="field">
            <span>채널 주소</span>
            <input value={channelCreate.channelUrl} onChange={(event) => setChannelCreate((current) => ({ ...current, channelUrl: event.target.value }))} />
          </label>
          <label className="field">
            <span>표시 이름</span>
            <input value={channelCreate.displayLabel ?? ''} onChange={(event) => setChannelCreate((current) => ({ ...current, displayLabel: event.target.value }))} />
          </label>
          <label className="field">
            <span>채널 유형</span>
            <select value={channelCreate.channelType} onChange={(event) => setChannelCreate((current) => ({ ...current, channelType: event.target.value }))}>
              <option value="GROUP_OFFICIAL">{formatChannelTypeLabel('GROUP_OFFICIAL')}</option>
              <option value="MEMBER_PERSONAL">{formatChannelTypeLabel('MEMBER_PERSONAL')}</option>
              <option value="SUB_UNIT">{formatChannelTypeLabel('SUB_UNIT')}</option>
              <option value="LABEL">{formatChannelTypeLabel('LABEL')}</option>
            </select>
          </label>
          <label className="field">
            <span>소유 유형</span>
            <select value={channelCreate.ownerType} onChange={(event) => setChannelCreate((current) => ({ ...current, ownerType: event.target.value }))}>
              <option value="GROUP">GROUP</option>
              <option value="MEMBER">MEMBER</option>
            </select>
          </label>
          <label className="field">
            <span>그룹 ID</span>
            <input value={channelCreate.ownerGroupId ?? ''} onChange={(event) => setChannelCreate((current) => ({ ...current, ownerGroupId: event.target.value }))} />
          </label>
          <label className="field">
            <span>멤버 ID</span>
            <input value={channelCreate.ownerMemberId ?? ''} onChange={(event) => setChannelCreate((current) => ({ ...current, ownerMemberId: event.target.value }))} />
          </label>
          <label className="field">
            <span>공식 여부</span>
            <select value={String(channelCreate.isOfficial ?? true)} onChange={(event) => setChannelCreate((current) => ({ ...current, isOfficial: event.target.value === 'true' }))}>
              <option value="true">{formatBooleanLabel(true)}</option>
              <option value="false">{formatBooleanLabel(false)}</option>
            </select>
          </label>
          <label className="field">
            <span>대표 여부</span>
            <select value={String(channelCreate.isPrimary ?? false)} onChange={(event) => setChannelCreate((current) => ({ ...current, isPrimary: event.target.value === 'true' }))}>
              <option value="true">{formatBooleanLabel(true)}</option>
              <option value="false">{formatBooleanLabel(false)}</option>
            </select>
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={submitCreateChannel} disabled={!channelCreate.externalChannelId.trim() || !channelCreate.channelUrl.trim() || channelCreateState.phase === 'loading'}>
            {channelCreateState.phase === 'loading' ? '생성 중...' : '채널 생성'}
          </button>
        </div>
        <MutationResult state={channelCreateState} />

        <div className="form-grid">
          <label className="field">
            <span>수정할 채널 ID</span>
            <input value={channelId} onChange={(event) => setChannelId(event.target.value)} />
          </label>
          <label className="field">
            <span>수정 표시 이름</span>
            <input value={channelUpdate.displayLabel ?? ''} onChange={(event) => setChannelUpdate((current) => ({ ...current, displayLabel: event.target.value || undefined }))} />
          </label>
          <label className="field">
            <span>수정 주소</span>
            <input value={channelUpdate.channelUrl ?? ''} onChange={(event) => setChannelUpdate((current) => ({ ...current, channelUrl: event.target.value || undefined }))} />
          </label>
        </div>
        <div className="panel-actions">
          <button className="secondary-button" onClick={submitUpdateChannel} disabled={!channelId.trim() || channelUpdateState.phase === 'loading'}>
            {channelUpdateState.phase === 'loading' ? '수정 중...' : '채널 수정'}
          </button>
        </div>
        <MutationResult state={channelUpdateState} />
      </section>

      <section className="section-card">
        <header className="section-header">
          <div>
            <h2>영상 생성 / 수정</h2>
            <p>POST `/internal/operator/videos` · PUT `/internal/operator/videos/{'{videoId}'}`</p>
          </div>
        </header>
        <div className="form-grid">
          <label className="field">
            <span>채널 ID</span>
            <input value={videoCreate.channelId} onChange={(event) => setVideoCreate((current) => ({ ...current, channelId: event.target.value }))} />
          </label>
          <label className="field">
            <span>외부 영상 ID</span>
            <input value={videoCreate.externalVideoId ?? ''} onChange={(event) => setVideoCreate((current) => ({ ...current, externalVideoId: event.target.value }))} />
          </label>
          <label className="field">
            <span>제목</span>
            <input value={videoCreate.title} onChange={(event) => setVideoCreate((current) => ({ ...current, title: event.target.value }))} />
          </label>
          <label className="field">
            <span>영상 타입</span>
            <select value={videoCreate.contentType ?? 'video'} onChange={(event) => setVideoCreate((current) => ({ ...current, contentType: event.target.value }))}>
              <option value="video">동영상</option>
              <option value="short">쇼츠</option>
            </select>
          </label>
          <label className="field">
            <span>영상 주소</span>
            <input value={videoCreate.videoUrl} onChange={(event) => setVideoCreate((current) => ({ ...current, videoUrl: event.target.value }))} />
          </label>
          <label className="field">
            <span>썸네일 주소</span>
            <input value={videoCreate.thumbnailUrl ?? ''} onChange={(event) => setVideoCreate((current) => ({ ...current, thumbnailUrl: event.target.value }))} />
          </label>
          <label className="field">
            <span>게시 시각 (ISO 형식)</span>
            <input value={videoCreate.publishedAt} onChange={(event) => setVideoCreate((current) => ({ ...current, publishedAt: event.target.value }))} placeholder="2026-04-05T08:30:00Z" />
          </label>
          <label className="field">
            <span>고정 여부</span>
            <select value={String(videoCreate.pinned ?? false)} onChange={(event) => setVideoCreate((current) => ({ ...current, pinned: event.target.value === 'true' }))}>
              <option value="true">{formatBooleanLabel(true)}</option>
              <option value="false">{formatBooleanLabel(false)}</option>
            </select>
          </label>
          <label className="field">
            <span>설명</span>
            <textarea rows={4} value={videoCreate.description ?? ''} onChange={(event) => setVideoCreate((current) => ({ ...current, description: event.target.value }))} />
          </label>
        </div>
        <div className="panel-actions">
          <button onClick={submitCreateVideo} disabled={!videoCreate.channelId.trim() || !videoCreate.title.trim() || !videoCreate.publishedAt.trim() || !videoCreate.videoUrl.trim() || videoCreateState.phase === 'loading'}>
            {videoCreateState.phase === 'loading' ? '생성 중...' : '영상 생성'}
          </button>
        </div>
        <MutationResult state={videoCreateState} />

        <div className="form-grid">
          <label className="field">
            <span>수정할 영상 ID</span>
            <input value={videoId} onChange={(event) => setVideoId(event.target.value)} />
          </label>
          <label className="field">
            <span>수정 제목</span>
            <input value={videoUpdate.title ?? ''} onChange={(event) => setVideoUpdate((current) => ({ ...current, title: event.target.value || undefined }))} />
          </label>
          <label className="field">
            <span>수정 영상 주소</span>
            <input value={videoUpdate.videoUrl ?? ''} onChange={(event) => setVideoUpdate((current) => ({ ...current, videoUrl: event.target.value || undefined }))} />
          </label>
        </div>
        <div className="panel-actions">
          <button className="secondary-button" onClick={submitUpdateVideo} disabled={!videoId.trim() || videoUpdateState.phase === 'loading'}>
            {videoUpdateState.phase === 'loading' ? '수정 중...' : '영상 수정'}
          </button>
        </div>
        <MutationResult state={videoUpdateState} />
      </section>
    </div>
  )
}

function groupOptionLabel(group: GroupListItem) {
  return `${group.groupName} (${group.groupSlug})`
}

function SearchableGroupField({
  label,
  placeholder,
  query,
  setQuery,
  selectedGroupId,
  setSelectedGroupId,
  groups,
}: SearchableGroupFieldProps) {
  const [open, setOpen] = useState(false)
  const normalizedQuery = query.trim().toLowerCase()
  const filteredGroups = normalizedQuery
    ? groups.filter((group) =>
        groupOptionLabel(group).toLowerCase().includes(normalizedQuery) ||
        group.groupName.toLowerCase().includes(normalizedQuery) ||
        group.groupSlug.toLowerCase().includes(normalizedQuery),
      )
    : groups

  return (
    <label className="field autocomplete-field">
      <span>{label}</span>
      <input
        value={query}
        onFocus={() => setOpen(true)}
        onBlur={() => {
          window.setTimeout(() => setOpen(false), 120)
        }}
        onChange={(event) => {
          const nextQuery = event.target.value
          setQuery(nextQuery)
          const matched = groups.find((group) => groupOptionLabel(group) === nextQuery)
          setSelectedGroupId(matched?.groupId ?? '')
          setOpen(true)
        }}
        placeholder={placeholder}
      />

      {open ? (
        <div className="autocomplete-panel">
          {filteredGroups.length > 0 ? (
            filteredGroups.map((group) => {
              const selected = selectedGroupId === group.groupId
              return (
                <button
                  key={group.groupId}
                  type="button"
                  className={`autocomplete-option ${selected ? 'selected' : ''}`}
                  onMouseDown={() => {
                    setQuery(groupOptionLabel(group))
                    setSelectedGroupId(group.groupId)
                    setOpen(false)
                  }}
                >
                  <strong>{group.groupName}</strong>
                  <span>{group.groupSlug}</span>
                </button>
              )
            })
          ) : (
            <div className="autocomplete-empty">일치하는 그룹이 없습니다.</div>
          )}
        </div>
      ) : null}
    </label>
  )
}

function slugifyName(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/[\s_]+/g, '-')
    .replace(/[^\p{L}\p{N}-]+/gu, '')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
}

function normalizeNameKey(value: string): string {
  return value.trim().toLowerCase().replace(/\s+/g, '')
}

function parseYouTubeChannelInput(value: string) {
  let url: URL
  try {
    url = new URL(value.trim())
  } catch {
    throw new Error('유효한 유튜브 채널 주소를 입력해주세요.')
  }

  const segments = url.pathname.split('/').map((item) => item.trim()).filter(Boolean)
  let externalChannelId = ''
  let handle: string | undefined

  if (segments[0]?.startsWith('@')) {
    externalChannelId = segments[0]
    handle = segments[0]
  } else if (segments[0] === 'channel' && segments[1]) {
    externalChannelId = segments[1]
  } else if ((segments[0] === 'c' || segments[0] === 'user') && segments[1]) {
    externalChannelId = segments[1]
    handle = `@${segments[1]}`
  } else if (segments[0]) {
    externalChannelId = segments[0]
    handle = segments[0].startsWith('@') ? segments[0] : `@${segments[0]}`
  }

  if (!externalChannelId) {
    throw new Error('유튜브 채널 주소에서 채널 정보를 추출하지 못했습니다.')
  }

  return {
    channelUrl: url.toString(),
    externalChannelId,
    handle,
  }
}

function normalizeGroupCreate(form: GroupOperatorCreateRequest): GroupOperatorCreateRequest {
  return {
    slug: slugifyName(form.slug),
    displayName: form.displayName.trim(),
    description: form.description?.trim() || undefined,
    coverImageUrl: form.coverImageUrl?.trim() || undefined,
    featured: form.featured ?? false,
    note: form.note?.trim() || undefined,
  }
}

function normalizeMemberCreate(form: MemberOperatorCreateRequest): MemberOperatorCreateRequest {
  return {
    groupId: form.groupId.trim(),
    slug: slugifyName(form.slug),
    displayName: form.displayName.trim(),
    profileImageUrl: form.profileImageUrl?.trim() || undefined,
    sortOrder: form.sortOrder,
    note: form.note?.trim() || undefined,
  }
}

function normalizeChannelCreate(form: ChannelOperatorCreateRequest): ChannelOperatorCreateRequest {
  return {
    ...form,
    platformCode: form.platformCode.trim(),
    externalChannelId: form.externalChannelId.trim(),
    handle: form.handle?.trim() || undefined,
    channelUrl: form.channelUrl.trim(),
    displayLabel: form.displayLabel?.trim() || undefined,
    ownerGroupId: form.ownerGroupId?.trim() || undefined,
    ownerMemberId: form.ownerMemberId?.trim() || undefined,
    note: form.note?.trim() || undefined,
  }
}

function normalizeVideoCreate(form: VideoOperatorCreateRequest): VideoOperatorCreateRequest {
  return {
    channelId: form.channelId.trim(),
    externalVideoId: form.externalVideoId?.trim() || undefined,
    title: form.title.trim(),
    description: form.description?.trim() || undefined,
    thumbnailUrl: form.thumbnailUrl?.trim() || undefined,
    publishedAt: form.publishedAt.trim(),
    videoUrl: form.videoUrl.trim(),
    contentType: form.contentType?.trim() || undefined,
    pinned: form.pinned ?? false,
    note: form.note?.trim() || undefined,
  }
}

function MutationResult({ state }: { state: MutationState }) {
  if (state.phase === 'idle') return null
  if (state.phase === 'loading') {
    return <div className="mutation-result loading">요청 처리 중입니다...</div>
  }
  if (state.phase === 'error') {
    return <div className="mutation-result error">{state.message}</div>
  }
  return (
    <div className="mutation-result success">
      <strong>{formatMutationStatusLabel(state.response.status)}</strong>
      <p>대상 ID: {state.response.entityId}</p>
      <details>
        <summary>원본 응답 보기</summary>
        <pre>{JSON.stringify(state.response, null, 2)}</pre>
      </details>
    </div>
  )
}
