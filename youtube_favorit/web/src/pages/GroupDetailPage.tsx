import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchGroupDetail } from '../api/client'
import { useDeveloperSettings } from '../app/useDeveloperSettings'
import { EnvelopeMetaBar } from '../components/EnvelopeMetaBar'
import { SectionCard } from '../components/SectionCard'
import type { ApiEnvelope, GroupDetailPayload } from '../types/api'
import {
  formatBadgeLabel,
  formatChannelTypeLabel,
} from '../utils/display'
type ContentFilter = 'all' | 'video' | 'short'

export function GroupDetailPage() {
  const { groupSlug } = useParams()
  const { backendUrl, refetchVersion } = useDeveloperSettings()
  const [phase, setPhase] = useState<'loading' | 'success' | 'error'>('loading')
  const [error, setError] = useState<string | null>(null)
  const [envelope, setEnvelope] = useState<ApiEnvelope<GroupDetailPayload> | null>(null)
  const [contentFilter, setContentFilter] = useState<ContentFilter>('all')

  useEffect(() => {
    if (!groupSlug) return
    const controller = new AbortController()
    fetchGroupDetail(backendUrl, groupSlug, { videosLimit: 8, contentType: contentFilter }, controller.signal)
      .then((nextEnvelope) => {
        setEnvelope(nextEnvelope)
        setPhase('success')
        setError(null)
      })
      .catch((requestError: unknown) => {
        setPhase('error')
        setError(requestError instanceof Error ? requestError.message : '그룹 상세를 불러오지 못했습니다.')
      })

    return () => controller.abort()
  }, [backendUrl, groupSlug, refetchVersion, contentFilter])

  async function loadMore() {
    if (!groupSlug) return
    const cursor = envelope?.data?.recentVideos.data.pageInfo.nextCursor
    if (!cursor) return
    try {
        const nextEnvelope = await fetchGroupDetail(backendUrl, groupSlug, {
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

  if (!groupSlug) {
    return <div className="page-state error">그룹 slug가 없습니다.</div>
  }

  if (phase === 'loading' && !envelope) {
    return <div className="page-state">그룹 상세를 불러오는 중입니다...</div>
  }

  if (phase === 'error' && !envelope) {
    return (
      <div className="page-state error">
        <h1>그룹 상세</h1>
        <p>{error}</p>
      </div>
    )
  }

  if (!envelope?.data) {
    return <div className="page-state">그룹 상세 데이터가 없습니다.</div>
  }

  const data = envelope.data

  return (
    <div className="page-grid">
      <div className="breadcrumb">
        <Link to="/groups">그룹</Link>
        <span>/</span>
        <span>{groupSlug}</span>
      </div>

      <EnvelopeMetaBar meta={envelope.meta} errors={envelope.errors} />

      <SectionCard
        title="그룹 헤더"
        section={data.groupHeader}
        emptyMessage="그룹 정보가 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="hero-card">
          <div>
            <p className="eyebrow">그룹</p>
            <h1>{data.groupHeader.data.groupName}</h1>
            <p>{data.groupHeader.data.description || '소개가 없습니다.'}</p>
          </div>
          <div className="tag-row">
            {data.groupHeader.data.isFeatured ? <span className="tag">추천 그룹</span> : null}
            <span className="tag">멤버 {data.groupHeader.data.memberCount}명</span>
            <span className="tag">공식 채널 {data.groupHeader.data.officialChannelCount}개</span>
            <span className="tag">개인 채널 {data.groupHeader.data.memberPersonalChannelCount}개</span>
          </div>
        </div>
      </SectionCard>

      <SectionCard
        title="공식 채널"
        section={data.officialChannels}
        emptyMessage="등록된 공식 채널이 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="card-list">
          {data.officialChannels.data.items.map((item) => (
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
        title="멤버"
        section={data.members}
        emptyMessage="등록된 멤버가 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="card-grid">
          {data.members.data.items.map((item) => (
            <article className="entity-card" key={item.memberId}>
              <div>
                <strong>{item.memberName}</strong>
                <p>{item.hasPersonalChannel ? '개인 채널 있음' : '개인 채널 없음'}</p>
              </div>
              <div className="entity-meta">
                <span>개인 채널 {item.personalChannelCount}개</span>
                <span>{item.latestVideoAt ? new Date(item.latestVideoAt).toLocaleString() : '최근 업로드 없음'}</span>
              </div>
              <Link className="inline-link" to={`/members/${item.memberSlug}`}>
                멤버 상세 보기
              </Link>
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
            <input type="radio" name="group-content-filter" checked={contentFilter === 'all'} onChange={() => setContentFilter('all')} />
            전체
          </label>
          <label>
            <input type="radio" name="group-content-filter" checked={contentFilter === 'video'} onChange={() => setContentFilter('video')} />
            동영상
          </label>
          <label>
            <input type="radio" name="group-content-filter" checked={contentFilter === 'short'} onChange={() => setContentFilter('short')} />
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
                  {item.member ? (
                    <Link className="feed-meta-link" to={`/members/${item.member.memberSlug}`}>
                      {item.member.memberName}
                    </Link>
                  ) : (
                    <span>{item.group.groupName}</span>
                  )}
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
