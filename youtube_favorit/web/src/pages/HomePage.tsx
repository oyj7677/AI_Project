import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchHome } from '../api/client'
import { useDeveloperSettings } from '../app/useDeveloperSettings'
import { EnvelopeMetaBar } from '../components/EnvelopeMetaBar'
import { SectionCard } from '../components/SectionCard'
import type { ApiEnvelope, HomePayload } from '../types/api'
import {
  formatBadgeLabel,
  formatChannelTypeLabel,
} from '../utils/display'

const FEED_LIMIT = 8
type ContentFilter = 'all' | 'video' | 'short'

export function HomePage() {
  const { backendUrl, refetchVersion } = useDeveloperSettings()
  const [phase, setPhase] = useState<'loading' | 'success' | 'error'>('loading')
  const [error, setError] = useState<string | null>(null)
  const [envelope, setEnvelope] = useState<ApiEnvelope<HomePayload> | null>(null)
  const [contentFilter, setContentFilter] = useState<ContentFilter>('all')

  useEffect(() => {
    const controller = new AbortController()
    fetchHome(backendUrl, { limit: FEED_LIMIT, contentType: contentFilter }, controller.signal)
      .then((nextEnvelope) => {
        setEnvelope(nextEnvelope)
        setPhase('success')
        setError(null)
      })
      .catch((requestError: unknown) => {
        setPhase('error')
        setError(requestError instanceof Error ? requestError.message : '홈 피드를 불러오지 못했습니다.')
      })

    return () => controller.abort()
  }, [backendUrl, refetchVersion, contentFilter])

  async function loadMore() {
    const cursor = envelope?.data?.recentVideos.data.pageInfo.nextCursor
    if (!cursor) return

    try {
      const nextEnvelope = await fetchHome(backendUrl, {
        cursor,
        limit: FEED_LIMIT,
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
      setError(requestError instanceof Error ? requestError.message : '피드를 더 불러오지 못했습니다.')
    }
  }

  if (phase === 'loading' && !envelope) {
    return <div className="page-state">홈 피드를 불러오는 중입니다...</div>
  }

  if (phase === 'error' && !envelope) {
    return (
      <div className="page-state error">
        <h1>홈</h1>
        <p>{error}</p>
      </div>
    )
  }

  if (!envelope?.data) {
    return <div className="page-state">홈 데이터가 없습니다.</div>
  }

  const { recentVideos, featuredGroups } = envelope.data

  return (
    <div className="page-grid">
      <section className="page-hero">
        <p className="eyebrow">아이돌 유튜브 허브</p>
        <h1>그룹별 최신 업로드</h1>
        <p>
          그룹 공식 채널과 멤버 개인 채널 영상을 한곳에서 보고, 그룹 단위로 이동해
          바로 탐색할 수 있습니다.
        </p>
      </section>

      <EnvelopeMetaBar meta={envelope.meta} errors={envelope.errors} />

      <SectionCard
        title="최근 업로드 피드"
        section={recentVideos}
        emptyMessage="표시할 영상이 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="filter-radio-group">
          <label>
            <input type="radio" name="home-content-filter" checked={contentFilter === 'all'} onChange={() => setContentFilter('all')} />
            전체
          </label>
          <label>
            <input type="radio" name="home-content-filter" checked={contentFilter === 'video'} onChange={() => setContentFilter('video')} />
            동영상
          </label>
          <label>
            <input type="radio" name="home-content-filter" checked={contentFilter === 'short'} onChange={() => setContentFilter('short')} />
            쇼츠
          </label>
        </div>
        <div className="feed-list">
          {recentVideos.data.items.map((item) => (
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
                    <Link className="feed-meta-link" to={`/members/${item.member.memberSlug}`}>
                      {item.member.memberName}
                    </Link>
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

        {recentVideos.data.pageInfo.hasNext ? (
          <div className="section-footer">
            <button onClick={loadMore}>더보기</button>
          </div>
        ) : null}
      </SectionCard>

      <SectionCard
        title="주목 그룹"
        section={featuredGroups}
        emptyMessage="표시할 그룹이 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="card-grid">
          {featuredGroups.data.items.map((item) => (
            <article className="entity-card" key={item.groupId}>
              <div>
                <strong>{item.groupName}</strong>
                <p>
                  공식 채널 {item.officialChannelCount}개 · 멤버 개인 채널 {item.memberPersonalChannelCount}개
                </p>
              </div>
              <div className="entity-meta">
                <span>{item.latestVideoAt ? new Date(item.latestVideoAt).toLocaleString() : '최근 업로드 없음'}</span>
              </div>
              <Link className="inline-link" to={`/groups/${item.groupSlug}`}>
                그룹 상세 보기
              </Link>
            </article>
          ))}
        </div>
      </SectionCard>

      {error ? <div className="page-state error inline">{error}</div> : null}
    </div>
  )
}
