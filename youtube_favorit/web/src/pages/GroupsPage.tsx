import { startTransition, useDeferredValue, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchGroups } from '../api/client'
import { useDeveloperSettings } from '../app/useDeveloperSettings'
import { EnvelopeMetaBar } from '../components/EnvelopeMetaBar'
import { SectionCard } from '../components/SectionCard'
import type { ApiEnvelope, GroupListPayload } from '../types/api'
import { formatBadgeLabel } from '../utils/display'

export function GroupsPage() {
  const { backendUrl, refetchVersion } = useDeveloperSettings()
  const [query, setQuery] = useState('')
  const [sort, setSort] = useState('featured')
  const [phase, setPhase] = useState<'loading' | 'success' | 'error'>('loading')
  const [error, setError] = useState<string | null>(null)
  const [envelope, setEnvelope] = useState<ApiEnvelope<GroupListPayload> | null>(null)
  const deferredQuery = useDeferredValue(query)

  useEffect(() => {
    const controller = new AbortController()
    fetchGroups(
      backendUrl,
      {
        q: deferredQuery || undefined,
        sort,
        limit: 12,
      },
      controller.signal,
    )
      .then((nextEnvelope) => {
        setEnvelope(nextEnvelope)
        setPhase('success')
        setError(null)
      })
      .catch((requestError: unknown) => {
        setPhase('error')
        setError(requestError instanceof Error ? requestError.message : '그룹 목록을 불러오지 못했습니다.')
      })

    return () => controller.abort()
  }, [backendUrl, deferredQuery, sort, refetchVersion])

  async function loadMore() {
    const cursor = envelope?.data?.results.data.pageInfo.nextCursor
    if (!cursor) return

    try {
      const nextEnvelope = await fetchGroups(backendUrl, {
        q: deferredQuery || undefined,
        sort,
        limit: 12,
        cursor,
      })
      if (!envelope?.data || !nextEnvelope.data) return

      setEnvelope({
        ...nextEnvelope,
        data: {
          ...nextEnvelope.data,
          results: {
            ...nextEnvelope.data.results,
            data: {
              ...nextEnvelope.data.results.data,
              items: [
                ...envelope.data.results.data.items,
                ...nextEnvelope.data.results.data.items,
              ],
            },
          },
        },
      })
      setError(null)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : '그룹을 더 불러오지 못했습니다.')
    }
  }

  if (phase === 'loading' && !envelope) {
    return <div className="page-state">그룹 목록을 불러오는 중입니다...</div>
  }

  if (phase === 'error' && !envelope) {
    return (
      <div className="page-state error">
        <h1>그룹</h1>
        <p>{error}</p>
      </div>
    )
  }

  if (!envelope?.data) {
    return <div className="page-state">그룹 목록 데이터가 없습니다.</div>
  }

  const data = envelope.data

  function updateSort(value: string) {
    startTransition(() => {
      setSort(value)
    })
  }

  return (
    <div className="page-grid">
      <section className="page-hero compact">
        <p className="eyebrow">그룹 디렉터리</p>
        <h1>그룹 목록</h1>
        <p>아이돌 그룹을 탐색하고 그룹별 공식 채널과 멤버 개인 채널 구성을 확인할 수 있습니다.</p>
      </section>

      <EnvelopeMetaBar meta={envelope.meta} errors={envelope.errors} />

      <section className="filter-panel">
        <label className="field grow">
          <span>검색</span>
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="그룹 이름 검색"
          />
        </label>

        <label className="field">
          <span>정렬</span>
          <select value={sort} onChange={(event) => updateSort(event.target.value)}>
            {data.filters.data.sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
      </section>

      <SectionCard
        title="그룹 결과"
        section={data.results}
        emptyMessage="조건에 맞는 그룹이 없습니다."
        onRetry={() => window.location.reload()}
      >
        <div className="card-grid">
          {data.results.data.items.map((item) => (
            <article className="entity-card" key={item.groupId}>
              <div>
                <strong>{item.groupName}</strong>
                <p>{item.description || '설명이 없습니다.'}</p>
              </div>
              <div className="tag-row">
                {item.badges.map((badge) => (
                  <span className="tag" key={`${item.groupId}-${badge}`}>
                    {formatBadgeLabel(badge)}
                  </span>
                ))}
              </div>
              <div className="entity-meta">
                <span>공식 채널 {item.officialChannelCount}개</span>
                <span>멤버 {item.memberCount}명</span>
                <span>개인 채널 {item.memberPersonalChannelCount}개</span>
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

        {data.results.data.pageInfo.hasNext ? (
          <div className="section-footer">
            <button onClick={loadMore}>더보기</button>
          </div>
        ) : null}
      </SectionCard>

      {error ? <div className="page-state error inline">{error}</div> : null}
    </div>
  )
}

