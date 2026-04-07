import type { FreshnessStatus, SectionStatus } from '../types/api'

function sectionStatusLabel(status: SectionStatus) {
  switch (status) {
    case 'success':
      return '정상'
    case 'partial':
      return '부분 실패'
    case 'failed':
      return '실패'
    case 'empty':
      return '비어 있음'
  }
}

function freshnessLabel(freshness: FreshnessStatus) {
  switch (freshness) {
    case 'fresh':
      return '최신'
    case 'stale':
      return '오래됨'
    case 'manual':
      return '수동'
    case 'unknown':
      return '알 수 없음'
  }
}

export function StatusPill({
  kind,
  value,
}: {
  kind: 'status' | 'freshness'
  value: SectionStatus | FreshnessStatus
}) {
  return (
    <span className={`status-pill ${kind} ${value}`}>
      {kind === 'status'
        ? sectionStatusLabel(value as SectionStatus)
        : freshnessLabel(value as FreshnessStatus)}
    </span>
  )
}
