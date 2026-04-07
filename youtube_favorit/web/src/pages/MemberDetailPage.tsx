import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchMemberDetail } from '../api/client'
import { useDeveloperSettings } from '../app/useDeveloperSettings'
import { EnvelopeMetaBar } from '../components/EnvelopeMetaBar'
import { SectionCard } from '../components/SectionCard'
import type { ApiEnvelope, MemberDetailPayload } from '../types/api'
import {
  formatBadgeLabel,
  formatChannelTypeLabel,
} from '../utils/display'
type ContentFilter = 'all' | 'video' | 'short'

export function MemberDetailPage() {
  const { memberSlug } = useParams()
  const { backendUrl, refetchVersion } = useDeveloperSettings()
  const [phase, setPhase] = useState<'loading' | 'success' | 'error'>('loading')
  const [error, setError] = useState<string | null>(null)
  const [envelope, setEnvelope] = useState<ApiEnvelope<MemberDetailPayload> | null>(null)
  const [contentFilter, setContentFilter] = useState<ContentFilter>('all')

  useEffect(() => {
    if (!memberSlug) return
    const controller = new AbortController()
    fetchMemberDetail(backendUrl, memberSlug, { videosLimit: 8, contentType: contentFilter }, controller.signal)
      .then((nextEnvelope) => {
        setEnvelope(nextEnvelope)
        setPhase('success')
        setError(null)
      })
      .catch((requestError: unknown) => {
        setPhase('error')
        setError(requestError instanceof Error ? requestError.message : '멤버 상세를 불러오지 못했습니다.')
      })

    return () => controller.abort()
  }, [backendUrl, memberSlug, refetchVersion, contentFilter])

  async function loadMore() {
    if (!memberSlug) return
    const cursor = envelope?.data?.recentVideos.data.pageInfo.nextCursor
    if (!cursor) return
    try {
        const nextEnvelope = await fetchMemberDetail(backendUrl, memberSlug, {
          videosCursor: cursor,
          videosLimit: 8,
          contentType: contentFilter,
        })
      if (!envelope?.data || !nextEnvelope.data) return

      setEnvelope({
        ...nextEnvelope,
        data: {
          ...nextEnvelope.data,
          recentVideos: {
            ...nextEnvelope.data.recentVideos,
            data: {
              ...nextEnvelope.data.recentVideos.data,
              items: [
                ...envelope.data.recentVideos.data.items,
                ...nextEnvelope.data.recentVideos.data.items,
              ],
            },
          },
        },
      })
      setError(null)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : '영상을 더 불러오지 못했습니다.')
    }
  }

  if (!memberSlug) {
    return <div className="page-state error">멤버 slug가 없습니다.</div>
  }

  if (phase === 'loading' && !envelope) {
    return <div className="page-state">멤버 상세를 불러오는 중입니다...</div>
  }

  if (phase === 'error' && !envelope) {
    return (
      <div className="page-state error">
        <h1>멤버 상세</h1>
        <p>{error}</p>
      </div>
    )
  }

  if (!envelope?.data) {
    return <div className="page-state">멤버 상세 데이터가 없습니다.</div>
  }

  const data = envelope.data

  return (
    <div className="page-grid">
      <div className="breadcrumb">
        <Link to="/groups">그룹</Link>
        <span>/</span>
        <Link to={`/groups/${data.memberProfile.data.group.groupSlug}`}>
          {data.memberProfile.data.group.groupName}
        </Link>
        <span>/</span>
        <span>{memberSlug}</span>
      </div>

      <EnvelopeMetaBar meta={envelope.meta} errors={envelope.errors} />

      <SectionCard
        title="멤버 프로필"
        section={data.memberProfile}
        emptyMessage="멤버 정보가 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="hero-card">
          <div>
            <p className="eyebrow">멤버</p>
            <h1>{data.memberProfile.data.memberName}</h1>
            <p>{data.memberProfile.data.group.groupName} 소속</p>
          </div>
        </div>
      </SectionCard>

      <SectionCard
        title="개인 채널"
        section={data.personalChannels}
        emptyMessage="등록된 개인 채널이 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="card-list">
          {data.personalChannels.data.items.map((item) => (
            <article className="entity-card" key={item.channelId}>
              <div>
                <strong>{item.channelName}</strong>
                <p>{formatChannelTypeLabel(item.channelType)}</p>
              </div>
              <div className="entity-meta">
                <span>{item.handle || '핸들 없음'}</span>
                <span>{item.latestVideoAt ? new Date(item.latestVideoAt).toLocaleString() : '최근 업로드 없음'}</span>
                <a href={item.channelUrl} target="_blank" rel="noreferrer">
                  채널 바로가기
                </a>
              </div>
            </article>
          ))}
        </div>
      </SectionCard>

      <SectionCard
        title="최근 업로드"
        section={data.recentVideos}
        emptyMessage="표시할 영상이 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="filter-radio-group">
          <label>
            <input type="radio" name="member-content-filter" checked={contentFilter === 'all'} onChange={() => setContentFilter('all')} />
            전체
          </label>
          <label>
            <input type="radio" name="member-content-filter" checked={contentFilter === 'video'} onChange={() => setContentFilter('video')} />
            동영상
          </label>
          <label>
            <input type="radio" name="member-content-filter" checked={contentFilter === 'short'} onChange={() => setContentFilter('short')} />
            쇼츠
          </label>
        </div>
        <div className="feed-list">
          {data.recentVideos.data.items.map((item) => (
            <article className="feed-card" key={item.videoId}>
              {item.thumbnailUrl ? (
                <a className="feed-thumbnail" href={item.videoUrl} target="_blank" rel="noreferrer">
                  <img src={item.thumbnailUrl} alt={item.title} loading="lazy" />
                </a>
              ) : null}
              <div className="feed-content">
                <div className="timeline-head">
                  <strong>{item.title}</strong>
                  <span>{formatChannelTypeLabel(item.channel.channelType)}</span>
                </div>
                <div className="feed-meta">
                  <Link className="feed-meta-link" to={`/groups/${item.group.groupSlug}`}>
                    {item.group.groupName}
                  </Link>
                  {item.member ? (
                    <span className="feed-meta-link">{item.member.memberName}</span>
                  ) : null}
                  <span>{item.contentType === 'short' ? '쇼츠' : '동영상'}</span>
                  <span>{new Date(item.publishedAt).toLocaleString()}</span>
                </div>
                <p>{item.description || '설명 요약이 없는 영상입니다.'}</p>
                <div className="tag-row">
                  {item.badges.map((badge) => (
                    <span className="tag" key={`${item.videoId}-${badge}`}>
                      {formatBadgeLabel(badge)}
                    </span>
                  ))}
                </div>
                <div className="panel-actions">
                  <a className="inline-link" href={item.channel.channelUrl} target="_blank" rel="noreferrer">
                    채널 보기
                  </a>
                  <a className="inline-link" href={item.videoUrl} target="_blank" rel="noreferrer">
                    유튜브에서 보기
                  </a>
                </div>
              </div>
            </article>
          ))}
        </div>

        {data.recentVideos.data.pageInfo.hasNext ? (
          <div className="section-footer">
            <button onClick={loadMore}>더보기</button>
          </div>
        ) : null}
      </SectionCard>

      {error ? <div className="page-state error inline">{error}</div> : null}
    </div>
  )
}
