import type { ActivityEntry } from '../hooks/useHabitTracker'

interface HistoryPanelProps {
  entries: ActivityEntry[]
}

export function HistoryPanel({ entries }: HistoryPanelProps) {
  return (
    <section className="history-panel" aria-labelledby="history-panel-title">
      <div className="section-heading">
        <div>
          <p className="eyebrow">히스토리</p>
          <h2 id="history-panel-title">최근 체크인</h2>
        </div>
        <p className="section-note">최근 흐름을 빠르게 확인해 보세요.</p>
      </div>

      {entries.length === 0 ? (
        <div className="empty-state">
          <h3>아직 기록이 없어요</h3>
          <p>체크인을 시작하면 완료 내역이 이곳에 차곡차곡 쌓여요.</p>
        </div>
      ) : (
        <ul className="history-list">
          {entries.map((entry) => (
            <li key={`${entry.habitId}-${entry.date}`} className="history-list__item">
              <div className="history-copy">
                <strong>{entry.habitTitle}</strong>
                <span className="history-meta">
                  {entry.category} • {entry.dateLabel}에 완료
                </span>
              </div>
              <span className="history-date">{entry.dateLabel}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
