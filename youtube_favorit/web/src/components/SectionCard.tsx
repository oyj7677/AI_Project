import type { SectionState } from '../types/api'
import { StatusPill } from './StatusPill'

export function SectionCard<T>({
  title,
  section,
  emptyMessage,
  onRetry,
  children,
}: {
  title: string
  section: SectionState<T>
  emptyMessage: string
  onRetry: () => void
  children: React.ReactNode
}) {
  const isFailed = section.status === 'failed'
  const isEmpty = section.status === 'empty'
  const hasError = section.status === 'partial' || section.status === 'failed'

  return (
    <section className="section-card">
      <header className="section-header">
        <div>
          <h2>{title}</h2>
          <p>
            생성 시각 {new Date(section.generatedAt).toLocaleTimeString()}
            {section.staleAt
              ? ` · 만료 기준 ${new Date(section.staleAt).toLocaleTimeString()}`
              : ''}
          </p>
        </div>
        <div className="section-pills">
          <StatusPill kind="status" value={section.status} />
          <StatusPill kind="freshness" value={section.freshness} />
        </div>
      </header>

      {hasError && section.error ? (
        <div className={`section-alert ${section.status}`}>
          <strong>{section.error.code}</strong>
          <p>{section.error.message}</p>
          {isFailed ? (
            <button className="secondary-button" onClick={onRetry}>
              다시 시도
            </button>
          ) : null}
        </div>
      ) : null}

      {isEmpty ? (
        <div className="section-empty">{emptyMessage}</div>
      ) : !isFailed ? (
        <div className="section-body">{children}</div>
      ) : null}
    </section>
  )
}
